package com.example.go4lunch.ui.recyclerview;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.di.Injections;
import com.example.go4lunch.di.ViewModelFactory;
import com.example.go4lunch.models.Restaurant;
import com.example.go4lunch.models.Worker;
import com.example.go4lunch.R;
import com.example.go4lunch.viewmodel.RestaurantViewModel;
import com.example.go4lunch.viewmodel.WorkerViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

public class WorkersAdapter extends RecyclerView.Adapter<WorkersAdapter.WorkersViewHolder> {

    private final List<Worker> workers;
    private final List<Restaurant> restaurants;

    public WorkersAdapter(final List<Worker> workers, final List<Restaurant> restaurants){
        this.workers = workers;
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public WorkersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker, parent, false);
        return new WorkersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkersViewHolder holder, int position) {
        holder.bind(workers.get(position));
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    public class WorkersViewHolder extends RecyclerView.ViewHolder {

        private final ImageView picture;
        private final TextView choice;

        public WorkersViewHolder(@NonNull View itemView) {
            super(itemView);

            picture = itemView.findViewById(R.id.worker_image);
            choice = itemView.findViewById(R.id.worker_choice);
        }

        public void bind(Worker worker) {

            if (worker.getPicture() != null){
                Glide.with(picture)
                        .load(worker.getPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(picture);
            } else
                picture.setImageResource(R.drawable.ic_baseline_circle_24);

            if (!worker.getRestaurantId().isEmpty()){
                for (int i = 0; i < restaurants.size(); i++){
                    if (worker.getRestaurantId().equals(restaurants.get(i).getId())){
                        choice.setText(itemView.getContext().getString(R.string.decision,
                                worker.getName(),
                                restaurants.get(i).getName()));
                    }
                }
            }else
                choice.setText(itemView.getContext().getString(R.string.undecided, worker.getName()));
        }

    }
}