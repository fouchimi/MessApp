package com.social.messapp.classes;

import com.parse.ParseUser;

/**
 * Created by ousmane on 1/12/17.
 */

public class ChatMessage {
    private String sender;
    private String receiver;
    private String message;
    private String date;

    public ChatMessage(){
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
