package me.scryptminers.android.incognito.Service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import me.scryptminers.android.incognito.Activity.ChatActivity;
import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.Message;
import me.scryptminers.android.incognito.Util.PGP;
import me.scryptminers.android.incognito.Util.SharedValues;

/**
 * Created by Samruddhi on 4/30/2017.
 */

public class KeyMessageService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    private String groupname, userEmail;
    private Handler handler = new Handler();
    private static int lastread=0;
    private Runnable runnableCode = new Runnable() {

        @Override
        public void run() {

            ReceiveMessageTask receiveMessageTask = new ReceiveMessageTask();
            receiveMessageTask.execute("dsds");
            //ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
            //db.getAllMessages(userEmail);

        }
    };

    public class ReceiveMessageTask extends AsyncTask<String, Void, Boolean> {

        private RequestQueue requestQueue;
        private JsonObjectRequest jsonObjectRequest;
        private final String URL="https://scryptminers.me/getKeyMessages";
        private boolean successrun;

        public ReceiveMessageTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Map<String,String> userMap = new HashMap<>();
            userMap.put("groupname",groupname);
            userMap.put("to", SharedValues.getValue("USER_EMAIL"));
            userMap.put("lastread",""+SharedValues.getLong("Last_Read"));
            try {
                // Simulate network access.
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(userMap), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("keymessages");
                            if(jsonArray.length()==0)
                            {
                                successrun=false;
                            }
                            else {
                                //String message_received  = response.getString("message");
                                successrun=true;
                                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                                    JSONObject objectInArray = jsonArray.getJSONObject(i);
                                    String ciphertext = objectInArray.getString("message");
                                    ciphertext = ciphertext.replace("_" + groupname + "_Key", "");
                                    Log.d("In Key Message service", ciphertext);
                                    //Decrypt Recieved Group Key by User's Private Key
									
                                    try {
                                        byte[] receiverPrKey = Base64.decode(SharedValues.getValue("PRIVATE_KEY"));
                                        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(receiverPrKey);
                                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                        PrivateKey recPrivateKey = keyFactory.generatePrivate(keySpec);
                                        byte[] messagebytes = PGP.decryptKeys(Base64.decode(ciphertext), recPrivateKey);
                                        String message = Base64.toBase64String(messagebytes);
                                        Log.d("Recieved", message);
                                        ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                        db.updateGroupKey(groupname, message);
										
										// Save Received Group key in Shared Preferences
                                        
										SharedValues.save(groupname + "_KEY", message);
                                        
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeySpecException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchPaddingException e) {
                                        e.printStackTrace();
                                    } catch (BadPaddingException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchProviderException e) {
                                        e.printStackTrace();
                                    } catch (IllegalBlockSizeException e) {
                                        e.printStackTrace();
                                    }
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
              //  sendBroadcast(new Intent("Update"));
                stopSelf();
            } else {

            }
            //handler.postDelayed(runnableCode,3000);
        }

        @Override
        protected void onCancelled() {
        }
    }

    public KeyMessageService() {
        super("MyMessageService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        groupname = intent.getStringExtra("groupname");
        userEmail = SharedValues.getValue("USER_EMAIL");
        handler.post(runnableCode);
    }
}
