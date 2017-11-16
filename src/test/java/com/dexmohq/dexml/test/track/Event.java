package com.dexmohq.dexml.test.track;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@XmlRootElement(name = "data")
public class Event {

    private String text;

    private long timestamp;

    public Event(String text, LocalDateTime timestamp) {
        this.text = text;
        this.timestamp = timestamp.toEpochSecond(ZoneOffset.UTC);
    }

    public Event() {
    }

    @XmlAttribute(name = "event-info")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlAttribute(name = "event-timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
