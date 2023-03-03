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
import com.dohro7.officemobiledtr.model.OfficeOrderModel;

import java.util.ArrayList;
import java.util.List;

public class OfficeOrderAdapter extends RecyclerView.Adapter<OfficeOrderAdapter.OfficeViewHolder> {

    private List<OfficeOrderModel> list = new ArrayList<>();

    public OfficeOrderAdapter() {

    }

    public void setList(List<OfficeOrderModel> list) {
        final OfficeOrderDiffUtilCallback diffCallback = new OfficeOrderDiffUtilCallback(this.list, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.list.clear();
        this.list.addAll(list);
        diffResult.dispatchUpdatesTo(this);
    }


    @NonNull
    @Override
    public OfficeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.so_item_layout, viewGroup, false);
        return new OfficeOrderAdapter.OfficeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfficeViewHolder officeViewHolder, int i) {
        officeViewHolder.soNumber.setText("S0# "+list.get(i).so_no);
        officeViewHolder.inclusiveDate.setText(list.get(i).inclusive_date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class OfficeViewHolder extends RecyclerView.ViewHolder {
        TextView soNumber;
        TextView inclusiveDate;

        public OfficeViewHolder(@NonNull View itemView) {
            super(itemView);
            soNumber = itemView.findViewById(R.id.so_no);
            inclusiveDate = itemView.findViewById(R.id.so_date);
        }
    }

    class OfficeOrderDiffUtilCallback extends DiffUtil.Callback {

        private final List<OfficeOrderModel> oldList;
        private final List<OfficeOrderModel> newList;

        public OfficeOrderDiffUtilCallback(List<OfficeOrderModel> oldList, List<OfficeOrderModel> newList) {
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
            final OfficeOrderModel oldData = oldList.get(oldItemPosition);
            final OfficeOrderModel newData = newList.get(newItemPosition);

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
