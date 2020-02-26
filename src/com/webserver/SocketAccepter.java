package com.webserver;


import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;


/**
 * @author Honghan Zhu
 */
public class SocketAccepter implements Runnable {
    private int tcpPort = 0;
    private ServerSocketChannel serverSocketChannel = null;
//    private Queue<Socket> socketQueue = null;
    private Selector selector = null;

    public SocketAccepter(int tcpPort, /*Queue<Socket> socketQueue,*/ Selector readSelector) {
        this.tcpPort = tcpPort;
//        this.socketQueue = socketQueue;
        this.selector = readSelector;
    }


    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(tcpPort));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("register server socket channel to accept new connection.");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        while (true) {
//            try {

//                SocketChannel socketChannel = serverSocketChannel.accept();
//                System.out.println("com.webserver.Socket accepted: " + socketChannel);
//                socketQueue.add(new Socket(socketChannel));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
