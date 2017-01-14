package com.social.messapp;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.social.messapp.adapters.ChatListAdapter;
import com.social.messapp.classes.ChatMessage;
import com.social.messapp.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = MessageActivity.class.getSimpleName();
    private ListView mMessageListView;
    private TextView emptyTextView;
    private ParseUser mCurrentUser, mReceiver;
    private String receiverId;
    private EditText mMessageEditText;
    private Button mSendButton;
    private List<ChatMessage> chats = new ArrayList<>();
    private ChatListAdapter mAdapter;

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
        mSendButton = (Button) findViewById(R.id.sendMessageButton);
        mMessageEditText = (EditText) findViewById(R.id.message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(receiverId != null || !receiverId.isEmpty()){
            fetchChatMessages(receiverId);
        }
    }

    public void fetchChatMessages(final String receiverId){
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
                        if(row.getObjectId().equals(mCurrentUser.getObjectId())){
                            chat.setSender(mCurrentUser.getObjectId());
                            chat.setReceiver(receiverId);
                        }else {
                            chat.setSender(mCurrentUser.getObjectId());
                            chat.setReceiver(receiverId);
                        }
                        chat.setMessage(row.getString(Constants.MESSAGE));
                        chat.setDate(row.getString(Constants.CREATED_AT));
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
    }


}
