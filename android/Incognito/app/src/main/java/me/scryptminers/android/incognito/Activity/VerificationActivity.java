package me.scryptminers.android.incognito.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import me.scryptminers.android.incognito.R;
import me.scryptminers.android.incognito.Util.KeyGenerator;
import me.scryptminers.android.incognito.Util.SharedValues;

public class VerificationActivity extends AppCompatActivity {

    private EditText code;
    private Button btn;
    private String vcode;
    public String email;
    private Verification verifyTask;
    private View mProgressView;
    private View verificationView;
    private final String URL = "https://scryptminers.me/verify";
    private String token;
    private boolean isVerified=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        code = (EditText) findViewById(R.id.editCode);

        email =getIntent().getStringExtra("email");
        mProgressView=findViewById(R.id.verification_progress);
        verificationView=findViewById(R.id.verification_view);
        btn = (Button) findViewById(R.id.btnSubmit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vcode = code.getText().toString().trim();
                if (vcode.length() != 6) {
                    Toast.makeText(VerificationActivity.this, ""+vcode.length(), Toast.LENGTH_LONG).show();
                    code.setError("Invalid Verification Code.");
                } else {
                    showProgress(true);
                    verifyTask = new Verification();
                    verifyTask.execute();
                }
            }
        });


    }

    public class Verification extends AsyncTask<String, Void, Boolean> {

        RequestQueue requestQueue;
        JsonObjectRequest jsonObjectRequest;
        @Override
        protected Boolean doInBackground(String... params) {
            Map<String,String> values = new HashMap<>();
            values.put("email",email);
            values.put("verification_code",vcode);
            try {
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                    jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, URL, new JSONObject(values), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String statuscode=response.getString("code");
                                if(statuscode.matches("4"))
                                {
                                    token=response.getString("token");
                                    isVerified = true;
                                }
                                else {
                                    String error = response.getString("error");
                                    Toast.makeText(VerificationActivity.this, error+" "+code , Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(VerificationActivity.this, "Error in Service Request. \n Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(jsonObjectRequest);

                while (!jsonObjectRequest.hasHadResponseDelivered())
                        Thread.sleep(2000);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
            return isVerified;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            verifyTask=null;
            showProgress(false);
            if(isVerified)
            {
                KeyGenerator.generateKeys();
                Intent startMainActivity = new Intent(VerificationActivity.this,MainActivity.class);
                startActivity(startMainActivity);
            }
            else
            {
                code.setError("Please Enter a valid Verification Code.");
                code.requestFocus();
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

            verificationView.setVisibility(show ? View.GONE : View.VISIBLE);
            verificationView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    verificationView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            verificationView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



}