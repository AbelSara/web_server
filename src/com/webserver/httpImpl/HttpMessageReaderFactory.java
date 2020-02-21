package com.webserver.httpImpl;

import com.webserver.IMessageReader;
import com.webserver.IMessageReaderFactory;

/**
 * @author Honghan Zhu
 */
public class HttpMessageReaderFactory implements IMessageReaderFactory {
    @Override
    public IMessageReader createMessageReader() {
        return new HttpMessageReader();
    }
}
