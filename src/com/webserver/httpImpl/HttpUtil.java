package com.webserver.httpImpl;


import java.io.UnsupportedEncodingException;

/**
 * @author Honghan Zhu
 */
public class HttpUtil {
    private static final byte[] GET = new byte[]{'G', 'E', 'T'};
    private static final byte[] POST = new byte[]{'P', 'O', 'S', 'T'};
    private static final byte[] PUT = new byte[]{'P', 'U', 'T'};
    private static final byte[] HEAD = new byte[]{'H', 'E', 'A', 'D'};
    private static final byte[] DELETE = new byte[]{'D', 'E', 'L', 'E', 'T', 'E'};

    private static final byte[] HOST = new byte[]{'H', 'o', 's', 't'};
    private static final byte[] CONTENT_LENGTH = new byte[]{'C', 'o', 'n', 't', 'e', 'n', 't', '-', 'L', 'e', 'n', 'g', 't', 'h'};

    public static int parseHttpRequest(byte[] src, int startIdx, int endIdx, HttpHeaders httpHeaders)
            throws UnsupportedEncodingException {
        int endOfFirstLine = findNextLineBreak(src, startIdx, endIdx);
        if (endOfFirstLine == -1)
            return -1;
        int startOfHeader = endOfFirstLine + 1;
        int endOfHeader = findNextLineBreak(src, startOfHeader, endIdx);
        // 循环寻找
        while (endOfHeader != -1 && endOfHeader != startOfHeader + 1) {
            if (matchesContentLength(src, startOfHeader, CONTENT_LENGTH)) {
                findContentLength(src, startOfHeader, endOfHeader, httpHeaders);
            }
            startOfHeader = endOfHeader + 1;
            endOfHeader = findNextLineBreak(src, startOfHeader, endIdx);
        }
        if (endOfHeader == -1)
            return -1;
        int bodyStartIndex = endOfHeader + 1;
        int bodyEndIndex = bodyStartIndex + httpHeaders.contentLength;
        if (bodyEndIndex <= endIdx) {
            httpHeaders.bodyStartIndex = bodyStartIndex;
            httpHeaders.bodyEndIndex = bodyEndIndex;
            return bodyEndIndex;
        }
        return -1;
    }

    private static void findContentLength(byte[] src, int startIdx, int endIdx, HttpHeaders httpHeaders)
            throws UnsupportedEncodingException {
        startIdx = findNext(src, startIdx, endIdx, (byte) ':');
        // will not jump;
        if (startIdx == -1)
            return;
        while (src[startIdx] == ' ')
            startIdx += 1;
        int lenStart = startIdx;
        int lenEnd = startIdx;
        while (lenEnd < endIdx) {
            if (src[lenEnd] >= '0' && src[lenEnd] <= '9')
                lenEnd += 1;
            else
                break;
        }
        httpHeaders.contentLength = Integer.parseInt(
                new String(src, lenStart, lenEnd - lenStart, "utf-8"));
    }

    private static int findNext(byte[] src, int startIdx, int endIdx, byte val) {
        for (int i = startIdx; i < endIdx; i++) {
            if (src[i] == val)
                return i + 1;
        }
        return -1;
    }

    public static int findNextLineBreak(byte[] src, int startIdx, int endIdx) {
        for (int i = startIdx; i < endIdx; i++) {
            if (i > startIdx && src[i] == '\n' && src[i - 1] == '\r')
                return i;
        }
        return -1;
    }

    public static boolean matchesContentLength(byte[] src, int startIdx, byte[] dest) {
        for (int i = 0; i < dest.length; i++) {
            if (src[i + startIdx] != dest[i])
                return false;
        }
        return true;
    }
}
