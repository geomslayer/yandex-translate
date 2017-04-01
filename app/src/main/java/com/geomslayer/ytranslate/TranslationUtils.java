package com.geomslayer.ytranslate;

import android.content.Context;
import android.content.SharedPreferences;

import com.geomslayer.ytranslate.storage.Translation;

public class TranslationUtils {

    public static final String DEFAULT_LANG_FROM = "ru";
    public static final String DEFAULT_LANG_TO = "en";
    public static final String LAST_TRANSLATION = "last_translation";

    public static Translation restoreFromSharedPreferences(Context context) {
        SharedPreferences sp = context.getSharedPreferences(LAST_TRANSLATION, 0);
        Translation tr = new Translation();
        tr.setLangFrom(sp.getString(Translation.Field.langFrom, DEFAULT_LANG_FROM));
        tr.setLangTo(sp.getString(Translation.Field.langTo, DEFAULT_LANG_TO));
        tr.setRawText(sp.getString(Translation.Field.rawText, ""));
        tr.setTranslation(sp.getString(Translation.Field.translation, ""));
        return tr;
    }

    public static void saveInSharedPreferences(Context context, Translation tr) {
        SharedPreferences sp = context.getSharedPreferences(LAST_TRANSLATION, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Translation.Field.rawText, tr.getRawText());
        editor.putString(Translation.Field.translation, tr.getTranslation());
        editor.putString(Translation.Field.langFrom, tr.getLangFrom());
        editor.putString(Translation.Field.langTo, tr.getLangTo());
        editor.apply();
    }

}
