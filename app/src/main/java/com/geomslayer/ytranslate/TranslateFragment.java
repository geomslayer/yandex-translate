package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.util.Calendar;
import java.util.Map;

import io.realm.Case;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;

public class TranslateFragment extends Fragment implements LanguageFragment.LanguageDialogListener {

    private static final int FROM = 0;
    private static final int TO = 1;

    private ViewGroup parent;
    private EditText toTranslate;
    private TextView translationView;
    private Button translateButton;
    private ImageView clearButton;
    private ImageView favoriteButton;
    private ImageView swapButton;
    private TextView leftLanguage;
    private TextView rightLanguage;
    private Unregistrar unregistrar;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        translationView = (TextView) fragmentView.findViewById(R.id.translationTextView);
        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        // translateButton = (Button) fragmentView.findViewById(R.id.translateButton);
        favoriteButton = (ImageView) fragmentView.findViewById(R.id.favoriteButton);
        clearButton = (ImageView) fragmentView.findViewById(R.id.clearButton);
        swapButton = (ImageView) fragmentView.findViewById(R.id.swapButton);
        leftLanguage = (TextView) fragmentView.findViewById(R.id.leftLang);
        rightLanguage = (TextView) fragmentView.findViewById(R.id.rightLang);
        parent = (ViewGroup) fragmentView;

        initListeners();
        setTranslation(TranslationUtils.restoreFromSharedPreferences(getActivity()));
        fetchLanguages();
        prepareScreen();

        prepareEditText();

        return fragmentView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregistrar.unregister();
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
                translate();
            }
        });
        unregistrar = KeyboardVisibilityEvent.registerEventListener(
                getActivity(),
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        saveInHistory();
                        if (!isOpen) {
                            toTranslate.clearFocus();
                        }
                    }
                });
    }

    private void prepareScreen() {
        setTranslation(TranslationUtils.restoreFromSharedPreferences(getActivity()));
    }

    private void translate() {
        final String rawText = toTranslate.getText().toString().trim();
        final String langFrom = leftLanguage.getTag().toString();
        final String langTo = rightLanguage.getTag().toString();

        BaseApp.getApi()
                .getTranslation(TranslateApi.KEY, rawText, langFrom + "-" + langTo)
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
                        boolean inFav = checkForFravorites(rawText, langTo);
                        updateFavoriteButton(inFav);
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translationView.setText(t.getMessage());
                    }
                });
    }

    private boolean checkForFravorites(String rawText, String langTo) {
        Translation translation = BaseApp.getRealm().where(Translation.class)
                .equalTo(Translation.Field.rawText, rawText, Case.INSENSITIVE)
                .equalTo(Translation.Field.langTo, langTo)
                .findFirst();
        return translation != null && translation.isInFavorites();
    }

    public void setTranslation(Translation translation) {
        if (translation == null) {
            return;
        }

        String langFrom = "";
        String langTo = "";
        try {
            langFrom = BaseApp.getRealm().where(Language.class)
                    .equalTo(Language.Field.simpleName, translation.getLangFrom())
                    .findFirst()
                    .getName();
            langTo = BaseApp.getRealm().where(Language.class)
                    .equalTo(Language.Field.simpleName, translation.getLangTo())
                    .findFirst()
                    .getName();
        } catch (NullPointerException e) {
            // fine
        }

        toTranslate.setText(translation.getRawText());
        translationView.setText(translation.getTranslation());
        leftLanguage.setTag(translation.getLangFrom());
        leftLanguage.setText(langFrom);
        rightLanguage.setTag(translation.getLangTo());
        rightLanguage.setText(langTo);

        boolean inFav = checkForFravorites(translation.getRawText(), translation.getLangTo());
        updateFavoriteButton(inFav);
    }

    private void updateFavoriteButton(boolean isActive) {
        if (isActive) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_inactive);
        }
    }

    private Translation saveInHistory() {
        String rawText = toTranslate.getText().toString().trim();
        String langTo = rightLanguage.getTag().toString();
        if (rawText.isEmpty()) {
            return null;
        }
        Realm realm = BaseApp.getRealm();
        Translation translation = realm.where(Translation.class)
                .equalTo(Translation.Field.rawText, rawText, Case.INSENSITIVE)
                .equalTo(Translation.Field.langTo, langTo)
                .findFirst();
        realm.beginTransaction();
        if (translation == null) {
            String translated = translationView.getText().toString().trim();
            String langFrom = leftLanguage.getTag().toString();
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
        TranslationUtils.saveInSharedPreferences(getActivity(), translation);
        return translation;
    }

    private void fetchLanguages() {
        BaseApp.getApi()
                .getLanguages(TranslateApi.KEY, TranslateApi.DEFAULT_UI)
                .enqueue(new Callback<LangCollection>() {
                    @Override
                    public void onResponse(Call<LangCollection> call, retrofit2.Response<LangCollection> response) {
                        Realm realm = BaseApp.getRealm();
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
                    }

                    @Override
                    public void onFailure(Call<LangCollection> call, Throwable t) {

                    }
                });
    }

    private void initListeners() {
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Translation translation = saveInHistory();
                if (translation != null) {
                    BaseApp.getRealm().beginTransaction();
                    translation.setInFavorites(!translation.isInFavorites());
                    BaseApp.getRealm().commitTransaction();
                    updateFavoriteButton(translation.isInFavorites());
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
                Object tag = leftLanguage.getTag();
                leftLanguage.setTag(rightLanguage.getTag());
                rightLanguage.setTag(tag);

                CharSequence text = leftLanguage.getText();
                leftLanguage.setText(rightLanguage.getText());
                rightLanguage.setText(text);

                // TODO add swap toTranslate and translation
            }
        });
        View.OnClickListener languageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        };
        leftLanguage.setOnClickListener(languageClickListener);
        rightLanguage.setOnClickListener(languageClickListener);
    }

    @Override
    public void onLanguageSelected(Language language, int type) {
        TextView langView;
        if (type == FROM) {
            langView = leftLanguage;
        } else {
            langView = rightLanguage;
        }
        langView.setText(language.getName());
        langView.setTag(language.getSimpleName());
    }

}
