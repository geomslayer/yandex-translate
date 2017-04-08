package com.geomslayer.ytranslate;

import android.app.Application;

import com.geomslayer.ytranslate.models.DaoMaster;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.models.TranslationDao;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.utils.UploadService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;

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

        OneoffTask languagesUploadTask = new OneoffTask.Builder()
                .setService(UploadService.class)
                .setTag("UploadLanguagesTask")
                .setExecutionWindow(0, 60)
                .setRequiredNetwork(Task.NETWORK_STATE_UNMETERED)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager manager = GcmNetworkManager.getInstance(this);
        manager.schedule(languagesUploadTask);
    }

}
