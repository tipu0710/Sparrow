package com.example.tsult.sparrow;

/**
 * Created by tsult on 28/3/2018.
 */

public class Users {
    private String image;
    private String name;
    private String status;
    private String thumb_image;
    private String type;

    public Users() {
    }

    public Users(String image, String name, String status, String thumb_image, String type) {
        this.image = image;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
