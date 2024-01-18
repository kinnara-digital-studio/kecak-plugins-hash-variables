package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.IdGeneratorField;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author aristo
 *
 * Generate running numbers from published environment variable
 *
 */
public class IdGeneratorHashVariable extends DefaultHashVariablePlugin {
    private static final Pattern variablePattern = Pattern.compile("\\w+");
    private static final Pattern digitsPattern = Pattern.compile("(?<=\\[)[0-9]+(?=]$)");

    @Override
    public String getPrefix() {
        return "idGenerator";
    }

    @Override
    public String processHashVariable(String variableKey) {
        Matcher variableMatcher = variablePattern.matcher(variableKey);
        Matcher digitsMatcher = digitsPattern.matcher(variableKey);

        if(!variableMatcher.find() || !digitsMatcher.find()) {
            LogUtil.warn(getClassName(), "Error processing variable key ["+variableKey+"]");
            return "";
        }

        String envVariable = variableMatcher.group();
        int digits;
        try {
            digits = Integer.parseInt(digitsMatcher.group());
        } catch (NumberFormatException e) {
            LogUtil.error(getClassName(), e, "Error processing variable key ["+variableKey+"]");
            digits = 1;
        }

        return getGeneratedValue(envVariable, digits);
    }

    @Override
    public String getName() {
        return getLabel();
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
        return "ID Generator Hash Variable";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }

    /**
     * Copy from {@link IdGeneratorField} with adjustments
     *
     * @param envVariable
     * @param digits
     * @return
     */
    protected String getGeneratedValue(String envVariable, int digits) {
        String value = "";
        try {
            AppDefinition appDef = getPublishedAppDefinition();
            EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) AppUtil.getApplicationContext().getBean("environmentVariableDao");

            Integer count = environmentVariableDao.getIncreasedCounter(envVariable, "Used for plugin: " + getLabel(), appDef);

            String format = IntStream.iterate(0, i -> i + 1).limit(digits).boxed().map(i -> "?").collect(Collectors.joining());
            value = format;
            Matcher m = Pattern.compile("(\\?+)").matcher(format);
            if (m.find()) {
                String pattern = m.group(1);
                String formater = pattern.replaceAll("\\?", "0");
                pattern = pattern.replaceAll("\\?", "\\\\?");

                DecimalFormat myFormatter = new DecimalFormat(formater);
                String runningNumber = myFormatter.format(count);
                value = value.replaceAll(pattern, runningNumber);
            }
        } catch (Exception e) {
            LogUtil.error(IdGeneratorField.class.getName(), e, "");
        }
        return value;
    }

    @Override
    public Collection<String> availableSyntax() {
        return Collections.singleton(getPrefix() + ".VARIABLE[DIGITS]");
    }

    /**
     * Get published {@link AppDefinition}
     *
     * @return
     */
    protected AppDefinition getPublishedAppDefinition() {
        AppDefinition currentAppDefinition = AppUtil.getCurrentAppDefinition();
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        return appDefinitionDao.loadVersion(currentAppDefinition.getAppId(), appDefinitionDao.getPublishedVersion(currentAppDefinition.getAppId()));
    }
}
