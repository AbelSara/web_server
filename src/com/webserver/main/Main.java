package com.webserver.main;

import com.webserver.IMessageProcessor;
import com.webserver.Message;
import com.webserver.Server;
import com.webserver.httpImpl.HttpMessageReaderFactory;

import java.io.IOException;

/**
 * @author Honghan Zhu
 */
public class Main {
    public static void main(String[] args) throws IOException {

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 38\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body>Hello World!</body></html>";

        byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");

        IMessageProcessor messageProcessor = (request, writeProxy) -> {
            System.out.println("Message Received from socket: " + request.socketId);

            Message response = writeProxy.getMessage();

            //bind write socket with read socket id;
            response.socketId = request.socketId;
            response.writeToMessage(httpResponseBytes);

            writeProxy.enq(response);
        };

        Server server = new Server(9999, new HttpMessageReaderFactory(), messageProcessor);

        server.startServer();

    }

}
