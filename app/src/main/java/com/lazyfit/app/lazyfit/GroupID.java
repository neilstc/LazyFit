package com.lazyfit.app.lazyfit;

public class GroupID {

    private   String name, image;

    public GroupID(){}

    public GroupID(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String group_name) {
        this.name = group_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
