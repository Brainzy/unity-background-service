package com.kdg.toast.plugin;

import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import dev.gustavoavila.websocketclient.WebSocketClient;

public class ClientConnection extends Thread {
    protected String serverIp;
    protected int serverPort;
    protected volatile boolean running = true;
    protected int bytesSent = 0;
    protected int bytesReceived = 0;
    private WebSocketClient webSocketClient;

    public ClientConnection(String parServerURL, int parPort) {
        serverIp = parServerURL;
        serverPort = parPort;
        createWebSocketClient();
    }

    public void createWebSocketClient() {
        URI uri;
        try {
            uri = new URI("ws://" + serverIp + ":" + serverPort);
            Log.i("PEDOMETER", "Set URI for connecting to server to "+uri);
        } catch (URISyntaxException e) {
            Log.i("PEDOMETER", "Exception happened when parsing ip " + serverIp + " with port " + serverPort + " " + e);
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                running = true;
                Log.i("PEDOMETER", "Opened Web Socket");
            }

            @Override
            public void onTextReceived(String message) {
                bytesReceived += message.length();
                Log.i("PEDOMETER", "Received message " + message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                Log.i("PEDOMETER", "Binary data received " + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
                Log.i("PEDOMETER", "Ping data received " + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.i("PEDOMETER", "Pong data received " + data.toString());
            }

            @Override
            public void onException(Exception e) {
                Log.i("PEDOMETER", "Web Socket exception happened " + e.toString());
            }

            @Override
            public void onCloseReceived(int reason, String description) {
                running = false;
                Log.i("PEDOMETER", "Closed Web Socket");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        //webSocketClient.addHeader("Origin", "http://developer.example.com");
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void send(String parMessage) {
        try {
            parMessage += '\n';
            bytesSent += parMessage.length();
            webSocketClient.send(parMessage);
            Log.i("PEDOMETER", "Send message success : " + parMessage);
        } catch (Exception e) {
            Log.i("PEDOMETER", "Send message failed : " + parMessage + " " + e.toString());
        }
    }

    public boolean isConnected() {
        return running;
    }
}
