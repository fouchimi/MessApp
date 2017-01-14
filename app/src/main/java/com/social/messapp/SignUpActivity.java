package com.social.messapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.messapp.constants.Constants;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private EditText usernameText;
    private EditText passwordText;
    private EditText passwordText2;
    private EditText emailText;
    //private Spinner  countrySpinner;
    private EditText cityText;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        passwordText2 = (EditText) findViewById(R.id.password2);
        emailText = (EditText) findViewById(R.id.email);
        //countrySpinner = (Spinner) findViewById(R.id.country);
        cityText = (EditText) findViewById(R.id.city);
    }

    private void resetForm(){
        usernameText.setText("");
        passwordText.setText("");
        passwordText2.setText("");
        emailText.setText("");
        cityText.setText("");
    }

    public void register(View view){
        currentUser = new ParseUser();
        String username = usernameText.getText().toString();
        final String password = passwordText.getText().toString();
        final String password2 = passwordText2.getText().toString();
        String email = emailText.getText().toString();
        //String country = countrySpinner.getSelectedItem().toString();
        final String city = cityText.getText().toString();
        if(username.isEmpty() || password.isEmpty() ||
                password2.isEmpty() || email.isEmpty()
                || city.isEmpty()){
            Toast.makeText(this, getString(R.string.required), Toast.LENGTH_LONG).show();
        }
        else if(!password.equals(password2)){
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_LONG).show();
        }else {
            currentUser.setUsername(username);
            currentUser.setPassword(password);
            currentUser.setEmail(email);
            //currentUser.put(Constants.COUNTRY, country);
            currentUser.put(Constants.LOCATION, city);

            currentUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {

                        Toast.makeText(SignUpActivity.this, getString(R.string.user_saving_failed), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error : " + e.getMessage() + ":::" + e.getCode());

                        if (e.getCode() == 202) {
                            Toast.makeText(SignUpActivity.this, getString(R.string.username_taken), Toast.LENGTH_LONG).show();
                            resetForm();
                        }

                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.user_saved),
                                Toast.LENGTH_SHORT).show();
                        resetForm();
                        ParseUser.logInInBackground(currentUser.getUsername(), password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        });

                    }
                }
            });
        }
    }
}
