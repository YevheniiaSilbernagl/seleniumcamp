package com.seleniumcamp.demo;

import com.jayway.restassured.internal.RequestSpecificationImpl;
import com.jayway.restassured.specification.RequestSpecification;
import com.seleniumcamp.runner.ConcurrentParametrized;
import com.seleniumcamp.runner.ConcurrentParametrizedDependent;
import org.hamcrest.Matchers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Parameter;
import ru.yandex.qatools.allure.annotations.Stories;

import java.util.*;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by evgeniyat on 08.02.16
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentParametrizedDependent.class)
@SuppressWarnings("unused")
public class Google {
    /*List of search requests*/
    private static final List<String> requests = Arrays.asList(
            "selenium camp 2016",
            "x1group careers",
            "x1group"
    );

    /*List of additional search parameters*/
    private static final Map<String, Map<String, String>> additionalParameters = new HashMap<String, Map<String, String>>() {{
        put("Text search", new HashMap<>());
        put("Pictures search", new HashMap<String, String>() {{
            put("tbm", "isch");
            put("sa", "X");
        }});
        put("Video search", new HashMap<String, String>() {{
            put("tbm", "vid");
            put("source", "lnms");
            put("sa", "X");
        }});
    }};

    @ConcurrentParametrized.Parameter
    public String testName;

    @Parameter("Parameters")
    @ConcurrentParametrized.Parameter(1)
    public Map<String, String> parameters;

    @ConcurrentParametrizedDependent.Parameters(name = "{0}", threads = 5)
    public static List<Object[]> data() {
        List<Object[]> resultSet = new ArrayList<>();
        additionalParameters.entrySet().stream().forEach(parameters -> requests.stream().forEach(request ->
                resultSet.add(new Object[]{parameters.getKey() + " for \"" + request + "\"", new HashMap<String, String>() {{
                    putAll(parameters.getValue());
                    put("q", request);
                }}})));
        return resultSet;
    }

    @Attachment(value = "Search response", type = "text/html")
    public String saveResponse(String request, String response) {
        return response;
    }

    @Attachment(value = "Search request", type = "text/plain")
    public String saveRequest(String method, String endpoint, RequestSpecification request) {
        String spec = method.toUpperCase() + ": " + endpoint + "\n";
        for (Map.Entry<String, Object> param : ((RequestSpecificationImpl) given().parameters(parameters)).getRequestParams().entrySet()) {
            spec += param.getKey() + " => " + param.getValue().toString() + "\n";
        }
        return spec;
    }

    @Stories("DEMO")
    @Features("Google search")
    @Test
    public void html() {
        String endPoint = "https://www.google.com.ua/search";
        RequestSpecification specification = given().parameters(parameters);
        saveRequest("GET", endPoint, specification);
        saveResponse(endPoint + " with parameters " + parameters.toString(),
                specification.expect().statusCode(Matchers.equalTo(200)).when().get(endPoint).getBody().asString());
    }
}
