package com.example.danie.util;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ToolBarSetup {
    public static String BOL_NUMBER;
    public static String SHIPMENT_ID;
   public static String hashMD5 (String s){
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest)
                hexString.append(Integer.toHexString(0xFF & aMessageDigest));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    public static void setupToolBar(AppCompatActivity activity, int id) {
        Toolbar myChildToolbar =
                (Toolbar) activity.findViewById(id);
        activity.setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = activity.getSupportActionBar();
        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static String getUserName(Context context){
        File directory = context.getFilesDir();
        File usernameFile = new File(directory, "user_Profile.txt");
        StringBuilder username = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(usernameFile));
            String fileLine;
            while((fileLine = br.readLine()) != null){
                username.append(fileLine);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return username.toString();
    }
}
