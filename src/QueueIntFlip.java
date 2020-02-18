/**
 * @Author Honghan Zhu
 */
public class QueueIntFlip {
    public int[] elements = null;

    public int capacity = 0;
    public int writePos = 0;
    public int readPos = 0;
    public boolean flipped = false;

    public QueueIntFlip(int capacity) {
        this.capacity = capacity;
        this.elements = new int[capacity];
    }

    public void reset() {
        capacity = 0;
        writePos = 0;
        readPos = 0;
        flipped = false;
    }

    public int available() {
        if (!flipped)
            return writePos - readPos;
        return capacity + writePos - readPos;
    }

    public int remaining() {
        if (!flipped)
            return capacity - writePos;
        return readPos - writePos;
    }

    public boolean put(int element) {
        if (!flipped) {
            if (writePos == capacity) {
                writePos = 0;
                flipped = true;
                if (writePos < readPos) {
                    elements[writePos++] = element;
                    return true;
                } else
                    return false;
            } else {
                elements[writePos++] = element;
                return true;
            }
        } else {
            if (writePos < readPos) {
                elements[writePos++] = element;
                return true;
            } else
                return false;
        }
    }

    public int put(int[] newElements, int length) {
        int newElementsReadPos = 0;
        if (!flipped) {
            if (capacity - writePos >= length) {
                while (newElementsReadPos < length)
                    elements[writePos++] = newElements[newElementsReadPos++];
            } else {
                while (writePos < capacity) {
                    elements[writePos++] = newElements[newElementsReadPos++];
                }
                writePos = 0;
                flipped = true;
                int endPos = Math.min(readPos, length - newElementsReadPos);
                while (writePos < endPos) {
                    elements[writePos++] = newElements[newElementsReadPos++];
                }
            }
        } else {
            int endPos = Math.min(readPos, writePos + length);
            while (writePos < endPos) {
                elements[writePos++] = newElements[newElementsReadPos++];
            }
        }
        return newElementsReadPos;
    }

    public int take() {
        if (!flipped) {
            if (readPos < writePos)
                return elements[readPos++];
            else return -1;
        } else {
            if (readPos == capacity) {
                readPos = 0;
                flipped = false;
                return take();
            } else {
                return elements[readPos++];
            }
        }
    }

    public int take(int[] into, int length) {
        int intoWritePos = 0;
        if (!flipped) {
            int endPos = Math.min(writePos, readPos + length);
            while (readPos < endPos) {
                into[intoWritePos++] = elements[readPos++];
            }
        } else {
            if (length <= capacity - readPos) {
                while (intoWritePos < length) {
                    into[intoWritePos++] = elements[readPos++];
                }
            } else {
                while (readPos < capacity) {
                    into[intoWritePos++] = elements[readPos++];
                }
                readPos = 0;
                flipped = false;
                int endPos = Math.min(writePos, length - intoWritePos);
                while (readPos < endPos) {
                    into[intoWritePos++] = elements[readPos++];
                }
            }
        }
        return intoWritePos;
    }
}
