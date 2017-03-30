package com.geomslayer.ytranslate.network;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class Response {

    @SerializedName("code")
    private int code;

    @SerializedName("lang")
    private String lang;

    @SerializedName("text")
    private ArrayList<String> text;

    public int getCode() {
        return code;
    }

    public String getLang() {
        return lang;
    }

    public ArrayList<String> getText() {
        return text;
    }

}