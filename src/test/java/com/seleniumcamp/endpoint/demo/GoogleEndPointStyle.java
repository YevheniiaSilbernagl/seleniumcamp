package com.seleniumcamp.endpoint.demo;

import com.jayway.restassured.specification.RequestSender;
import com.jayway.restassured.specification.RequestSpecification;
import com.seleniumcamp.runner.ConcurrentParametrized;
import com.seleniumcamp.runner.ConcurrentParametrizedDependent;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

import java.util.*;
import java.util.function.Function;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by evgeniyat on 08.02.16
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentParametrizedDependent.class)
@SuppressWarnings("unused")
public class GoogleEndPointStyle {
    @ConcurrentParametrized.Parameter
    public String testName;

    @ConcurrentParametrized.Parameter(1)
    public Function<RequestSpecification, RequestSender> request;

    @ConcurrentParametrizedDependent.Parameters(name = "{0}", threads = 5)
    public static List<Object[]> data() {
        return new ArrayList<Object[]>() {
            {
                add(new Object[]{"Text simple search", request((x) -> x.param("q", "Java 8 lambdas").expect().statusCode(200))});
                add(new Object[]{"Pictures search", request((x) -> x
                        .param("tbm", "isch")
                        .param("sa", "X")
                        .param("q", "Java 8 lambdas")
                        .expect().statusCode(200).log().ifError())});
            }
        };
    }

    @Stories("DEMO")
    @Features("Google search")
    @Test
    public void html() {
        new GoogleEndPoint("/search").get(request);
    }

    private static Function<RequestSpecification, RequestSender> request(Function<RequestSpecification, RequestSender> requestSpecification) {
        return requestSpecification;
    }
}
