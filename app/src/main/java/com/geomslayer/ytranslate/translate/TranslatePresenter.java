package com.geomslayer.ytranslate.translate;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.RequestHandler;
import com.geomslayer.ytranslate.models.Capture;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.LanguageDao;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.models.TranslationDao;
import com.geomslayer.ytranslate.network.Response;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;

@InjectViewState
public class TranslatePresenter extends MvpPresenter<TranslateView>
        implements RequestHandler.OnRequestReadyListener {

    private RequestHandler requestHandler;
    private LanguageDao languageDao;
    private TranslationDao translationDao;

    private Translation lastTranslation;

    private boolean wasRequestSave = false;

    TranslatePresenter(DaoSession daoSession) {
        languageDao = daoSession.getLanguageDao();
        translationDao = daoSession.getTranslationDao();
        requestHandler = new RequestHandler(this);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        lastTranslation = loadLastTranslation();
        if (lastTranslation == null) {
            lastTranslation = new Translation();
            lastTranslation.setSourceText("");
            lastTranslation.setTranslatedText("");
            lastTranslation.setSource(new Language("ru", "Russian"));
            lastTranslation.setTarget(new Language("en", "English"));
            getViewState().setSourceLanguage(lastTranslation.getSource());
            getViewState().setTargetLanguage(lastTranslation.getTarget());
        } else {
            getViewState().setTranslation(lastTranslation);
        }
    }

    void translate() {
        if (requestHandler.isCancelled()) {
            requestHandler = new RequestHandler(this);
        }
        requestHandler.doFakeRequest();
    }

    @Override
    public void doRealRequest() {
        getViewState().requestData();
    }

    void translateNow(Capture capture) {
        if (!BaseApp.isLanguagesReceived()) {
            BaseApp.loadLanguages(languageDao);
            return;
        }
        // first we detect source language then translate
        BaseApp.getApi()
                .detectLanguage(capture.getText(), capture.getSource())
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        if (response.body() != null) {
                            String lang = response.body().getLang();
                            if (lang != null && !lang.isEmpty()) {
                                if (lang.equals(capture.getTarget())) {
                                    capture.setTarget(capture.getSource());
                                }
                                capture.setSource(lang);

                            }
                        }
                        finishTranslation(capture);
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        requestHandler.onCompleteRequest();
                        getViewState().setPlaceholderVisibility(true);
                    }
                });
    }

    // translate
    private void finishTranslation(Capture capture) {
        lastTranslation = findTranslation(capture);
        // looking for translation in cache
        if (lastTranslation != null) {
            onTranslationReceived();
            return;
        }
        BaseApp.getApi()
                .getTranslation(capture.getText(),
                        capture.getSource() + "-" + capture.getTarget())
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        getViewState().setPlaceholderVisibility(false);
                        StringBuilder stringBuilder = new StringBuilder();
                        if (response.body() != null) {
                            String prefix = "";
                            for (String str : response.body().getText()) {
                                stringBuilder.append(prefix).append(str);
                                prefix = "\n";
                            }
                        }
                        lastTranslation = new Translation();
                        lastTranslation.setSourceText(capture.getText());
                        lastTranslation.setTranslatedText(stringBuilder.toString());
                        lastTranslation.setSourceCode(capture.getSource());
                        lastTranslation.setTargetCode(capture.getTarget());
                        // save in cache
                        translationDao.insert(lastTranslation);

                        if (lastTranslation.getSource() != null && lastTranslation.getTarget() != null) {
                            onTranslationReceived();
                        }
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        requestHandler.onCompleteRequest();
                        getViewState().setPlaceholderVisibility(true);
                    }
                });
    }

    private void onTranslationReceived() {
        requestHandler.onCompleteRequest();
        if (wasRequestSave && requestHandler.getRequestsCount() == 0) {
            wasRequestSave = false;
            saveInHistory();
        }
        getViewState().updateTranslation(lastTranslation);
    }

    private Translation findTranslation(Capture capture) {
        return findTranslation(capture.getText(), capture.getSource(), capture.getTarget());
    }

    private Language findLanguage(String code) {
        return languageDao.queryBuilder()
                .where(LanguageDao.Properties.Code.eq(code))
                .unique();
    }

    // get translation from database
    private Translation findTranslation(String text, String source, String target) {
        return translationDao.queryBuilder()
                .where(TranslationDao.Properties.SourceText.eq(text))
                .where(TranslationDao.Properties.SourceCode.eq(source))
                .where(TranslationDao.Properties.TargetCode.eq(target))
                .limit(1)
                .unique();
    }

    private Translation loadLastTranslation() {
        return translationDao.queryBuilder()
                .where(TranslationDao.Properties.InHistory.eq(true))
                .orderDesc(TranslationDao.Properties.Moment)
                .limit(1)
                .unique();
    }

    void onFavoriteButtonPressed() {
        if (lastTranslation.getSourceText().isEmpty()) {
            return;
        }
        boolean inFavorites = !lastTranslation.isInFavorites();
        lastTranslation.setInFavorites(inFavorites);
        translationDao.insertOrReplace(lastTranslation);
        getViewState().updateFavoriteIcon(inFavorites);
    }

    void onClearButtonPressed() {
        lastTranslation = lastTranslation.emptyInstance();
        translationDao.insert(lastTranslation);
        getViewState().clearScreen();
    }

    void onSwapButtonPressed(Capture capture, String translated) {
        wasRequestSave = true;
        Language source = findLanguage(capture.getSource());
        Language target = findLanguage(capture.getTarget());
        getViewState().setSourceLanguage(target);
        getViewState().setTargetLanguage(source);
        getViewState().setTranslatedText(capture.getText());
        getViewState().setSourceText(translated);
    }

    void onLanguageSelected(String languages[], String updated, final int type) {
        final int other = type ^ 1;
        if (!languages[type].equals(updated)) {
            if (languages[other].equals(updated)) {
                Capture capture = new Capture(lastTranslation.getSourceText(), languages[0], languages[1]);
                onSwapButtonPressed(capture, lastTranslation.getTranslatedText());
            } else {
                Language lang = findLanguage(updated);
                if (type == TranslateFragment.TARGET) {
                    wasRequestSave = true;
                    getViewState().setTargetLanguage(lang);
                    getViewState().requestData();
                } else {
                    getViewState().setSourceLanguage(lang);
                }
            }
        }
    }

    void onKeyboardClosed() {
        // save translation in history
        if (requestHandler.getRequestsCount() == 0) {
            saveInHistory();
        } else {
            wasRequestSave = true;
        }
    }

    private void saveInHistory() {
        if (lastTranslation.getSourceText().isEmpty()) {
            return;
        }
        lastTranslation.setInHistory(true);
        lastTranslation.setMoment(Calendar.getInstance().getTime());
        translationDao.insertOrReplace(lastTranslation);
    }

    void onViewPaused() {
        requestHandler.cancel(true);
    }

    // called when user clicks on translation in History or Favorites
    void showLastInHistory() {
        lastTranslation = loadLastTranslation();
        getViewState().setTranslation(lastTranslation);
    }
}
