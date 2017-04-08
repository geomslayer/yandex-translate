package com.geomslayer.ytranslate.translate;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.geomslayer.ytranslate.models.Language;
import com.geomslayer.ytranslate.models.Translation;

@StateStrategyType(SkipStrategy.class)
interface TranslateView extends MvpView {

    void requestData();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void clearScreen();

    void setSourceText(String text);

    @StateStrategyType(SingleStateStrategy.class)
    void setTranslation(Translation translation);

    @StateStrategyType(SingleStateStrategy.class)
    void updateTranslation(Translation translation);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setPlaceholderVisibility(boolean visible);

    void setLanguage(Language language, final int type);

    void updateFavoriteIcon(boolean active);
}
