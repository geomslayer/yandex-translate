package com.geomslayer.ytranslate.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;

public class LangCollection {

    @SerializedName("dirs")
    private ArrayList<String> dirs;

    @SerializedName("langs")
    private Map<String, String> langs;

    public ArrayList<String> getDirs() {
        return dirs;
    }

    public Map<String, String> getLangs() {
        return langs;
    }

}
