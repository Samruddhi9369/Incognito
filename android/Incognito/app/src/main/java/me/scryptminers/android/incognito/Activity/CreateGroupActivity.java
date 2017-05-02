package me.scryptminers.android.incognito.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.scryptminers.android.incognito.Adapter.CustomFriendsAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Util.SharedValues;

public class CreateGroupActivity extends AppCompatActivity {
    EditText groupName;
    Button btnCreateGroup;
    ListView listViewGroups;
    ArrayAdapter arrayAdapter;
    private List<User> friends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        groupName = (EditText) findViewById(R.id.edit_group_name);
        btnCreateGroup = (Button) findViewById(R.id.button_create_group);
        listViewGroups = (ListView) findViewById(R.id.listview_groupfriends);
        loadFriends();
    }
    public void loadFriends(){
        ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
        //User user = new User("sam","kal","email","8978675645","pwd","cpwd");

        friends = db.getAllUsers();
        String[] friendNames = new String[friends.size()];
        for(int i =0;i<friends.size();i++){
            User user = friends.get(i);
            friendNames[i] = user.getFirstName() + " " + user.getLastName();
        }
        arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, friendNames);
        listViewGroups.setAdapter(arrayAdapter);
        listViewGroups.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strGroupName = groupName.getText().toString();
                if(strGroupName.length() > 0){
                    SparseBooleanArray selectedItems = listViewGroups.getCheckedItemPositions();
                    ArrayList<String> groupMembers = new ArrayList<>();
                    for (int i = 0; i < friends.size(); i++) {
                        if (selectedItems.get(i)) {
                            User user = friends.get(i);
                            groupMembers.add(String.valueOf(user.getEmail()));
                            Log.e("userid",String.valueOf(user.getEmail()));
                        }
                    }
                    if(groupMembers.size() > 0){
                        //Add yourself to the group
                        String adminEmail = SharedValues.getValue("USER_EMAIL");
                        groupMembers.add(SharedValues.getValue("USER_EMAIL"));
                        createGroup(strGroupName, groupMembers, adminEmail);
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "Select at least one friend to create a group.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Group Name cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void createGroup(String groupName, ArrayList<String> groupMembers, String groupAdmin){
        //String[] members = new String[groupMembers.size()];
        String[] members = groupMembers.toArray(new String[groupMembers.size()]);
        Group group = new Group(groupName,groupAdmin,members);
        for(int i =0;i<members.length;i++){
            Log.e("member",members[i].toString());
        }
        Log.e("details",groupName+" "+ groupAdmin);
        StringBuilder builder = new StringBuilder();
        String[] members1 = group.getGroupMembers();
        for (String member : members1) {
            builder.append(member).append(",");
        }

        builder.deleteCharAt(builder.length() - 1);
        Log.e("MEmbers",builder.toString());
        ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
        db.insertGroup(group);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
