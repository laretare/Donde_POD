package com.example.danie.techedgebarcode.models;

import java.io.Serializable;

/**
 * Created by danie on 4/15/2018.
 */

public class Destination implements Serializable {
    private String name;
    private String company;
    private String street1;
    private String city;
    private String state;
    private String country;
    private String phone;
    private String email;
    private String zip;


    public Destination(String name, String company, String street1, String city, String state, String phone, String email, String zip, String country) {
        this.name = name;
        this.company = company;
        this.street1 = street1;
        this.city = city;
        this.state = state;
        this.country = country;
        this.phone = phone;
        this.email = email;
        this.zip = zip;



    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getStreet1() {
        return street1;
    }

    public void setStreet1(String street1) {
        this.street1 = street1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return this.street1 + "," + this.city + "," + this.zip;
    }
}
