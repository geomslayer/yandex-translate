package com.geomslayer.ytranslate;

import android.app.Application;

import com.geomslayer.ytranslate.models.DaoMaster;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.models.TranslationDao;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.utils.Utils;

import java.util.List;

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

        List<Translation> toDelete = daoSession.getTranslationDao().queryBuilder()
                .where(TranslationDao.Properties.InHistory.eq(false))
                .where(TranslationDao.Properties.InFavorites.eq(false))
                .list();
        daoSession.getTranslationDao().deleteInTx(toDelete);

        Utils.loadLanguages(daoSession.getLanguageDao());
    }

}
