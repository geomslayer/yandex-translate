package com.geomslayer.ytranslate.utils;

import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.TranslateApi;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class Utils {

    public static void loadLanguages(LanguageDao languageDao) {
        BaseApp.getApi()
                .getLanguages(TranslateApi.DEFAULT_UI)
                .enqueue(new Callback<LangCollection>() {
                    @Override
                    public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                        for (Map.Entry<String, String> langPair : response.body().getLangs().entrySet()) {
                            String code = langPair.getKey();
                            String name = langPair.getValue();
                            Language current = new Language(code, name);
                            languageDao.insertOrReplace(current);
                        }
                    }

                    @Override
                    public void onFailure(Call<LangCollection> call, Throwable t) {}
                });
    }

}
