package com.social.messapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.social.messapp.constants.Constants;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private TextView usernameTextView;
    private TextView passwordTextView;
    private TextView passwordTextView2;
    private TextView emailTextView;
    private Spinner  countrySpinner;
    private TextView cityTextView;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        usernameTextView = (TextView) findViewById(R.id.username);
        passwordTextView = (TextView) findViewById(R.id.password);
        passwordTextView2 = (TextView) findViewById(R.id.password2);
        emailTextView = (TextView) findViewById(R.id.email);
        countrySpinner = (Spinner) findViewById(R.id.country);
        cityTextView = (TextView) findViewById(R.id.city);
    }

    public void register(View view){
        currentUser = new ParseUser();
        String username = usernameTextView.getText().toString();
        final String password = passwordTextView.getText().toString();
        final String password2 = passwordTextView2.getText().toString();
        String email = emailTextView.getText().toString();
        String country = countrySpinner.getSelectedItem().toString();
        final String city = cityTextView.getText().toString();
        if(username.isEmpty() || password.isEmpty() ||
                password2.isEmpty() || email.isEmpty() ||
                country.isEmpty() || city.isEmpty()){
            Toast.makeText(this, getString(R.string.required), Toast.LENGTH_LONG).show();
        }
        else if(!password.equals(password2)){
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_LONG).show();
        }else if(!country.equals(getString(R.string.country_prompt))){
            Toast.makeText(this, getString(R.string.country_prompt), Toast.LENGTH_LONG).show();
        }else {
            currentUser.put(Constants.USERNAME, username);
            currentUser.put(Constants.PASSWORD, password);
            currentUser.put(Constants.EMAIL, email);
            currentUser.put(Constants.COUNTRY, country);
            currentUser.put(Constants.CITY, city);

            currentUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {

                        Toast.makeText(SignUpActivity.this,
                                getString(R.string.user_saving_failed), Toast.LENGTH_SHORT).show();
                        Log.w(TAG,
                                "Error : " + e.getMessage() + ":::" + e.getCode());

                        if (e.getCode() == 202) {

                            Toast.makeText(
                                    SignUpActivity.this,
                                    getString(R.string.username_taken),
                                    Toast.LENGTH_LONG).show();
                            usernameTextView.setText("");
                            passwordTextView.setText("");
                            passwordTextView2.setText("");
                            emailTextView.setText("");
                            cityTextView.setText("");
                        }

                    } else {
                        Toast.makeText(SignUpActivity.this, getString(R.string.user_saved),
                                Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
}
