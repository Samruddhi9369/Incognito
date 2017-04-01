package me.scryptminers.android.incognito.Model;

/**
 * Created by Samruddhi on 3/30/2017.
 */

public class Message {
    private String author;
    private String message;
    private String direction;

    public Message(String author, String message, String direction) {
        this.author = author;
        this.message = message;
        this.direction = direction;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
