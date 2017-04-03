package com.geomslayer.ytranslate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.Response;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.storage.Language;
import com.geomslayer.ytranslate.storage.Translation;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.util.Calendar;
import java.util.Map;

import io.realm.Case;
import retrofit2.Call;
import retrofit2.Callback;

import static com.geomslayer.ytranslate.BaseApp.getRealm;

public class TranslateFragment extends Fragment implements LanguageFragment.LanguageDialogListener {

    private static final int FROM = 0;
    private static final int TO = 1;

    private EditText toTranslate;
    private TextView translationView;
    private ImageView clearButton;
    private ImageView favoriteButton;
    private ImageView swapButton;
    private TextView leftLanguage;
    private TextView rightLanguage;
    private ViewGroup placeholder;
    private Unregistrar unregistrar;

    private boolean errorConnection = false;
    private RequestHandler requestHandler;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        requestHandler = new RequestHandler();

        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        translationView = (TextView) fragmentView.findViewById(R.id.translationTextView);
        leftLanguage = (TextView) fragmentView.findViewById(R.id.leftLang);
        rightLanguage = (TextView) fragmentView.findViewById(R.id.rightLang);
        favoriteButton = (ImageView) fragmentView.findViewById(R.id.favoriteButton);
        clearButton = (ImageView) fragmentView.findViewById(R.id.clearButton);
        swapButton = (ImageView) fragmentView.findViewById(R.id.swapButton);
        placeholder = (ViewGroup) fragmentView.findViewById(R.id.placeholder);

        initListeners();
        prepareEditText();
        setTranslation(TranslationUtils.restoreFromSharedPreferences(getActivity()));
        fetchLanguages(); // TODO

        return fragmentView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregistrar.unregister();
    }

    @Override
    public void onStop() {
        requestHandler.cancel(true);
        super.onStop();
    }

    private void prepareEditText() {
        toTranslate.setHorizontallyScrolling(false);
        toTranslate.setMaxLines(5);
        toTranslate.setLines(5);
        toTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable text) {
//                translate();
                requestHandler.requestTranslation();
            }
        });
        unregistrar = KeyboardVisibilityEvent.registerEventListener(
                getActivity(),
                isOpen -> {
                    saveInHistory();
                    if (!isOpen) {
                        toTranslate.clearFocus();
                    }
                });
    }

    private Translation getCurrentTranslation() {
        Translation translation = new Translation();
        translation.setRawText(toTranslate.getText().toString().trim());
        translation.setTranslation(translationView.getText().toString().trim());
        translation.setLangFrom(leftLanguage.getTag().toString());
        translation.setLangTo(rightLanguage.getTag().toString());
        return translation;
    }

    public void setTranslation(Translation translation) {
        if (translation == null) {
            return;
        }

        String langFrom = getLanguageName(translation.getLangFrom());
        String langTo = getLanguageName(translation.getLangTo());

        leftLanguage.setTag(translation.getLangFrom());
        leftLanguage.setText(langFrom);
        rightLanguage.setTag(translation.getLangTo());
        rightLanguage.setText(langTo);

        translationView.setText(translation.getTranslation());
        toTranslate.setText(translation.getRawText());

        updateFavoriteButton(checkForFavorites(translation));
    }

    private Translation findTranslation(Translation translation) {
        return getRealm().where(Translation.class)
                .equalTo(Translation.Field.rawText, translation.getRawText(), Case.INSENSITIVE)
                .equalTo(Translation.Field.langTo, translation.getLangTo())
                .findFirst();
    }

    private void translate() {
        final Translation cur = getCurrentTranslation();

        if (cur.getRawText().isEmpty()) {
            translationView.setText("");
            return;
        }

        Translation realmTranslation = findTranslation(cur);
        if (realmTranslation != null) {
            hidePlaceholder();
            updateFavoriteButton(realmTranslation.isInFavorites());
            translationView.setText(realmTranslation.getTranslation());
            return;
        }

        BaseApp.getApi()
                .detectLanguage(cur.getRawText(), cur.getLangFrom())
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        String lang = response.body().getLang();
                        if (lang != null && !lang.isEmpty()) {
                            cur.setLangFrom(lang);
                            setSourceLanguage(lang);
                        }
                        finishTranslation(cur);
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translationView.setText("");
                        showPlaceholder();
                    }
                });
    }

    private void finishTranslation(final Translation translation) {
        BaseApp.getApi()
                .getTranslation(translation.getRawText(),
                        translation.getLangFrom() + "-" + translation.getLangTo())
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        hidePlaceholder();
                        StringBuilder stringBuilder = new StringBuilder();
                        if (response.body() != null) {
                            for (String str : response.body().getText()) {
                                stringBuilder.append(str).append('\n');
                            }
                        }
                        translation.setTranslation(stringBuilder.toString().trim());
                        updateFavoriteButton(checkForFavorites(translation));
                        translationView.setText(translation.getTranslation());
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translationView.setText("");
                        showPlaceholder();
                    }
                });
    }

    private boolean checkForFavorites(Translation translation) {
        Translation realmTranslation = findTranslation(translation);
        return realmTranslation != null && realmTranslation.isInFavorites();
    }

    private String getLanguageName(String simpleName) {
        Language lang = getRealm().where(Language.class)
                .equalTo(Language.Field.simpleName, simpleName)
                .findFirst();
        return lang == null ? "" : lang.getName();
    }

    private void updateFavoriteButton(boolean isActive) {
        if (isActive) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_inactive);
        }
    }

    private Translation saveInHistory() {
        if (errorConnection) {
            return null;
        }

        Translation cur = getCurrentTranslation();

        if (cur.getRawText().isEmpty()) {
            return null;
        }

        Translation translation = findTranslation(cur);

        getRealm().beginTransaction();
        if (translation == null) {
            translation = getRealm().copyToRealm(cur);
            translation.setInFavorites(false);
        }
        translation.setInHistory(true);
        translation.setMoment(Calendar.getInstance().getTime());
        getRealm().commitTransaction();
        TranslationUtils.saveInSharedPreferences(getActivity(), translation);

        return translation;
    }

    private void showPlaceholder() {
        placeholder.setVisibility(View.VISIBLE);
        errorConnection = true;
    }

    private void hidePlaceholder() {
        placeholder.setVisibility(View.GONE);
        errorConnection = false;
    }

    private void fetchLanguages() {
        BaseApp.getApi()
                .getLanguages(TranslateApi.DEFAULT_UI)
                .enqueue(new Callback<LangCollection>() {
                    @Override
                    public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                        getRealm().beginTransaction();
                        for (Map.Entry<String, String> langPair : response.body().getLangs().entrySet()) {
                            String simpleName = langPair.getKey();
                            String name = langPair.getValue();
                            Language language = getRealm().where(Language.class)
                                    .equalTo(Language.Field.simpleName, simpleName)
                                    .findFirst();
                            if (language == null) {
                                language = getRealm().createObject(Language.class);
                                language.setSimpleName(simpleName);
                                language.setName(name);
                            }
                        }
                        getRealm().commitTransaction();
                        leftLanguage.setText(getLanguageName(leftLanguage.getTag().toString()));
                        rightLanguage.setText(getLanguageName(rightLanguage.getTag().toString()));
                    }

                    @Override
                    public void onFailure(Call<LangCollection> call, Throwable t) {}
                });
    }

    private void initListeners() {
        favoriteButton.setOnClickListener(view -> {
            Translation translation = saveInHistory();
            if (translation != null) {
                getRealm().beginTransaction();
                translation.setInFavorites(!translation.isInFavorites());
                getRealm().commitTransaction();
                updateFavoriteButton(translation.isInFavorites());
            }
        });
        clearButton.setOnClickListener(view -> {
            toTranslate.setText("");
            translationView.setText("");
            updateFavoriteButton(false);
            TranslationUtils.saveInSharedPreferences(getActivity(), getCurrentTranslation());
        });
        swapButton.setOnClickListener(view -> {
            swapLanguages();

            CharSequence text = translationView.getText();
            translationView.setText(toTranslate.getText());
            toTranslate.setText(text);
        });
        View.OnClickListener languageClickListener = view -> {
            int type;
            String title;
            if (view.getId() == R.id.leftLang) {
                type = FROM;
                title = getString(R.string.languageFrom);
            } else {
                type = TO;
                title = getString(R.string.languageTo);
            }
            LanguageFragment fragment = LanguageFragment.newInstance(title, type);
            fragment.setTargetFragment(TranslateFragment.this, 666);
            fragment.show(getActivity().getSupportFragmentManager(), "dialog");
        };
        leftLanguage.setOnClickListener(languageClickListener);
        rightLanguage.setOnClickListener(languageClickListener);
    }

    private void setSourceLanguage(String lang) {
        if (lang.equals(leftLanguage.getTag().toString())) {
            return;
        }
        if (lang.equals(rightLanguage.getTag().toString())) {
            swapLanguages();
            return;
        }
        leftLanguage.setTag(lang);
        leftLanguage.setText(getLanguageName(lang));
    }

    private void swapLanguages() {
        Object tag = leftLanguage.getTag();
        leftLanguage.setTag(rightLanguage.getTag());
        rightLanguage.setTag(tag);

        CharSequence text = leftLanguage.getText();
        leftLanguage.setText(rightLanguage.getText());
        rightLanguage.setText(text);
    }

    @Override
    public void onLanguageSelected(Language language, int type) {
        TextView currentLangView;
        TextView otherLangView;
        if (type == FROM) {
            currentLangView = leftLanguage;
            otherLangView = rightLanguage;
        } else {
            currentLangView = rightLanguage;
            otherLangView = leftLanguage;
        }
        if (language.getSimpleName().equals(otherLangView.getTag().toString())) {
            swapButton.callOnClick();
        } else {
            currentLangView.setText(language.getName());
            currentLangView.setTag(language.getSimpleName());
            translate();
        }
    }

    private class RequestHandler extends AsyncTask<Void, Void, Void> {

        private final static long DELAY = 350;
        private final static long WAIT_TIME = 25;

        volatile private boolean wasQuery = false;
        volatile private long passedTime = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (isCancelled()) {
                    break;
                }

                if (wasQuery && passedTime <= 0) {
                    wasQuery = false;
                    publishProgress();
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    // fine
                }
                passedTime -= WAIT_TIME;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            translate();
        }

        protected void requestTranslation() {
            if (this.getStatus() != Status.RUNNING) {
                this.execute();
            }
            this.passedTime = DELAY;
            wasQuery = true;
        }

    }

}
