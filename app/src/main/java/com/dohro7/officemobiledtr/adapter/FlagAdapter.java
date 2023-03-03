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
import com.dohro7.officemobiledtr.model.FlagModel;
import com.dohro7.officemobiledtr.utility.DateTimeUtility;

import java.util.ArrayList;
import java.util.List;

public class FlagAdapter extends RecyclerView.Adapter<FlagAdapter.FlagViewHolder> {

    private List<FlagModel> list = new ArrayList<>();

    public FlagAdapter(){}

    public void setList(List<FlagModel> list){
        final FlagDiffUtilCallback diffCallback = new FlagAdapter.FlagDiffUtilCallback(this.list, list);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.list.clear();
        this.list.addAll(list);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public FlagViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flag_item_layout, viewGroup, false);
        return new FlagAdapter.FlagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlagAdapter.FlagViewHolder flagViewHolder, int i)
    {
        int year = Integer.parseInt(list.get(i).date.split("-")[0]);
        int month = Integer.parseInt(list.get(i).date.split("-")[1]);
        int day = Integer.parseInt(list.get(i).date.split("-")[2]);
        flagViewHolder.date.setText(DateTimeUtility.getCurrentDateString(year, month, day));

        String remark = list.get(i).remarks;
        if(remark!=null){
            if(remark.trim().equalsIgnoreCase("FLAG_MORNING")){
                flagViewHolder.remark.setText("FLAG CEREMONY");
            }else if(remark.trim().equalsIgnoreCase("FLAG_RETREAT")){
                flagViewHolder.remark.setText("FLAG RETREAT");
            }
        }

        flagViewHolder.time.setText(list.get(i).time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class FlagViewHolder extends RecyclerView.ViewHolder
    {
        TextView remark;
        TextView date;
        TextView time;

        public FlagViewHolder(@NonNull View itemView)
        {
            super(itemView);
            remark = itemView.findViewById(R.id.flag_remark);
            date = itemView.findViewById(R.id.flag_date);
            time = itemView.findViewById(R.id.flag_time);
        }
    }

    class FlagDiffUtilCallback extends DiffUtil.Callback
    {

        private final List<FlagModel> oldList;
        private final List<FlagModel> newList;

        public FlagDiffUtilCallback(List<FlagModel> oldList, List<FlagModel> newList)
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
            final FlagModel oldData = oldList.get(oldItemPosition);
            final FlagModel newData = newList.get(newItemPosition);

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
