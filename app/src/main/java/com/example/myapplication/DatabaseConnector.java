package com.example.myapplication;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import com.google.gson.internal.LinkedTreeMap;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

public class DatabaseConnector {
    private DatabaseReference driverDB;
    private DatabaseReference connectedRef;
    MainActivity fromActivity;
    String names[];
    int i=0;
    List<Driver> drivers;

    /*
    public interface DataStatus{
        void DataIsLoaded(List<Driver> drivers, List<String> keys);
        void DataIsInserted();
        void DataIsUpdated();
        void dataIsDeleted();
    }
    */

    DatabaseConnector(MainActivity activity) {

        fromActivity = activity;

        /*
        URL url = null;
        try {
            url = new URL("https://olbur-d1df7-default-rtdb.asia-southeast1.firebasedatabase.app");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        driverDB = FirebaseDatabase.getInstance(url.toString()).getReference("Drivers");
        connectedRef = FirebaseDatabase.getInstance(url.toString()).getReference(".info/connected");
        driverDB.keepSynced(true);
        */

    }

    public DatabaseReference getDriverDB() {
        return driverDB;
    }

    public DatabaseReference getConnectedRef() {
        return connectedRef;
    }

    public String init(){
        Utils utils = new Utils();
        String results = utils.getJsonFromAssets(fromActivity.getApplicationContext(), "database.json");
        return results;
    }

    public String[] getDriverNames(String jsonFileString) {
        int i=0;
        Log.i("JSON content: ", jsonFileString);
        Gson gson = new GsonBuilder().create();

        Type DriverType = new TypeToken<ArrayList<Driver>>() {}.getType();
        ArrayList<Driver> drivers = gson.fromJson(jsonFileString, DriverType);
        Log.d("Database", "Number of drivers in the database" + drivers.size());
        names = new String[drivers.size()];
        for (int count=0; count<drivers.size(); count++) {
            Driver driver = (Driver) drivers.get(count);
            names[count] = driver.getDriver_name();
            Log.d("Database", "Driver name is " + driver.getDriver_name());
        }

        /*
        // The onComplete & onAddValue listeners are not getting invoked due to the following error
        // V/FA: Processing queued up service tasks: 1 followed by V/FA: Inactivity, disconnecting from AppMeasurementService
        // Following solutions from forums were tried but were not successful:
        //      1. Clearing the Android Studio cache & restart, Android mobile restart
        //      2. Forcefully reconnecting if DB connection does not exist
        //      3. Set persistence flag
        connectedRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("Connection", "connected");
                    //getDriverNames();
                } else {
                    Log.d("Connection", "Not Connected");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Stupid Error", "Connection was cancelled");
            }
        });

        getDriverDB().get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        Log.i("FIREBASE", childSnapshot.getKey());
                    }
                }
            }
        });
        */
        return names;
    }

};



