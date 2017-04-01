package com.geomslayer.ytranslate.network;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class LangCollection {

    @SerializedName("langs")
    private Map<String, String> langs;

    public Map<String, String> getLangs() {
        return langs;
    }

}
