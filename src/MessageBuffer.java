/**
 * @author Honghan Zhu
 */
public class MessageBuffer {
    public static int KB = 1024;
    public static int MB = 1024 * KB;

    private static final int CAPACITY_SMALL = 4 * KB;
    private static final int CAPACITY_MEDIUM = 128 * KB;
    private static final int CAPACITY_LARGE = 1024 * KB;

    byte[] smallMessageBuffer = new byte[1024 * CAPACITY_SMALL]; //4MB
    byte[] mediumMessageBuffer = new byte[128 * CAPACITY_MEDIUM]; //16MB
    byte[] largeMessageBuffer = new byte[16 * CAPACITY_LARGE]; //16MB

    QueueIntFlip smallMessageBufferFreeBlocks = new QueueIntFlip(1024);
    QueueIntFlip mediumMessageBufferFreeBlocks = new QueueIntFlip(128);
    QueueIntFlip largeMessageBufferFreeBlocks = new QueueIntFlip(16);

    public MessageBuffer() {
        for (int i = 0; i < smallMessageBuffer.length; i += CAPACITY_SMALL)
            smallMessageBufferFreeBlocks.put(i);
        for (int i = 0; i < mediumMessageBuffer.length; i += CAPACITY_MEDIUM)
            mediumMessageBufferFreeBlocks.put(i);
        for (int i = 0; i < largeMessageBuffer.length; i += CAPACITY_LARGE)
            largeMessageBufferFreeBlocks.put(i);
    }

    public Message getMessage() {
        int nextFreeSmallBlock = smallMessageBufferFreeBlocks.take();
        if (nextFreeSmallBlock == -1)
            return null;
        Message message = new Message(this);
        message.sharedArray = smallMessageBuffer;
        message.capacity = CAPACITY_SMALL;
        message.offset = nextFreeSmallBlock;
        message.length = 0;
        return message;
    }

    public boolean expandMessage(Message message) {
        if (message.capacity == CAPACITY_SMALL) {
            return moveMessage(message, smallMessageBufferFreeBlocks,
                    mediumMessageBufferFreeBlocks, mediumMessageBuffer, CAPACITY_MEDIUM);
        } else if (message.capacity == CAPACITY_MEDIUM) {
            return moveMessage(message, mediumMessageBufferFreeBlocks,
                    largeMessageBufferFreeBlocks, largeMessageBuffer, CAPACITY_LARGE);
        }
        return false;
    }

    public boolean moveMessage(Message message, QueueIntFlip fromQueueIntFlip,
                               QueueIntFlip toQueueIntFlip, byte[] buffer, int capacity) {
        int nextFreeBlock = toQueueIntFlip.take();
        if (nextFreeBlock == -1)
            return false;
        System.arraycopy(message.sharedArray, message.offset, buffer, nextFreeBlock, message.length);
        fromQueueIntFlip.put(message.offset);
        message.offset = nextFreeBlock;
        message.sharedArray = buffer;
        message.capacity = capacity;
        return true;
    }

}
