package me.scryptminers.android.incognito.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.GroupMessage;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Model.User;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ChatDatabase";
    // Labels table name
    private static final String TABLE_FRIENDS = "Friends";
    private static final String TABLE_MESSAGES = "Messages";
    private static final String TABLE_GROUP_MESSAGES = "GroupMessages";
    // Labels Table Columns names
    private static final String KEY_FRIEND_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PUBLIC_KEY = "public_key";
    // Group Table
    private static final String TABLE_GROUPS = "Groups";
    // Labels Table Columns names
    private static final String KEY_GROUP_ID = "id";
    private static final String KEY_ADMIN_EMAIL = "admin_email";
    private static final String KEY_MEMBERS = "members";
    private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_GROUP_KEY = "group_key";
    //Messages Table
    private static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_DIRECTION = "direction";
    private static final String KEY_FROM_EMAIL = "from_email";
    private static final String KEY_TO_EMAIL = "to_email";
    private static final String KEY_MESSAGE = "message";
    //Group Messages Table


    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table friends query
        String CREATE_FRIENDS_TABLE = "CREATE TABLE " +
                TABLE_FRIENDS + "("
                + KEY_FRIEND_ID + " INTEGER PRIMARY KEY, "
                + KEY_USER_ID + " INTEGER, "
                + KEY_FIRST_NAME + " TEXT, "
                + KEY_LAST_NAME + " TEXT, "
                + KEY_EMAIL + " TEXT, "
                + KEY_PHONE + " TEXT, "
                + KEY_PUBLIC_KEY + " TEXT "
                + " );";
        // Create table groups query
        String CREATE_GROUPS_TABLE = "CREATE TABLE " +
                TABLE_GROUPS + "("
                + KEY_GROUP_ID + " INTEGER PRIMARY KEY, "
                + KEY_ADMIN_EMAIL + " TEXT, "
                + KEY_MEMBERS + " TEXT, "
                + KEY_GROUP_KEY + " TEXT, "
                + KEY_GROUP_NAME + " TEXT "
                + " );";

        // Create table messages query
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " +
                TABLE_MESSAGES + "("
                + KEY_MESSAGE_ID + " INTEGER PRIMARY KEY, "
                + KEY_DIRECTION + " TEXT, "
                + KEY_FROM_EMAIL + " TEXT, "
                + KEY_TO_EMAIL + " TEXT, "
                + KEY_MESSAGE + " TEXT "
                + " );";
        // Create table group messages query
        String CREATE_GROUP_MESSAGES_TABLE = "CREATE TABLE " +
                TABLE_GROUP_MESSAGES + "("
                + KEY_MESSAGE_ID + " INTEGER PRIMARY KEY, "
                + KEY_DIRECTION + " TEXT, "
                + KEY_GROUP_NAME + " TEXT, "
                + KEY_FROM_EMAIL + " TEXT, "
                + KEY_TO_EMAIL + " TEXT, "
                + KEY_MESSAGE + " TEXT "
                + " );";

        db.execSQL(CREATE_FRIENDS_TABLE);
        db.execSQL(CREATE_GROUPS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
        db.execSQL(CREATE_GROUP_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_MESSAGES);
        // Create tables again
        onCreate(db);
    }

    // Add a new friend into the table friends
    public void insertFriend(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID,user.getUserId());
        values.put(KEY_FIRST_NAME,user.getFirstName());
        values.put(KEY_LAST_NAME,user.getLastName());
        values.put(KEY_EMAIL,user.getEmail());
        values.put(KEY_PHONE,user.getPhone());
        values.put(KEY_PUBLIC_KEY,user.getPublicKey());
        // Inserting Row
        db.insert(TABLE_FRIENDS, null, values);
        db.close();
        // Closing database connection
    }

    // Get the public key of the friend
    public String getFriendPublicKey(String email){
        String publicKey="";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] Projection = {
                KEY_PUBLIC_KEY
        };

        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = { ""+email };

        Cursor cursor = db.query(
                TABLE_FRIENDS,
                Projection,
                selection,
                selectionArgs,
                null,
                null,
                null

        );
        cursor.moveToNext();
        publicKey = cursor.getString(0);
        // closing connection
        cursor.close();
        db.close();
        return publicKey;
    }

    // Get all friends of the user
    public List<User> getAllUsers(){
        List<User> friends = new ArrayList<User>();
        // Select All Query
        String selectQuery = "SELECT * FROM " +
                TABLE_FRIENDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                friends.add(new User(cursor.getLong(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6)));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning friends
        return friends;
    }

    // Add a group into the table Groups
    public void insertGroup(Group group){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ADMIN_EMAIL,group.getGroupAdmin());
        //Convert String Array to comma Separated String
        StringBuilder builder = new StringBuilder();
        String[] members = group.getGroupMembers();
        for (String member : members) {
            builder.append(member).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);

        values.put(KEY_MEMBERS,builder.toString());
        values.put(KEY_GROUP_NAME,group.getGroupName());
        values.put(KEY_GROUP_KEY,"");
        // Inserting Row
        db.insert(TABLE_GROUPS, null, values);
        db.close();
        // Closing database connection
    }

    // Get all groups whose user as an admin
    public List<Group> getAllGroups(){
        List<Group> groups = new ArrayList<Group>();
        // Select All Query
        String selectQuery = "SELECT * FROM " +
                TABLE_GROUPS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String[] members = cursor.getString(2).split(",");
                groups.add(new Group(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GROUP_NAME)),cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADMIN_EMAIL)),members));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning groups
        return groups;
    }

    // Add a message into the database
    public void addMessage(Message message){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DIRECTION,message.getDirection());
        values.put(KEY_FROM_EMAIL,message.getFrom());
        values.put(KEY_TO_EMAIL,message.getTo());
        values.put(KEY_MESSAGE,message.getMessage());
        // Inserting Row
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // Get all messages from the database
    public List<Message> getAllMessages(String userEmail, String friendEmail){
        List<Message> messages = new ArrayList<Message>();
        // Select All Query

        String selectQuery = "SELECT * FROM " +
                TABLE_MESSAGES +
                " WHERE " +
                "(" + KEY_FROM_EMAIL + " = " +
                "'"+userEmail+"'" +" OR "+
                KEY_TO_EMAIL + " = " +
                "'"+userEmail+"'" + ")" +
                "AND" +
                "(" + KEY_FROM_EMAIL + " = " +
                "'"+friendEmail+"'" +" OR "+
                KEY_TO_EMAIL + " = " +
                "'"+friendEmail+"'" + ")";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                messages.add(new Message(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TO_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIRECTION))));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning groups
        return messages;
    }

    // Add a group message into the table Group Messages
    //direction, group_name, from, message
    public void addGroupMessage(GroupMessage groupMessage){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DIRECTION,groupMessage.getDirection());
        values.put(KEY_GROUP_NAME,groupMessage.getgroupName());
        values.put(KEY_FROM_EMAIL,groupMessage.getFrom());
        values.put(KEY_TO_EMAIL,groupMessage.getTo());
        values.put(KEY_MESSAGE,groupMessage.getMessage());
        // Inserting Row
        db.insert(TABLE_GROUP_MESSAGES, null, values);
        db.close();
    }

    // Get all group messages
    public List<GroupMessage> getAllGroupMessages(String userEmail, String groupName){
        List<GroupMessage> messages = new ArrayList<GroupMessage>();
        // Select All Query
        String selectQuery = "SELECT * FROM " +
                TABLE_GROUP_MESSAGES +
                " WHERE ("+
                KEY_TO_EMAIL + "='" + userEmail+"'"+
                " OR " +
                KEY_FROM_EMAIL + "='" + userEmail+"'"+ ")" +
                " AND "+ KEY_GROUP_NAME + "='" + groupName +"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        //message, from, groupname, direction
        if (cursor.moveToFirst()) {
            do {
                messages.add(new GroupMessage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MESSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_FROM_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TO_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_GROUP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_DIRECTION))));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning groups
        return messages;
    }

    // Get all members of the group
    public String[] getAllGroupMembers(String groupName){
        String[] members={};
        String selectQuery = "SELECT * FROM " +
                TABLE_GROUPS +
                " WHERE " +
                KEY_GROUP_NAME +" = "
                + "'"+groupName+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                members = cursor.getString(2).split(",");
                //groups.add(new Group(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GROUP_NAME)),cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADMIN_EMAIL)),members));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning groups
        return members;
    }

    // Method to update the groupkey acquired from the admin.
    public void updateGroupKey(String groupname,String groupkey){
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = KEY_GROUP_NAME + "= ?";
        String[] whereArgs = {groupname};
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_GROUP_KEY, groupkey);
        db.update(TABLE_GROUPS, newValues, whereClause, whereArgs);
        // closing connection
        db.close();
    }

    // Get the group key of the group
    public String getGroupKey(String groupName) {
        String groupkey="";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] Projection = {
                KEY_GROUP_KEY
        };

        String selection = KEY_GROUP_NAME + " = ?";
        String[] selectionArgs = {""+groupName};

        Cursor cursor = db.query(
                TABLE_GROUPS,
                Projection,
                selection,
                selectionArgs,
                null,
                null,
                null

        );
        cursor.moveToNext();
        groupkey = cursor.getString(0);
        // closing connection
        cursor.close();
        db.close();
        return groupkey;

    }

    // Get admin of the group
    public String getAdmin(String groupName){
        String admin="";
        String selectQuery = "SELECT * FROM " +
                TABLE_GROUPS +
                " WHERE " +
                KEY_GROUP_NAME +" = "
                + "'"+groupName+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                admin = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADMIN_EMAIL));
                //groups.add(new Group(cursor.getString(cursor.getColumnIndexOrThrow(KEY_GROUP_NAME)),cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADMIN_EMAIL)),members));
            } while (cursor.moveToNext());
        }
        // closing connection
        cursor.close();
        db.close();
        // returning groups
        return admin;
    }
}
