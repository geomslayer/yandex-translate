package com.geomslayer.ytranslate;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geomslayer.ytranslate.storage.Language;

import java.util.ArrayList;

import io.realm.RealmResults;

public class LanguageFragment extends DialogFragment
        implements LanguageAdapter.OnItemClickListener {

    private static final String TITLE = "title";
    private static final String TYPE = "type";

    RecyclerView recyclerView;
    LanguageAdapter adapter;
    TextView title;

    public static LanguageFragment newInstance(String title, int type) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putInt(TYPE, type);
        LanguageFragment fragment = new LanguageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_language, container, false);

        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        title = (TextView) fragmentView.findViewById(R.id.title);

        title.setText(getArguments().getString(TITLE));
        initRecyclerView();

        return fragmentView;
    }

    private void initRecyclerView() {
        RealmResults<Language> entries;
        ArrayList<Language> dataset = new ArrayList<>();
        entries = BaseApp.getRealm().where(Language.class)
                .findAllSorted(Language.Field.name);
        for (Language entry : entries) {
            dataset.add(entry);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LanguageAdapter(this);
        adapter.setDataset(dataset);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(int position) {
        LanguageDialogListener listener = (LanguageDialogListener) getTargetFragment();
        int type = getArguments().getInt(TYPE);
        listener.onLanguageSelected(adapter.getDataset().get(position), type);
        dismiss();
    }

    public interface LanguageDialogListener {
        void onLanguageSelected(Language language, int type);
    }

}
