package com.webserver;

/**
 * @author Honghan Zhu
 */
public interface IMessageProcessor {
    public void process(Message message, WriteProxy writeProxy);
}
