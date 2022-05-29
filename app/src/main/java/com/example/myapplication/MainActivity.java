package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Random.*;

public class MainActivity extends AppCompatActivity{
    Button button_nxt;
    TextView ride_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        button_nxt = (Button) findViewById(R.id.button_nxt);
        ride_name = (TextView)findViewById(R.id.ride_name);
        button_nxt.setOnClickListener(v -> openNewActivity());

        DatabaseConnector db = new DatabaseConnector(MainActivity.this);
        String result = db.init();
        String names[] = db.getDriverNames(result);
        if (names != null){
            int allocated_driver= (int)(Math.random()*(names.length-1));
            ride_name.setText("Driver Name is: "+ names[allocated_driver]);
            Log.d("checking", "Your allocated driver is " + names[allocated_driver]);
        }
    }

    public void openNewActivity(){
        Intent intent = new Intent(this, Activity1.class);
        startActivity(intent);
        finish();
    }
}