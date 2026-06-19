package com.beyond.qiin.domain.rag.exception;

import com.beyond.qiin.common.exception.BaseException;

public class RagException extends BaseException {

    public RagException(final RagErrorCode errorCode) {
        super(errorCode);
    }

    public RagException(final RagErrorCode errorCode, final String message) {
        super(errorCode, message);
    }
}
