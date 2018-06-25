package com.example.danie.techedgebarcode.driver;

import java.io.Serializable;

/**
 * Created by danie on 4/15/2018.
 */

public class Driver implements Serializable {
    private String firstName;
    private String lastName;
    private  String phone_number;
    public Driver(){

    }
    public Driver(String firstName, String lastName, String phone_number) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone_number = phone_number;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getPhonenumber() {
        return phone_number;
    }

    public void setPhonenumber(String phonenumber) {
        this.phone_number = phonenumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
