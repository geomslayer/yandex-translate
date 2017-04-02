package com.geomslayer.ytranslate;

import android.app.Application;

import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.storage.Translation;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApp extends Application {

    private static TranslateApi translateApi;
    private static Realm realm;     // TODO change

    public static TranslateApi getApi() {
        return translateApi;
    }

    public static Realm getRealm() {
        return realm;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Translation> rubbish = realm.where(Translation.class)
                        .equalTo(Translation.Field.inFavorites, false)
                        .equalTo(Translation.Field.inHistory, false)
                        .findAll();
                rubbish.deleteAllFromRealm();
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TranslateApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        translateApi = retrofit.create(TranslateApi.class);
    }

}
