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
import java.util.List;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by evgeniyat on 12.02.16
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentParametrizedDependent.class)
@SuppressWarnings("unused")
public class RandomGenerator {

    @Parameter("Length")
    @ConcurrentParametrized.Parameter
    public Integer length;

    @ConcurrentParametrizedDependent.Parameters(name = "{0}")
    public static List<Object[]> data() {
        return new ArrayList<Object[]>() {{
            add(new Object[]{1});
            add(new Object[]{10});
            add(new Object[]{0});
            add(new Object[]{-1});
        }};
    }

    @Attachment(value = "Search response", type = "application/json")
    public String saveResponse(String request, String response) {
        return response;
    }

    @Stories("DEMO")
    @Features("Google search")
    @Test
    public void json() {
        String endPoint = "https://qrng.anu.edu.au/API/jsonI.php";
        saveResponse(endPoint + " with length parameter [" + length + "]",
                given().parameter("type", "uint8")
                        .parameter("length", length)
                        .expect().statusCode(Matchers.equalTo(200))
                        .when().get(endPoint).getBody().asString());
    }

}
