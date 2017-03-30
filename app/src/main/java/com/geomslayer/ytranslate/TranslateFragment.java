package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.geomslayer.ytranslate.network.Response;
import com.geomslayer.ytranslate.network.TranslateApi;

import retrofit2.Call;
import retrofit2.Callback;

public class TranslateFragment extends Fragment {

    private EditText toTranslate;
    private TextView translation;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        translation = (TextView) fragmentView.findViewById(R.id.translationTextView);
        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        toTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                translate();
            }
        });

        return fragmentView;
    }

    private void translate() {
        String text = toTranslate.getText().toString();
        BaseApp.getApi()
                .getTranslation(TranslateApi.KEY, text, "en-ru")
                .enqueue(new Callback<Response>() {
                    @Override
                    public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                        StringBuilder  stringBuilder = new StringBuilder();
                        try {
                            for (String str : response.body().getText()) {
                                stringBuilder.append(str).append('\n');
                            }
                        } catch (NullPointerException e) {
                            // empty request
                        }
                        translation.setText(stringBuilder.toString().trim());
                    }

                    @Override
                    public void onFailure(Call<Response> call, Throwable t) {

                    }
                });
    }

}
