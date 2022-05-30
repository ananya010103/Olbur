package com.example.myapplication;

public class Driver {
    private String Driver_id;
    private String Driver_name;
    private String Vehicle_id;

    public Driver(){ };

    public Driver(String Driver_id,String Driver_name,String Vehicle_id)
    {
        this.Driver_id = Driver_id;
        this.Driver_name = Driver_id;
        this.Vehicle_id = Driver_id;


    }

    public String getDriver_id() {
        return Driver_id;
    }

    public String getDriver_name() {
        return Driver_name;
    }

    public String getDriver_Vehicle_id() {
        return Vehicle_id;
    }

    public void setDriver_id(String driver_id) {
        Driver_id = driver_id;
    }

    public void setDriver_name(String driver_name) {
        Driver_name = driver_name;
    }

    public void setDriver_Vehicle_id(String vehicle_id) { Vehicle_id = vehicle_id; }
}
