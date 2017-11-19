package com.dexmohq.dexml.exception;

public class XmlFormatException extends RuntimeException {
    public XmlFormatException() {
    }

    public XmlFormatException(String message) {
        super(message);
    }

    public XmlFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlFormatException(Throwable cause) {
        super(cause);
    }

    public XmlFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
