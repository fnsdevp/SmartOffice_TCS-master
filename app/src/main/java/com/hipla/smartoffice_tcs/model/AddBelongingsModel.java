package com.hipla.smartoffice_tcs.model;

public class AddBelongingsModel {

    String name, value ;

    public AddBelongingsModel(){}

    public AddBelongingsModel(String name , String value ){

        this.name = name ;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
