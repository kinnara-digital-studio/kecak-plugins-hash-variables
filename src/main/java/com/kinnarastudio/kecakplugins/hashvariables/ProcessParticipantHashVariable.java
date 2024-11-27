package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProcessParticipantHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "Process Participant Hash Variable";

    @Override
    public String getPrefix() {
        return "participant";
    }

    @Override
    public String processHashVariable(String key) {
        final String[] participantIds = key.split(";");

        final WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        final WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        final WorkflowProcess process = workflowManager.getProcess(wfAssignment.getProcessDefId());

        return Arrays.stream(participantIds)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> String.join(";", WorkflowUtil.getAssignmentUsers(process.getPackageId(), wfAssignment.getProcessDefId(), wfAssignment.getProcessId(), wfAssignment.getProcessVersion(), wfAssignment.getActivityId(), "", s)))
                .collect(Collectors.joining(";"));
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
        return Collections.singleton(getPrefix() + ".PARTICIPANT_IDs");
    }
}
