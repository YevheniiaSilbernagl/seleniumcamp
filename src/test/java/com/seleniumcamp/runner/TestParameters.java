package com.seleniumcamp.runner;

/**
 * Created by evgeniyat on 05.11.14
 */
public class TestParameters {
    private Object[] parameters;

    public TestParameters(Object... params) {
        parameters = params;
    }

    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        String params = "";
        for (int i = 0; i < parameters.length; i++) {
            String parameterValue = parameters[i].toString();
            if (parameterValue.isEmpty()) {
                break;
            }
            params += parameterValue + (i == (parameters.length - 1) ? "" : ", ");
        }
        params = params.trim();
        if (params.endsWith(",")) {
            params = params.substring(0, params.length() - 1);
        }
        return params;
    }
}
