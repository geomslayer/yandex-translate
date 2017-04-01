package com.geomslayer.ytranslate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.geomslayer.ytranslate.storage.Translation;

import java.util.ArrayList;

import io.realm.RealmResults;
import io.realm.Sort;

public class ListFragment extends Fragment
        implements TranslationAdapter.FavoriteClickListener,
        TranslationAdapter.ItemClickListener,
        AlertFragment.DialogListener {

    private static final String TYPE = "type";
    public static final int HISTORY = 0;
    public static final int FAVORITES = 1;

    public static final String LAST_QUERY = "last_query";

    private Toolbar toolbar;
    private RecyclerView recycler;
    private TranslationAdapter adapter;

    private Callback callback;

    public static ListFragment newInstance(int type) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            throw new UnsupportedOperationException("Context must implement Callback!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        toolbar = (Toolbar) fragmentView.findViewById(R.id.toolbar);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);

        initToolbar();
        initRecyclerView();

        return fragmentView;
    }

    private void initRecyclerView() {
        RealmResults<Translation> entries;
        ArrayList<Translation> dataset = new ArrayList<>();
        if (getArguments().getInt(TYPE) == HISTORY) {
            entries = BaseApp.getRealm().where(Translation.class)
                    .equalTo(Translation.Field.inHistory, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        } else {
            entries = BaseApp.getRealm().where(Translation.class)
                    .equalTo(Translation.Field.inFavorites, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        }
        for (Translation entry : entries) {
            dataset.add(entry);
        }

        adapter = new TranslationAdapter(this, this);
        adapter.setDataset(dataset);
        recycler.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(decoration);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void initToolbar() {
        final String title;
        if (getArguments().getInt(TYPE) == HISTORY) {
            title = getString(R.string.history);
        } else {
            title = getString(R.string.favorites);
        }
        toolbar.setTitle(title);
        toolbar.inflateMenu(R.menu.erase);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.erase) {
                    AlertFragment alert = AlertFragment.newInstance(title);
                    alert.setTargetFragment(ListFragment.this, 666);
                    alert.show(getFragmentManager(), "alert");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onFavoriteClick(int position) {
        BaseApp.getRealm().beginTransaction();
        Translation translation = adapter.getDataset().get(position);
        translation.setInFavorites(!translation.isInFavorites());
        BaseApp.getRealm().commitTransaction();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClick(int position) {
        Translation translation = adapter.getDataset().get(position);
        callback.showTranslation(translation);
    }

    @Override
    public void onPositiveButtonClick() {
        BaseApp.getRealm().beginTransaction();
        for (Translation translation : adapter.getDataset()) {
            translation.deleteFromRealm();
        }
        BaseApp.getRealm().commitTransaction();
        adapter.getDataset().clear();
        adapter.notifyDataSetChanged();
    }

    public interface Callback {
        void showTranslation(Translation translation);
    }

}
