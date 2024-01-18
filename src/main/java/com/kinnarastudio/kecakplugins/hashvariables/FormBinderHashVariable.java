package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.kecakplugins.hashvariables.formatter.Utilities;
import org.joget.apps.app.lib.FormHashVariable;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            final String formDefId = Arrays.stream(split).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));
            final String fieldAndKey = Arrays.stream(split).skip(1).findFirst().orElseThrow(() -> new IllegalArgumentException("Incomplete arguments"));

            final String field;
            final String primaryKey;

            if(fieldAndKey.matches("\\w+\\[\\w+\\]")) {
                final Matcher matcherField = Pattern.compile("^\\w+(?=\\[)").matcher(fieldAndKey);
                field = matcherField.find() ? matcherField.toString() : "";

                final Matcher matcherKey = Pattern.compile("(?<=\\[)\\w+(?=\\])").matcher(fieldAndKey);
                primaryKey = matcherKey.find() ? matcherKey.group() : "";
            } else {
                field = fieldAndKey;
                primaryKey = Optional.ofNullable(getPrimaryKey())
                        .map(Arrays::stream)
                        .orElseGet(Stream::empty)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Primary Key is not defined"));
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
        return null;
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
        syntax.add(String.join(".", getPrefix(), "FORM_DEF_ID", "GET_FIELD") + "[KEY]");
        return syntax;
    }

    protected String[] getPrimaryKey() {
        String[] primaryKeys = null;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            if(request.getParameter("id") != null && !request.getParameter("id").isEmpty()) {
                primaryKeys = new String[] { request.getParameter("id") };
            } else if(request.getParameter("primaryKey") != null && !request.getParameter("primaryKey").isEmpty()) {
                primaryKeys = new String[] { request.getParameter("primaryKey") };
            }
        }

        if(primaryKeys != null) {
            return primaryKeys;
        }

        WorkflowAssignment wfAssignment = (WorkflowAssignment) getProperty("workflowAssignment");
        if(wfAssignment == null) {
            WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            String assignmentId = request.getParameter("assignmentId");
            String activityId = request.getParameter("activityId");
            if(assignmentId != null && !assignmentId.isEmpty()) {
                wfAssignment = workflowManager.getAssignment(assignmentId);
            } else if(activityId != null && !activityId.isEmpty()) {
                wfAssignment = workflowManager.getAssignment(activityId);
            }
        }

        if (wfAssignment != null) {
            try {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
                WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(wfAssignment.getProcessId());

                if (link != null) {
                    primaryKeys = new String[] { link.getOriginProcessId() };
                } else {
                    primaryKeys = new String[] { wfAssignment.getProcessId() };
                }

                return primaryKeys;
            } catch (Exception ex) {
                LogUtil.error(FormHashVariable.class.getName(), ex, ex.getMessage());
            }
        }

        return null;
    }
}
