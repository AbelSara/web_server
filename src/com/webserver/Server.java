package com.webserver;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Honghan Zhu
 */
public class Server {
    private SocketAccepter socketAccepter = null;
    private SocketProcessor socketPorcessor = null;

    private int tcpPort = 0;
    private IMessageReaderFactory messageReaderFactory = null;
    private IMessageProcessor messageProcessor = null;

    public Server(int tcpPort, IMessageReaderFactory messageReaderFactory, IMessageProcessor messageProcessor) {
        this.tcpPort = tcpPort;
        this.messageProcessor = messageProcessor;
        this.messageReaderFactory = messageReaderFactory;
    }

    public void startServer() throws IOException {
//        Queue<Socket> socketQueue = new LinkedBlockingQueue<>();
        MessageBuffer readBuffer = new MessageBuffer();
        MessageBuffer writeBuffer = new MessageBuffer();
        Selector readSelector = Selector.open();
        socketAccepter = new SocketAccepter(tcpPort, readSelector);
        socketPorcessor = new SocketProcessor(readBuffer, writeBuffer,
                messageProcessor, messageReaderFactory,readSelector);
        new Thread(socketAccepter).start();
        new Thread(socketPorcessor).start();
    }
}
