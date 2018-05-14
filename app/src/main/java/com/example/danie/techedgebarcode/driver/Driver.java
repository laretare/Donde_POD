package com.example.danie.techedgebarcode.driver;

import java.io.Serializable;

/**
 * Created by danie on 4/15/2018.
 */

public class Driver implements Serializable {
    private String name;
    private  String phone_number;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phone_number;
    }

    public void setPhonenumber(String phonenumber) {
        this.phone_number = phonenumber;
    }
}
