package com.example.ohmypc.network;

public interface INetworkAPI {
    boolean openChannel(int channel);
    boolean closeChannel(int channel);
    boolean sendMessage(int channel, String message);
    boolean sendMessageTo(String targetId, int channel, String message);
    String receiveMessage();
    String getNetworkId();
    int[] getActiveChannels();
}