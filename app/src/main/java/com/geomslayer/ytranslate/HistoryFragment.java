package com.geomslayer.ytranslate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geomslayer.ytranslate.storage.Translation;

import io.realm.Realm;
import io.realm.RealmResults;

public class HistoryFragment extends Fragment {

    private RecyclerView recycler;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        Log.d("!!!!!!!!!!", "onCreateView: !!!!!!!");

        recycler = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        initRecyclerView();

        return fragmentView;
    }

    private void initRecyclerView() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Translation> dataset = realm.where(Translation.class).findAll();
        HistoryAdapter adapter = new HistoryAdapter();
        adapter.setDataset(dataset);
        recycler.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL);
        recycler.addItemDecoration(decoration);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

}
