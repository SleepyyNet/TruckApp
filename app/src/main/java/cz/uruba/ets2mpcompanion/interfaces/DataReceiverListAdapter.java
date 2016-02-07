package cz.uruba.ets2mpcompanion.interfaces;

import android.content.Context;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cz.uruba.ets2mpcompanion.R;
import cz.uruba.ets2mpcompanion.adapters.viewholders.EmptyViewHolder;
import cz.uruba.ets2mpcompanion.adapters.viewholders.LastUpdatedViewHolder;
import cz.uruba.ets2mpcompanion.views.LastUpdatedTextView;

public abstract class DataReceiverListAdapter<T, U extends List<T>> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_DATA_ENTRY = 0;
    protected static final int TYPE_LAST_UPDATED = 1;
    protected static final int TYPE_FOOTER = 2;

    protected Context context;

    protected DataReceiver<?> callbackDataReceiver;

    protected U dataCollection;

    protected LastUpdatedTextView lastUpdatedTextView;

    public DataReceiverListAdapter(Context context, U dataCollection, DataReceiver<?> callbackDataReceiver) {
        this.context = context;
        this.dataCollection = dataCollection;
        this.callbackDataReceiver = callbackDataReceiver;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LAST_UPDATED:
                View itemView = LayoutInflater
                        .from(context)
                        .inflate(R.layout.block_lastupdated, parent, false);

                lastUpdatedTextView = (LastUpdatedTextView) itemView.findViewById(R.id.last_updated);

                return new LastUpdatedViewHolder(itemView);

            case TYPE_FOOTER:
                Space emptyView = new Space(context);
                emptyView.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 84, context.getResources().getDisplayMetrics())
                        )
                );

                return new EmptyViewHolder(emptyView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_LAST_UPDATED:
                LastUpdatedViewHolder lastUpdatedViewHolder = (LastUpdatedViewHolder) holder;
                lastUpdatedViewHolder.lastUpdated.setTime(callbackDataReceiver.getLastUpdated());
                break;
        }
    }

    public void restartLastUpdatedTextView() {
        if (lastUpdatedTextView != null) {
            lastUpdatedTextView.restartAutoRefresh();
        }
    }

    public U getDataCollection() {
        return dataCollection;
    }

    public void setDataCollection(U newCollection) {
        for (int i = dataCollection.size() - 1; i >= 0; i--) {
            T originalItem = dataCollection.get(i);
            if (!newCollection.contains(originalItem)) {
                removeItem(i);
            }
        }

        for (int i = 0, count = newCollection.size(); i < count; i++) {
            T newItem = newCollection.get(i);
            if (!dataCollection.contains(newItem)) {
                addItem(i, newItem);
            }
        }
    }

    public void removeItem(int position) {
        dataCollection.remove(position);
        notifyItemRemoved(position + 1);
    }

    public void addItem(int position, T newItem) {
        dataCollection.add(position, newItem);
        notifyItemInserted(position + 1);
    }

    public int getDataCollectionSize() {
        return dataCollection.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_LAST_UPDATED;
        } else if (position == getDataCollectionSize() + 1) {
            return TYPE_FOOTER;
        }

        return TYPE_DATA_ENTRY;
    }

    @Override
    public int getItemCount() {
        return getDataCollectionSize() + 2;
    }
}
