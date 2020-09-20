package com.senchuuhi.datakeeper.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * Created by QQQZF on 2020/9/20.
 */
public class CheckInitDataErrorException  extends RuntimeException {

    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public CheckInitDataErrorException(Throwable cause, String errorMsg, Object... objs) {
        super(MessageFormatter.arrayFormat(errorMsg, objs).getMessage(), cause);
    }


    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public CheckInitDataErrorException(String errorMsg, Object... objs) {
        this(null, errorMsg, objs);
    }
}
