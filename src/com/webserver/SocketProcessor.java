package com.webserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

/**
 * @author Honghan Zhu
 */
public class SocketProcessor implements Runnable {

    private Queue<Socket> inboundSocketQueue = new LinkedList<>();

    //todo Not used now - but perhaps will be later - to check for space in the buffer before reading from sockets (space for more to write?)
    private MessageBuffer readMessageBuffer = new MessageBuffer();
    private MessageBuffer writeMessageBuffer = new MessageBuffer();

    private IMessageReaderFactory messageReaderFactory = null;

    //todo use a better/faster queue
    private Queue<Message> outboundMessageQueue = new LinkedList<>();

    private Map<Long, Socket> socketMap = new HashMap<>();

    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);
    private Selector readSelector = null;
    private Selector writeSelector = null;

    private IMessageProcessor messageProcessor = null;
    private WriteProxy writeProxy = null;

    private long nextSocketId = 16 * 1024;

    private Set<Socket> nonEmptySockets = new HashSet<>();
    private Set<Socket> emptySockets = new HashSet<>();

    public SocketProcessor(/*Queue<Socket> inboundSocketQueue,*/ MessageBuffer readMessageBuffer, MessageBuffer writeMessageBuffer,
                           IMessageProcessor messageProcessor, IMessageReaderFactory messageReaderFactory, Selector readSelector) throws IOException {
//        this.inboundSocketQueue = inboundSocketQueue;
        this.readMessageBuffer = readMessageBuffer;
        this.writeMessageBuffer = writeMessageBuffer;
        this.messageProcessor = messageProcessor;
        this.messageReaderFactory = messageReaderFactory;
        this.writeProxy = new WriteProxy(writeMessageBuffer, outboundMessageQueue);
        this.readSelector = readSelector;
        this.writeSelector = Selector.open();
    }

    public void executeCycle() throws IOException {
//        takeNewSocket();
        readFromSocket();
        writeToSockets();
    }

    /* won't use this function
    public void takeNewSocket() throws IOException {
        Socket socket = inboundSocketQueue.poll();
        while (socket != null) {
            socket.socketId = nextSocketId++;
            socket.socketChannel.configureBlocking(false);
            socket.reader = messageReaderFactory.createMessageReader();
            socket.reader.init(readMessageBuffer);
            //a single MessageWriter for a single socket
            socket.writer = new MessageWriter();
            socketMap.put(socket.socketId, socket);
            SelectionKey key = socket.socketChannel.register(readSelector, SelectionKey.OP_READ, socket);
            socket = inboundSocketQueue.poll();
        }
    }
    */

    public void readFromSocket() throws IOException {
        int readySocketNum = readSelector.selectNow();
        if (readySocketNum > 0) {
            Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    acceptSocket(key);
                } else if (key.isReadable()) {
                    readFromSocket(key);
                }
                iterator.remove();
            }
            //does not help?
            selectionKeys.clear();
        }
    }

    private void acceptSocket(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        Socket socket = new Socket(socketChannel);
        socket.socketId = nextSocketId++;
        socket.socketChannel.configureBlocking(false);
        socket.reader = messageReaderFactory.createMessageReader();
        socket.reader.init(readMessageBuffer);
        //a single MessageWriter for a single socket
        socket.writer = new MessageWriter();
        socketMap.put(socket.socketId, socket);
        socket.socketChannel.register(readSelector, SelectionKey.OP_READ, socket);
    }

    private void readFromSocket(SelectionKey key) throws IOException {
        Socket socket = (Socket) key.attachment();
        socket.reader.read(socket, readByteBuffer);

        List<Message> fullMessages = socket.reader.getMessages();
        if (fullMessages.size() > 0) {
            //response while read full message.
            for (Message message : fullMessages) {
                message.socketId = socket.socketId;
                //only join response message to write queue.
                messageProcessor.process(message, writeProxy);
            }
            fullMessages.clear();
        }
        //为什么在这里移除socket
        if (socket.endOfStreamReached) {
            System.out.println("socket closed: " + socket.socketId);
            socketMap.remove(socket.socketId);
            key.attach(null);
            key.cancel();
            key.channel().close();
        }
    }

    public void writeToSockets() throws IOException {
        takeNewOutBoundMessages();
        cancelEmptySockets();
        registerNonEmptySockets();
        int writeSocketNum = writeSelector.selectNow();
        if (writeSocketNum > 0) {
            Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                Socket socket = (Socket) key.attachment();
                socket.writer.write(socket, writeByteBuffer);
                // next loop will cancel this socket in writer selector;
                if (socket.writer.isEmpty())
                    emptySockets.add(socket);
                iterator.remove();
            }
            selectionKeys.clear();
        }
    }

    private void takeNewOutBoundMessages() {
        Message outMessage = outboundMessageQueue.poll();
        while (outMessage != null) {
            // join writer queue from shared queue in write proxy with socket id
            Socket socket = socketMap.get(outMessage.socketId);
            if (socket != null) {
                MessageWriter writer = socket.writer;
                // change used socket state
                // if writer is empty, the socket was not used before
                if (writer.isEmpty()) {
                    emptySockets.remove(socket);
                    nonEmptySockets.add(socket);
                }

                socket.writer.enq(outMessage);
            }
            outMessage = outboundMessageQueue.poll();
        }
    }

    private void cancelEmptySockets() {
        for (Socket socket : emptySockets) {
            SelectionKey key = socket.socketChannel.keyFor(writeSelector);
            key.cancel();
        }
        emptySockets.clear();
    }

    private void registerNonEmptySockets() throws ClosedChannelException {
        for (Socket socket : nonEmptySockets) {
            SelectionKey key = socket.socketChannel.register(writeSelector, SelectionKey.OP_WRITE, socket);
        }
        nonEmptySockets.clear();
    }

    @Override
    public void run() {
        while (true) {
            try {
                executeCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
