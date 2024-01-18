package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author aristo
 *
 * Usage : #formatter.stringEscape.[method].[value]#
 *
 */
public class StringEscape extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "formatter.stringEscape";
    }

    @Override
    public String processHashVariable(String variableKey) {
        // list down available methods
        String messageAvailableMethods = "Available method(s) : " + Arrays.stream(StringEscapeUtils.class.getMethods())
                .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class)
                .map(Method::getName)
                .collect(Collectors.joining(", "));

        String[] split = variableKey.split("\\.", 2);
        if(split.length < 2) {
            LogUtil.warn(getClassName(), "Key error ["+variableKey+"]. Missing parameter(s). Usage : " + getPrefix() + ".[method].[value], please refer to single string parameter method of class " + StringEscapeUtils.class.getName());
            LogUtil.warn(getClassName(), messageAvailableMethods);
            return "";
        }

        try {
            Method m = StringEscapeUtils.class.getMethod(split[0], String.class);
            return String.valueOf(m.invoke(null, split[1]));
        } catch (NoSuchMethodException e) {
            LogUtil.error(getClassName(), e, messageAvailableMethods);

        } catch (InvocationTargetException | IllegalAccessException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
        }

        return "";
    }

    @Override
    public String getName() {
        return "HTML Escape Hash Variable";
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        return resourceBundle.getString("buildNumber");
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
}
