package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.geomslayer.ytranslate.storage.Translation;

public class MainActivity extends AppCompatActivity
        implements ListFragment.Callback {

    public static final String HOME_TAG = "home";
    public static final String HISTORY_TAG = "history";
    public static final String FAVORITES_TAG = "favorites";

    private BottomNavigationView navigation;
    private Translation currentTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragManager = getSupportFragmentManager();
            Fragment fragment;
            String tag;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    tag = HOME_TAG;
                    fragment = fragManager.findFragmentByTag(HOME_TAG);
                    if (fragment == null) {
                        Log.d("MA", "new fragment!");
                        fragment = TranslateFragment.newInstance();
                    }
                    break;

                case R.id.nav_history:
                    tag = HISTORY_TAG;
                    fragment = fragManager.findFragmentByTag(HISTORY_TAG);
                    if (fragment == null) {
                        fragment = ListFragment.newInstance(ListFragment.HISTORY);
                    }
                    break;

                case R.id.nav_favorites:
                    tag = FAVORITES_TAG;
                    fragment = fragManager.findFragmentByTag(FAVORITES_TAG);
                    if (fragment == null) {
                        fragment = ListFragment.newInstance(ListFragment.FAVORITES);
                    }
                    break;

                default:
                    return false;
            }

            setFragment(fragment, tag);
            return true;
        }

    };

    private void setFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment, tag)
                .commit();
    }

    @Override
    public void showTranslation(Translation translation) {
        TranslationUtils.saveInSharedPreferences(this, translation);
        navigation.setSelectedItemId(R.id.nav_home);
    }

}
