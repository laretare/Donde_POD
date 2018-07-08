package com.example.danie.util.models;

import java.io.Serializable;

/**
 * Created by danie on 4/15/2018.
 */

public class Origin implements Serializable{
    private String name;
    private String street1;
    private String city;
    private String address_type;
    private String state;
    private String zip;
    private String phone;
    private String company;
    private String country;

    public Origin(String name, String street1,  String city, String address_type, String phone, String state, String zip, String company, String country) {
        this.name = name;
        this.street1 = street1;
        this.city = city;
        this.address_type = address_type;
        this.state = state;
        this.zip = zip;
        this.phone = phone;
        this.company = company;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddress_type() {
        return address_type;
    }

    public void setAddress_type(String address_type) {
        this.address_type = address_type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
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

    public void setPhoneNumber(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return this.street1 + "," + this.city + "," + this.zip;
    }
}
