package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geomslayer.ytranslate.storage.Translation;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ListFragment extends Fragment {

    private static final String TYPE = "type";
    public static final int HISTORY = 0;
    public static final int FAVORITES = 1;

    private RecyclerView recycler;

    public static ListFragment newInstance(int type) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        Toolbar toolbar = (Toolbar) fragmentView.findViewById(R.id.toolbar);
        if (getArguments().getInt(TYPE) == HISTORY) {
            toolbar.setTitle(R.string.history);
        } else {
            toolbar.setTitle(R.string.favorites);
        }

        recycler = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        initRecyclerView();

        return fragmentView;
    }

    private void initRecyclerView() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Translation> dataset;
        if (getArguments().getInt(TYPE) == HISTORY) {
            dataset = realm.where(Translation.class)
                    .equalTo(Translation.Field.inHistory, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        } else {
            dataset = realm.where(Translation.class)
                    .equalTo(Translation.Field.inFavorites, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        }
        TranslationAdapter adapter = new TranslationAdapter();
        adapter.setDataset(dataset);
        recycler.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(decoration);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

}
