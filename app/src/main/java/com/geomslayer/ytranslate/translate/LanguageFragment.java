package com.geomslayer.ytranslate.translate;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatDialogFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.R;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Language;

import java.util.ArrayList;

public class LanguageFragment extends MvpAppCompatDialogFragment
        implements DialogView, LanguageAdapter.OnItemClickListener {

    private static final String TITLE = "title";
    private static final String TYPE = "type";

    @InjectPresenter
    DialogPresenter presenter;

    @ProvidePresenter
    DialogPresenter providePresenter() {
        DaoSession session = ((BaseApp) getActivity().getApplication()).getDaoSession();
        return new DialogPresenter(session);
    }

    LanguageDialogListener parent;

    RecyclerView recyclerView;
    LanguageAdapter adapter;
    TextView title;
    EditText searchField;

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
        super.onResume();

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_language, container, false);

        parent = (LanguageDialogListener) getTargetFragment();

        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        title = (TextView) fragmentView.findViewById(R.id.title);
        searchField = (EditText) fragmentView.findViewById(R.id.search);

        title.setText(getArguments().getString(TITLE));
        initRecyclerView();
        initSearchBar();

        return fragmentView;
    }

    private void initSearchBar() {
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                presenter.filterLanguages(editable.toString());
            }
        });
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LanguageAdapter(this);
        recyclerView.setAdapter(adapter);
        presenter.fetchLanguages();
    }

    @Override
    public void onClick(int position) {
        LanguageDialogListener listener = (LanguageDialogListener) getTargetFragment();
        int type = getArguments().getInt(TYPE);
        listener.onLanguageSelected(adapter.getDataset().get(position), type);
        dismiss();
    }

    @Override
    public void showLanguages(ArrayList<Language> langs) {
        adapter.setDataset(langs);
        adapter.notifyDataSetChanged();
    }

    public interface LanguageDialogListener {
        void onLanguageSelected(Language language, int type);
    }

}
