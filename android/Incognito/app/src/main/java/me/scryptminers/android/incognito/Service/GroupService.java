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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.GroupListFragment;
import me.scryptminers.android.incognito.Model.Group;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.SharedValues;
import org.spongycastle.util.encoders.Base64;
/**
 * Created by Samruddhi on 5/1/2017.
 */

public class GroupService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private String userEmail;
    private Group group;
    private Handler handler = new Handler();
    private static int lastread=0;
    private Runnable runnableCode = new Runnable() {

        @Override
        public void run() {

            GetGroupsTask getGroupsTask = new GetGroupsTask();
            getGroupsTask.execute("dsds");
            //ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
            //db.getAllMessages(userEmail);

        }
    };

    public class GetGroupsTask extends AsyncTask<String, Void, Boolean> {

        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/getGroups";
        private boolean successrun;
        boolean isAdmin=false;

        public GetGroupsTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            userEmail=SharedValues.getValue("USER_EMAIL");
            Map<String,String> userMap = new HashMap<>();
            userMap.put("user_email", SharedValues.getValue("USER_EMAIL"));
            userMap.put("last_read",""+SharedValues.getLong("Last_Group_ID"));
            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("groups");
                            if(jsonArray.length()==0)
                            {
                                successrun=false;
                            }
                            else {
                                //String message_received  = response.getString("message");
                                successrun=true;
                                ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                                    JSONObject objectInArray = jsonArray.getJSONObject(i);
                                    String groupName = objectInArray.getString("name");
                                    String admin = objectInArray.getString("admin");
                                    String[] members = objectInArray.getString("members").split(",");
                                    String[] keys = objectInArray.getString("keys").split(",");
                                    int position=0;
                                    if(admin.matches(userEmail))
                                    {
                                        isAdmin=true;
                                        break;
                                    }
                                   /* for(int count=0;count<members.length;count++)
                                    {
                                        if(members[count].matches(SharedValues.getValue("USER_EMAIL")))
                                        {
                                            position=count;
                                            break;
                                        }
                                    }
                                   // String key = keys[position];
                                    Log.e("Encrypted Group Key",key);
                                    try {
                                        byte[] receiverPrKey = Base64.decode(SharedValues.getValue("PRIVATE_KEY"));
                                        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(receiverPrKey);
                                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                        PrivateKey recPrivateKey = keyFactory.generatePrivate(keySpec);
                                        byte[] decodedGroupKey = Base64.decode(key);
                                        //byte[] gpkey = PGP.decryptKeys(decodedGroupKey,recPrivateKey);
                                       // String groupkey = PGP.decryptMessage(key,SharedValues.getValue("PRIVATE_KEY"));
                                        //String groupkey = Base64.toBase64String(gpkey);
                                        Cipher cipher = Cipher.getInstance("RSA");
                                        cipher.init(Cipher.DECRYPT_MODE,recPrivateKey);
                                        byte[] decryptedGroupKey = cipher.doFinal(decodedGroupKey);
                                        String groupkey = Base64.toBase64String(decryptedGroupKey);
                                        //String groupkey = android.util.Base64.encodeToString(decryptedGroupKey,android.util.Base64.NO_PADDING);
                                        Log.e("Received Group Key",groupkey);
                                        SharedValues.save(groupName+"_Group_Key",groupkey);

                                    }
                                    catch (NoSuchAlgorithmException e)
                                    {
                                        e.printStackTrace();
                                    } catch (InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    } catch (BadPaddingException e) {
                                        e.printStackTrace();
                                    } catch (IllegalBlockSizeException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchPaddingException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    }*/
                                    group = new Group(groupName,admin,members);
                                    db.insertGroup(group);
                                    GroupListFragment.groups.add(group);
                                    SharedValues.save("Last_Group_ID",objectInArray.getLong("id"));
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
            if(isAdmin)
                return isAdmin;
            return successrun;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                sendBroadcast(new Intent("Groups"));
                /*if(!isAdmin) {
                    Intent msgIntent = new Intent(getApplicationContext(), MessageService.class);
                    msgIntent.putExtra("groupname", group.getGroupName());
                    msgIntent.putExtra("admin", group.getGroupAdmin());
                    msgIntent.putExtra("to", group.getGroupName());
                    startService(msgIntent);
                }*/
            } else {

            }
            handler.postDelayed(runnableCode,3000);
        }

        @Override
        protected void onCancelled() {
        }
    }

    public GroupService() {
        super("MyGroupService");
    }

    /*@Override
    protected void onHandleIntent(@Nullable Intent intent) {
        friendEmail = intent.getStringExtra("FRIEND_EMAIL");
        userEmail = SharedValues.getValue("USER_EMAIL");
        handler.post(runnableCode);
    }*/





    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
       // userEmail = SharedValues.getValue("USER_EMAIL");
        handler.post(runnableCode);
    }
}
