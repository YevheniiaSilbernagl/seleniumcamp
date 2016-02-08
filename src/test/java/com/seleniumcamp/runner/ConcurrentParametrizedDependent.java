package com.seleniumcamp.runner;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ConcurrentParametrizedDependent extends ConcurrentParametrized {

    public ConcurrentParametrizedDependent() throws InitializationError {
        super(Object.class, NO_RUNNERS);
    }

    public ConcurrentParametrizedDependent(Class<?> klass) throws Throwable {
        super(klass);
    }

    public ConcurrentParametrizedDependent(Class<?> klass, List<Runner> runners) throws Throwable {
        super(klass, runners);
    }

    public ConcurrentParametrizedDependent(Class<?> klass, Object[] predefinedParameters) throws Throwable {
        super(klass, predefinedParameters);
    }

    public ConcurrentParametrizedDependent(Class<?> klass, TestParameters allParameters, List<Runner> runners)
            throws Throwable {
        super(klass, runners);
        ConcurrentParametrized.Parameters parameters = getParametersMethod().getAnnotation(
                ConcurrentParametrized.Parameters.class);
        ArrayList<Object[]> params = new ArrayList<>();
        params.add(allParameters.getParameters());
        createRunnersForParameters(params, parameters);
//        setScheduler(new NonBlockingAsynchronousRunnerScheduler(1));
    }

    protected void createRunnersForParameters(Iterable<Object[]> allParameters,
                                              Parameters parameters) throws Exception {
        try {
            int i = 0;
            for (Object[] parametersOfSingleTest : allParameters) {
                String name = nameFor(parameters.name(), i, parametersOfSingleTest);
                DClassRunnerForParameters runner = new DClassRunnerForParameters(
                        getTestClass().getJavaClass(), parametersOfSingleTest,
                        name, parameters);
                runners.add(runner);
                ++i;
            }
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    @Override
    public DClassRunnerForParameters getRunner(Class testClass, Object[] parametersOfSingleTest, Parameters parameters)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return new DClassRunnerForParameters(testClass, parametersOfSingleTest, nameFor(parameters.name(), 0, parametersOfSingleTest), parameters) {
            @Override
            public Description getDescription() {
                Description description = Description.createTestDescription(getTestClass().getJavaClass(), getTestClass().getJavaClass().getSimpleName());
                for (FrameworkMethod child : getChildren()) {
                    description.addChild(describeChild(child));
                }
                return description;
            }
        };
    }

    private class DClassRunnerForParameters extends ClassRunnerForParameters {
        DClassRunnerForParameters(Class<?> type, Object[] parameters, String name,
                                  Parameters params) throws InitializationError {
            super(type, parameters, name, params);
        }

        protected void runChildren(final RunNotifier notifier) {
            scheduler.schedule(() -> {
                for (final FrameworkMethod each : getChildren()) {
                    DClassRunnerForParameters.this.runChild(each, notifier);
                }
            });
            scheduler.finished();
        }
    }
}
