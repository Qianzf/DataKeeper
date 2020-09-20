package com.senchuuhi.datakeeper.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * 链接错误异常
 * Created by QQQZF on 2020/9/20.
 */
public class ConnectionErrorException extends RuntimeException  {

    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public ConnectionErrorException(Throwable cause, String errorMsg, Object... objs) {
        super(MessageFormatter.arrayFormat(errorMsg, objs).getMessage(), cause);
    }


    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public ConnectionErrorException(String errorMsg, Object... objs) {
        this(null, errorMsg, objs);
    }
}
