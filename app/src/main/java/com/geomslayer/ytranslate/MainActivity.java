package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.geomslayer.ytranslate.lists.ListFragment;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.translate.TranslateFragment;

public class MainActivity extends AppCompatActivity
        implements ListFragment.Callback {

    public static final String HOME_TAG = "home";
    public static final String HISTORY_TAG = "history";
    public static final String FAVORITES_TAG = "favorites";

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navigationListener);

        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener = item -> {
        Fragment fragment;
        String tag;
        FragmentManager manager = getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.nav_home:
                fragment = manager.findFragmentByTag(HOME_TAG);
                tag = HOME_TAG;
                if (fragment == null) {
                    fragment = TranslateFragment.newInstance();
                }
                break;

            case R.id.nav_history:
                fragment = manager.findFragmentByTag(HISTORY_TAG);
                tag = HISTORY_TAG;
                if (fragment == null) {
                    fragment = ListFragment.newInstance(ListFragment.HISTORY);
                }
                break;

            case R.id.nav_favorites:
                fragment = manager.findFragmentByTag(FAVORITES_TAG);
                tag = FAVORITES_TAG;
                if (fragment == null) {
                    fragment = ListFragment.newInstance(ListFragment.FAVORITES);
                }
                break;

            default:
                return false;
        }

        setFragment(fragment, tag);
        return true;
    };

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void showTranslation(Translation translation) {
        TranslateFragment homeFrag = (TranslateFragment)
                getSupportFragmentManager().findFragmentByTag(HOME_TAG);
        homeFrag.showLastInHistory();
        navigation.setSelectedItemId(R.id.nav_home);
    }

}
