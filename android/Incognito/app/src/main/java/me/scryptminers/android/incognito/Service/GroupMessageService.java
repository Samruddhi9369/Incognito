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
import org.spongycastle.util.encoders.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
                                
                                successrun=true;
                                ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                
                                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                                    JSONObject objectInArray = jsonArray.getJSONObject(i);
                                    String ciphertext = objectInArray.getString("message");
                                
									// Get groupKey from Shared Preferences
                                    String groupKey=SharedValues.getValue(groupName+"_KEY");
                                
                                    byte[] gkey = Base64.decode(groupKey);
                                    SecretKey encryptionKey = new SecretKeySpec(gkey, "AES");
                                    byte[] rawAESkey = encryptionKey.getEncoded();
									
									// Decrypt received message with symmetric group key
                                    byte[] decryptedMessage = PGP.decryptGroupMessage(ciphertext, encryptionKey);
                                
                                    String message = new String(decryptedMessage);
                                
                                    String direction="";
                                
                                    db.addGroupMessage(new GroupMessage(message,userEmail,groupName,direction));
                                    GroupChatActivity.groupMessages.add(new GroupMessage(message,userEmail,groupName,"right"));
                                    Log.d("Recieved", message);
                                    // Update Last Read value
									SharedValues.save("Last_Group_Read", objectInArray.getInt("id"));
                                    
                                }
                                //listViewChat.invalidate();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
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
                sendBroadcast(new Intent("UpdateGroupMessage"));
            } else {

            }
            handler.postDelayed(runnableCode,3000);
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
