package com.webserver;

import java.nio.ByteBuffer;

/**
 * @author Honghan Zhu
 */
public class Message {
    private MessageBuffer messageBuffer = null;
    public long socketId = 0;
    public byte[] sharedArray = null;
    public int offset = 0;
    public int capacity = 0;
    public int length = 0;
    public Object metaData = null;

    public Message(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public int writeToMessage(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();
        while (remaining > capacity - length) {
            if (!messageBuffer.expandMessage(this))
                return -1;
        }
        //为什么在这一步需要比较，如果剩余空间不够且未扩容成功上一步会返回
        int copyBytes = Math.min(remaining, capacity - length);
        //从上一次结束的pos继续复制
        byteBuffer.get(sharedArray, offset + length, copyBytes);
        length += copyBytes;
        return copyBytes;
    }

    public int writeToMessage(byte[] bytes, int offset, int copyLen) {
        int remaining = copyLen;
        while (remaining > capacity - length) {
            if (!messageBuffer.expandMessage(this))
                return -1;
        }
        int copyBytes = Math.min(remaining, capacity - length);
        System.arraycopy(bytes, offset, sharedArray, this.offset + this.length, copyBytes);
        this.length += copyBytes;
        return copyBytes;
    }

    public int writeToMessage(byte[] bytes) {
        return writeToMessage(bytes, 0, bytes.length);
    }

    public int writePartitionMessageToMessage(Message message, int lastIdx) {
        int startIdx = message.offset + lastIdx;
        int len = message.length - (lastIdx - message.offset);
        while (len > capacity - this.length) {
            if (!messageBuffer.expandMessage(this))
                return -1;
        }
        System.arraycopy(message.sharedArray, startIdx, this.sharedArray, this.offset, len);
        return len;
    }
}
