package com.dohro7.officemobiledtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dohro7.officemobiledtr.R;
import com.dohro7.officemobiledtr.model.LeaveModel;

import java.util.ArrayList;
import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.LeaveViewHolder>
{

    private List<LeaveModel> list = new ArrayList<>(); //list of LeaveModel

    public LeaveAdapter() { }

    public void setList(List<LeaveModel> list)
    {
        final LeaveDiffUtilCallback diffCallback = new LeaveDiffUtilCallback(this.list, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.list.clear();
        this.list.addAll(list);
        diffResult.dispatchUpdatesTo(this);
    }


    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.leave_item_layout, viewGroup, false);
        return new LeaveAdapter.LeaveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder officeViewHolder, int i)
    {
        officeViewHolder.inclusiveDates.setText(list.get(i).inclusive_date);
        officeViewHolder.leaveType.setText(list.get(i).type);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    class LeaveViewHolder extends RecyclerView.ViewHolder
    {
        TextView inclusiveDates;
        TextView leaveType;

        public LeaveViewHolder(@NonNull View itemView)
        {
            super(itemView);
            inclusiveDates = itemView.findViewById(R.id.leave_from_value);
            leaveType = itemView.findViewById(R.id.leave_type_value);
        }
    }



    class LeaveDiffUtilCallback extends DiffUtil.Callback
    {

        private final List<LeaveModel> oldList;
        private final List<LeaveModel> newList;

        public LeaveDiffUtilCallback(List<LeaveModel> oldList, List<LeaveModel> newList)
        {
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
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
        {   return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;  }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
        {
            final LeaveModel oldData = oldList.get(oldItemPosition);
            final LeaveModel newData = newList.get(newItemPosition);

            return oldData.id == newData.id;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition)
        {
            // Implement method if you're going to use ItemAnimator
            notifyItemInserted(newItemPosition);
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }

    }
}
