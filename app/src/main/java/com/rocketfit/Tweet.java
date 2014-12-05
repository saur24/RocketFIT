package com.rocketfit;

/**
 * Created by Matt on 11/19/2014.
 */
public class Tweet {
    private String status;
    private int id;

    public Tweet(String status, int id){
        super();
        this.status = status;
        this.id = id;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String text){
        this.status = text;
    }

    public int getId(){
        return id;
    }
}
