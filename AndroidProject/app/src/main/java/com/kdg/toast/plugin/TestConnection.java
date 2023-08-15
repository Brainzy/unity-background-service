package com.kdg.toast.plugin;
import java.io.IOException;

public class TestConnection extends AbstractClientConnection {
    public TestConnection(String parServerURL) {
        super(parServerURL);
    }

    @Override
    public void connectNewSocket(int parPort)throws IOException {
        super.connectNewSocket(parPort);
    }
    @Override
    protected void treatOutput(String parOutput) {
        System.out.println(parOutput);
    }
}
