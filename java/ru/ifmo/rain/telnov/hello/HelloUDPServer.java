package ru.ifmo.rain.telnov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Telnov Sergey on 13.05.2018.
 */
public class HelloUDPServer implements HelloServer {
    private ExecutorService workers;
    private DatagramSocket socket;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <port> <threads>");
        } else {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            int sendBufferSize = socket.getSendBufferSize();
            workers = Executors.newFixedThreadPool(threads);

            for (int i = 0; i != threads; i++) {
                workers.execute(() -> {
                    byte[] buf = new byte[sendBufferSize];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);

                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            socket.receive(packet);
                            String receiveMessage = new String(
                                    packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");

                            byte[] sendBuff = String.format("Hello, %s", receiveMessage).getBytes("UTF-8");
                            socket.send(new DatagramPacket(sendBuff, sendBuff.length, packet.getSocketAddress()));
                        } catch (IOException e) {
                            System.err.println(String.format("\nError in sending/receiving message: '%s'\n", e.getMessage()));
                        }
                    }
                });
            }
        } catch (SocketException e) {
            System.err.println(String.format("Can't connect to port: '%d'", port));
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        if (workers != null) {
            workers.shutdownNow();
        }
    }
}
