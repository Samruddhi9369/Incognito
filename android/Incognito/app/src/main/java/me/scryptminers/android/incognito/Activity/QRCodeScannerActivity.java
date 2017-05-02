package me.scryptminers.android.incognito.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.scryptminers.android.incognito.Database.ChatDatabaseHelper;
import me.scryptminers.android.incognito.Model.User;
import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Util.KeyGenerator;
import me.scryptminers.android.incognito.Util.SharedValues;

public class QRCodeScannerActivity extends AppCompatActivity {

    private Button btnScan;
    private User friend;
    final Activity activity = this;
    private long recUserId;
    private QRCodeScanner scannerTask;
    private View mProgressView;
    private View scannerView;
    private String recPublicKey;
    private boolean isScanned=false;
    private final String URL = "https://scryptminers.me/getuser";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        mProgressView=findViewById(R.id.scanner_progress);
        scannerView=findViewById(R.id.scanner_view);
        btnScan = (Button) findViewById(R.id.buttonScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQRCode();
            }
        });

    }

    public void scanQRCode(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setPrompt("Scan");
        intentIntegrator.setCameraId(0);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setBarcodeImageEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "You have cancelled the scanning", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                String content = result.getContents();
                String[] splitCode = content.split("%%%");
                recPublicKey = splitCode[0];
                recUserId = Long.parseLong(splitCode[1]);
                showProgress(true);
                scannerTask = new QRCodeScanner();
                scannerTask.execute("abc");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public class QRCodeScanner extends AsyncTask<String, Void, Boolean> {

        RequestQueue requestQueue;
        JsonObjectRequest jsonObjectRequest;
        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> values = new HashMap<>();
            values.put("id",recUserId+"");
            values.put("sender_id",""+SharedValues.getLong("USER_ID"));
            try {
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(values), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String statuscode=response.getString("code");
                            if(statuscode.matches("11"))
                            {
                                JSONObject user = response.getJSONObject("user");
                                friend = new User(user.getLong("id"),user.getString("firstname"),user.getString("lastname"),user.getString("email"), user.getString("phone"),recPublicKey);
                                Log.e("name",user.getString("firstname"));
                                Log.e("INSERTED","inserted froedm");
                                ChatDatabaseHelper db = new ChatDatabaseHelper(getApplicationContext());
                                db.insertFriend(friend);
                                isScanned = true;
                            }
                            else {
                                String error = response.getString("error");
                                Toast.makeText(QRCodeScannerActivity.this, error+" "+statuscode , Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(QRCodeScannerActivity.this, "Error in Service Request. \n Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("Authorization","Bearer "+SharedValues.getValue("JWT_TOKEN"));
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);

                while (!jsonObjectRequest.hasHadResponseDelivered())
                    Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return isScanned;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            scannerTask=null;
            showProgress(false);
            if(isScanned)
            {
                Intent startMainActivity = new Intent(QRCodeScannerActivity.this,MainActivity.class);
                //startMainActivity.putExtra("friend",friend);
                startActivity(startMainActivity);
            }
            else
            {
                Toast.makeText(QRCodeScannerActivity.this, "Scanning failed. Please try again later.", Toast.LENGTH_SHORT).show();
            }


        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            scannerView.setVisibility(show ? View.GONE : View.VISIBLE);
            scannerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scannerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            scannerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
