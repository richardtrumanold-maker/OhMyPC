package com.example.ohmypc.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Guidebook + Craftbook в одном.
 * Левая панель — категории. Правая — контент + рецепт.
 */
public class ManualScreen extends Screen {

    // ── Данные страниц ────────────────────────────────────────────────────────
    record Recipe(String result, String... ingredients) {}
    record Page(String title, String[] lines, Recipe recipe) {}
    record Category(String name, String icon, List<Page> pages) {}

    private static final List<Category> CATEGORIES = List.of(

        new Category("§4⚠ ВАЖНО", "!", List.of(
            new Page("Критическая инициализация", new String[]{
                "§c§lОБЯЗАТЕЛЬНО К ПРОЧТЕНИЮ",
                "",
                "§fДиагностика дисплея §7— это",
                "фундаментальная процедура,",
                "без которой стабильная работа",
                "компьютера §cне гарантируется§7.",
                "",
                "§eТребования:",
                "§7 • §fВидеокарта T2§7 (обязательно)",
                "§7 • Подключённый проектор",
                "§7 • Запуск при каждом включении",
                "",
                "§7§oПропуск этой процедуры может",
                "§7§oпривести к нестабильной работе.",
            }, null),
            new Page("Команда диагностики", new String[]{
                "§f§lrr()",
                "",
                "§7Запускает полную диагностику",
                "видеовыхода и синхронизацию",
                "цветовой матрицы дисплея.",
                "",
                "§eДобавь в startup.lua:",
                "§8-- Критическая инициализация",
                "§arr()",
                "",
                "§7Без Video Card T2 команда",
                "вернёт ошибку калибровки.",
                "§cНе игнорируй эту ошибку."
            }, new Recipe("Для запуска нужно:",
                "Video Card T2 (в слоте GPU)",
                "Cinema Projector (подключён)",
                "rr() в startup.lua")),
            new Page("Почему это важно", new String[]{
                "§7Система §fOhMyPC§7 использует",
                "референсный видеосигнал для",
                "точной калибровки пикселей.",
                "",
                "§7Без ежезагрузочной синхронизации:",
                "§c • цвета могут сдвинуться",
                "§c • контрастность нарушится",
                "§c • мониторы desync",
                "",
                "§a§lПравильный startup.lua:",
                "§8-- OhMyPC Critical Boot Seq.",
                "§a rr()  §8-- НЕ УБИРАТЬ",
                "§7 -- ваш код ниже..."
            }, null)
        )),

        new Category("§aНачало", "▶", List.of(
            new Page("Добро пожаловать", new String[]{
                "§fOhMyPC §7— мод на программируемые",
                "компьютеры для Minecraft.",
                "",
                "§eБыстрый старт:",
                " 1. Скрафти §fСтартовый набор",
                " 2. Поставь §fСистемный блок",
                " 3. ПКМ → §fСборка§7, вставь детали",
                " 4. Нажми §aBoot",
                " 5. Открой §fТерминал§7 — пустая рука",
                "",
                "§7Первая программа:",
                "§e print(\"Hello World!\")",
                "",
                "§4§l⚠ Не забудь rr()!",
                "§7§oСм. главу §4§o⚠ ВАЖНО"
            }, null),
            new Page("Индикатор", new String[]{
                "Светодиод на передней панели",
                "показывает состояние компьютера:",
                "",
                "§8● Серый  §7— выключен",
                "§e● Жёлтый §7— загружается",
                "§a● Зелёный§7— работает",
                "§c● Красный§7— авария / BSOD",
                "",
                "§7При аварии монитор показывает",
                "текст ошибки на синем фоне."
            }, null)
        )),

        new Category("§bКомпоненты", "⚙", List.of(
            new Page("CPU — Процессор", new String[]{
                "§fЦП§7 определяет возможности мода.",
                "",
                "§eTier I  §7— 100 МГц, базовые операции",
                "§bTier II §7— 500 МГц + сеть",
                "",
                "§7Обязателен для запуска.",
                "Установить: слот §eCPU §7в меню сборки."
            }, new Recipe("CPU Tier I",
                "Circuit Board × 3",
                "Transistor × 4",
                "Diamond × 1")),
            new Page("RAM — Память", new String[]{
                "§fОЗУ §7нужна для запуска.",
                "Можно вставить 2 модуля (слоты RAM1/2).",
                "",
                "§7Без ОЗУ компьютер не загрузится.",
                "Чем больше RAM — тем сложнее",
                "скрипты можно запускать."
            }, new Recipe("RAM Module × 2",
                "Transistor × 3",
                "Circuit Board × 3")),
            new Page("GPU — Видеокарта", new String[]{
                "§fGPU§7 нужен для графического вывода.",
                "",
                "§eTier I  §7— режим изображений (PNG/JPEG)",
                "§bTier II §7— видео и GIF",
                "",
                "§7Без GPU — только текстовый режим.",
                "Нужен для §emonitor.setUrl()§7.",
                "Для видео: §eprojector.setUrl(url, true)"
            }, new Recipe("Видеокарта T I",
                "Gold Ingot × 8",
                "Circuit Board × 1")),
            new Page("Storage — Хранилище", new String[]{
                "§fHDD§7 — медленный, вместительный.",
                "§fSSD§7 — быстрый, меньше объём.",
                "",
                "§7Хранит файлы скриптов компьютера.",
                "Без него нет §efs_write/read§7.",
                "",
                "§7Используй §efs_write(path, text)§7",
                "для сохранения скриптов."
            }, new Recipe("Hard Disk Drive",
                "Iron Ingot × 8",
                "Circuit Board × 1")),
            new Page("Сеть — Карта / Модем", new String[]{
                "§fСетевые карты§7 нужны для:",
                "→ §enet.send() §7/ §enet.receive()",
                "→ §ehttp.get() §7/ §ehttp.post()",
                "",
                "§fWi-Fi модем§7 — без кабеля, до 64 блоков.",
                "§fСетевая карта§7 — кабелем, без ограничений.",
                "",
                "Tier II карта → 1 Гбит + HTTP API."
            }, new Recipe("Network Card (100 Mbit)",
                "Circuit Board × 3",
                "Iron Ingot × 2",
                "Data Bus Cable × 1"))
        )),

        new Category("§dБлоки", "■", List.of(
            new Page("Системный блок", new String[]{
                "§fОсновной блок§7 компьютера.",
                "",
                "§eПКМ пустой рукой §7(вкл) → Терминал",
                "§eПКМ предметом    §7→ Меню сборки",
                "",
                "§7Меню сборки: слоты для CPU, RAM,",
                "GPU, Storage, Сеть. Кнопки Boot/Shutdown.",
                "",
                "§7Ломается с выпадением компонентов."
            }, new Recipe("Computer Case",
                "Iron Ingot × 8",
                "Circuit Board × 1",
                "CPU Tier I × 1")),
            new Page("Монитор", new String[]{
                "§fОтображает §7текст и изображения.",
                "",
                "§7Подключается §eсетевым кабелем§7.",
                "Поддерживает §eмультиблок§7 до 12×24.",
                "",
                "§eРазрешение по размеру:",
                "§7 6×9  → FHD  (1920×1080)",
                "§7 6×12 → 2K   (2560×1440)",
                "§7 12×24→ 4K   (3840×2160)"
            }, new Recipe("Monitor",
                "Iron Ingot × 8",
                "Glass × 1",
                "Circuit Board × 1")),
            new Page("Кино проектор", new String[]{
                "§fРисует парящий экран §7перед блоком.",
                "",
                "§7Настройки через Lua:",
                "§e hologram.setProjection(dist, w, h, alpha)",
                "",
                "§7dist  §f= 1–16 §7блоков",
                "§7w/h   §f= размер §7в блоках",
                "§7alpha §f= 0.1–1.0 §7прозрачность",
                "",
                "§7Требует GPU для изображений."
            }, new Recipe("Cinema Projector",
                "Ender Pearl × 2",
                "Glass × 2",
                "Circuit Board × 1",
                "Iron Ingot × 3")),
            new Page("Сетевой кабель", new String[]{
                "§fСоединяет§7 компьютер с мониторами,",
                "проекторами и дисководами.",
                "",
                "§eПКМ по кабелю§7 — настройка связи.",
                "Первый клик — первое устройство.",
                "Второй клик — второе устройство.",
                "Третий клик — отключить.",
                "",
                "§7Прокладывается в любую сторону."
            }, new Recipe("Network Cable × 6",
                "Copper Ingot × 2",
                "String × 1")),
            new Page("Дисковод", new String[]{
                "§fЧитает и пишет флоппи-диски.",
                "",
                "§7Подключи к компьютеру кабелем.",
                "Вставь диск в слот дисковода.",
                "Диск смонтируется как §e/disk/",
                "",
                "§eLua команды:",
                "§7 disk.read(\"file.lua\")",
                "§7 disk.write(\"file.lua\", code)",
                "§7 disk.list(\"\")",
                "§7 disk.color()"
            }, null)
        )),

        new Category("§6Флоппи", "💾", List.of(
            new Page("Флоппи-диски", new String[]{
                "§f16 цветов §7— каждый цвет уникален.",
                "",
                "§7Файлы хранятся в реальной папке:",
                "§e [minecraft]/floppy/[цвет]/",
                "",
                "§7Одинаковый цвет = те же данные",
                "на любом мире и сервере!",
                "",
                "Перенеси диск с кодом →",
                "скрипт работает в новом мире."
            }, new Recipe("White Floppy Disk",
                "Iron Nugget × 6",
                "Paper × 1",
                "Redstone × 1")),
            new Page("Цветные диски", new String[]{
                "§7Окраска: белый флоппи + краситель.",
                "",
                "§cКрасный   §7— экшен / боёвка",
                "§9Синий     §7— документация",
                "§aЗелёный   §7— утилиты",
                "§eЖёлтый    §7— конфиги",
                "§5Фиолетовый§7— сеть",
                "§3Бирюзовый §7— медиа",
                "§8Чёрный    §7— системные скрипты",
                "",
                "§7Любой краситель + белый флоппи."
            }, new Recipe("Colored Floppy (any)",
                "White Floppy Disk × 1",
                "Any Dye × 1"))
        )),

        new Category("§3Сеть", "⬡", List.of(
            new Page("Сеть между компьютерами", new String[]{
                "§7Каждый компьютер имеет §eадрес§7.",
                "Узнать: §enet.getAddress()",
                "",
                "§eОтправка сообщения:",
                "§7 net.send(addr, chan, msg)",
                "",
                "§eПриём (неблокирующий):",
                "§7 msg = net.receive(chan, timeout)",
                "",
                "§eШирокое вещание:",
                "§7 net.broadcast(chan, msg)"
            }, null),
            new Page("Wi-Fi vs Кабель", new String[]{
                "§fWi-Fi модем",
                "→ без кабеля",
                "→ радиус §e64 блока",
                "→ разные измерения = НЕ работает",
                "",
                "§fСетевая карта",
                "→ нужен кабель до Network Hub",
                "→ без ограничений дистанции",
                "→ рекомендуется для серверов"
            }, null),
            new Page("HTTP API", new String[]{
                "§7Запросы к интернету из Lua.",
                "Требует сетевую карту T2+.",
                "",
                "§eGET запрос:",
                "§7 result = http.get(url)",
                "",
                "§ePOST запрос:",
                "§7 result = http.post(url, body)",
                "",
                "§7Лимит: 64 КБ на ответ.",
                "Только https:// и http://",
                "§cНе работает к localhost/LAN"
            }, null)
        )),

        new Category("§eLua API", "{ }", List.of(
            new Page("Базовые функции", new String[]{
                "§eprint(...)      §7вывод в терминал",
                "§eclear()         §7очистить терминал",
                "§etime()          §7unix-время (float)",
                "§esleep(s)        §7пауза N секунд",
                "§eshutdown()      §7выключить ПК",
                "§egetVersion()    §7версия мода",
                "§ehelp()          §7список команд",
                "",
                "§7§-коды цвета работают везде:",
                "§7 §a зелёный  §c красный  §b голубой",
                "§7 §e жёлтый   §5 фиолет   §f белый"
            }, null),
            new Page("Файловая система", new String[]{
                "§efs_read(path)          §7→ string|nil",
                "§efs_write(path, text)   §7→ true",
                "§efs_exists(path)        §7→ bool",
                "§efs_list(dir)           §7→ table",
                "§efs_delete(path)        §7→ bool",
                "§efs_mkdir(path)         §7→ true",
                "",
                "§7Диск (флоппи):",
                "§edisk.read/write/list/exists",
                "§edisk.color()  disk.isPresent()",
                "§edisk.free()   §7→ свободно байт"
            }, null),
            new Page("Монитор и голограмма", new String[]{
                "§emonitor.write(line)    §7текст",
                "§emonitor.clear()",
                "§emonitor.setLines(tbl)  §7всё сразу",
                "§emonitor.setUrl(url)    §7картинка",
                "§emonitor.clearUrl()",
                "§emonitor.getResolution() §7→ '1920x1080'",
                "",
                "§ehologram.write/clear/setUrl",
                "§ehologram.setProjection(",
                "§7  dist, width, height, alpha)"
            }, null),
            new Page("Безопасность", new String[]{
                "§eSecurity API:",
                "§7 security.setPassword(pass)",
                "§7 security.lock()",
                "§7 security.unlock(pass) §7→ bool",
                "§7 security.isLocked()   §7→ bool",
                "§7 security.clearPassword()",
                "",
                "§7Пароль хранится как SHA-256 хеш.",
                "§7При блокировке терминал закрыт",
                "для всех кроме владельца."
            }, null)
        )),

        new Category("§cМатериалы", "◈", List.of(
            new Page("Транзистор", new String[]{
                "§7Базовый материал для всего.",
                "Начни с него."
            }, new Recipe("Transistor × 4",
                "Gold Nugget × 2",
                "Iron Nugget × 2",
                "Redstone × 1",
                "Copper Ingot × 1")),
            new Page("Плата схем", new String[]{
                "§7Нужна почти во всех рецептах.",
                "Делай сразу много."
            }, new Recipe("Circuit Board",
                "Transistor × 8",
                "Gold Ingot × 1")),
            new Page("Шина данных", new String[]{
                "§7Нужна для сетевых карт.",
                "Делается из меди + транзистора."
            }, new Recipe("Data Bus Cable × 4",
                "Copper Ingot × 1",
                "Transistor × 1")),
            new Page("Стартовый набор", new String[]{
                "§fОдноразовый предмет§7.",
                "Даёт полный комплект для старта:",
                "",
                "§7• Системный блок",
                "§7• CPU T1 + RAM × 2 + HDD",
                "§7• Монитор + кабель × 4",
                "§7• Белый флоппи + Руководство",
                "",
                "§eПКМ §7для распаковки."
            }, new Recipe("Starter Kit",
                "Activation Key × 1",
                "Data Bus Cable × 1",
                "Computer Case (item) × 1")),
            new Page("Ключ активации", new String[]{
                "§7Нужен для крафта Стартового набора.",
                "Одноразовый, теряется при распаковке."
            }, new Recipe("Activation Key",
                "Iron Ingot × 2",
                "Gold Ingot × 1",
                "Circuit Board × 1"))
        ))
    );

    // ── GUI ───────────────────────────────────────────────────────────────────
    private static final int W = 340, H = 220;
    private static final int CAT_W = 80;

    private int catIdx  = 0;
    private int pageIdx = 0;
    private int scrollY = 0;

    public ManualScreen() { super(Component.literal("OhMyPC Manual")); }

    @Override
    protected void init() {
        super.init();
        int ox = (width - W) / 2, oy = (height - H) / 2;

        // Кнопки категорий (слева)
        for (int i = 0; i < CATEGORIES.size(); i++) {
            final int ci = i;
            addRenderableWidget(Button.builder(
                    Component.literal(CATEGORIES.get(i).name()),
                    b -> { catIdx = ci; pageIdx = 0; scrollY = 0; })
                .bounds(ox + 2, oy + 2 + i * 26, CAT_W - 4, 22)
                .build());
        }
        // Кнопки навигации страниц
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            if (pageIdx > 0) { pageIdx--; scrollY = 0; }
        }).bounds(ox + CAT_W + 2, oy + H - 22, 20, 18).build());

        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            List<Page> pages = CATEGORIES.get(catIdx).pages();
            if (pageIdx < pages.size() - 1) { pageIdx++; scrollY = 0; }
        }).bounds(ox + W - 24, oy + H - 22, 20, 18).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        int ox = (width - W) / 2, oy = (height - H) / 2;

        // Фон книги
        g.fill(ox, oy, ox + W, oy + H, 0xFF1A1A1A);
        g.fill(ox + 1, oy + 1, ox + W - 1, oy + H - 1, 0xFF252525);

        // Разделитель категорий
        g.fill(ox + CAT_W, oy, ox + CAT_W + 1, oy + H, 0xFF444444);

        // Заголовок текущей категории
        Category cat = CATEGORIES.get(catIdx);
        g.drawString(font, cat.name(), ox + CAT_W + 6, oy + 4, 0xFFFFFFFF, false);

        // Разделитель заголовка
        g.fill(ox + CAT_W + 1, oy + 14, ox + W, oy + 15, 0xFF444444);

        // Текущая страница
        List<Page> pages = cat.pages();
        if (!pages.isEmpty()) {
            Page page = pages.get(pageIdx);
            int tx = ox + CAT_W + 6, ty = oy + 18;

            // Заголовок страницы
            g.drawString(font, "§f" + page.title(), tx, ty, 0xFFFFFFFF, false);
            ty += 12;

            // Строки контента
            for (String line : page.lines()) {
                if (ty > oy + H - 50) break;
                g.drawString(font, line, tx, ty, 0xFFCCCCCC, false);
                ty += 9;
            }

            // Рецепт
            if (page.recipe() != null) {
                ty = Math.max(ty + 4, oy + H - 52);
                g.fill(ox + CAT_W + 1, ty - 2, ox + W - 1, ty - 1, 0xFF444444);
                g.drawString(font, "§eCrафт: §f" + page.recipe().result(), tx, ty, 0xFFFFFF00, false);
                ty += 10;
                for (String ing : page.recipe().ingredients()) {
                    if (ty >= oy + H - 22) break;
                    g.drawString(font, "§8 + §7" + ing, tx, ty, 0xFFAAAAAA, false);
                    ty += 9;
                }
            }

            // Счётчик страниц
            String pgStr = (pageIdx + 1) + "/" + pages.size();
            g.drawString(font, pgStr, ox + CAT_W + (W - CAT_W) / 2 - font.width(pgStr) / 2,
                    oy + H - 17, 0xFF888888, false);
        }

        super.render(g, mx, my, partial);
    }

    @Override public boolean isPauseScreen() { return false; }
}
