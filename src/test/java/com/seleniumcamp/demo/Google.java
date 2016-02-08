package com.seleniumcamp.demo;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by evgeniyat on 08.02.16
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentParametrizedDependent.class)
public class Google {
    @ConcurrentParametrized.Parameter
    public String testName;

    @Parameter("Parameters")
    @ConcurrentParametrized.Parameter(1)
    public Map<String, String> parameters;

    @ConcurrentParametrizedDependent.Parameters(name = "{0}", threads = 4)
    public static List<Object[]> data() {
        return new ArrayList<Object[]>() {
            {
                add(new Object[]{"Text request", new HashMap<String, String>() {{
                    put("q", "selenium camp 2016");
                }}});
                add(new Object[]{"Pictures request", new HashMap<String, String>() {{
                    put("q", "x1group careers");
                    put("tbm", "isch");
                    put("sa", "X");
                }}});
                add(new Object[]{"Video request", new HashMap<String, String>() {{
                    put("q", "x1group");
                    put("tbm", "vid");
                    put("source", "lnms");
                    put("sa", "X");
                }}});
            }
        };
    }

    @Attachment(value = "Search response for request {0}", type = "text/html")
    public static String saveResponse(String request, String response) {
        return response;
    }

    @Stories("DEMO")
    @Features("Google search")
    @Test
    public void demo() {
        String endPoint = "https://www.google.com.ua/search";
        saveResponse(endPoint + " with parameters " + parameters.toString(), given().parameters(parameters)
                .expect().statusCode(Matchers.equalTo(200))
                .when().get(endPoint).getBody().asString());
    }
}
