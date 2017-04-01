package com.geomslayer.ytranslate.storage;

import java.util.Date;

import io.realm.RealmObject;

public class Translation extends RealmObject {

    public interface Field {
        String rawText = "rawText";
        String translation = "translation";
        String moment = "moment";
        String langFrom = "langFrom";
        String langTo = "langTo";
        String inHistory = "inHistory";
        String inFavorites = "inFavorites";
    }

    private String rawText;
    private String translation;
    private Date moment;
    private String langFrom;
    private String langTo;
    private boolean inHistory;
    private boolean inFavorites;

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

    public boolean isInFavorites() {
        return inFavorites;
    }

    public void setInFavorites(boolean inFavorites) {
        this.inFavorites = inFavorites;
    }

    public String getLangFrom() {
        return langFrom;
    }

    public void setLangFrom(String langFrom) {
        this.langFrom = langFrom;
    }

    public String getLangTo() {
        return langTo;
    }

    public void setLangTo(String langTo) {
        this.langTo = langTo;
    }
}
