package com.example.aplicativo_horacerta.socket;

public interface SocketClientListener {
    void onClientDataReceived(String data);
    void onClientError(Exception error);
    void onClientDisconnected();
}