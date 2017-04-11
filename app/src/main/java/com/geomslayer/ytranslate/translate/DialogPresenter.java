package com.geomslayer.ytranslate.translate;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.TranslateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

@InjectViewState
public class DialogPresenter extends MvpPresenter<DialogView> {

    LanguageDao languageDao;

    DialogPresenter(DaoSession session) {
        this.languageDao = session.getLanguageDao();
    }

    void fetchLanguages() {
        if (languageDao.count() > 0) {
            List<Language> entries = languageDao.queryBuilder()
                    .orderAsc(LanguageDao.Properties.Name)
                    .list();
            getViewState().showLanguages(new ArrayList<>(entries));
        } else {
            BaseApp.getApi()
                    .getLanguages(TranslateApi.DEFAULT_UI)
                    .enqueue(new Callback<LangCollection>() {
                        @Override
                        public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                            ArrayList<Language> result = new ArrayList<>();
                            for (Map.Entry<String, String> langPair : response.body().getLangs().entrySet()) {
                                String code = langPair.getKey();
                                String name = langPair.getValue();
                                Language lang = new Language(code, name);
                                languageDao.insertOrReplace(lang);
                                result.add(lang);
                            }
                            result.sort((fst, scd) -> fst.getName().compareTo(scd.getName()));
                            getViewState().showLanguages(result);
                        }

                        @Override
                        public void onFailure(Call<LangCollection> call, Throwable t) {}
                    });
        }
    }

    void filterLanguages(String query) {
        query = "%" + query.trim() + "%";
        List<Language> entries = languageDao.queryBuilder()
                .where(LanguageDao.Properties.Name.like(query))
                .orderAsc(LanguageDao.Properties.Name)
                .list();
        getViewState().showLanguages(new ArrayList<>(entries));
    }

}
