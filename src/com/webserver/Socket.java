package com.webserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Honghan Zhu
 */
public class Socket {
    public long socketId;
    public SocketChannel socketChannel = null;
    public IMessageReader reader = null;
    public MessageWriter writer = null;
    public boolean endOfStreamReached = false;

    public Socket(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public int read(ByteBuffer byteBuffer) throws IOException{
        int len = 0;
        try {
            len = socketChannel.read(byteBuffer);
        }catch(IOException e){
            len = -1;
        }
        int total = len;
        while (len > 0) {
            len = socketChannel.read(byteBuffer);
            total += len;
        }
        if (len == -1)
            endOfStreamReached = true;
        return total;
    }

    public int write(ByteBuffer byteBuffer) throws IOException {
        int len = socketChannel.write(byteBuffer);
        int total = len;
        while (byteBuffer.hasRemaining()) {
            len = socketChannel.write(byteBuffer);
            total += len;
        }
        return total;
    }
}
