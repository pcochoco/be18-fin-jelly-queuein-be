package com.beyond.qiin.domain.chat.exception;

import com.beyond.qiin.common.exception.BaseException;

public class ChatException extends BaseException {

    public ChatException(final ChatErrorCode errorCode) {
        super(errorCode);
    }

    public ChatException(final ChatErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
