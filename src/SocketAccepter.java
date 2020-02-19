import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * @author Honghan Zhu
 */
public class SocketAccepter implements Runnable {
    private int tcpPort = 0;
    private ServerSocketChannel serverSocketChannel = null;
    private Queue<Socket> socketQueue = null;

    public SocketAccepter(int tcpPort, Queue<Socket> socketQueue) {
        this.tcpPort = tcpPort;
        this.socketQueue = socketQueue;
    }


    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(tcpPort));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("Socket accepted: " + socketChannel);
                socketQueue.add(new Socket(socketChannel));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
