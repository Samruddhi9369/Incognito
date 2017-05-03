package me.scryptminers.android.incognito.Model;

/**
 * Created by Samruddhi on 5/2/2017.
 */

public class GroupMessage {
    private String message;
    private String from;
    private String groupName;
    private String direction;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getgroupName() {
        return groupName;
    }

    public void setgroupName(String groupName) {
        this.groupName = groupName;
    }

    public GroupMessage(String message, String from, String groupName, String direction) {
        this.message = message;
        this.from = from;
        this.groupName = groupName;
        this.direction = direction;
    }
}
