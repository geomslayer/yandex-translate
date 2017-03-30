package com.geomslayer.ytranslate.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateApi {

    String KEY = "trnsl.1.1.20170330T105849Z.f968c146029d80f9.80389f91271a5e2f8ba4a8e7351cc595ea4df91f";

    String BASE_URL = "https://translate.yandex.net";

    @GET("api/v1.5/tr.json/translate")
    Call<Response> getTranslation(@Query("key") String key,
                                  @Query("text") String text,
                                  @Query("lang") String lang);

    @GET("/api/v1.5/tr.json/getLangs")
    Call<LangCollection> getLangs(@Query("key") String key);

}
