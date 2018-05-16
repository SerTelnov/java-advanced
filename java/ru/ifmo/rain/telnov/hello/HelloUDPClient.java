package ru.ifmo.rain.telnov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Telnov Sergey on 13.05.2018.
 */
public class HelloUDPClient implements HelloClient {

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: <server ip-address> <port> <prefix> " +
                    "<threads> <requests>");
        } else {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getByName(host), port);
            ExecutorService workers = Executors.newFixedThreadPool(threads);
            for (int i = 0; i != threads; i++) {
                workers.execute(createRunnable(prefix, i, requests, address));
            }

            workers.shutdown();
            try {
                if (!workers.awaitTermination(60, TimeUnit.MINUTES)) {
                    workers.shutdownNow();
                    if (!workers.awaitTermination(60, TimeUnit.MINUTES)) {
                        System.err.println("Workers didn't terminate");
                    }
                }
            } catch (InterruptedException ie) {
                workers.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (UnknownHostException e) {
            System.err.println(String.format("Can't connect to host: '%s'", host));
        }
    }

    private Runnable createRunnable(final String message, final int id, final int requests, final SocketAddress address) {
        return () -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                final int receiveBufferSize = socket.getReceiveBufferSize();

                for (int i = 0; i != requests; i++) {
                    String sendMessage = String.format("%s%d_%d", message, id, i);
                    int currTimeout = 32;

                    byte[] sendBuff;
                    try {
                        sendBuff = sendMessage.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("Can't create message");
                        return;
                    }

                    DatagramPacket packet = new DatagramPacket(sendBuff, sendBuff.length, address);
                    while (true) {
                        socket.setSoTimeout(currTimeout);
                        try {
                            socket.send(packet);

                            DatagramPacket answer = new DatagramPacket(
                                    new byte[receiveBufferSize], receiveBufferSize, address);
                            try {
                                socket.receive(answer);

                                String receivedMessage = new String(
                                        answer.getData(), answer.getOffset(), answer.getLength(), "UTF-8");

                                if (receivedMessage.contains(sendMessage)) {
                                    System.out.println(receivedMessage);
                                    break;
                                }
                            } catch (SocketTimeoutException e) {
                                if (currTimeout < 6000) {
                                    currTimeout *= 2;
                                }
                            }
                        } catch (IOException e) {
                            System.err.println(String.format("\nError in sending/receiving message: '%s'\n", e.getMessage()));
                        }
                    }
                }
            } catch (SocketException e) {
                System.err.println("Can't connect to port");
            }
        };
    }
}
