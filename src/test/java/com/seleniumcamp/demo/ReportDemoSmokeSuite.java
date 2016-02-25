package com.seleniumcamp.demo;

import com.seleniumcamp.endpoint.demo.GoogleEndPointStyle;
import com.seleniumcamp.runner.ConcurrentParametrized;
import com.seleniumcamp.runner.SmokeRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static com.seleniumcamp.runner.SmokeRunner.RunningMode.all;

/**
 * Created by evgeniyat on 07.01.16
 */
@RunWith(SmokeRunner.class)
public class ReportDemoSmokeSuite {
    @ConcurrentParametrized.Parameters(threads = 25)
    public static Collection dataProvider() {
        return Arrays.asList(new Object[][]{
                {RandomGenerator.class, all()},
                {Google.class, all()},
                {GoogleEndPointStyle.class, all()},
        });
    }
}
