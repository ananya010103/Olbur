package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button button_nxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        button_nxt = (Button) findViewById(R.id.button_nxt);
        button_nxt.setOnClickListener(v -> openNewActivity());
    }
    public void openNewActivity(){
        Intent intent = new Intent(this, Activity1.class);
        startActivity(intent);
        finish();
    }
}