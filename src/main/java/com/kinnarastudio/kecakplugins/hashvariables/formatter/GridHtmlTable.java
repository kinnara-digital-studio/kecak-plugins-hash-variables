package com.kinnarastudio.kecakplugins.hashvariables.formatter;

import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;

import java.util.stream.Collectors;

/**
 * Display grid in HTML Table format
 * usage : formatter.gridTable.[formDefId].[gridField]
 */
public class GridHtmlTable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "formatter.gridHtmlTable";
    }

    /**
     * formatter.gridHtmlTable.[formDefId].[gridField]
     * @param variableKey
     * @return
     */
    @Override
    public String processHashVariable(String variableKey) {
        int limit = 2;
        String[] keys = variableKey.split("\\.", limit);
        if(keys.length < limit) {
            LogUtil.info(getClassName(), "VariableKey ["+variableKey+"]");
            LogUtil.warn(getClassName(), "Missing parameter, usage : formatter.gridTable.[formDefId].[gridField]");
            return "";
        }

        String formDefId = keys[0];
        String gridField = keys[1];

        Form form = Utilities.generateForm(formDefId);
        if(form == null) {
            LogUtil.warn(getClassName(), "Form ["+formDefId+"] not found");
            return "";
        }

        FormData formData = new FormData();
        Element gridElement = FormUtil.findElement(gridField, form, formData, true);
        if(gridElement == null) {
            LogUtil.warn(getClassName(), "Element ["+gridField+"] not found");
            return "";
        }

        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        if(wfAssignment == null) {
            LogUtil.warn(getClassName(), "Assignment not found");
            return "";
        }

        formData.setPrimaryKeyValue(appService.getOriginProcessId(wfAssignment.getProcessId()));
        formData = FormUtil.executeLoadBinders(gridElement, formData);
        FormRowSet rowSet = formData.getLoadBinderData(gridElement);
        LogUtil.info(getClassName(), "rowSet size ["+rowSet.size()+"]");


        FormRowSet options = (FormRowSet)gridElement.getProperty(FormUtil.PROPERTY_OPTIONS);
        if(options == null) {
            LogUtil.warn(getClassName(), "Error in grid configuration ["+gridField+"]");
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<table id='").append(variableKey).append("' class=' gridtable-tab'>");
        sb.append("<tr class=' gridtable-head-row'>");
        sb.append(options.stream()
                .map(optionsRow -> optionsRow.getProperty(FormUtil.PROPERTY_LABEL))
                .map(StringEscapeUtils::escapeHtml)
                .collect(Collectors.joining("</th><th class=' gridtable-head-col'>","<th class=' gridtable-head-col'>","</th>")));
        sb.append("</tr>");
        sb.append(rowSet.stream()
                .map(row -> options.stream()
                        .map(optionsRow -> row.getProperty(optionsRow.getProperty(FormUtil.PROPERTY_VALUE)))
                        .map(StringEscapeUtils::escapeHtml)
                        .collect(Collectors.joining("</td><td class=' gridtable-body-col'>", "<td class=' gridtable-body-col'>", "</td>")))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("</tr><tr class=' gridtable-body-row'>", "<tr class=' gridtable-body-row'>", "</tr>")));
        sb.append("</table>");

        return sb.toString();
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
        return "Grid Html Table Formatter Hash Variable";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
}
