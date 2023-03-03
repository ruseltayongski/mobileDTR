package com.dohro7.officemobiledtr.adapter;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.CtoModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CtoAdapter extends RecyclerView.Adapter<CtoAdapter.CtoViewHolder> {

    private List<CtoModel> list = new ArrayList<>();

    public CtoAdapter() {
    }

    public void setList(List<CtoModel> list) {
        final CtoDiffUtilCallback diffCallback = new CtoDiffUtilCallback(this.list, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.list.clear();
        this.list.addAll(list);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CtoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cto_item_layout, viewGroup, false);
        return new CtoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CtoViewHolder ctoViewHolder, int i) { //i = position
        ctoViewHolder.inclusiveDate.setText(list.get(i).inclusive_date);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class CtoViewHolder extends RecyclerView.ViewHolder {
        TextView inclusiveDate;

        public CtoViewHolder(@NonNull View itemView) {
            super(itemView);
            inclusiveDate = itemView.findViewById(R.id.cto_date);
        }

    }

    class CtoDiffUtilCallback extends DiffUtil.Callback {

        private final List<CtoModel> oldList;
        private final List<CtoModel> newList;

        public CtoDiffUtilCallback(List<CtoModel> oldList, List<CtoModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final CtoModel oldData = oldList.get(oldItemPosition);
            final CtoModel newData = newList.get(newItemPosition);

            return oldData.id == newData.id;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // Implement method if you're going to use ItemAnimator
            notifyItemInserted(newItemPosition);
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }

    }
}
