package com.example.myapplication;

public class Driver {
    private String Driver_id;
    private String Driver_name;
    private String Driver_address;

    public Driver(){ };

    public Driver(String Driver_id,String Driver_name,String Driver_address)
    {
        this.Driver_id = Driver_id;
        this.Driver_name = Driver_id;
        this.Driver_address = Driver_id;


    }

    public String getDriver_id() {
        return Driver_id;
    }

    public String getDriver_name() {
        return Driver_name;
    }

    public String getDriver_Address() {
        return Driver_address;
    }

    public void setDriver_id(String driver_id) {
        Driver_id = driver_id;
    }

    public void setDriver_name(String driver_name) {
        Driver_name = driver_name;
    }

    public void setDriver_Address(String driver_Address) {
        Driver_address = driver_Address;
    }
}
