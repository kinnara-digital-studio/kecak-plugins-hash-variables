package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get first non-empty words inside brackets []
 *
 * coalesce.[VAL1][VAL2][VAL3]
 */
public class CoalesceHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "Coalesce Hash Variable";

    @Override
    public String getPrefix() {
        return "coalesce";
    }

    @Override
    public String processHashVariable(String key) {
        final Pattern p = Pattern.compile("(?<=\\[)\\w*(?=])");
        final Matcher m = p.matcher(key);
        while(m.find()) {
            final String value = m.group();
            if(!value.isEmpty()) {
                return value;
            }
        }
        return "";
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
        return Collections.singleton(getPrefix() + ".[VAL_1][VAL_2]...[VAL_N]");
    }
}
