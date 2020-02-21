package com.webserver;

import java.util.Queue;

/**
 * @author Honghan Zhu
 */
public class WriteProxy {
    private MessageBuffer messageBuffer = null;
    private Queue<Message> writeQueue = null;

    public WriteProxy(MessageBuffer messageBuffer, Queue<Message> writeQueue) {
        this.messageBuffer = messageBuffer;
        this.writeQueue = writeQueue;
    }

    public Message getMessage() {
        return this.messageBuffer.getMessage();
    }

    public boolean enq(Message message) {
        return writeQueue.offer(message);
    }
}
