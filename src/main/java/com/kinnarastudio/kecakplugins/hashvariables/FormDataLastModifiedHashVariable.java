package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.kecakplugins.hashvariables.exception.VariableParseException;
import com.kinnarastudio.kecakplugins.hashvariables.formatter.Utilities;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Get last changed data date on form based on modified date
 */
public class FormDataLastModifiedHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "Form Data Last Modified Variable";

    @Override
    public String getPrefix() {
        return "formDataLastChanged";
    }

    @Override
    public String processHashVariable(String key) {
        final String[] split = key.split("\\.", 2);

        try {
            final String formDefId = Arrays.stream(split).findFirst().orElseThrow(() -> new VariableParseException("At least 1 parameter is required"));
            final String format = Arrays.stream(split).skip(1).findFirst().orElse("yyyy-MM-dd hh:mm:ss");
            final DateFormat df = new SimpleDateFormat(format);

            final Form form = Utilities.generateForm(formDefId);

            final FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
            final FormRowSet rowSet = formDataDao.find(form, null, null, "dateModified", true, null, 1);
            return Optional.ofNullable(rowSet)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .findFirst()
                    .map(FormRow::getDateModified)
                    .map(df::format)
                    .orElse("");

        } catch (VariableParseException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
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
        return new ArrayList<String>() {{
            add(getPrefix() + ".FORM_ID");
            add(getPrefix() + ".FORM_ID.DATE_FORMAT");
        }};
    }
}
