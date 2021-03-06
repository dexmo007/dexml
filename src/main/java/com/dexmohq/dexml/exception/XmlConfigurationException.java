package com.dexmohq.dexml.exception;

public class XmlConfigurationException extends RuntimeException {
    public XmlConfigurationException() {
    }

    public XmlConfigurationException(String message) {
        super(message);
    }

    public XmlConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlConfigurationException(Throwable cause) {
        super(cause);
    }

    public XmlConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
