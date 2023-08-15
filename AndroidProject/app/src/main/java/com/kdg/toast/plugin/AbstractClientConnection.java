package com.kdg.toast.plugin;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class AbstractClientConnection extends Thread {
    protected final int MAX_CONNECTION_ATTEMPTS = 4;
    protected final int CONNECTION_TIMEOUT = 5000;
    protected final String serverURL;
    protected Socket socket;
    protected BufferedReader reader;
    protected int bytesSent = 0;
    protected int bytesReceived = 0;
    protected volatile boolean running = true;


    public AbstractClientConnection(String parServerURL) {
        serverURL = parServerURL;
        socket = new Socket();
    }
    

    public void connectNewSocket(int parPort) throws IOException {
        boolean locIsConnected = false;
        int locNbrAttempt = 0;
        while (!locIsConnected && locNbrAttempt < MAX_CONNECTION_ATTEMPTS){
            locNbrAttempt += 1;
            try {
                //Log.i("PEDOMETER", "AAAAAAAAAAAAAAAAAAAAAAAAAA: "+locSocketAddress);
                InetSocketAddress locSocketAddress = new InetSocketAddress(serverURL, parPort);
                socket = new Socket(); // Vérifier si c'est vraiment nécessaire de faire un new
                socket.connect(locSocketAddress, CONNECTION_TIMEOUT);
                socket.setSoTimeout(0);
            } catch (IOException e) {
            }


            if (socket.isConnected())
                locIsConnected = true;
        }

        if (!socket.isConnected())
        {
            throw new IOException("Attempt to connect to the server " + MAX_CONNECTION_ATTEMPTS + " times without success");
        }

        bytesSent = 0;
        bytesReceived = 0;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //launch a new thread for reading on this socket
        start();
    }

    public void send(String parMessage) {
        parMessage += '\n';
        try {
            bytesSent += parMessage.length();
            socket.getOutputStream().write(parMessage.getBytes());
            socket.getOutputStream().flush();
            Log.i("PEDOMETER", "Send message success : "+parMessage+" "+socket.getLocalAddress());
        } catch (IOException locE) {
            onSendException(parMessage, locE);
            Log.i("PEDOMETER", "Send message failed : "+parMessage+" "+locE);
        }
    }

    protected void onSendException(String parMessageNotSent, IOException parException) {
    }

    public boolean isConnected(){
        return socket.isConnected();
    }

    public void disconnect() {
        try {
            socket.close();
            socket = null;
            running = false;
        } catch (IOException locE) {
            onDisconnectException(locE);
        }
    }

    protected void onDisconnectException(IOException parException) {
    }

    @Override
    public void run() {
        try {
            while (running) {
                byte[] locBuffer = new byte[4096];
                String locInput = reader.readLine();
                if (locInput == null) {
                    onEOF();
                    break;
                }

                bytesReceived += locInput.length();
                treatOutput(locInput);
            }
        } catch (IOException e) {
            onConnectionLost();
        }
    }

    protected void onEOF() {
    }

    protected void onConnectionLost() {
    }

    protected abstract void treatOutput(String parOutput);
}
