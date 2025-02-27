package com.example.dayplanner.main.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayplanner.R;
import com.example.dayplanner.main.MainActivity;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    EditText title_input, description_input, length_input;
    Button add_button;

    TextView dateTextView;
    Button pickDateButton;

    TextView timeTextView;
    Button pickTimeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        //date picker
        dateTextView = findViewById(R.id.dateTextView);
        pickDateButton = findViewById(R.id.pickDateButton);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        dateTextView.setText(String.valueOf(currentDay) + "." + String.valueOf(currentMonth + 1) + " " + String.valueOf(currentYear));

        pickDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        dateTextView.setText(String.valueOf(day) + "." + String.valueOf(month+1) + " " + String.valueOf(year));

                    }
                }, currentYear, currentMonth, currentDay);

                datePickerDialog.show();
            }
        });

        //time picker
        timeTextView = findViewById(R.id.timeTextView);
        pickTimeButton = findViewById(R.id.pickTimeButton);

        pickTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {

                        timeTextView.setText(String.valueOf(hours) + ":" + String.valueOf(minutes));
                    }
                }, 15, 25, true);

                timePickerDialog.show();
            }
        });

        //Input fields in adding new task
        title_input = findViewById(R.id.title_input);
        description_input = findViewById(R.id.description_input);
        length_input = findViewById(R.id.length_input);

        add_button = findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DB", "clicked");

                // Create Task object
                Task newTask = new Task(
                        null, // ID is null since it's a new task
                        title_input.getText().toString().trim(),
                        description_input.getText().toString().trim(),
                        formatDate(dateTextView.getText().toString().trim()),
                        timeTextView.getText().toString().trim(),
                        Integer.parseInt(length_input.getText().toString().trim()),
                        false
                );

                // Insert task into the database
                TasksDBHelper timelineDbHelper = new TasksDBHelper(AddTaskActivity.this);
                timelineDbHelper.addTask(newTask);

                Log.d("CLICKED", newTask.toString());

                // Navigate back to MainActivity
                Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Get the original padding from the XML (defined as 30dp)
            int originalLeftPadding = v.getPaddingLeft();
            int originalTopPadding = v.getPaddingTop();
            int originalRightPadding = v.getPaddingRight();
            int originalBottomPadding = v.getPaddingBottom();

            // Apply both the original padding and the system bar insets
            v.setPadding(
                    originalLeftPadding + systemBars.left,
                    originalTopPadding + systemBars.top,
                    originalRightPadding + systemBars.right,
                    originalBottomPadding + systemBars.bottom
            );

            return insets;
        });

    }

    public static String formatDate(String date) {
        String formattedDate = "";

        if (date != null && !date.isEmpty()) {
            // Split the input date by space
            String[] parts = date.split(" ");
            if (parts.length == 2) {
                // Remove the dots and concatenate the parts
                formattedDate = parts[0].replace(".", "") + parts[1];
            }
        }

        return formattedDate;
    }
}