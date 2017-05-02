package me.scryptminers.android.incognito.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import me.scryptminers.android.incognito.Util.HashFunctions;
import me.scryptminers.android.incognito.Util.SharedValues;
import me.scryptminers.android.incognito.Util.Validations;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private String email;
    private String password;
    private RequestQueue requestQueue;
    private JsonObjectRequest jsonObjectRequest;
    boolean isPostLoginOne = false;
    boolean isPostLoginTwo = false;
    boolean isLoggedIn = false;
    private String TAG="";
    private final String URL_LOGIN_PART1 = "https://scryptminers.me/login/1";
    private final String URL_LOGIN_PART2 = "https://scryptminers.me/login/2";

    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View focusView = null;
    private boolean cancel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Create object of Shared Values
        SharedValues.init(getApplicationContext());
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        resetErrors();
        cancel = validateEmailAndPassword(cancel);

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute("values");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    // Function : Post Login Step 1
    public boolean postLoginStepOne(final Map<String,String> userLoginMap){
        try {
            // Simulate network access.
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_LOGIN_PART1, new JSONObject(userLoginMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.getString("c") != null && response.getString("salt") != null){
                            String responseChallenge = response.getString("c"); // get Challenge from Server
                            String responseSalt = response.getString("salt"); // get Salt from Server
                            String passwordHash = HashFunctions.getPasswordHash(password, responseSalt); // Calculate password hash
                            TAG = HashFunctions.HMAC(responseChallenge,passwordHash); // Generate Tag from challenge and password hash
                            isPostLoginOne = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
            while (!jsonObjectRequest.hasHadResponseDelivered())
                Thread.sleep(2000);
        } catch (InterruptedException e) {
            return false;
        }
        return isPostLoginOne;
    }
    // Function: Post Login Step 2
    public boolean postLoginStepTwo(final Map<String,String> userTagMap){
        try {
            // Simulate network access.
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_LOGIN_PART2, new JSONObject(userTagMap), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try { // Send TAG calculated in Step 1 to Server
                        if(response.getString("code").matches("0")){
                            String responseToken = response.getString("token");// If TAG is verified, token is returned by server
                            SharedValues.save("JWT_TOKEN",responseToken);
                            long userID = response.getLong("user_id");
                            SharedValues.save("USER_ID",userID);
                            if(responseToken != null){
                                Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                isPostLoginTwo = true;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    Log.e("LoginError","Something went wrong!");
                }
            });
            requestQueue.add(jsonObjectRequest);
            while (!jsonObjectRequest.hasHadResponseDelivered())
                Thread.sleep(2000);
        } catch (InterruptedException e) {
            return false;
        }
        return isPostLoginTwo;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, Boolean> {

        private final String userEmail;
        private final String userPassword;

        UserLoginTask() {
            userEmail = email;
            userPassword = password;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            Map<String,String> userLoginMap = new HashMap<>();
            userLoginMap.put("email",email);

            if(postLoginStepOne(userLoginMap)){

                Map<String,String> userTagMap = new HashMap<>();
                userTagMap.put("email",email);
                userTagMap.put("tag",TAG);

                if(postLoginStepTwo(userTagMap)){
                    isLoggedIn = true;
                } else{
                    Toast.makeText(LoginActivity.this, "Something went wrong login 2!", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(LoginActivity.this, "Step One Completed", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

            return isLoggedIn;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                i.putExtra("email",userEmail);
                SharedValues.save("USER_EMAIL",userEmail);
                startActivity(i);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    private void registerUser() {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    private boolean validateEmailAndPassword(boolean cancel){

        Validations validator = new Validations();
        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!validator.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        return cancel;
    }

    private void resetErrors(){
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
    }
}

