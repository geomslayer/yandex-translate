package com.geomslayer.ytranslate.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateApi {

    String KEY = "trnsl.1.1.20170330T105849Z.f968c146029d80f9.80389f91271a5e2f8ba4a8e7351cc595ea4df91f";

    String BASE_URL = "https://translate.yandex.net";

    String DEFAULT_UI = "en";

    @GET("api/v1.5/tr.json/translate?key=" + KEY)
    Call<Response> getTranslation(@Query("text") String text,
                                  @Query("lang") String lang);

    @GET("api/v1.5/tr.json/detect?key=" + KEY)
    Call<Response> detectLanguage(@Query("text") String text,
                                  @Query("hint") String hint);

    @GET("api/v1.5/tr.json/getLangs?key=" + KEY)
    Call<LangCollection> getLanguages(@Query("ui") String ui);

}
