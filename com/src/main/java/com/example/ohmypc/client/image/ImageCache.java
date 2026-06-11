package com.example.ohmypc.client.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Клиентский кеш текстур, загруженных по URL.
 *
 * Жизненный цикл:
 *  1. get(url) → LOADING / возвращает null
 *  2. Фоновый поток качает → конвертирует → регистрирует DynamicTexture
 *  3. get(url) → ResourceLocation готовой текстуры
 *
 * Кеш хранит не более MAX_ENTRIES текстур; при переполнении вытесняет LRU.
 */
public class ImageCache {

    private static final Logger LOGGER = LogManager.getLogger("OhMyPC/ImageCache");

    private static final int  MAX_ENTRIES     = 32;
    // 12 МБ на изображение — хватит для 4K JPEG
    private static final long MAX_BYTES       = 12 * 1024 * 1024;
    private static final int  TIMEOUT_SECONDS = 15;
    // Разрешённые MIME-типы
    private static final java.util.Set<String> ALLOWED_MIME = java.util.Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp", "image/bmp"
    );

    /** Состояние загрузки */
    public enum State { LOADING, READY, ERROR }

    /** Запись кеша */
    public record Entry(State state, ResourceLocation location, String error) {}

    // URL → запись
    private static final Map<String, Entry>         cache    = new ConcurrentHashMap<>();
    // LRU-счётчик
    private static final Map<String, Long>          lruTime  = new ConcurrentHashMap<>();
    private static final AtomicInteger              texId    = new AtomicInteger(0);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final ExecutorService POOL = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "OhMyPC-ImageLoader");
        t.setDaemon(true);
        return t;
    });

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Запрашивает текстуру по URL.
     * @return null если ещё грузится; Entry с state=ERROR если ошибка; Entry с state=READY иначе.
     */
    public static Entry get(String url) {
        if (url == null || url.isBlank()) return null;

        lruTime.put(url, System.currentTimeMillis());
        Entry existing = cache.get(url);

        if (existing != null) return existing;

        // Начинаем загрузку
        cache.put(url, new Entry(State.LOADING, null, null));
        POOL.submit(() -> load(url));
        return null;
    }

    /** Принудительно сбросить кеш для URL */
    public static void invalidate(String url) {
        Entry e = cache.remove(url);
        lruTime.remove(url);
        if (e != null && e.location() != null) {
            Minecraft.getInstance().execute(() ->
                    Minecraft.getInstance().getTextureManager().release(e.location()));
        }
    }

    /** Очистить весь кеш */
    public static void clearAll() {
        cache.keySet().forEach(ImageCache::invalidate);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static void load(String url) {
        try {
            // 1. Скачать
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("User-Agent", "OhMyPC-Minecraft-Mod/0.1")
                    .GET().build();

            HttpResponse<InputStream> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());

            // Проверка кода
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                fail(url, "HTTP " + resp.statusCode());
                return;
            }

            // Проверка Content-Type
            String mime = resp.headers().firstValue("Content-Type").orElse("").toLowerCase();
            boolean mimeOk = ALLOWED_MIME.stream().anyMatch(mime::startsWith);
            if (!mimeOk) {
                fail(url, "Unsupported type: " + mime);
                return;
            }

            // Читаем с ограничением по размеру
            byte[] bytes = resp.body().readNBytes((int) MAX_BYTES);

            // 2. Декодировать через AWT (поддержка PNG/JPG/GIF/BMP/WEBP через TwelveMonkeys если есть)
            BufferedImage awt = ImageIO.read(new java.io.ByteArrayInputStream(bytes));
            if (awt == null) {
                fail(url, "Cannot decode image");
                return;
            }

            // 3. Конвертировать AWT → NativeImage (RGBA)
            int w = awt.getWidth(), h = awt.getHeight();
            NativeImage ni = new NativeImage(NativeImage.Format.RGBA, w, h, false);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = awt.getRGB(x, y);
                    // AWT: ARGB  →  NativeImage: ABGR (little-endian RGBA)
                    int a = (argb >> 24) & 0xFF;
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >>  8) & 0xFF;
                    int b =  argb        & 0xFF;
                    ni.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }

            // 4. Зарегистрировать как DynamicTexture на главном потоке
            ResourceLocation loc = new ResourceLocation("ohmypc", "dynamic/img_" + texId.getAndIncrement());
            Minecraft.getInstance().execute(() -> {
                DynamicTexture dt = new DynamicTexture(ni);
                Minecraft.getInstance().getTextureManager().register(loc, dt);
                cache.put(url, new Entry(State.READY, loc, null));
                evictIfNeeded();
                LOGGER.info("[ImageCache] Loaded {} → {}", url, loc);
            });

        } catch (Exception e) {
            fail(url, e.getMessage());
        }
    }

    private static void fail(String url, String reason) {
        LOGGER.warn("[ImageCache] Failed to load {}: {}", url, reason);
        cache.put(url, new Entry(State.ERROR, null, reason));
    }

    /** LRU-вытеснение если кеш переполнен */
    private static void evictIfNeeded() {
        if (cache.size() <= MAX_ENTRIES) return;
        cache.keySet().stream()
                .filter(u -> cache.get(u).state() == State.READY)
                .sorted((a, b) -> Long.compare(
                        lruTime.getOrDefault(a, 0L),
                        lruTime.getOrDefault(b, 0L)))
                .limit(cache.size() - MAX_ENTRIES)
                .forEach(ImageCache::invalidate);
    }
}
