package com.geomslayer.ytranslate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private TextView translationView;
    private Button translateButton;
    private ImageView clearButton;
    private ImageView favoriteButton;
    private OnSetupListener callback;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnSetupListener) {
            callback = (OnSetupListener) context;
        } else {
            throw new UnsupportedOperationException("Context must implement Callback!");
        }
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

        addListeners();
        setTranslation(callback.getCurrentTranslation());

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
                        translationView.setText(translated);
                        saveInHistory(rawText, translated);
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {
                        translationView.setText("Failure!");
                    }
                });
    }

    public void setTranslation(Translation translation) {
        if (translation == null) {
            return;
        }
        toTranslate.setText(translation.getRawText());
        translationView.setText(translation.getTranslation());
        Realm realm = Realm.getDefaultInstance();
        long count = 0;
        try {
            count = realm.where(Translation.class)
                    .equalTo(Translation.Field.translation, translation.getTranslation())
                    .equalTo(Translation.Field.inFavorites, true)
                    .count();
        } finally {
            realm.close();
        }
        updateFavoriteButton(count > 0);
    }

    private void updateFavoriteButton(boolean isActive) {
        if (isActive) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_inactive);
        }
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
            updateFavoriteButton(translation.isInFavorites());
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
    }

    interface OnSetupListener {
        Translation getCurrentTranslation();
    }

}
