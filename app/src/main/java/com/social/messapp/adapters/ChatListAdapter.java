package com.social.messapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseUser;
import com.social.messapp.R;
import com.social.messapp.classes.ChatMessage;
import com.social.messapp.utils.Utility;

import java.util.List;

/**
 * Created by ousmane on 1/12/17.
 */

public class ChatListAdapter extends BaseAdapter {
    private Context mContext;
   private List<ChatMessage> chats;
   private String mCurrentUser;

    public ChatListAdapter(Context context, List<ChatMessage> chats, String currentUser) {
        this.mContext = context;
        this.chats = chats;
        mCurrentUser = currentUser;
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Object getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage message = chats.get(position);

        LayoutInflater mInflater  = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (chats.get(position).getSender().equals(mCurrentUser)) {
            convertView = mInflater.inflate(R.layout.list_row_layout_right, null);
        } else {
            convertView = mInflater.inflate(R.layout.list_row_layout_left, null);
        }

        TextView messageView = (TextView)  convertView.findViewById(R.id.message_text_view);
        TextView dateView = (TextView) convertView.findViewById(R.id.date);
        messageView.setText(message.getMessage());

        String friendlyDate = Utility.getFriendlyDayAndTimeString(mContext, message.getDate().getTime());
        dateView.setText(friendlyDate);
        return convertView;
    }
}
