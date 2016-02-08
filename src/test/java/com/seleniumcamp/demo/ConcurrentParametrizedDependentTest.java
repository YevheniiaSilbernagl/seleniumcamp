package com.seleniumcamp.demo;

import com.seleniumcamp.runner.ConcurrentParametrized;
import com.seleniumcamp.runner.ConcurrentParametrizedDependent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Parameter;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.annotations.Stories;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by evgeniyat on 07.01.16
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(ConcurrentParametrizedDependent.class)
public class ConcurrentParametrizedDependentTest {
    @Parameter("Target")
    @ConcurrentParametrized.Parameter
    public Target target;

    @Parameter("Category")
    @ConcurrentParametrized.Parameter(1)
    public Category category;

    @ConcurrentParametrizedDependent.Parameters(name = "Show {1} to all {0}", threads = 4)
    public static List<Object[]> data() {
        return Arrays.asList(Target.values()).stream().map(target ->
                Arrays.asList(Category.values()).stream().map(category ->
                        new Object[]{target, category}).collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toList());

    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("ClassPreconditions");
    }

    @Step("Preconditions")
    @Before
    public void before() {
        System.out.println("Method Preconditions for " + target + "||" + category);
    }

    @Stories("DEMO")
    @Features("Feature1")
    @Test
    public void demo() {
        System.out.println("Test 1 for " + target);
    }

    @Stories("DEMO")
    @Features("Feature2")
    @Test
    public void tv() {
        System.out.println("Test 2 for " + category);
    }
}
