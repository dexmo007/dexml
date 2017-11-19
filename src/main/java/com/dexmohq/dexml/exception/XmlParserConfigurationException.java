package com.dexmohq.dexml.exception;

class XmlParserConfigurationException extends RuntimeException {
    public XmlParserConfigurationException() {
    }

    public XmlParserConfigurationException(String message) {
        super(message);
    }

    public XmlParserConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlParserConfigurationException(Throwable cause) {
        super(cause);
    }

    public XmlParserConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
