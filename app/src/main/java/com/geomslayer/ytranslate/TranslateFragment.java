package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.geomslayer.ytranslate.network.LangCollection;
import com.geomslayer.ytranslate.network.Response;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.storage.Language;
import com.geomslayer.ytranslate.storage.Translation;

import java.util.Calendar;
import java.util.Map;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;

public class TranslateFragment extends Fragment {

    private EditText toTranslate;
    private TextView translationView;
    private Button translateButton;
    private ImageView clearButton;
    private ImageView favoriteButton;
    private OnSetupListener callback;
    private ImageView swapButton;
    private TextView leftLanguage;
    private TextView rightLanguage;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        translationView = (TextView) fragmentView.findViewById(R.id.translationTextView);
        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        translateButton = (Button) fragmentView.findViewById(R.id.translateButton);
        favoriteButton = (ImageView) fragmentView.findViewById(R.id.favoriteButton);
        clearButton = (ImageView) fragmentView.findViewById(R.id.clearButton);
        swapButton = (ImageView) fragmentView.findViewById(R.id.swapButton);
        leftLanguage = (TextView) fragmentView.findViewById(R.id.leftLang);
        rightLanguage = (TextView) fragmentView.findViewById(R.id.rightLang);

        addListeners();
        setTranslation(TranslationUtils.restoreFromSharedPreferences(getActivity()));
        fetchLanguages();
        prepareScreen();

        return fragmentView;
    }

    private void prepareScreen() {
        setTranslation(TranslationUtils.restoreFromSharedPreferences(getActivity()));
    }

    private void translate() {
        final String rawText = toTranslate.getText().toString().trim();
        BaseApp.getApi()
                .getTranslation(TranslateApi.KEY, rawText, "en-ru")
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        StringBuilder stringBuilder = new StringBuilder();
                        try {
                            for (String str : response.body().getText()) {
                                stringBuilder.append(str).append('\n');
                            }
                        } catch (NullPointerException e) {
                            // empty request
                        }
                        String translated = stringBuilder.toString().trim();
                        translationView.setText(translated);
                        saveInHistory();
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translationView.setText(t.getMessage());
                    }
                });
    }

    public void setTranslation(Translation translation) {
        if (translation == null) {
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        String langFrom;
        String langTo;
        long count = 0;
        try {
            langFrom = realm.where(Language.class)
                    .equalTo(Language.Field.simpleName, translation.getLangFrom())
                    .findFirst()
                    .getName();
            langTo = realm.where(Language.class)
                    .equalTo(Language.Field.simpleName, translation.getLangTo())
                    .findFirst()
                    .getName();
            count = realm.where(Translation.class)
                    .equalTo(Translation.Field.translation, translation.getTranslation())
                    .equalTo(Translation.Field.inFavorites, true)
                    .count();
        } finally {
            realm.close();
        }

        toTranslate.setText(translation.getRawText());
        translationView.setText(translation.getTranslation());
        leftLanguage.setTag(translation.getLangFrom());
        leftLanguage.setText(langFrom);
        rightLanguage.setTag(translation.getLangTo());
        rightLanguage.setText(langTo);

        updateFavoriteButton(count > 0);
    }

    private void updateFavoriteButton(boolean isActive) {
        if (isActive) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_inactive);
        }
    }

    private void saveInHistory() {
        String rawText = toTranslate.getText().toString().trim();
        if (rawText.isEmpty()) {
            return;
        }
        Realm realm = Realm.getDefaultInstance();
        try {
            Translation translation = realm.where(Translation.class)
                    .equalTo(Translation.Field.rawText, rawText)
                    .findFirst();
            realm.beginTransaction();
            if (translation == null) {
                String translated = translationView.getText().toString().trim();
                String langFrom = leftLanguage.getTag().toString();
                String langTo = rightLanguage.getTag().toString();
                translation = realm.createObject(Translation.class);
                translation.setRawText(rawText);
                translation.setTranslation(translated);
                translation.setLangFrom(langFrom);
                translation.setLangTo(langTo);
                translation.setInHistory(true);
                translation.setInFavorites(false);
            }
            translation.setMoment(Calendar.getInstance().getTime());
            updateFavoriteButton(translation.isInFavorites());
            realm.commitTransaction();


        } finally {
            realm.close();
        }
    }

    private void fetchLanguages() {
        BaseApp.getApi()
                .getLanguages(TranslateApi.KEY, TranslateApi.DEFAULT_UI)
                .enqueue(new Callback<LangCollection>() {
                    @Override
                    public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.beginTransaction();
                            for (Map.Entry<String, String> langPair : response.body().getLangs().entrySet()) {
                                String simpleName = langPair.getKey();
                                String name = langPair.getValue();
                                Language language = realm.where(Language.class)
                                        .equalTo(Language.Field.simpleName, simpleName)
                                        .findFirst();
                                if (language == null) {
                                    language = realm.createObject(Language.class);
                                    language.setSimpleName(simpleName);
                                    language.setName(name);
                                }
                                Log.d(simpleName, name);
                            }
                            realm.commitTransaction();
                        } finally {
                            realm.close();
                        }
                    }

                    @Override
                    public void onFailure(Call<LangCollection> call, Throwable t) {

                    }
                });
    }

    private void addListeners() {
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translate();
            }
        });
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String translated = translationView.getText().toString().trim();
                if (translated.isEmpty()) {
                    return;
                }
                Realm realm = Realm.getDefaultInstance();
                try {
                    Translation translation = realm.where(Translation.class)
                            .equalTo(Translation.Field.translation, translated)
                            .findFirst();
                    if (translation != null) {
                        realm.beginTransaction();
                        translation.setInFavorites(!translation.isInFavorites());
                        realm.commitTransaction();
                        updateFavoriteButton(translation.isInFavorites());
                    }
                } finally {
                    realm.close();
                }
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toTranslate.setText("");
                translationView.setText("");
                updateFavoriteButton(false);
            }
        });
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
    }

}
