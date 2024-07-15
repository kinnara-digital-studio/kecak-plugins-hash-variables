package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.kecakplugins.hashvariables.formatter.Utilities;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.stream.Stream;

public class FormBinderHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "Form Binder Hash Variable";
    @Override
    public String getPrefix() {
        return "formBinder";
    }

    @Override
    public String processHashVariable(String key) {
        try {
            final String[] split = key.split("\\.");
            final String mode = Arrays.stream(split).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));
            final String formDefId = Arrays.stream(split).skip(1).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));
            final String primaryKey = Arrays.stream(split).skip(2).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));
            final String field = Arrays.stream(split).skip(3).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));

            if(!"load".equalsIgnoreCase(mode)) {
                throw new IllegalArgumentException("Unknown mode ["+mode+"]");
            }

            final Form form = Optional.of(formDefId)
                    .map(Utilities::generateForm)
                    .orElseThrow(() -> new IllegalArgumentException("Error generating form [" +formDefId+"]"));

            final FormData formData = new FormData();
            formData.setPrimaryKeyValue(primaryKey);

            return Optional.ofNullable(form.getLoadBinder())
                    .map(b -> b.load(form, primaryKey, formData))
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .findFirst()
                    .map(r -> r.getProperty(field))
                    .orElse("");

        } catch (IllegalArgumentException e) {
            LogUtil.error(getClassName(), e, e.getMessage());
            return "";
        }
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
        return null;
    }

    @Override
    public Collection<String> availableSyntax() {
        final Collection<String> syntax = new ArrayList<>();
        syntax.add(String.join(".", getPrefix(), "load", "FORM", "KEY", "GET_FIELD"));
        return syntax;
    }
}
