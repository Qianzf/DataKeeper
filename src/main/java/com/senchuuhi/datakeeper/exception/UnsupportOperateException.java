package com.senchuuhi.datakeeper.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * 不支持的操作异常
 * Created by QQQZF on 2020/9/20.
 */
public class UnsupportOperateException extends RuntimeException {


    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public UnsupportOperateException(Throwable cause, String errorMsg, Object... objs) {
        super(MessageFormatter.arrayFormat(errorMsg, objs).getMessage(), cause);
    }


    /**
     * 链接错误异常
     * @param errorMsg 错误信息
     */
    public UnsupportOperateException(String errorMsg, Object... objs) {
        this(null, errorMsg, objs);
    }

}
