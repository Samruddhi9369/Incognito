package me.scryptminers.android.incognito.Model;

/**
 * Created by Samruddhi on 4/27/2017.
 */

public class Group {
    private String groupName;
    private String groupAdmin;
    private String[] groupMembers;

    public Group(){

    }

    public Group(String groupName, String groupAdmin, String[] groupMembers) {
        this.groupName = groupName;
        this.groupAdmin = groupAdmin;
        this.groupMembers = groupMembers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public String[] getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(String[] groupMembers) {
        this.groupMembers = groupMembers;
    }

}
