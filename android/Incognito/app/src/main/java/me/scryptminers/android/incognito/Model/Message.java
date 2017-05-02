package me.scryptminers.android.incognito.Model;

/**
 * Created by Samruddhi on 3/30/2017.
 */

public class Message {
    private String from;

    public Message(String from, String to, String message, String direction) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.direction = direction;
    }

    private String to;
    private String message;
    private String direction;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }


}
