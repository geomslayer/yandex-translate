package com.geomslayer.ytranslate;

import android.app.Application;

import com.geomslayer.ytranslate.network.TranslateApi;

import io.realm.Realm;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseApp extends Application {

    private Retrofit retrofit;
    private static TranslateApi translateApi;
    private static Realm realm;

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

        retrofit = new Retrofit.Builder()
                .baseUrl(TranslateApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        translateApi = retrofit.create(TranslateApi.class);
    }

}