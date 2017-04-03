package com.geomslayer.ytranslate;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geomslayer.ytranslate.storage.Translation;

import java.util.ArrayList;

public class TranslationAdapter extends RecyclerView.Adapter<TranslationAdapter.ViewHolder> {

    private final AdapterListener listener;

    private ArrayList<Translation> dataset;

    public ArrayList<Translation> getDataset() {
        return dataset;
    }

    public TranslationAdapter(@NonNull AdapterListener listener) {
        this.listener = listener;
    }

    public void setDataset(ArrayList<Translation> dataset) {
        this.dataset = dataset;
        notifyDataSetHasChanged();
    }

    public void notifyDataSetHasChanged() {
        listener.changeNotificationVisibility(dataset.isEmpty());
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_translation, parent, false);
        return new ViewHolder(view, listener);
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
        ImageView favorite;
        TextView languages;

        public ViewHolder(View itemView, final AdapterListener listener) {
            super(itemView);

            rawText = (TextView) itemView.findViewById(R.id.rawText);
            translationTextView = (TextView) itemView.findViewById(R.id.translation);
            favorite = (ImageView) itemView.findViewById(R.id.favoriteButton);
            languages = (TextView) itemView.findViewById(R.id.languages);

            favorite.setOnClickListener(view -> listener.onFavoriteClick(getAdapterPosition()));
            itemView.setOnClickListener(view -> listener.onItemClick(getAdapterPosition()));
            itemView.setOnLongClickListener(view -> {
                listener.onItemLongClick(getAdapterPosition());
                return true;
            });
        }

        public void bindTranslation(Translation translation) {
            rawText.setText(translation.getRawText());
            translationTextView.setText(translation.getTranslation());
            languages.setText(translation.getLangFrom() + " - " + translation.getLangTo());
            if (translation.isInFavorites()) {
                favorite.setImageResource(R.drawable.ic_favorite_active);
            } else {
                favorite.setImageResource(R.drawable.ic_favorite_inactive);
            }
        }

    }

    interface AdapterListener {
        void onFavoriteClick(int position);

        void onItemClick(int position);

        void onItemLongClick(int position);

        void changeNotificationVisibility(boolean visible);
    }

}
