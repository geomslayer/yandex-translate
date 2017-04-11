package com.geomslayer.ytranslate.translate;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.geomslayer.ytranslate.models.Language;

import java.util.ArrayList;

@StateStrategyType(AddToEndSingleStrategy.class)
interface DialogView extends MvpView {

    void showLanguages(ArrayList<Language> languages);
}
