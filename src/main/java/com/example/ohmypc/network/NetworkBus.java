package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Серверная шина сообщений между компьютерами.
 *
 * Адрес = UUID строкой, хранится в NBT компьютера.
 * Сброс при рестарте сервера (статика — это ок, регистрация происходит при boot()).
 *
 * Wi-Fi (wireless_modem) — работает в радиусе WIFI_RANGE блоков.
 * Сетевая карта — работает через кабели без ограничения дистанции.
 */
public class NetworkBus {

    public static final int WIFI_RANGE = 64;
    private static final int MAX_QUEUE  = 256; // макс. сообщений в очереди одного компьютера

    // Адрес → очередь сообщений
    private static final Map<String, Deque<NetworkMessage>> queues = new ConcurrentHashMap<>();
    // Адрес → BlockPos (для Wi-Fi дистанции)
    private static final Map<String, BlockPos> positions = new ConcurrentHashMap<>();
    // Адрес → ссылка на Level (для проверки измерения)
    private static final Map<String, WeakReference<Level>> levels = new ConcurrentHashMap<>();
    // Адрес → тип подключения
    private static final Map<String, ConnectionType> connections = new ConcurrentHashMap<>();

    public enum ConnectionType { WIRED, WIRELESS }

    // ── Регистрация ───────────────────────────────────────────────────────────

    public static void register(String address, ComputerBlockEntity computer, ConnectionType type) {
        queues.put(address, new LinkedBlockingDeque<>(MAX_QUEUE));
        positions.put(address, computer.getBlockPos());
        levels.put(address, new WeakReference<>(computer.getLevel()));
        connections.put(address, type);
    }

    public static void unregister(String address) {
        queues.remove(address);
        positions.remove(address);
        levels.remove(address);
        connections.remove(address);
    }

    // ── Отправка ──────────────────────────────────────────────────────────────

    /**
     * Прямая отправка. Если адресат — Wi-Fi и слишком далеко — отказ.
     */
    public static boolean send(String fromAddr, String toAddr, String channel, String message) {
        Deque<NetworkMessage> q = queues.get(toAddr);
        if (q == null) return false; // адрес не найден / компьютер выключен

        // Проверка дальности для Wi-Fi
        ConnectionType senderType = connections.get(fromAddr);
        ConnectionType targetType = connections.get(toAddr);
        if (senderType == ConnectionType.WIRELESS || targetType == ConnectionType.WIRELESS) {
            BlockPos from = positions.get(fromAddr);
            BlockPos to   = positions.get(toAddr);
            Level   fl    = levels.containsKey(fromAddr) ? levels.get(fromAddr).get() : null;
            Level   tl    = levels.containsKey(toAddr)   ? levels.get(toAddr).get()   : null;
            if (from == null || to == null || fl == null || tl == null) return false;
            if (fl != tl) return false; // разные измерения
            if (from.distSqr(to) > (long) WIFI_RANGE * WIFI_RANGE) return false;
        }

        if (q.size() >= MAX_QUEUE) q.pollFirst(); // вытеснить старое
        return q.offerLast(new NetworkMessage(fromAddr, toAddr, channel, message,
                System.currentTimeMillis()));
    }

    /**
     * Широковещание — всем на том же канале.
     * Wi-Fi — только в радиусе. Wired — всем в измерении.
     */
    public static int broadcast(String fromAddr, String channel, String message) {
        BlockPos fromPos = positions.get(fromAddr);
        Level    fromLvl = levels.containsKey(fromAddr) ? levels.get(fromAddr).get() : null;
        ConnectionType senderType = connections.getOrDefault(fromAddr, ConnectionType.WIRED);
        int count = 0;

        for (String addr : queues.keySet()) {
            if (addr.equals(fromAddr)) continue;
            BlockPos toPos = positions.get(addr);
            Level    toLvl = levels.containsKey(addr) ? levels.get(addr).get() : null;
            if (toPos == null || toLvl == null || fromLvl != toLvl) continue;

            if (senderType == ConnectionType.WIRELESS) {
                if (fromPos == null || fromPos.distSqr(toPos) > (long) WIFI_RANGE * WIFI_RANGE) continue;
            }
            Deque<NetworkMessage> q = queues.get(addr);
            if (q != null && q.size() < MAX_QUEUE) {
                q.offerLast(new NetworkMessage(fromAddr, addr, channel, message,
                        System.currentTimeMillis()));
                count++;
            }
        }
        return count;
    }

    /**
     * Неблокирующий приём — null если нет сообщений.
     * Можно фильтровать по каналу (null = любой).
     */
    public static NetworkMessage poll(String address, String channelFilter) {
        Deque<NetworkMessage> q = queues.get(address);
        if (q == null) return null;
        if (channelFilter == null) return q.pollFirst();
        // Поиск конкретного канала
        Iterator<NetworkMessage> it = q.iterator();
        while (it.hasNext()) {
            NetworkMessage msg = it.next();
            if (channelFilter.equals(msg.channel())) { it.remove(); return msg; }
        }
        return null;
    }

    public static boolean isRegistered(String address) { return queues.containsKey(address); }
    public static Set<String> getOnlineAddresses() { return Collections.unmodifiableSet(queues.keySet()); }
}
