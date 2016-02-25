package com.seleniumcamp.endpoint.demo;

/**
 * Created by sivashchenko on 24.06.15
 */

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    POST_MULTY ("POST_MULTY"),
    POST_PLAIN("POST_PLAIN"),
    DELETE("DELETE"),
    JSON("JSON"),
    RAW_FILE("RAW_FILE");

    private String name;

    HttpMethod(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
