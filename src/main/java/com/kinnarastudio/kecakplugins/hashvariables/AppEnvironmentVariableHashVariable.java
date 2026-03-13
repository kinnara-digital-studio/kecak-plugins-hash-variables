package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.lib.EnvironmentVariableHashVariable;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class AppEnvironmentVariableHashVariable extends EnvironmentVariableHashVariable {
    public final static String LABEL = "App Environment Variable";

    @Override
    public String getPrefix() {
        return "appEnvVariable";
    }

    @Override
    public String processHashVariable(String variableKey) {
        final String[] split = variableKey.split("\\.", 2);
        final AppDefinition appDefinition;
        final String envVariable;
        if(split.length > 1) {
            String appDefinitionId = split[0];
            envVariable = split[1];
            ApplicationContext applicationContext = AppUtil.getApplicationContext();
            AppDefinitionDao appDefinitionDao = (AppDefinitionDao) applicationContext.getBean("appDefinitionDao");
            appDefinition = appDefinitionDao.getPublishedAppDefinition(appDefinitionId);
        } else {
            appDefinition = (AppDefinition)this.getProperty("appDefinition");
            envVariable = split[0];
        }

        setProperty("appDefinition", appDefinition);
        return super.processHashVariable(envVariable);
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
        return AppEnvironmentVariableHashVariable.class.getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> list = new ArrayList();
        list.add(this.getPrefix() + ".APPLICATION.VARIABLE");
        return list;
    }
}
