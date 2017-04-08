package com.geomslayer.ytranslate.translate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.R;
import com.geomslayer.ytranslate.models.Capture;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.Translation;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

public class TranslateFragment extends MvpAppCompatFragment
        implements LanguageFragment.LanguageDialogListener, TranslateView {

    private static final String TAG = "TranslateFragment";

    @InjectPresenter
    TranslatePresenter presenter;

    @ProvidePresenter
    TranslatePresenter providePresenter() {
        DaoSession session = ((BaseApp) getActivity().getApplication()).getDaoSession();
        return new TranslatePresenter(session);
    }

    private static final int TIMES = 2;
    int firstTimes = TIMES;

    private EditText toTranslate;
    private TextView translationView;
    private ImageView clearButton;
    private ImageView favoriteButton;
    private ImageView swapButton;
    private ViewGroup placeholder;

    private TextView[] languageViews;
    public static final int SOURCE = 0;
    public static final int TARGET = 1;

    private Unregistrar unregistrar;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_translate, container, false);

        toTranslate = (EditText) fragmentView.findViewById(R.id.translateEditText);
        translationView = (TextView) fragmentView.findViewById(R.id.translationTextView);

        languageViews = new TextView[2];
        languageViews[SOURCE] = (TextView) fragmentView.findViewById(R.id.sourceLanguage);
        languageViews[TARGET] = (TextView) fragmentView.findViewById(R.id.targetLanguage);

        favoriteButton = (ImageView) fragmentView.findViewById(R.id.favoriteButton);
        clearButton = (ImageView) fragmentView.findViewById(R.id.clearButton);
        swapButton = (ImageView) fragmentView.findViewById(R.id.swapButton);
        placeholder = (ViewGroup) fragmentView.findViewById(R.id.placeholder);

        initListeners();
        prepareEditText();

        return fragmentView;
    }

    private void prepareEditText() {
        toTranslate.setHorizontallyScrolling(false);
        toTranslate.setMaxLines(5);
        toTranslate.setLines(5);
        toTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable text) {
                if (firstTimes > 0) {
                    --firstTimes;
                    return;
                }
                presenter.translate();
            }
        });
        unregistrar = KeyboardVisibilityEvent.registerEventListener(
                getActivity(),
                isOpen -> {
                    if (!isOpen) {
                        presenter.onKeyboardClosed();
                        toTranslate.clearFocus();
                    }
                });
    }

    private void initListeners() {
        favoriteButton.setOnClickListener(view -> presenter.onFavoriteButtonPressed());
        clearButton.setOnClickListener(view -> presenter.onClearButtonPressed());
        swapButton.setOnClickListener(view -> {
            String[] languages = new String[]{getLanguage(SOURCE), getLanguage(TARGET)};
            presenter.onSwapButtonPressed(languages);
        });
        languageViews[SOURCE].setOnClickListener(view -> showDialog(SOURCE));
        languageViews[TARGET].setOnClickListener(view -> showDialog(TARGET));
    }

    public void showDialog(final int type) {
        final String title;
        if (type == SOURCE) {
            title = getString(R.string.languageFrom);
        } else {
            title = getString(R.string.languageTo);
        }
        LanguageFragment dialog = LanguageFragment.newInstance(title, type);
        dialog.setTargetFragment(this, 666);
        dialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregistrar.unregister();
    }

    @Override
    public void onPause() {
        presenter.onViewPaused();
        super.onPause();
    }

    private String getLanguage(final int type) {
        return languageViews[type].getTag().toString();
    }

    @Override
    public void requestData() {
        presenter.translateNow(getCapture());
    }

    private String getTextToTranslate() {
        return toTranslate.getText().toString().trim();
    }

    @Override
    public void setSourceText(String text) {
        toTranslate.setText(text);
    }

    private Capture getCapture() {
        return new Capture(
                getTextToTranslate(),
                getLanguage(SOURCE),
                getLanguage(TARGET)
        );
    }

    @Override
    public void clearScreen() {
        toTranslate.setText("");
        translationView.setText("");
        updateFavoriteIcon(false);
    }

    @Override
    public void setTranslation(@NonNull Translation translation) {
        updateTranslation(translation);
        toTranslate.setText(translation.getSourceText());
    }

    @Override
    public void updateTranslation(Translation translation) {
        setLanguage(translation.getSource(), SOURCE);
        setLanguage(translation.getTarget(), TARGET);
        translationView.setText(translation.getTranslatedText());
        updateFavoriteIcon(translation.isInFavorites());
    }

    @Override
    public void updateFavoriteIcon(boolean isActive) {
        if (isActive) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_active);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_inactive);
        }
    }

    @Override
    public void setPlaceholderVisibility(boolean visible) {
        if (visible) {
            placeholder.setVisibility(View.VISIBLE);
            translationView.setVisibility(View.GONE);
            favoriteButton.setVisibility(View.GONE);
        } else {
            placeholder.setVisibility(View.GONE);
            translationView.setVisibility(View.VISIBLE);
            favoriteButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLanguage(Language lang, final int type) {
        languageViews[type].setTag(lang.getCode());
        languageViews[type].setText(lang.getName());
    }

    @Override
    public void onLanguageSelected(Language language, final int type) {
        presenter.onLanguageSelected(new String[]{getLanguage(SOURCE), getLanguage(TARGET)},
                language.getCode(), type);
    }

    public void showLastInHistory() {
        firstTimes = TIMES;
        presenter.showLastInHistory();
    }

}
