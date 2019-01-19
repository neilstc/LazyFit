package com.lazyfit.app.lazyfit;

public class Contacts {

    private   String name, aboutMe , image;

    public Contacts(){

    }
    public Contacts(String name, String aboutMe, String image) {
        this.name = name;
        this.aboutMe = aboutMe;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

