package com.geomslayer.ytranslate;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geomslayer.ytranslate.storage.Translation;

import io.realm.RealmResults;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private RealmResults<Translation> dataset;

    public RealmResults<Translation> getDataset() {
        return dataset;
    }

    public void setDataset(RealmResults<Translation> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_translation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTranslation(dataset.get(position));
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView rawText;
        TextView translationTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            rawText = (TextView) itemView.findViewById(R.id.rawText);
            translationTextView = (TextView) itemView.findViewById(R.id.translation);
        }

        public void bindTranslation(Translation translation) {
            rawText.setText(translation.getRawText());
            translationTextView.setText(translation.getTranslation());
        }

    }

}
