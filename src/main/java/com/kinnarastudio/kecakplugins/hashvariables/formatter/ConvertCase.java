package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ConvertCase extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "formatter.convertCase";
    }

    /**
     *
     * @param variableKey usage : formatter.convertCase.upper|lower|lowerCamel|upperCamel|underscore.[value]
     * @return
     */
    @Override
    public String processHashVariable(String variableKey) {
        String[] split = variableKey.split("\\.", 2);
        if(split.length < 2) {
            LogUtil.warn(getClassName(), "Missing parameter usage : formatter.convertCase.upper|lower|lowerCamel|upperCamel|underscore.[value]");
            return "";
        }

        String mode = split[0];
        String value = split[1];
        switch (mode) {
            case "lower" :
                return value.toLowerCase();
            case "upper" :
                return value.toUpperCase();
            default :
                return value;
        }
    }

    @Override
    public String getName() {
        return "Convert Case Formatter Hash Variable";
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

    @Override
    public Collection<String> availableSyntax() {
        List<String> syntax = new ArrayList<>();

        syntax.add("lower");
        syntax.add("upper");

        return syntax.stream()
                .map(s -> String.join(".", getPrefix(), s, "VALUE"))
                .collect(Collectors.toList());
    }
}
