package ru.ifmo.rain.telnov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Telnov Sergey on 13.05.2018.
 */
public class HelloUDPServerTest {

    private HelloClient client;
    private HelloServer server;
    private final int port = 8888;

    @Before
    public void setUp() {
        server = new HelloUDPServer();
        client = new HelloUDPClient();
        server.start(port, 1);
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() {
        client.run("localhost", port, "hello server", 1, 1);
    }

    @After
    public void tearDown() {
        server.close();
    }
}
