import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Honghan Zhu
 */
public interface IMessageReader {
    public void init(MessageBuffer messageBuffer);
    public void read(Socket socket, ByteBuffer byteBuffer) throws IOException;
    public List<Message> getMessages();
}
