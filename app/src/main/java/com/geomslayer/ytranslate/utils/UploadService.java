package com.geomslayer.ytranslate.utils;

import android.util.Log;

import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class UploadService extends GcmTaskService {

    @Override
    public int onRunTask(TaskParams taskParams) {
        DaoSession session = ((BaseApp) getApplication()).getDaoSession();
        LanguageDao languageDao = session.getLanguageDao();
        loadLanguages(languageDao);
        return 0;
    }

    private void loadLanguages(LanguageDao languageDao) {
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
