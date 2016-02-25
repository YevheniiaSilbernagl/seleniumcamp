package com.seleniumcamp.endpoint.demo;

import com.jayway.restassured.specification.RequestSpecification;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by TJ on 25.02.2016.
 */
public class GoogleEndPoint extends EndPoint {
    public GoogleEndPoint(String url) {
        super(url);
        serviceUrl = "https://www.google.com.ua";
    }

    public GoogleEndPoint() {
        super("");
        serviceUrl = "https://www.google.com.ua";
    }

    @Override
    public RequestSpecification getRequestSpecification() {
        return given();
    }
}
