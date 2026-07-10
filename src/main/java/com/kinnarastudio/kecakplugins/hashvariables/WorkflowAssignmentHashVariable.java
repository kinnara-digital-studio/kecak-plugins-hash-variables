package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.service.WorkflowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WorkflowAssignmentHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "Assignment Hash Variable";

    @Override
    public String getPrefix() {
        return "workflowAssignment";
    }

    @Override
    public String processHashVariable(String key) {
        final WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        final WorkflowAssignment workflowAssignment = (WorkflowAssignment) getProperty("workflowAssignment");

        if ("recordId".equals(key)) {
            final WorkflowProcess workflowProcess = workflowManager.getRunningProcessById(workflowAssignment.getProcessId());
            String recordId = workflowProcess.getInstanceId();
            WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(recordId);
            if (link != null) {
                recordId = link.getOriginProcessId();
            }

            return recordId;
        }


        boolean isActivity = Optional.ofNullable(workflowAssignment)
                .map(WorkflowAssignment::getActivityId)
                .map(workflowManager::getActivityById)
                .map(WorkflowActivity::getType)
                .map(WorkflowActivity.TYPE_NORMAL::equals)
                .orElse(false);

        if (!isActivity) {
            return null;
        }

        if ("pendingAssignmentId".equals(key)) {
            return workflowAssignment.getActivityId();
        } else if ("pendingAssignees".equals(key)) {
            return Optional.of(workflowAssignment)
                    .map(WorkflowAssignment::getAssigneeList)
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.joining(";"));
        }

        return null;
    }

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
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
        Collection<String> list = new ArrayList<>();
        list.add(getPrefix() + ".pendingAssignmentId");
        list.add(getPrefix() + ".recordId");
        list.add(getPrefix() + ".pendingAssignees");
        return list;
    }
}
