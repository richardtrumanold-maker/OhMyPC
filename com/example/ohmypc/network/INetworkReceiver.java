package com.example.ohmypc.network;

import net.minecraft.core.BlockPos;

public interface INetworkReceiver {
    void receiveMessage(int channel, String message, INetworkReceiver sender);
    BlockPos getPosition();
    String getNetworkId();
}