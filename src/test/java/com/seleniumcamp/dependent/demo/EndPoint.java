package com.seleniumcamp.dependent.demo;

import com.google.gson.JsonParser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import ru.yandex.qatools.allure.annotations.Attachment;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by evgeniyat on 28.08.15
 */
@SuppressWarnings("unused")
public abstract class EndPoint {
    protected final Logger logger = Logger.getLogger(EndPoint.class);
    protected String url;
    protected String login;
    protected String password;
    protected String serviceUrl;

    public EndPoint(String url) {
        Pattern urlPattern = Pattern.compile("http(s)?\\:\\/\\/(\\w+[-.]?)*");
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.find()) {
            url = matcher.replaceAll("");
        }
        this.url = url;
        logger.info(url);
    }

    @Attachment(value = "{0}: {1}", type = "text/plain")
    public static String saveRequest(HttpMethod method, String requestUrl) {
        return method.getName() + ": " + requestUrl;//todo parameters
    }

    @Attachment(value = "{0}", type = "application/json")
    public static String saveJson(String response) {
        return response;
    }

    @Attachment(value = "{0}", type = "text/html")
    public static String saveHtml(String response) {
        return response;
    }

    @Attachment(value = "{0}", type = "text/plain")
    public static String saveText(String response) {
        return response;
    }

    public static String saveResponse(String response) {
        if (isJson(response)) return saveJson(response);
        if (isHtml(response)) return saveHtml(response);
        return saveText(response);
    }

    private static Boolean isJson(String response) {
        try {
            new JsonParser().parse(response);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private static Boolean isHtml(String response) {
        try {
            new DomSerializer(new CleanerProperties()).createDOM(new HtmlCleaner().clean(response));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public String getUrl() {
        return serviceUrl + url;
    }

    public abstract RequestSpecification getRequestSpecification();

    public Response get() {
        saveRequest(HttpMethod.GET, this.getUrl());
        Response response = this.getRequestSpecification().get(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    public Response get(Function<RequestSpecification, RequestSender> whatToDo) {
        saveRequest(HttpMethod.GET, this.getUrl());
        Response response = whatToDo.apply(this.getRequestSpecification()).get(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    public Response post() {
        saveRequest(HttpMethod.POST, this.getUrl());
        Response response = this.getRequestSpecification().post(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    public Response post(Function<RequestSpecification, RequestSender> whatToDo) {
        saveRequest(HttpMethod.POST, this.getUrl());
        Response response = whatToDo.apply(this.getRequestSpecification()).post(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    public Response put(Function<RequestSpecification, RequestSender> whatToDo) {
        saveRequest(HttpMethod.PUT, this.getUrl());
        Response response = whatToDo.apply(this.getRequestSpecification()).put(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    public Response put() {
        saveRequest(HttpMethod.PUT, this.getUrl());
        Response response = this.getRequestSpecification().put(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
