package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.util.ResourceBundle;

/**
 * Retrieve Field Label from field
 * Usage : formatter.optionsValue.[formDefId].[field]
 */
public class FieldLabel extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "formatter.fieldLabel";
    }

    @Override
    public String processHashVariable(String variableKey) {
        String[] split = variableKey.split("\\.", 3);
        if(split.length < 2) {
            LogUtil.warn(getClassName(), "Missing parameter, usage : formatter.optionsValue.[formDefId].[field]");
            return "";
        }

        String formDefId = split[0];
        if(formDefId == null || formDefId.isEmpty()) {
            LogUtil.warn(getClassName(), "parameter [formDefId] not provided");
            return "";
        }

        String field = split[1];
        if(field == null || field.isEmpty()) {
            LogUtil.warn(getClassName(), "parameter [field] not provided");
            return "";
        }

        Form form = Utilities.generateForm(formDefId);
        if(form == null) {
            LogUtil.warn(getClassName(), "Error generating form ["+formDefId+"]");
            return "";
        }

        FormData formData = new FormData();
        Element element = FormUtil.findElement(field, form, formData);
        if(element == null) {
            LogUtil.warn(getClassName(), "Error finding element [" + field + "] in form [" + formDefId + "]");
            return "";
        }

        String label = element.getPropertyString(FormUtil.PROPERTY_LABEL);
        return label == null ? element.getPropertyString(FormUtil.PROPERTY_ID) : label;
    }

    @Override
    public String getName() {
        return "Field Label Hash Variable";
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
        return null;
    }
}
