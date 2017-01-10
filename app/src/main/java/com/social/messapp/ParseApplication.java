package com.social.messapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.interceptors.ParseLogInterceptor;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ousmane on 1/7/17.
 */

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getApplicationContext().getString(R.string.app_id))
                .clientKey("")
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server(getApplicationContext().getString(R.string.server))
                .build()
        );

    }
}
