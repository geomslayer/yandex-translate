package com.geomslayer.ytranslate.storage;

import java.util.Date;

import io.realm.RealmObject;

public class Translation extends RealmObject {
    private String rawText;
    private String translation;
    private Date moment;
    private boolean inHistory;
    private boolean inFavourites;

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public Date getMoment() {
        return moment;
    }

    public void setMoment(Date moment) {
        this.moment = moment;
    }

    public boolean isInHistory() {
        return inHistory;
    }

    public void setInHistory(boolean inHistory) {
        this.inHistory = inHistory;
    }

    public boolean isInFavourites() {
        return inFavourites;
    }

    public void setInFavourites(boolean inFavourites) {
        this.inFavourites = inFavourites;
    }

}
