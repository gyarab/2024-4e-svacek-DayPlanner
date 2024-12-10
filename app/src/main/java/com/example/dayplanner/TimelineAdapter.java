package com.example.dayplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder> {

    private Context context;
    private ArrayList task_id, task_title, task_description, task_length;

    TimelineAdapter(Context context, ArrayList task_id, ArrayList task_title, ArrayList task_description, ArrayList task_length) {
        this.context = context;
        this.task_id = task_id;
        this.task_title = task_title;
        this.task_description = task_description;
        this.task_length = task_length;
    }

    @NonNull
    @Override
    public TimelineAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.my_row, parent, false); //Converts XML layout (my_row.xml) into an actual View object.
        return new MyViewHolder(view); //This is a custom ViewHolder, which holds references to the TextViews in my_row.
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineAdapter.MyViewHolder holder, int position) {
        //onBindViewHolder() binds data to the views in each item:
        holder.task_id_txt.setText(String.valueOf(task_id.get(position))); //ets the text for task_id_txt with the task ID at the current position in task_id
        holder.task_title_txt.setText(String.valueOf(task_title.get(position)));
        holder.task_description_txt.setText(String.valueOf(task_description.get(position)));
        holder.task_length_txt.setText(String.valueOf(task_length.get(position)));
    }

    @Override
    public int getItemCount() {
        //Returns the total number of items that the RecyclerView should display, which is the size of task_id (assuming all lists are the same size)
        return task_id.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView task_id_txt, task_title_txt, task_description_txt, task_length_txt;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            task_id_txt = itemView.findViewById(R.id.task_id_txt);
            task_title_txt = itemView.findViewById(R.id.task_title_txt);
            task_description_txt = itemView.findViewById(R.id.task_description_txt);
            task_length_txt = itemView.findViewById(R.id.task_length_txt);
        }
    }
}
