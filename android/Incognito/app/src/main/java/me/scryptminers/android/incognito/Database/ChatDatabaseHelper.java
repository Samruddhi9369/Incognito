package me.scryptminers.android.incognito.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import me.scryptminers.android.incognito.Model.User;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ChatDatabase";
    // Labels table name
    private static final String TABLE_FRIENDS = "Friends";
    // Labels Table Columns names
    private static final String KEY_FRIEND_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PUBLIC_KEY = "public_key";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Category table friends query
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

        db.execSQL(CREATE_FRIENDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        // Create tables again
        onCreate(db);
    }


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

}
