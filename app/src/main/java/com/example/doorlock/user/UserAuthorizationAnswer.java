package com.example.doorlock.user;

import java.io.Serializable;

public class UserAuthorizationAnswer{

    private String status;

    private UserAuthorizationAnswerData data;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setData(UserAuthorizationAnswerData data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public UserAuthorizationAnswerData getData() {
        return data;
    }
}
