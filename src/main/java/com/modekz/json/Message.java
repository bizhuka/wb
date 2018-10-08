package com.modekz.json;

public class Message {
    public String error;

    public static Message createError(String error) {
        Message message = new Message();
        message.error = error;
        return message;
    }
}
