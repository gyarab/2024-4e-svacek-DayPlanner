package com.example.dayplanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder> {

    private Context context;
    private ArrayList task_id, task_start_time, task_date, task_title, task_description, task_length;

    TimelineAdapter(Context context, ArrayList task_id, ArrayList task_start_time, ArrayList task_date, ArrayList task_title, ArrayList task_description, ArrayList task_length) {
        this.context = context;
        this.task_id = task_id;
        this.task_start_time = task_start_time;
        this.task_date = task_date;
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
        String taskID = String.valueOf(task_id.get(position));
        String taskStartTime = String.valueOf(task_start_time.get(position));
        String taskDate = String.valueOf(task_date.get(position));
        String taskTitle = String.valueOf(task_title.get(position));
        String taskDescription = String.valueOf(task_description.get(position));
        String taskLength = String.valueOf(task_length.get(position));

        holder.task_start_time_txt.setText(taskStartTime); //ets the text for task_id_txt with the task ID at the current position in task_id
        holder.task_title_txt.setText(taskTitle);
        holder.task_description_txt.setText(taskDescription);
        holder.task_length_txt.setText(taskLength);
        holder.task_title_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(taskID, taskStartTime, taskDate, taskTitle, taskDescription, taskLength);
                Log.d("CLICKED", taskID + " " + taskStartTime + " " + taskDate + " " + taskTitle + " " + taskDescription + " " + taskLength);
            }
        });
    }

    public void showDetail(String id, String startTime, String taskDate, String title, String description, String length) {
        TaskDialogFragment dialogFragment = new TaskDialogFragment(true, id, startTime, taskDate, title, description, length);
        dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditTaskDialog");
    }


    @Override
    public int getItemCount() {
        //Returns the total number of items that the RecyclerView should display, which is the size of task_id (assuming all lists are the same size)
        return task_start_time.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView task_start_time_txt, task_title_txt, task_description_txt, task_length_txt;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            task_start_time_txt = itemView.findViewById(R.id.task_start_time_txt);
            task_title_txt = itemView.findViewById(R.id.task_title_txt);
            task_description_txt = itemView.findViewById(R.id.task_description_txt);
            task_length_txt = itemView.findViewById(R.id.task_length_txt);
        }
    }
}
