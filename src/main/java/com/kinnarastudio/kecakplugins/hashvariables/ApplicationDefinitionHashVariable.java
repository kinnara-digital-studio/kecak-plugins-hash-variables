package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;

public class ApplicationDefinitionHashVariable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "appDefinition";
    }

    @Override
    public String processHashVariable(String key) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        switch (key) {
            case "id":
                return appDefinition.getAppId();
            case "version":
                return appDefinition.getVersion().toString();
            case "name":
                return appDefinition.getName();
            case "description":
                return appDefinition.getDescription();
            default:
                return "";
        }

    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return "Application Definition Hash Variable";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return null;
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<>();
        syntax.add(getPrefix() + ".id");
        syntax.add(getPrefix() + ".version");
        syntax.add(getPrefix() + ".name");
        syntax.add(getPrefix() + ".description");
        return syntax;
    }
}
