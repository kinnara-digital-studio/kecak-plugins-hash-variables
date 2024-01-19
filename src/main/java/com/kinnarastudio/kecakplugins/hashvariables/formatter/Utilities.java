package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Utilities {

    private final static Map<String, Form> formCache = new HashMap<>();

    public static Form generateForm(String formDefId) {
        return generateForm(formDefId, true);
    }
    public static Form generateForm(String formDefId, boolean processHashVariable) {
        return generateForm(AppUtil.getCurrentAppDefinition(), formDefId, processHashVariable);
    }

    public static Form generateForm(String appId, String appVersion, String formDefId, boolean processHashVariable) {
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao) AppUtil.getApplicationContext().getBean("appDefinitionDao");
        return generateForm(appDefinitionDao.loadVersion(appId, Long.valueOf(appVersion)), formDefId, processHashVariable);
    }

    public static Form generateForm(AppDefinition appDef, String formDefId, boolean processHashVariable) {
        // check in cache
        if (formCache.containsKey(formDefId)) {
            return formCache.get(formDefId);
        }

        try {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            FormService formService = (FormService) appContext.getBean("formService");

            JSONObject json = getJsonForm(appDef, formDefId);
            if(json == null) return null;

            Form form = (Form) formService.createElementFromJson(json.toString(), processHashVariable);

            // put in cache if possible
            formCache.put(formDefId, form);

            return form;
        } catch (JSONException e) {
            LogUtil.error(Utilities.class.getName(), e, e.getMessage());
            return null;
        }
    }

    public static JSONObject getJsonForm(String formDefId) throws JSONException {
        return getJsonForm(AppUtil.getCurrentAppDefinition(), formDefId);
    }

    public static JSONObject getJsonForm(AppDefinition appDefinition, String formDefId) throws JSONException {
        if (appDefinition != null && formDefId != null && !formDefId.isEmpty()) {
            FormDefinitionDao formDefinitionDao =
                    (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");

            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDefinition);
            if (formDef != null) {
                return new JSONObject(formDef.getJson());
            }
        }

        return null;
    }

    public static FormLoadBinder getFormLoadBinder(JSONObject jsonForm, WorkflowAssignment assignment) {
        final PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        return Optional.ofNullable(jsonForm)
                .map(Try.onFunction(json -> json.getJSONObject("properties").getJSONObject("loadBinder")))
                .map(json -> AppUtil.processHashVariable(json.toString(), assignment, null, null))
                .map(Try.onFunction(JSONObject::new))
                .map(Try.onFunction(json -> {
                    String className = json.getString(FormUtil.PROPERTY_CLASS_NAME);
                    Map<String, Object> properties = FormUtil.parsePropertyFromJsonObject(json);
                    FormBinder binder = (FormBinder) pluginManager.getPlugin(className);
                    binder.setProperties(properties);
                    return binder;
                }))
                .filter(b -> b instanceof FormLoadElementBinder)
                .map(b -> (FormLoadBinder)b)
                .orElse(null);
    }
}
