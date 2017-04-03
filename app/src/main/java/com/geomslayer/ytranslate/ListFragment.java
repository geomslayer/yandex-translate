package com.geomslayer.ytranslate;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geomslayer.ytranslate.storage.Translation;

import java.util.ArrayList;

import io.realm.RealmResults;
import io.realm.Sort;

import static com.geomslayer.ytranslate.BaseApp.getRealm;

public class ListFragment extends Fragment
        implements AlertFragment.DialogListener,
        TranslationAdapter.AdapterListener {

    private static final String TYPE = "type";
    public static final int HISTORY = 0;
    public static final int FAVORITES = 1;

    private Toolbar toolbar;
    private RecyclerView recycler;
    private TranslationAdapter adapter;
    private ViewGroup placeholderView;

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
        View fragmentView = inflater.inflate(R.layout.fragment_lists, container, false);

        toolbar = (Toolbar) fragmentView.findViewById(R.id.toolbar);
        recycler = (RecyclerView) fragmentView.findViewById(R.id.recyclerView);
        placeholderView = (ViewGroup) fragmentView.findViewById(R.id.placeholder);

        initToolbar();
        initRecyclerView();
        initPlaceholder();

        return fragmentView;
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
        RealmResults<Translation> entries;
        ArrayList<Translation> dataset = new ArrayList<>();
        if (getArguments().getInt(TYPE) == HISTORY) {
            entries = getRealm().where(Translation.class)
                    .equalTo(Translation.Field.inHistory, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        } else {
            entries = getRealm().where(Translation.class)
                    .equalTo(Translation.Field.inFavorites, true)
                    .findAllSorted(Translation.Field.moment, Sort.DESCENDING);
        }
        dataset.addAll(entries);

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
        getRealm().beginTransaction();
        Translation translation = adapter.getDataset().get(position);
        translation.setInFavorites(!translation.isInFavorites());
        getRealm().commitTransaction();
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemClick(int position) {
        Translation translation = adapter.getDataset().get(position);
        callback.showTranslation(translation);
    }

    @Override
    public void onItemLongClick(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{getString(R.string.delete)}, (dialog, which) -> {
            final int type = getArguments().getInt(TYPE);
            getRealm().beginTransaction();
            if (type == HISTORY) {
                adapter.getDataset().get(position).setInHistory(false);
            } else {
                adapter.getDataset().get(position).setInFavorites(false);
            }
            getRealm().commitTransaction();
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
    public void changeNotificationVisibility(boolean visible) {
        placeholderView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPositiveButtonClick() {
        final int type = getArguments().getInt(TYPE);
        getRealm().beginTransaction();
        for (Translation translation : adapter.getDataset()) {
            if (type == HISTORY) {
                translation.setInHistory(false);
            } else {
                translation.setInFavorites(false);
            }
            if (!translation.isInFavorites() && !translation.isInHistory()) {
                translation.deleteFromRealm();
            }
        }
        getRealm().commitTransaction();
        adapter.getDataset().clear();
        adapter.notifyDataSetHasChanged();
    }

    public interface Callback {
        void showTranslation(Translation translation);
    }

}
