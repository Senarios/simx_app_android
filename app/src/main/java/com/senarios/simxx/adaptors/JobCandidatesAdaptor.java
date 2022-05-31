package com.senarios.simxx.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.senarios.simxx.R;
import com.senarios.simxx.databinding.ItemJobCandidatesBinding;
import com.hdev.common.datamodels.JobCandidates;

import java.util.ArrayList;
import java.util.List;

public class JobCandidatesAdaptor extends RecyclerView.Adapter<JobCandidatesAdaptor.ViewHolder> {
    private Context context;
    private List<JobCandidates> arraylist=new ArrayList<>();
    private RecyclerViewCallback listener;

    public JobCandidatesAdaptor(Context context, List<JobCandidates> arraylist, RecyclerViewCallback listener) {
        this.context = context;
        this.arraylist = arraylist;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_job_candidates,parent,false);
        ItemJobCandidatesBinding binding= DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.setModel(arraylist.get(position).getUser());

    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public List<JobCandidates> getData() {
        return arraylist;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ItemJobCandidatesBinding binding;

        public ViewHolder(@NonNull ItemJobCandidatesBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
            binding.options.setOnClickListener(this);
            binding.imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.options:
                    listener.onItemOptions(getAdapterPosition(),arraylist.get(getAdapterPosition()),binding.root);
                    break;
                case R.id.imageView:
                    listener.onItemButtonClick(getAdapterPosition(),arraylist.get(getAdapterPosition()));
                    break;

            }

        }
    }
}
