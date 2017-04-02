package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.geomslayer.ytranslate.storage.Translation;

public class MainActivity extends AppCompatActivity
        implements ListFragment.Callback {

    private BottomNavigationView navigation;

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
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragment = TranslateFragment.newInstance();
                    break;

                case R.id.nav_history:
                    fragment = ListFragment.newInstance(ListFragment.HISTORY);
                    break;

                case R.id.nav_favorites:
                    fragment = ListFragment.newInstance(ListFragment.FAVORITES);
                    break;

                default:
                    return false;
            }

            setFragment(fragment);
            return true;
        }

    };

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    @Override
    public void showTranslation(Translation translation) {
        TranslationUtils.saveInSharedPreferences(this, translation);
        navigation.setSelectedItemId(R.id.nav_home);
    }

}
