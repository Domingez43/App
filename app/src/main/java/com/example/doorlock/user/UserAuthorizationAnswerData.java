package com.example.doorlock.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserAuthorizationAnswerData implements Serializable {

    @SerializedName("ID")
    private Integer id;

    @SerializedName("MAC")
    private String mac;

    @SerializedName("PASS")
    private String pass;

    @SerializedName("ACCESS_LEVEL")
    private int accessLevel;

    @SerializedName("DESCRIPTION")
    private String description;


    public void setId(Integer id) {
        this.id = id;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public String getMac() {
        return mac;
    }

    public String getPass() {
        return pass;
    }

    public int getAccessLvl() {
        return accessLevel;
    }

    public String getDescription() {
        return description;
    }
}
