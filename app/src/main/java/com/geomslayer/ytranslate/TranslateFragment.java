package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.geomslayer.ytranslate.network.Response;
import com.geomslayer.ytranslate.network.TranslateApi;
import com.geomslayer.ytranslate.storage.Translation;

import java.util.Calendar;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;

public class TranslateFragment extends Fragment {

    private EditText toTranslate;
    private TextView translation;
    private Button translateButton;
    private Button addToFavorites;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        translation = (TextView) fragmentView.findViewById(R.id.translationTextView);
        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        translateButton = (Button) fragmentView.findViewById(R.id.translateButton);
        addToFavorites = (Button) fragmentView.findViewById(R.id.favoritesButton);

        addListeners();

        return fragmentView;
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
                        translation.setText(translated);
                        saveInHistory(rawText, translated);
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translation.setText("Failure!");
                    }
                });
    }

    private void saveInHistory(String rawText, String translated) {
        if (translated.isEmpty() || rawText.isEmpty()) {
            return;
        }
        Realm realm = Realm.getDefaultInstance();
        try {
            Translation translation = realm.where(Translation.class)
                    .equalTo(Translation.Field.rawText, rawText)
                    .findFirst();
            realm.beginTransaction();
            if (translation == null) {
                translation = realm.createObject(Translation.class);
                translation.setRawText(rawText);
                translation.setTranslation(translated);
                translation.setInHistory(true);
                translation.setInFavorites(false);
            }
            translation.setMoment(Calendar.getInstance().getTime());
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    private void addListeners() {
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translate();
            }
        });
        addToFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String translated = translation.getText().toString().trim();
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
                        translation.setInFavorites(true);
                        realm.commitTransaction();
                    }
                } finally {
                    realm.close();
                }
            }
        });
    }

}
