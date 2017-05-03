package me.scryptminers.android.incognito.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Activity.GroupChatActivity;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.GroupMessage;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.SharedValues;

/**
 * Created by Samruddhi on 5/2/2017.
 */

public class GroupMessageService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private String groupName, userEmail;
    private Handler handler = new Handler();
    private static int lastread=0;
    private Runnable runnableCode = new Runnable() {

        @Override
        public void run() {

            ReceiveGroupMessageTask receiveGroupMessageTask = new ReceiveGroupMessageTask();
            receiveGroupMessageTask.execute("dsds");
            //ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
            //db.getAllMessages(userEmail);

        }
    };

    public class ReceiveGroupMessageTask extends AsyncTask<String, Void, Boolean> {

        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/getGroupMessage";
        private boolean successrun;

        public ReceiveGroupMessageTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Map<String,String> userMap = new HashMap<>();
            userMap.put("from", SharedValues.getValue("USER_EMAIL"));
            userMap.put("group_name", groupName);
            userMap.put("Last_Group_Read",""+SharedValues.getLong("Last_Group_Read"));
            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("messages");
                            if(jsonArray.length()==0)
                            {
                                successrun=false;
                            }
                            else {
                                //String message_received  = response.getString("message");
                                successrun=true;
                                ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                //String receiverPublicKey = db.getFriendPublicKey(friendEmail);
                                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                                    JSONObject objectInArray = jsonArray.getJSONObject(i);
                                    String ciphertext = objectInArray.getString("message");
                                    String message = PGP.decryptMessage(ciphertext, SharedValues.getValue("PRIVATE_KEY"));
                                    //db.addMessage(new Message(friendEmail, userEmail, message, "right"));
                                    String direction="";
                                    if(userEmail.matches(objectInArray.getString("from"))){
                                        direction = "left";
                                    }
                                    else {
                                        direction = "right";
                                    }
                                    db.addGroupMessage(new GroupMessage(message,userEmail,groupName,direction));
                                    GroupChatActivity.groupMessages.add(new GroupMessage(message,userEmail,groupName,direction));
                                    Log.d("Recieved", message);
                                    SharedValues.save("Last_Group_Read", objectInArray.getInt("id"));
                                    //msgs.add(new Message(friendName,message,"right"));
                                }
                                //listViewChat.invalidate();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);
                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d("error",e.toString());
            }

            return successrun;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                sendBroadcast(new Intent("UpdateGroup"));
            } else {

            }
            handler.postDelayed(runnableCode,2000);
        }

        @Override
        protected void onCancelled() {
        }
    }

    public GroupMessageService() {
        super("MyGroupMessageService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        groupName = intent.getStringExtra("GROUP_NAME");
        userEmail = SharedValues.getValue("USER_EMAIL");
        handler.post(runnableCode);
    }
}
