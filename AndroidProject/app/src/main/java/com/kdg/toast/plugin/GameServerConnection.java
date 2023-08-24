package com.kdg.toast.plugin;

import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class GameServerConnection extends Thread {
    protected String serverIp;
    protected int serverPort;
    protected volatile boolean running = true;
    protected int bytesSent = 0;
    public WebSocketClient webSocketClient;

    public GameServerConnection(String parServerURL, int parPort) {
        Log.i("PEDOMETER", "Connecting to game server "+parServerURL+" with port "+parPort);
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
                UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                        "OpenedSocketForGameServer", // this is a callback in C#
                        ""); // msg
                Log.i("PEDOMETER", "Opened Web Socket for game server");
            }

            @Override
            public void onTextReceived(String message) {
                UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                        "ReceiveByteMessageFromGameServer", // this is a callback in C#
                        message); // msg
                Log.i("PEDOMETER", "Received message from game server aaaaa " + message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                String encodedBytesAsString = new String(data, StandardCharsets.UTF_8);
                UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                        "ReceiveByteMessageFromGameServer", // this is a callback in C#
                        encodedBytesAsString); // msg

                Log.i("PEDOMETER", "Binary data received from game server " + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
                Log.i("PEDOMETER", "Ping data received from game server " + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
                Log.i("PEDOMETER", "Pong data received from game server " + data.toString());
            }

            @Override
            public void onException(Exception e) {
                Log.i("PEDOMETER", "Web Socket exception happened on game server connection " + e.toString());
            }

            @Override
            public void onCloseReceived(int reason, String description) {
                running = false;
                UnityPlayer.UnitySendMessage("BackgroundService", // gameObject name
                        "GameServerClosedSocket", // this is a callback in C#
                        "description"); // msg
                Log.i("PEDOMETER", "Closed Web Socket on game server ");
            }
        };

        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void send(String parMessage) {
        try {
            parMessage += '\n';
            bytesSent += parMessage.length();
            webSocketClient.send(parMessage);
            Log.i("PEDOMETER", "Send message to game server success : " + parMessage);
        } catch (Exception e) {
            Log.i("PEDOMETER", "Send message to game server failed : " + parMessage + " " + e);
        }
    }

    public boolean isConnected() {
        return running;
    }
}
