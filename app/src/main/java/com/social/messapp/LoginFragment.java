package com.social.messapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.messapp.constants.Constants;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;


/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();

    private EditText usernameText;
    private EditText passwordText;
    private LoginButton fbLoginButton;
    private Button signInButton;
    private Button registerButton;
    private AccessToken mAccessToken;
    private AccessTokenTracker mAccessTokenTracker;
    private TwitterLoginButton twitterLoginButton;
    private ParseUser mCurrentUser;


    private CallbackManager mCallbackManager;
    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            if(Profile.getCurrentProfile() == null){
                GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                final String id = object.optString("id");
                                final String email = object.optString("email");
                                final String name = object.optString("name");
                                final String profile_picture = Constants.FACEBOOK_BASE_URL + "/"+id + "/picture?type=large";
                                final String username = (email.isEmpty()) ? name+"_"+id : email;
                                final String password = getString(R.string.default_password);
                                String location="";
                                try {
                                    location = object.getJSONObject("location").getString("name");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
                                queryUser.whereEqualTo(Constants.USERNAME, username);
                                final String finalLocation = location;
                                queryUser.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> users, ParseException e) {
                                        if(e == null) {
                                            if (users.size() == 0) {
                                                Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                mCurrentUser = new ParseUser();
                                                mCurrentUser.setUsername(username);
                                                mCurrentUser.setPassword(password);
                                                mCurrentUser.put(Constants.PROFILE_PICTURE, profile_picture);
                                                mCurrentUser.put(Constants.LOCATION, finalLocation);
                                                if(email.isEmpty()) mCurrentUser.setEmail(name+"@messapp.com");
                                                else mCurrentUser.setEmail(email);

                                                mCurrentUser.signUpInBackground(new SignUpCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e != null) {
                                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            Log.w(TAG, "Error : " + e.getMessage() + ":" + e.getCode());
                                                            if (e.getCode() == 202) {
                                                                Toast.makeText(getActivity(), getString(R.string.username_taken), Toast.LENGTH_LONG).show();
                                                            }

                                                        } else {
                                                            //Toast.makeText(getActivity(), getString(R.string.user_saved), Toast.LENGTH_SHORT).show();
                                                            ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                                @Override
                                                                public void done(ParseUser user, ParseException e) {
                                                                    if(e == null){
                                                                        goToHomeActivity();
                                                                    }else {
                                                                        Log.d(TAG, e.getMessage());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }else {
                                                Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                    @Override
                                                    public void done(ParseUser user, ParseException e) {
                                                        goToHomeActivity();
                                                    }
                                                });
                                            }
                                        }else {
                                            //An error has occured
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                Log.d(TAG, response.getRawResponse());

                            }
                        }).executeAsync();
            }
        }
        @Override
        public void onCancel() {
            Toast.makeText(getActivity(), getString(R.string.auth_canceled), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(getActivity(), getString(R.string.facebook_error), Toast.LENGTH_LONG).show();
        }
    };

    public LoginFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        AppEventsLogger.activateApp(getContext());

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            }
        };

        mAccessToken = AccessToken.getCurrentAccessToken();
        mAccessTokenTracker.startTracking();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        usernameText = (EditText) view.findViewById(R.id.username);
        passwordText = (EditText) view.findViewById(R.id.password);
        fbLoginButton = (LoginButton) view.findViewById(R.id.login_button);
        twitterLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button);
        signInButton = (Button) view.findViewById(R.id.loginButton);
        registerButton = (Button) view.findViewById(R.id.registerButton);

        fbLoginButton.setReadPermissions(Arrays.asList("email,public_profile,user_location"));
        fbLoginButton.setFragment(this);

        fbLoginButton.registerCallback(mCallbackManager, mCallBack);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                Twitter twitter = Twitter.getInstance();
                TwitterApiClient api = twitter.core.getApiClient(session);
                AccountService  service = api.getAccountService();
                Call<User> user = service.verifyCredentials(true, true);

                user.enqueue(new Callback<User>()
                {
                    @Override
                    public void success(Result<User> userResult)
                    {
                        String name = userResult.data.name;
                        String email = userResult.data.email;
                        final String username = userResult.data.screenName;
                        final String profile_picture = userResult.data.profileImageUrl;
                        final String location = userResult.data.location;
                        Log.d(TAG, userResult.data.screenName);
                        Log.d(TAG, name +" " + email);
                        final String parseEmail = (email == null) ? userResult.data.screenName+"@messapp.com" : email;
                        final String password = getString(R.string.default_password);
                        ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
                        queryUser.whereEqualTo(Constants.USERNAME, username);
                        queryUser.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> users, ParseException e) {
                                if(e == null) {
                                    if (users.size() == 0) {
                                        mCurrentUser = new ParseUser();
                                        mCurrentUser.setUsername(username);
                                        mCurrentUser.setEmail(parseEmail);
                                        mCurrentUser.setPassword(password);
                                        mCurrentUser.put(Constants.PROFILE_PICTURE, profile_picture);
                                        mCurrentUser.put(Constants.LOCATION, location);
                                        mCurrentUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e == null){
                                                    Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                        @Override
                                                        public void done(ParseUser user, ParseException e) {
                                                            if( e == null) goToHomeActivity();
                                                            else {
                                                                Log.d(TAG, e.getMessage());
                                                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }else {
                                                    Log.d(TAG, e.getMessage());
                                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }else {
                                        // Go To home Screen
                                        if(users.size() == 1){
                                            Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                            ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                @Override
                                                public void done(ParseUser user, ParseException e) {
                                                    if( e == null) {
                                                        goToHomeActivity();
                                                    }
                                                    else {
                                                        Log.d(TAG, e.getMessage());
                                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }else {
                                            Toast.makeText(getActivity(), "Sorry, try again later", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }else {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.d(TAG, e.getMessage());
                                }
                            }
                        });

                    }

                    @Override
                    public void failure(TwitterException e)
                    {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(getActivity(), getString(R.string.twitter_email_failed), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getActivity(), getString(R.string.twitter_failure), Toast.LENGTH_LONG).show();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameText.getText().toString();
                final String password = passwordText.getText().toString();
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            getActivity().startActivity(intent);
                            usernameText.setText("");
                            passwordText.setText("");
                        } else {
                            Log.d(TAG, e.getMessage());
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStop() {
        super.onStop();
        mAccessTokenTracker.stopTracking();
    }

    private void goToHomeActivity(){
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
