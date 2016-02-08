package com.seleniumcamp.demo;

import com.seleniumcamp.runner.ConcurrentParametrized;
import com.seleniumcamp.runner.SmokeRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static com.seleniumcamp.runner.SmokeRunner.RunningMode.*;

/**
 * Created by evgeniyat on 07.01.16
 */
@RunWith(SmokeRunner.class)//TODO
public class DemoSmokeSuite {
    @ConcurrentParametrized.Parameters(threads = 25)
    public static Collection dataProvider() {
        return Arrays.asList(new Object[][]{
                {ConcurrentParametrizedDependentTest.class, data(Target.KIDS, Category.EDUCATION)},
                {ConcurrentParametrizedDependentTest.class, all()},
                {ConcurrentParametrizedDependentTest.class, method("test1")},
        });
    }
}
