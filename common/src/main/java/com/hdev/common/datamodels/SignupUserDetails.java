package com.hdev.common.datamodels;

public class SignupUserDetails {
    private String name;
    private String image;
    private String email;

    public SignupUserDetails() {
    }

    public SignupUserDetails(String name, String image, String email) {
        this.name = name;
        this.image = image;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
