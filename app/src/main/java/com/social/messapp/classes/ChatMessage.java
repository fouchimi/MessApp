package com.social.messapp.classes;

import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by ousmane on 1/12/17.
 */

public class ChatMessage {
    private String sender;
    private String receiver;
    private String message;
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
