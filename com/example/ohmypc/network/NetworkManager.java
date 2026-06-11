package com.example.ohmypc.network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    private static final Map<Integer, List<INetworkReceiver>> NETWORK = new ConcurrentHashMap<>();

    public static void registerReceiver(int channel, INetworkReceiver receiver) {
        NETWORK.computeIfAbsent(channel, k -> new ArrayList<>()).add(receiver);
    }

    public static void unregisterReceiver(int channel, INetworkReceiver receiver) {
        List<INetworkReceiver> receivers = NETWORK.get(channel);
        if (receivers != null) {
            receivers.remove(receiver);
        }
    }

    public static void sendMessage(int channel, String message, INetworkReceiver sender, int range) {
        List<INetworkReceiver> receivers = NETWORK.get(channel);
        if (receivers == null) return;

        for (INetworkReceiver receiver : receivers) {
            if (receiver.equals(sender)) continue;

            double distance = sender.getPosition().distToCenterSqr(
                    receiver.getPosition().getX(),
                    receiver.getPosition().getY(),
                    receiver.getPosition().getZ()
            );

            if (distance <= range * range) {
                receiver.receiveMessage(channel, message, sender);
            }
        }
    }
}