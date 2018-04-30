package com.hipla.smartoffice_tcs.model;

public class AddRecipientsModel {

    String name, email ;

    public AddRecipientsModel(){}

    public AddRecipientsModel(String name , String email ){

        this.name = name ;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }
}
