import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * @author Honghan Zhu
 */
public class MessageWriter {
    private LinkedList<Message> writerQueue = new LinkedList<>();
    private Message messageInProgress = null;
    private int bytesWritten = 0;

    public void enq(Message message) {
        if (messageInProgress == null)
            messageInProgress = message;
        else writerQueue.add(message);
    }

    public void write(Socket socket, ByteBuffer byteBuffer) throws IOException {
        if (isEmpty())
            return;
        else if (messageInProgress == null)
            messageInProgress = writerQueue.removeFirst();
        byteBuffer.put(messageInProgress.sharedArray, messageInProgress.offset, messageInProgress.length);
        byteBuffer.flip();
        bytesWritten = socket.getChannel().write(byteBuffer);
        byteBuffer.clear();
        if (bytesWritten >= messageInProgress.length) {
            messageInProgress = null;
            if (!writerQueue.isEmpty())
                messageInProgress = writerQueue.removeFirst();
        }
    }

    public boolean isEmpty() {
        return messageInProgress == null && writerQueue.isEmpty();
    }
}
