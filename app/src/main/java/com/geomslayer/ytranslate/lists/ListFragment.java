package com.geomslayer.ytranslate.lists;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.geomslayer.ytranslate.BaseApp;
import com.geomslayer.ytranslate.R;
import com.geomslayer.ytranslate.RequestHandler;
import com.geomslayer.ytranslate.models.DaoSession;
import com.geomslayer.ytranslate.models.Translation;
import com.geomslayer.ytranslate.models.TranslationDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class ListFragment extends Fragment
        implements AlertFragment.DialogListener,
        TranslationAdapter.AdapterListener,
        RequestHandler.OnRequestReadyListener {

    private static final String TYPE = "type";
    public static final int HISTORY = 0;
    public static final int FAVORITES = 1;

    private Toolbar toolbar;
    private RecyclerView recycler;
    private TranslationAdapter adapter;
    private ViewGroup placeholderView;
    private EditText searchBar;
    private ImageView clearButton;

    private Callback callback;
    private RequestHandler requestHandler;

    private TranslationDao translationDao;

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
        View fragmentView = inflater.inflate(R.layout.fragment_lists, container, false);

        requestHandler = new RequestHandler(this);

        DaoSession session = ((BaseApp) getActivity().getApplication()).getDaoSession();
        translationDao = session.getTranslationDao();

        toolbar = (Toolbar) fragmentView.findViewById(R.id.toolbar);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        placeholderView = (ViewGroup) fragmentView.findViewById(R.id.placeholder);
        searchBar = (EditText) fragmentView.findViewById(R.id.search);
        clearButton = (ImageView) fragmentView.findViewById(R.id.clearButton);

        initToolbar();
        initRecyclerView();
        initPlaceholder();
        initSearchBar();

        return fragmentView;
    }

    private void initSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (requestHandler.isCancelled()) {
                    requestHandler = new RequestHandler(ListFragment.this);
                }
                if (editable.toString().isEmpty()) {
                    clearButton.setVisibility(View.GONE);
                } else {
                    clearButton.setVisibility(View.VISIBLE);
                }
                requestHandler.doFakeRequest();
            }
        });
        clearButton.setOnClickListener(view -> searchBar.setText(""));
    }

    private void initPlaceholder() {
        TextView message = (TextView) placeholderView.findViewById(R.id.placeholderMessage);
        ImageView icon = (ImageView) placeholderView.findViewById(R.id.placeholderIcon);
        String mask = getString(R.string.noTranslations);
        String screenName;
        if (getArguments().getInt(TYPE) == FAVORITES) {
            icon.setImageResource(R.drawable.ic_stars_grey);
            screenName = getString(R.string.favorites).toLowerCase();
        } else {
            screenName = getString(R.string.history).toLowerCase();
        }
        message.setText(String.format(mask, screenName));
    }

    private void initRecyclerView() {
        QueryBuilder<Translation> query = translationDao.queryBuilder();
        if (getArguments().getInt(TYPE) == HISTORY) {
            query.where(TranslationDao.Properties.InHistory.eq(true));
        } else {
            query.where(TranslationDao.Properties.InFavorites.eq(true));
        }
        List<Translation> entries = query.orderDesc(TranslationDao.Properties.Moment).list();
        ArrayList<Translation> dataset = new ArrayList<>(entries);

        adapter = new TranslationAdapter(this);
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
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.erase) {
                AlertFragment alert = AlertFragment.newInstance(title);
                alert.setTargetFragment(ListFragment.this, 666);
                alert.show(getFragmentManager(), "alert");
                return true;
            }
            return false;
        });
    }

    @Override
    public void onFavoriteClick(int position) {
        Translation translation = adapter.getDataset().get(position);
        translation.setInFavorites(!translation.isInFavorites());
        translationDao.update(translation);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClick(int position) {
        Translation translation = adapter.getDataset().get(position);
        translation.setMoment(Calendar.getInstance().getTime());
        translation.setInHistory(true);
        translationDao.update(translation);
        callback.showTranslation(translation);
    }

    @Override
    public void onItemLongClick(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{getString(R.string.delete)}, (dialog, which) -> {
            final int type = getArguments().getInt(TYPE);
            Translation translation = adapter.getDataset().get(position);
            if (type == HISTORY) {
                translation.setInHistory(false);
            } else {
                translation.setInFavorites(false);
            }
            translationDao.update(translation);
            adapter.getDataset().remove(position);
            if (adapter.getDataset().isEmpty()) {
                adapter.notifyDataSetHasChanged();
            } else {
                adapter.notifyItemRemoved(position);
            }
        });
        builder.show();
    }

    @Override
    public void onPause() {
        requestHandler.cancel(true);
        super.onPause();
    }

    @Override
    public void setPlaceholderVisibility(boolean visible) {
        placeholderView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPositiveButtonClick() {
        final int type = getArguments().getInt(TYPE);
        for (Translation translation : adapter.getDataset()) {
            if (type == HISTORY) {
                translation.setInHistory(false);
            } else {
                translation.setInFavorites(false);
            }
            translationDao.update(translation);
        }
        adapter.getDataset().clear();
        adapter.notifyDataSetHasChanged();
    }

    @Override
    public void doRealRequest() {
        String query = "%" + searchBar.getText().toString().trim() + "%";
        QueryBuilder<Translation> builder = translationDao.queryBuilder()
                .whereOr(TranslationDao.Properties.SourceText.like(query),
                        TranslationDao.Properties.TranslatedText.like(query))
                .orderDesc(TranslationDao.Properties.Moment);
        if (getArguments().getInt(TYPE) == HISTORY) {
            builder.where(TranslationDao.Properties.InHistory.eq(true));
        } else {
            builder.where(TranslationDao.Properties.InFavorites.eq(true));
        }
        adapter.setDataset(new ArrayList<>(builder.list()));
        adapter.notifyDataSetHasChanged();
    }

    public interface Callback {
        void showTranslation(Translation translation);
    }

}
