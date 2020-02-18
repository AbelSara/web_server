import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String httpResponse = "HTTP/1.1 200 OK\r\n"+
                "Content-Length: 38\r\n"+
                "Content-Type: text/html\r\n"+
                "\r\n"+
                "<html><body>Hello ZHH!</body></html>";
        byte[] httpResponseByte = httpResponse.getBytes("UTF-8");


    }
}
