package com.example.ohmypc.client;

import java.util.*;

/** Клиентский кеш последних строк терминала по позиции блока */
public class TerminalCache {

    private static final Map<Long, List<String>> cache = new HashMap<>();

    public static void update(long posLong, List<String> lines) {
        cache.put(posLong, new ArrayList<>(lines));
    }

    public static List<String> get(long posLong) {
        return cache.getOrDefault(posLong, List.of());
    }

    public static void clear(long posLong) {
        cache.remove(posLong);
    }
}
