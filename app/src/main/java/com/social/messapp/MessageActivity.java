package com.social.messapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.social.messapp.adapters.ChatListAdapter;
import com.social.messapp.classes.ChatMessage;
import com.social.messapp.constants.Constants;
import com.social.messapp.services.ParsePushBroadcastReceiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = MessageActivity.class.getSimpleName();
    private ListView mMessageListView;
    private ParseUser mCurrentUser;
    private String receiverId;
    private EditText mMessageEditText;
    private ImageButton mSendButton;
    private List<ChatMessage> chats = new ArrayList<>();
    private ChatListAdapter mAdapter;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "onReceive invoked!", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentUser = ParseUser.getCurrentUser();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            receiverId = bundle.getString(Constants.USER_ID);
        }

        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mSendButton = (ImageButton) findViewById(R.id.sendMessageButton);
        mMessageEditText = (EditText) findViewById(R.id.message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(receiverId != null || !receiverId.isEmpty()){
            fetchChatMessages(receiverId);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(ParsePushBroadcastReceiver.intentAction));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    public void fetchChatMessages(final String receiverId){
        chats.clear();
        Log.d(TAG, "CURRENT USER_ID: "+ mCurrentUser.getObjectId());
        Log.d(TAG, "FRIEND USER_ID: " + receiverId);

        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        final ParseQuery firstQuery = new ParseQuery(Constants.CHATS_TABLE);
        firstQuery.whereEqualTo(Constants.SENDER, mCurrentUser.getObjectId());
        firstQuery.whereEqualTo(Constants.RECEIVER, receiverId);

        final ParseQuery secondQuery = new ParseQuery(Constants.CHATS_TABLE);
        secondQuery.whereEqualTo(Constants.SENDER, receiverId);
        secondQuery.whereEqualTo(Constants.RECEIVER, mCurrentUser.getObjectId());

        queries.add(firstQuery);
        queries.add(secondQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByAscending(Constants.CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> rows, ParseException e) {
                if(e == null){
                    Log.d(TAG, "found: " + rows.size() + " records");
                    for(ParseObject row : rows){
                        ChatMessage chat = new ChatMessage();
                        if(row.getString(Constants.SENDER).equals(mCurrentUser.getObjectId())){
                            chat.setSender(mCurrentUser.getObjectId());
                            chat.setReceiver(receiverId);
                        }else {
                            chat.setSender(receiverId);
                            chat.setReceiver(mCurrentUser.getObjectId());
                        }
                        chat.setMessage(row.getString(Constants.MESSAGE));
                        chat.setDate(row.getCreatedAt());
                        if(row != null) Log.d(TAG, "DATE: "+ row.getCreatedAt());
                        chats.add(chat);
                    }
                    if(!chats.isEmpty()){
                        mAdapter = new ChatListAdapter(MessageActivity.this, chats, mCurrentUser.getObjectId());
                        mMessageListView.setAdapter(mAdapter);
                    }
                }else {
                    Log.d(TAG, e.getMessage());
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void saveMessage(View view){
        final String messageText = mMessageEditText.getText().toString();
        Log.d(TAG, "Send button pressed!!!");
        if(!messageText.isEmpty() && receiverId != null || receiverId.isEmpty() ){
            ParseObject newRecord = new ParseObject(Constants.CHATS_TABLE);
            newRecord.put(Constants.SENDER, mCurrentUser.getObjectId());
            newRecord.put(Constants.RECEIVER, receiverId);
            newRecord.put(Constants.MESSAGE, messageText);
            final ChatMessage newChat = new ChatMessage();
            newChat.setSender(mCurrentUser.getObjectId());
            newChat.setReceiver(receiverId);
            newChat.setMessage(messageText);
            newChat.setDate(new Date());
            chats.add(newChat);

            newRecord.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        mMessageEditText.setText("");
                        Log.d(TAG, "Record saved");
                        mAdapter = new ChatListAdapter(MessageActivity.this, chats, mCurrentUser.getObjectId());
                        mMessageListView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        HashMap<String, String> payload = new HashMap<>();
                        payload.put("sender", mCurrentUser.getObjectId());
                        payload.put("receiver", receiverId);
                        payload.put("text", messageText);
                        //This make run the push notification in the back-end
                        ParseQuery pushQuery = ParseInstallation.getQuery();
                        pushQuery.whereEqualTo("channels", "chatChannel"); // Set the channel
                        pushQuery.whereEqualTo("receiver", receiverId);

                        // Send push notification to query
                        ParsePush push = new ParsePush();
                        push.setQuery(pushQuery);
                        push.setMessage(messageText);
                        push.sendInBackground();
                    }else {
                        Log.d(TAG, e.getMessage());
                        Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


}
