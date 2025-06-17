package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class StringFormatter extends DefaultHashVariablePlugin {
    public final static String LABEL = "String Formatter";

    @Override
    public String getPrefix() {
        return "formatter.stringFormatter";
    }

    @Override
    public String processHashVariable(String key) {
        final String[] split = key.split("(?<=\")\\.(?=\\[)", 2);
        final String format = Arrays.stream(split).findFirst().orElseThrow();

        final Pattern p = Pattern.compile("%[.,0-9]*[sdf]");

        final Object[] values = Arrays.stream(split)
                .skip(1)
                .findFirst()
                .map(s -> s.replaceAll("^\\[|]$", ""))
                .stream()
                .map(s -> s.split("(?<!\\\\),"))
                .flatMap(Arrays::stream)
                .map(o -> {

                    return Integer.parseInt(o);
                })
                .toArray(Object[]::new);

        return String.format(format, values);
    }

    @Override
    public String getName() {
        return LABEL;
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
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public Collection<String> availableSyntax() {
        return Collections.singleton(String.join(".", getPrefix(), "\"FORMAT\"", "[VALUE1, VALUE2, ...]"));
    }
}
