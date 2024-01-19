package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.util.*;

/**
 * @author aristo
 * Format value using Options Binder label if available
 * Usage : formatter.optionsValue.[formDefId].[field].[value]
 */
public class OptionsLabel extends DefaultHashVariablePlugin {
    private final static Map<Element, FormRowSet> cacheOptionBinder = new HashMap<>();

    @Override
    public String getPrefix() {
        return "formatter.optionsLabel";
    }

    /**
     *
     * @param variableKey [formDefId].[field].[value]
     * @return
     */
    @Override
    public String processHashVariable(String variableKey) {
        String[] split = variableKey.split("\\.", 3);
        if(split.length < 3) {
            LogUtil.warn(getClassName(), "Missing parameter, usage : formatter.optionsValue.[formDefId].[field].[value]");
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

        String value = split[2];
        if(value == null || value.isEmpty()) {
            LogUtil.warn(getClassName(), "parameter [value] not provided");
            return "";
        }

        Form form = Utilities.generateForm(formDefId);
        if(form == null) {
            LogUtil.warn(getClassName(), "Error generating form ["+formDefId+"]");
            return value;
        }

        FormData formData = new FormData();
        Element element = FormUtil.findElement(field, form, formData);
        if(element == null) {
            LogUtil.warn(getClassName(), "Error finding element [" + field + "] in form [" + formDefId + "]");
            return value;
        }

        FormRowSet options = getOptionsBinderData(element, formData);
        Comparator<FormRow> c = Comparator.comparing(o -> o.getProperty(FormUtil.PROPERTY_VALUE));
        options.sort(c);
        FormRow searchKey = new FormRow();
        searchKey.setProperty(FormUtil.PROPERTY_VALUE, value);
        int i = Collections.binarySearch(options, searchKey, c);
        if(i < 0) {
            return value;
        }

        return String.valueOf(options.get(i).getProperty(FormUtil.PROPERTY_LABEL));
    }

    @Override
    public String getName() {
        return "Options Label Formatter Hash Variable";
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

    private FormRowSet getOptionsBinderData(Element element, FormData formData) {
        if(element.getOptionsBinder() != null) {
            if(cacheOptionBinder.containsKey(element)) {
                return cacheOptionBinder.get(element);
            }

            FormUtil.executeOptionBinders(element, formData);
            FormRowSet ret = formData.getOptionsBinderData(element, null);

            cacheOptionBinder.put(element, ret);

            return ret;
        } else if(element.getProperty(FormUtil.PROPERTY_OPTIONS) != null) {
            return (FormRowSet)element.getProperty(FormUtil.PROPERTY_OPTIONS);
        } else {
            return new FormRowSet();
        }
    }
}
