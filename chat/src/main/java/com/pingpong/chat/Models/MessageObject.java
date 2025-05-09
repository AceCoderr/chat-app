package com.pingpong.chat.Models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageObject{
    @JsonProperty("type")
    private  String type;
    @JsonProperty("username")
    private  String username;
    @JsonProperty("text")
    private  String text;
    @JsonProperty("timestamp")
    private  String timestamp;

    // Add this default constructor
    public MessageObject() {
    }

    public MessageObject(String type, String username, String text, String timestamp) {
        this.type = type;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



}
