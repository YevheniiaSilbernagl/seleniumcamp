package com.seleniumcamp.runner;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * autotest
 * Created by evgeniyat on 23.03.15
 */
public class SmokeRunner extends ConcurrentParametrized {

    public SmokeRunner(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        ConcurrentParametrized.Parameters parameters = getParametersMethod().getAnnotation(
                ConcurrentParametrized.Parameters.class);
        createRunnersForParameters(allParameters(), parameters);
        setScheduler(new NonBlockingAsynchronousRunnerScheduler(parameters));
    }

    protected void createRunnersForParameters(Iterable<Object[]> allParameters,
                                              ConcurrentParametrized.Parameters parameters) throws Exception {
        try {
            for (Object[] parametersOfSingleTest : allParameters) {
                Class testClass = (Class) parametersOfSingleTest[0];
                Object runningParameters = parametersOfSingleTest.length > 1 ?
                        parametersOfSingleTest[1] : null;
                if (runningParameters == null) {
                    runners.add(new BlockJUnit4ClassRunner(testClass));
                } else if (runningParameters instanceof TestParameters) {
                    runners.add(getRunner(testClass, ((TestParameters) runningParameters).getParameters()));
                } else if (runningParameters instanceof TestMethod) {
                    runners.add(getRunner(testClass, ((TestMethod) runningParameters).getMethod()));
                } else {//Mode
                    switch ((RunningMode.Mode) runningParameters) {
                        case SINGLE:
                            runners.add(new BlockJUnit4ClassRunner(testClass));
                            break;
                        case RUNNER:
                            runners.add(getRunner(testClass));
                            break;
                    }
                }
            }
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    @SuppressWarnings("unchecked")
    public ConcurrentParametrized.ClassRunnerForParameters getRunner(Class testClass, Object[] parameters)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ConcurrentParametrized.Parameters parametersAnnotation = new TestClass(testClass)
                .getAnnotatedMethods(ConcurrentParametrized.Parameters.class).get(0)
                .getAnnotation(ConcurrentParametrized.Parameters.class);
        Annotation runWith = testClass.getAnnotation(RunWith.class);
        Class<? extends ConcurrentParametrized> runner = (Class<? extends ConcurrentParametrized>) ((RunWith) runWith).value();
        final ConcurrentParametrized testRunner = runner.getConstructor(Class.class, Object[].class).newInstance(testClass, parameters);
        return testRunner.getRunner(testClass, parameters, parametersAnnotation);
    }

    @SuppressWarnings("unchecked")
    public Runner getRunner(Class testClass)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Annotation annotation = testClass.getAnnotation(RunWith.class);
        if (annotation != null) {
            return ((RunWith) annotation).value().getConstructor(Class.class).newInstance(testClass);
        } else {
            return new BlockJUnit4ClassRunner(testClass);
        }
    }

    @SuppressWarnings("unchecked")
    public Runner getRunner(Class testClass, final String testMethodName)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return new BlockJUnit4ClassRunner(testClass) {
            @Override
            protected List<FrameworkMethod> getChildren() {
                return super.getChildren().stream().filter(method -> method.getName().equals(testMethodName)).collect(Collectors.toList());
            }
        };
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(getTestClass().getJavaClass());
        for (Runner child : getChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    public static class RunningMode {
        public static TestMethod method(String name) {
            return new TestMethod(name);
        }

        public static TestParameters data(Object... params) {
            return new TestParameters(params);
        }

        public static Mode single() {
            return Mode.SINGLE;
        }

        public static Mode all() {
            return Mode.RUNNER;
        }

        protected enum Mode {
            SINGLE, RUNNER
        }
    }
}
