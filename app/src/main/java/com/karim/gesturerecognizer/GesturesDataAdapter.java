package com.karim.gesturerecognizer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class GesturesDataAdapter extends RecyclerView.Adapter<GesturesDataAdapter.GestureViewHolder> {

    public List<Gesture> gestures;

    public class GestureViewHolder extends RecyclerView.ViewHolder{
        private TextView name, description;

        public GestureViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            description = (TextView) view.findViewById(R.id.description);
        }
    }
    public GesturesDataAdapter(List<Gesture> gestures){
        this.gestures=gestures;
    }

    @NonNull
    @Override
    public GesturesDataAdapter.GestureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gesture_row,parent,false);
        return new GestureViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GestureViewHolder holder, int position) {

        Gesture gesture = gestures.get(position);
        holder.name.setText(gesture.getName());
        holder.description.setText(gesture.getDescription());
    }

    @Override
    public int getItemCount() {
        return gestures.size();
    }
}
