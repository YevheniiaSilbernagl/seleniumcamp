package com.seleniumcamp.endpoint.demo;

import com.google.gson.JsonParser;
import com.jayway.restassured.authentication.AuthenticationScheme;
import com.jayway.restassured.authentication.BasicAuthScheme;
import com.jayway.restassured.internal.RequestSpecificationImpl;
import com.jayway.restassured.internal.ResponseSpecificationImpl;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Step;

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

    @Attachment(value = "{0}: {1}", type = "text/plain")
    public static String saveRequest(HttpMethod method, String requestUrl, RequestSender sender) {
        String params = "";
        RequestSpecificationImpl spec = null;
        if (sender instanceof RequestSpecificationImpl) {
            spec = (RequestSpecificationImpl) sender;
        } else if (sender instanceof ResponseSpecificationImpl) {
            spec = (RequestSpecificationImpl) ((ResponseSpecificationImpl) sender).given();
        }
        if (spec == null) return method.getName() + ": " + requestUrl;
        for (Header header : spec.getHeaders()) {
            params += header.getName() + ": " + header.getValue() + "\n";
        }
        params += (spec.getBody() == null) ? "" : ("Body: " + spec.getBody() + "\n");
        params += (spec.getPathParams() == null || spec.getPathParams().isEmpty()) ? "" : ("Path params: " + spec.getPathParams() + "\n");
        params += (spec.getFormParams() == null || spec.getFormParams().isEmpty()) ? "" : ("Form params: " + spec.getFormParams() + "\n");
        params += (spec.getRequestParams() == null || spec.getRequestParams().isEmpty()) ? "" : ("Request params: " + spec.getRequestParams() + "\n");
        params += (spec.getMultiPartParams() == null || spec.getMultiPartParams().isEmpty()) ? "" : ("Multipart params: " + spec.getMultiPartParams() + "\n");
        params += (spec.getCookies() == null || spec.getCookies().size() > 0) ? "" : ("Cookies: " + spec.getCookies() + "\n");
        AuthenticationScheme scheme = spec.getAuthenticationScheme();
        if (scheme != null && scheme instanceof BasicAuthScheme) {
            BasicAuthScheme auth = ((BasicAuthScheme) scheme);
            params += "Basic authentication: " + auth.getUserName() + "/" + auth.getPassword();
        }
        return method.getName() + ": " + requestUrl + "\n" + params;
    }

    @Step("'{method}'")
    public Response get() {
        RequestSender sender = this.getRequestSpecification();
        saveRequest(HttpMethod.GET, this.getUrl(), sender);
        Response response = sender.get(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Step("'{method} {0}'")
    public Response get(Function<RequestSpecification, RequestSender> whatToDo) {
        RequestSender sender = whatToDo.apply(this.getRequestSpecification());
        saveRequest(HttpMethod.GET, this.getUrl(), sender);
        Response response = sender.get(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Step("'{method}'")
    public Response post() {
        RequestSender sender = this.getRequestSpecification();
        saveRequest(HttpMethod.POST, this.getUrl(), sender);
        Response response = sender.post(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Step("'{method} {0}'")
    public Response post(Function<RequestSpecification, RequestSender> whatToDo) {
        RequestSender sender = whatToDo.apply(this.getRequestSpecification());
        saveRequest(HttpMethod.POST, this.getUrl(), sender);
        Response response = sender.post(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Step("'{method} {0}'")
    public Response put(Function<RequestSpecification, RequestSender> whatToDo) {
        RequestSender sender = whatToDo.apply(this.getRequestSpecification());
        saveRequest(HttpMethod.PUT, this.getUrl(), sender);
        Response response = sender.put(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Step("'{method}'")
    public Response put() {
        RequestSender sender = this.getRequestSpecification();
        saveRequest(HttpMethod.PUT, this.getUrl(), sender);
        Response response = sender.put(this.getUrl());
        saveResponse(response.getBody().asString());
        return response;
    }

    @Override
    public String toString() {
        return getUrl();
    }
}
