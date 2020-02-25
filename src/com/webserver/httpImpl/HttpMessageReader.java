package com.webserver.httpImpl;

import com.webserver.IMessageReader;
import com.webserver.Message;
import com.webserver.MessageBuffer;
import com.webserver.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Honghan Zhu
 */
public class HttpMessageReader implements IMessageReader {
    private MessageBuffer messageBuffer = null;
    private List<Message> completeMessages = new ArrayList<>();
    private Message nextMessage = null;

    @Override
    public void init(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
        this.nextMessage = this.messageBuffer.getMessage();
        this.nextMessage.metaData = new HttpHeaders();
    }

    @Override
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
        int bytesRead = socket.read(byteBuffer);
        byteBuffer.flip();
        if (!byteBuffer.hasRemaining()) {
            byteBuffer.clear();
            return;
        }
        nextMessage.writeToMessage(byteBuffer);
        int endIdx = HttpUtil.parseHttpRequest(nextMessage.sharedArray, nextMessage.offset,
                nextMessage.offset + nextMessage.length, (HttpHeaders) nextMessage.metaData);

        if (endIdx != -1) {
            Message message = this.messageBuffer.getMessage();
            message.metaData = new HttpHeaders();

            message.writePartitionMessageToMessage(nextMessage, endIdx);
            //是否应该将nextMessage中的内容截断
            completeMessages.add(nextMessage);
            nextMessage = message;
        }
        byteBuffer.clear();
    }

    @Override
    public List<Message> getMessages() {
        return this.completeMessages;
    }
}
