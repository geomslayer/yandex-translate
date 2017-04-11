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

    void clearScreen();

    void setSourceText(String text);

    void setTranslatedText(String text);

    void updateFavoriteIcon(boolean active);

    @StateStrategyType(SingleStateStrategy.class)
    void setTranslation(Translation translation);

    @StateStrategyType(SingleStateStrategy.class)
    void updateTranslation(Translation translation);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setPlaceholderVisibility(boolean visible);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setSourceLanguage(Language language);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setTargetLanguage(Language language);

}
