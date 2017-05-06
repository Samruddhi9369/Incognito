package me.scryptminers.android.incognito.Activity;

import android.content.Intent;
import android.os.AsyncTask;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import me.scryptminers.android.incognito.Adapter.CustomFriendsAdapter;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.SharedValues;

public class CreateGroupActivity extends AppCompatActivity {
    EditText groupName;
    Button btnCreateGroup;
    ListView listViewGroups;
    ArrayAdapter arrayAdapter;
    private List<User> friends;
    private boolean isCreated=false;
    CreateGroupTask createGroupTask;
    KeyFactory keyFactory;
    PublicKey receiverPublicKey;
    byte[] encodedKeys;
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
                SecretKey groupKey=null;
                String strGroupName = groupName.getText().toString();
                if(strGroupName.length() > 0){
                    try {
                        // Generate symmetric group key
                        KeyGenerator keygen = KeyGenerator.getInstance("AES");
                        keygen.init(256);
                        groupKey = keygen.generateKey();
                        String groupkey = Base64.toBase64String(groupKey.getEncoded());
                        //String groupkey = new String(groupKey.getEncoded(),"UTF-8");
                       // String stringKey = android.util.Base64.encodeToString(groupKey.getEncoded(), android.util.Base64.DEFAULT);
                        SharedValues.save(strGroupName+"_KEY",groupkey);
                        //SharedValues.save(strGroupName+"_Group_Key",stringKey);

                    }
                    catch (NoSuchAlgorithmException e)
                    {
                        e.printStackTrace();
                    }
                    SparseBooleanArray selectedItems = listViewGroups.getCheckedItemPositions();
                    ArrayList<String> groupMembers = new ArrayList<>();
                    ArrayList<String> memberKeys = new ArrayList<String>();
                    for (int i = 0; i < friends.size(); i++) {
                        if (selectedItems.get(i)) {
                            User user = friends.get(i);
                            groupMembers.add(String.valueOf(user.getEmail()));
                            Log.e("userid",String.valueOf(user.getEmail()));
                            String memberKey = user.getPublicKey();
                            byte[] decodeKey = Base64.decode(memberKey);
                            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey);
                            try {
                                keyFactory = KeyFactory.getInstance("RSA");
                                receiverPublicKey = keyFactory.generatePublic(keySpec);
                                encodedKeys = PGP.encryptKeys(groupKey.getEncoded(),receiverPublicKey);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (InvalidKeySpecException e) {
                                e.printStackTrace();
                            } catch (NoSuchPaddingException e) {
                                e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            } catch (IllegalBlockSizeException e) {
                                e.printStackTrace();
                            } catch (BadPaddingException e) {
                                e.printStackTrace();
                            } catch (NoSuchProviderException e) {
                                e.printStackTrace();
                            }

                            //byte[] encryptedGroupKey = PGP.encryptKeys(groupKey.getEncoded(), receiverPublicKey);
                                String strGroupKey = Base64.toBase64String(encodedKeys);
                                Log.e("strGroupKey",strGroupKey);
                                //String encryptedGroupKey = PGP.encryptMessage(strGroupKey, memberKey);

                            //memberKeys.add(Base64.toBase64String(encryptedGroupKey));
                                //Cipher cipher = Cipher.getInstance("RSA");
                                //cipher.init(Cipher.ENCRYPT_MODE,receiverPublicKey);
                                //byte[] encryptedGroupKey = cipher.doFinal(groupKey.getEncoded());
                              // String encrytedgp = Base64.toBase64String(encryptedGroupKey);
                              //  String encrytedgp = android.util.Base64.encodeToString(encryptedGroupKey,android.util.Base64.NO_PADDING);
                                strGroupKey=strGroupKey+"_"+groupName.getText().toString()+"_Key"; // groupkey_grouname_Key
                                //SendKeyMessageTask sendKeyMessageTask = new SendKeyMessageTask(new Message(SharedValues.getValue("USER_EMAIL"),user.getEmail(),strGroupKey,"left"));
                                //sendKeyMessageTask.execute("asd");
                               // memberKeys.add(encryptedGroupKey);

                        }
                    }
                    if(groupMembers.size() > 0){
                        //Add yourself to the group
                        String adminEmail = SharedValues.getValue("USER_EMAIL");
                        groupMembers.add(SharedValues.getValue("USER_EMAIL"));
                        createGroup(strGroupName, groupMembers, adminEmail,memberKeys);
                    } else {
                        Toast.makeText(CreateGroupActivity.this, "Select at least one friend to create a group.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateGroupActivity.this, "Group Name cannot be blank", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //group name, group members, group admin
    public void createGroup(String groupName, ArrayList<String> groupMembers, String groupAdmin, ArrayList<String> memberKeys){
        //String[] members = new String[groupMembers.size()];
        String[] members = groupMembers.toArray(new String[groupMembers.size()]);
        String[] memberkeys = memberKeys.toArray(new String[memberKeys.size()]);
        Group group = new Group(groupName,groupAdmin,members,memberkeys);
        for(int i =0;i<members.length;i++){
            Log.e("member",members[i].toString());
        }
        // Save group in database
        createGroupTask = new CreateGroupTask(group);
        createGroupTask.execute();
    }

    public class CreateGroupTask extends AsyncTask<String, Void, Boolean> {

        private final Group group;
        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/createGroup";

        public CreateGroupTask(Group group) {
            this.group = group;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("name",group.getGroupName());
            userMap.put("admin",group.getGroupAdmin());
            //Convert String Array to comma Separated String
            StringBuilder builder = new StringBuilder();
            String[] members = group.getGroupMembers();
            for (String member : members) {
                builder.append(member).append(",");
            }

            builder.deleteCharAt(builder.length() - 1);
            userMap.put("members",builder.toString());

/*            StringBuilder builder1 = new StringBuilder();
            String[] keys = group.getGroupkeys();
            for (String key : keys) {
                builder1.append(key).append(",");
            }
            builder1.deleteCharAt(builder1.length() - 1);*/
            userMap.put("keys","");

            Log.e("name",group.getGroupName());
            Log.e("admin",group.getGroupAdmin());
            Log.e("members",builder.toString());

            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String statuscode=response.getString("code");
                            Log.e("statuscode",statuscode);
                            if(statuscode.matches("0") || statuscode.matches("1"))
                            {
                                String error = response.getString("error");
                                Toast.makeText(CreateGroupActivity.this, error+" "+statuscode , Toast.LENGTH_SHORT).show();
                                isCreated = false;
                            }
                            else {
                                isCreated = true;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CreateGroupActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("error",e.toString());
            }
            return isCreated;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                //refresh chat adapter list view
                SharedValues.save("Last_Read"+group.getGroupName(), 0);
                ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                db.insertGroup(group);
                Intent intent = new Intent(CreateGroupActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(CreateGroupActivity.this, "Unable to create group "+ group.getGroupName() , Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class SendKeyMessageTask extends AsyncTask<String, Void, Boolean> {

        private final Message message;
        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/send";

        public SendKeyMessageTask(Message message) {
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> userMap = new HashMap<>();
            userMap.put("sender",SharedValues.getValue("USER_EMAIL"));
            userMap.put("message",message.getMessage());
            userMap.put("receiver",message.getTo());

            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message_received  = response.getString("message");
                            //customChatAdapter.add(new Message(friendName,message_received,"left"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CreateGroupActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("error",e.toString());
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {

                //refresh chat adapter list view
            } else {

            }
        }

    }

}
