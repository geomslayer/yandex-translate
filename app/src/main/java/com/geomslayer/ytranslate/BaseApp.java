package com.geomslayer.ytranslate;

import android.app.Application;
import android.util.Log;

import com.geomslayer.ytranslate.models.DaoMaster;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.models.TranslationDao;
import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.TranslateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApp extends Application {

    private static TranslateApi translateApi;

    public static TranslateApi getApi() {
        return translateApi;
    }

    private DaoSession daoSession;

    public DaoSession getDaoSession() {
        return daoSession;
    }

    private static boolean uploaded = false;

    public static boolean isLanguagesReceived() {
        return uploaded;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TranslateApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        translateApi = retrofit.create(TranslateApi.class);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "translations-db");
        daoSession = new DaoMaster(helper.getWritableDb()).newSession();

        LanguageDao languageDao = daoSession.getLanguageDao();
        TranslationDao translationDao = daoSession.getTranslationDao();

        List<Translation> toDelete = translationDao.queryBuilder()
                .where(TranslationDao.Properties.InHistory.eq(false))
                .where(TranslationDao.Properties.InFavorites.eq(false))
                .list();
        translationDao.deleteInTx(toDelete);

        if (languageDao.count() == 0) {
            languageDao.insert(new Language("ru", "Russian"));
            languageDao.insert(new Language("en", "English"));
        }

        loadLanguages(languageDao);
    }

    public static void loadLanguages(LanguageDao languageDao) {
        if (languageDao.count() > 2) {
            uploaded = true;
            return;
        }
        BaseApp.getApi()
                .getLanguages(TranslateApi.DEFAULT_UI)
                .enqueue(new Callback<LangCollection>() {
                    @Override
                    public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                        if (uploaded) {
                            return;
                        }
                        ArrayList<Language> langs = new ArrayList<>();
                        for (Map.Entry<String, String> langPair : response.body().getLangs().entrySet()) {
                            String code = langPair.getKey();
                            String name = langPair.getValue();
                            langs.add(new Language(code, name));
                        }
                        languageDao.insertInTx(langs);
                        uploaded = true;
                    }

                    @Override
                    public void onFailure(Call<LangCollection> call, Throwable t) {}
                });
    }

}
