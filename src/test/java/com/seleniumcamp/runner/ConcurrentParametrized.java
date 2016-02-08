package com.seleniumcamp.runner;

import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.*;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentParametrized extends Suite {
    protected static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private static final int DEFAULT_THREADS_COUNT = 15;
    protected final ArrayList<Runner> runners = new ArrayList<>();

    public ConcurrentParametrized(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, getAnnotatedClasses(klass));
    }

    public ConcurrentParametrized(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        Parameters parameters = getParametersMethod().getAnnotation(
                Parameters.class);
        createRunnersForParameters(allParameters(), parameters);
        setScheduler(new NonBlockingAsynchronousRunnerScheduler(parameters));
    }

    public ConcurrentParametrized(Class<?> klass, Object[] predefinedParameters) throws Throwable {
        super(klass, NO_RUNNERS);
        Parameters parameters = getParametersMethod().getAnnotation(
                Parameters.class);
        createRunnersForParameters(predefinedParameters, parameters);
        setScheduler(new NonBlockingAsynchronousRunnerScheduler(parameters));
    }

    public ConcurrentParametrized(Class<?> klass, TestParameters allParameters, List<Runner> runners) throws Throwable {
        super(klass, runners);
        Parameters parameters = getParametersMethod().getAnnotation(
                Parameters.class);
        ArrayList<Object[]> params = new ArrayList<>();
        params.add(allParameters.getParameters());
        createRunnersForParameters(params, parameters);
        setScheduler(new NonBlockingAsynchronousRunnerScheduler(1));
    }

    public ConcurrentParametrized(Class<?> klass, List<Runner> runners) throws InitializationError {
        super(klass, runners);
    }

    private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @SuppressWarnings("unchecked")
    protected Iterable<Object[]> allParameters() throws Throwable {
        Object parameters = getParametersMethod().invokeExplosively(null);
        if (parameters instanceof Iterable) {
            return (Iterable<Object[]>) parameters;
        } else {
            throw parametersMethodReturnedWrongType();
        }
    }

    protected FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
                Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
                + getTestClass().getName());
    }

    protected void createRunnersForParameters(Iterable<Object[]> allParameters,
                                              Parameters parameters) throws Exception {
        try {
            int i = 0;
            for (Object[] parametersOfSingleTest : allParameters) {
                String name = nameFor(parameters.name(), i, parametersOfSingleTest);
                runners.add(getRunner(getTestClass().getJavaClass(), parametersOfSingleTest, name, parameters));
                ++i;
            }
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    protected void createRunnersForParameters(Object[] parametersOfSingleTest,
                                              Parameters parameters) throws Exception {
        try {
            int i = 0;
            String name = nameFor(parameters.name(), i, parametersOfSingleTest);
            runners.add(getRunner(getTestClass().getJavaClass(), parametersOfSingleTest, name, parameters));
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    public ClassRunnerForParameters getRunner(Class testClass, Object[] parametersOfSingleTest, String name, Parameters parameters)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return new ClassRunnerForParameters(testClass, parametersOfSingleTest, name, parameters);
    }

    public ClassRunnerForParameters getRunner(Class testClass, Object[] parametersOfSingleTest, Parameters parameters)
            throws InitializationError, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return new ClassRunnerForParameters(testClass, parametersOfSingleTest, nameFor(parameters.name(), 0, parametersOfSingleTest), parameters) {
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

    protected String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}",
                Integer.toString(index));
        Object[] parameters_ = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Object param = parameters[i];
            if (param instanceof Class) {
                parameters_[i] = ((Class) param).getSimpleName();
            } else {
                parameters_[i] = param;
            }
        }
        String name = MessageFormat.format(finalPattern, parameters_);
        return "[" + name.replaceAll("(\\{\\d+\\})", "").trim() + "]";
    }

    protected Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format(
                "{0}.{1}() must return an Iterable of arrays.",
                className, methodName);
        return new Exception(message);
    }

    protected List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    protected boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Parameters {
        String name() default "{0}";

        int threads() default DEFAULT_THREADS_COUNT;
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Parameter {
        int value() default 0;
    }


    protected static class NonBlockingAsynchronousRunnerScheduler implements RunnerScheduler {
        private final List<Future<Object>> futures =
                Collections.synchronizedList(new ArrayList<>());
        private final ExecutorService fService;
        protected Logger logger = Logger.getLogger(NonBlockingAsynchronousRunnerScheduler.class.getName());

        public NonBlockingAsynchronousRunnerScheduler(Parameters parameters) {
            int numThreads;
            if (parameters.threads() == DEFAULT_THREADS_COUNT) {
                String threads = System.getProperty("junit.parallel.threads", "16");
                numThreads = Integer.parseInt(threads);
            } else {
                numThreads = parameters.threads();
            }
            fService = Executors.newFixedThreadPool(numThreads);
        }

        public NonBlockingAsynchronousRunnerScheduler(int numThreads) {
            logger.warn("Number of threads = " + numThreads);
            fService = Executors.newFixedThreadPool(numThreads);
        }

        public void schedule(final Runnable childStatement) {
            final Callable<Object> objectCallable = () -> {
                childStatement.run();
                return null;
            };
            futures.add(fService.submit(objectCallable));
        }

        public void finished() {
            waitForCompletion();
        }

        public void waitForCompletion() {
            for (Future<Object> each : futures) {
                try {
                    each.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    protected class ClassRunnerForParameters extends BlockJUnit4ClassRunner {
        protected final RunnerScheduler scheduler;
        private final Object[] fParameters;
        private final String fName;

        ClassRunnerForParameters(Class<?> type, Object[] parameters,
                                 String name, Parameters params) throws InitializationError {
            super(type);
            fParameters = parameters;
            fName = name;
            scheduler = new NonBlockingAsynchronousRunnerScheduler(params);
        }

        @Override
        public Object createTest() throws Exception {
            if (fieldsAreAnnotated()) {
                return createTestUsingFieldInjection();
            } else {
                return createTestUsingConstructorInjection();
            }
        }

        private Object createTestUsingConstructorInjection() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(fParameters);
        }

        private Object createTestUsingFieldInjection() throws Exception {
            List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
         /*   if (annotatedFieldsByParameter.size() < fParameters.length) {
                throw new Exception("Wrong number of parameters and @Parameter fields." +
                    " @Parameter fields counted: " + annotatedFieldsByParameter.size()
                    + ", available parameters: " + fParameters.length + ".");
            }*/
            Object testClassInstance = getTestClass().getJavaClass().newInstance();
            for (FrameworkField each : annotatedFieldsByParameter) {
                Field field = each.getField();
                Parameter annotation = field.getAnnotation(Parameter.class);
                int index = annotation.value();
                try {
                    if (fParameters.length >= (index + 1)) {
                        field.set(testClassInstance, fParameters[index]);
                    }
                } catch (IllegalArgumentException iare) {
                    throw new Exception(
                            getTestClass().getName() + ": Trying to set " + field.getName() +
                                    " with the value " + fParameters[index] +
                                    " that is not the right type (" + fParameters[index].getClass()
                                    .getSimpleName() + " instead of " +
                                    field.getType().getSimpleName() + ").", iare);
                }
            }
            return testClassInstance;
        }

        @Override
        protected String getName() {
            return fName;
        }

        @Override
        protected String testName(FrameworkMethod method) {
            return method.getName() + getName();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
            if (fieldsAreAnnotated()) {
                validateZeroArgConstructor(errors);
            }
        }

        @Override
        protected void validateFields(List<Throwable> errors) {
            super.validateFields(errors);
            if (fieldsAreAnnotated()) {
                List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
                int[] usedIndices = new int[annotatedFieldsByParameter.size()];
                for (FrameworkField each : annotatedFieldsByParameter) {
                    int index = each.getField().getAnnotation(Parameter.class).value();
                    if (index < 0 || index > annotatedFieldsByParameter.size() - 1) {
                        errors.add(
                                new Exception("Invalid @Parameter value: " + index
                                        + ". @Parameter fields counted: " +
                                        annotatedFieldsByParameter.size()
                                        + ". Please use an index between 0 and " +
                                        (annotatedFieldsByParameter.size() - 1) + ".")
                        );
                    } else {
                        usedIndices[index]++;
                    }
                }
                for (int index = 0; index < usedIndices.length; index++) {
                    int numberOfUse = usedIndices[index];
                    if (numberOfUse == 0) {
                        errors.add(new Exception("@Parameter(" + index + ") is never used."));
                    } else if (numberOfUse > 1) {
                        errors.add(new Exception(
                                "@Parameter(" + index + ") is used more than once (" + numberOfUse
                                        + ")."));
                    }
                }
            }
        }

        @Override
        protected Statement classBlock(final RunNotifier notifier) {
            return new Statement() {
                @Override
                public void evaluate() {
                    runChildren(notifier);
                }
            };
        }

        @Override
        protected Annotation[] getRunnerAnnotations() {
            return new Annotation[0];
        }

        protected void runChildren(final RunNotifier notifier) {
            for (final FrameworkMethod each : getChildren()) {
                scheduler.schedule(() -> ClassRunnerForParameters.this.runChild(each, notifier));
            }
            scheduler.finished();
        }
    }
}
