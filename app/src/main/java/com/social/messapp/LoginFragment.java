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
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.social.messapp.constants.Constants;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();

    private static final String TWITTER_KEY = "xk2lnnj6egivTcKE94jhHwA9B";
    private static final String TWITTER_SECRET = "pkQRQe4AIiE50TyGG53JAGGtNVhVsCxLBpTDppY2ZkfYrb27c";

    private EditText usernameText;
    private EditText passwordText;
    private LoginButton loginButton;
    private Button signInButton;
    private Button registerButton;
    private AccessToken mAccessToken;
    private AccessTokenTracker mAccessTokenTracker;
    private ProfileTracker mProfileTracker;
    private Profile mProfile;
    private ParseUser mCurrentUser;


    private CallbackManager mCallbackManager;
    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            if(Profile.getCurrentProfile() == null){
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        mProfileTracker.stopTracking();
                        Log.v(Constants.USERNAME, currentProfile.getName());
                        mProfile = Profile.getCurrentProfile();
                        final String username = mProfile.getName();
                        final String email = getString(R.string.default_email);
                        final String password = getString(R.string.default_password);
                        ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
                        queryUser.whereEqualTo(Constants.USERNAME, username);
                        queryUser.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> users, ParseException e) {
                                if(e == null){
                                    if(users.size() == 0){
                                        Toast.makeText(getActivity(), getString(R.string.login_text), Toast.LENGTH_LONG).show();
                                        mCurrentUser = new ParseUser();
                                        mCurrentUser.setUsername(username);
                                        mCurrentUser.setPassword(password);
                                        mCurrentUser.setEmail(email);
                                        mCurrentUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {
                                                    Toast.makeText(getActivity(), getString(R.string.user_saving_failed), Toast.LENGTH_SHORT).show();
                                                    Log.w(TAG, "Error : " + e.getMessage() + ":::" + e.getCode());
                                                    if (e.getCode() == 202) {
                                                        Toast.makeText(getActivity(), getString(R.string.username_taken), Toast.LENGTH_LONG).show();
                                                    }

                                                } else {
                                                    Toast.makeText(getActivity(), getString(R.string.user_saved), Toast.LENGTH_SHORT).show();
                                                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                                                        @Override
                                                        public void done(ParseUser user, ParseException e) {
                                                            goToHomeActivity();
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
                    }
                };
                mProfileTracker.startTracking();
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
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(getActivity(), new Twitter(authConfig));
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
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameText = (EditText) view.findViewById(R.id.username);
        passwordText = (EditText) view.findViewById(R.id.password);
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        signInButton = (Button) view.findViewById(R.id.loginButton);
        registerButton = (Button) view.findViewById(R.id.registerButton);

        loginButton.setReadPermissions("public_profile");
        loginButton.setFragment(this);

        loginButton.registerCallback(mCallbackManager, mCallBack);

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
