package com.example.tsult.sparrow;

import java.io.Serializable;

public class checkItem implements Serializable{
    private boolean isChecked;
    private String userId;

    public checkItem() {
    }

    public checkItem(boolean isChecked, String userId) {
        this.isChecked = isChecked;
        this.userId = userId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

