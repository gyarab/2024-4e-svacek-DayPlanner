package com.example.dayplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddTaskActivity extends AppCompatActivity {

    EditText title_input, description_input, length_input;
    Button add_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_task);

        title_input = findViewById(R.id.title_input);
        description_input = findViewById(R.id.description_input);
        length_input = findViewById(R.id.length_input);
        add_button = findViewById(R.id.add_button);

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimelineDbHelper timelineDbHelper = new TimelineDbHelper(AddTaskActivity.this);
                timelineDbHelper.addTask(
                        title_input.getText().toString().trim(),
                        description_input.getText().toString().trim(),
                        Integer.parseInt(length_input.getText().toString().trim())
                );
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
}