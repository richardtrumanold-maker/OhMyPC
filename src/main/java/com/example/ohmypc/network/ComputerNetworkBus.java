package com.example.ohmypc.network;

import com.example.ohmypc.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Серверная шина сообщений между компьютерами.
 *
 * Адрес компьютера — строка UUID, назначаемая при первом boot().
 * Хранится в NBT компьютера → не меняется при рестарте.
 *
 * net.send(addr, msg)      — найти получателя по адресу, положить в его очередь
 * net.broadcast(msg)       — всем кто в том же мире
 * net.receive(timeout)     — блокирует Lua-поток до прихода сообщения или таймаута
 * net.getAddress()         — вернуть адрес текущего компьютера
 *
 * Очередь: максимум 64 сообщения, старые вытесняются при переполнении.
 */
public class ComputerNetworkBus {

    // Один экземпляр на сервер
    private static final ComputerNetworkBus INSTANCE = new ComputerNetworkBus();
    public static ComputerNetworkBus get() { return INSTANCE; }

    /** адрес → входящая очередь */
    private final Map<String, LinkedBlockingDeque<NetMessage>> queues   = new ConcurrentHashMap<>();
    /** адрес → позиция в мире (для дистанционных ограничений Wi-Fi) */
    private final Map<String, LocatedComputer>                 registry = new ConcurrentHashMap<>();

    public record NetMessage(String from, String channel, String body) {}
    public record LocatedComputer(BlockPos pos, String levelKey) {}

    private static final int MAX_QUEUE = 64;

    // ── Регистрация / дерегистрация ───────────────────────────────────────────

    public void register(String address, BlockPos pos, String levelKey) {
        registry.put(address, new LocatedComputer(pos, levelKey));
        queues.computeIfAbsent(address, k -> new LinkedBlockingDeque<>(MAX_QUEUE));
    }

    public void unregister(String address) {
        registry.remove(address);
        queues.remove(address);
    }

    // ── Отправка ──────────────────────────────────────────────────────────────

    /**
     * Отправить сообщение конкретному адресату.
     * @return false если адресат не найден
     */
    public boolean send(String fromAddr, String toAddr, String channel, String body) {
        LinkedBlockingDeque<NetMessage> q = queues.get(toAddr);
        if (q == null) return false;
        NetMessage msg = new NetMessage(fromAddr, channel, body);
        if (q.size() >= MAX_QUEUE) q.pollFirst(); // вытеснить старое
        q.offerLast(msg);
        return true;
    }

    /**
     * Broadcast — всем зарегистрированным компьютерам в том же мире.
     * Если у отправителя Wi-Fi модем — только в радиусе maxRange блоков.
     */
    public void broadcast(String fromAddr, String channel, String body,
                           double maxRange, String levelKey) {
        LocatedComputer sender = registry.get(fromAddr);
        NetMessage msg = new NetMessage(fromAddr, channel, body);

        registry.forEach((addr, loc) -> {
            if (addr.equals(fromAddr)) return;
            if (!loc.levelKey().equals(levelKey)) return;
            if (sender != null && maxRange > 0) {
                double dist = sender.pos().distSqr(loc.pos());
                if (dist > maxRange * maxRange) return;
            }
            LinkedBlockingDeque<NetMessage> q = queues.get(addr);
            if (q != null) {
                if (q.size() >= MAX_QUEUE) q.pollFirst();
                q.offerLast(msg);
            }
        });
    }

    /**
     * Блокирующий receive. Вызывается из Lua-потока.
     * @param timeoutMs 0 = не ждать, >0 = ждать до N мс
     * @return сообщение или null если таймаут
     */
    public NetMessage receive(String address, long timeoutMs) throws InterruptedException {
        LinkedBlockingDeque<NetMessage> q = queues.computeIfAbsent(
                address, k -> new LinkedBlockingDeque<>(MAX_QUEUE));
        if (timeoutMs <= 0) return q.pollFirst();
        return q.pollFirst(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /** Есть ли сообщение без блокировки */
    public boolean available(String address) {
        LinkedBlockingDeque<NetMessage> q = queues.get(address);
        return q != null && !q.isEmpty();
    }

    /** Сброс всех данных при остановке сервера */
    public void reset() {
        queues.clear();
        registry.clear();
    }
}
