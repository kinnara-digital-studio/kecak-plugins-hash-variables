package com.kinnara.kecakplugins.hashvariables;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityInfoHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String getPrefix() {
        return "activityInfo";
    }

    @Override
    public String processHashVariable(String variableKey) {
        if (variableKey.startsWith("{") && variableKey.contains("}")) {
            return null;
        }

        String temp[] = variableKey.split("\\.");
        String activityId = temp[0].trim();
        String key = temp[1].trim();

        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowActivity activity = workflowManager.getActivityById(activityId);
        WorkflowActivity trackWflowActivity = workflowManager.getRunningActivityInfo(activityId);

        if (activity != null) {
            return Optional.ofNullable(getActivityAttribute(activity, trackWflowActivity, key)).orElseGet(() -> {
                Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activity.getId());
                for (WorkflowVariable wVar : variableList) {
                    if (wVar.getName().equals(key)) {
                        return (String) wVar.getVal();
                    }
                }
                return "";
            });
        }

        return null;
    }

    protected @Nullable String getActivityAttribute(WorkflowActivity activity, WorkflowActivity runningActivityInfo, String attribute) {
        LogUtil.debug(getClassName(), "getActivityAttribute : attribute [" + attribute + "]");

        try {
            final Method method = WorkflowActivity.class.getDeclaredMethod("get" + attribute);
            Object value = Optional.ofNullable(method.invoke(runningActivityInfo)).orElseGet(Try.onSupplier(() -> method.invoke(activity)));
            if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                return sdf.format(value);
            } else {
                return (String) method.invoke(runningActivityInfo);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return null;
        }
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

    @Override
    public String getName() {
        return "Activity Info Hash Variable";
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
    public Collection<String> availableSyntax() {
        Collection<String> syntax = Stream.concat(getGetterMethods(WorkflowActivity.class).stream().map(s -> s.replaceAll("^get", "")),
                        getWorkflowVariables(AppUtil.getCurrentAppDefinition()).stream())
                .map(s -> String.join(".", getPrefix(), "ACTIVITY_ID", s))
                .collect(Collectors.toSet());

        return syntax;
    }

    /**
     * Get collection of getter methods
     *
     * @param cls Class
     * @return
     */
    protected Set<String> getGetterMethods(Class<?> cls) {
        return Optional.of(cls.getName())
                .map(Try.onFunction(Class::forName))
                .map(Class::getMethods)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(m -> m.getParameterCount() == 0)
                .map(Try.onFunction(Method::getName))
                .filter(s -> s.startsWith("get"))
                .collect(Collectors.toSet());
    }

    /**
     * Get collection of workflow variables
     *
     * @param appDefinition Application definition
     * @return
     */
    protected Set<String> getWorkflowVariables(AppDefinition appDefinition) {
        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) applicationContext.getBean("workflowManager");
        return Optional.of(appDefinition)
                .map(AppDefinition::getPackageDefinition)
                .map(p -> workflowManager.getProcessList(p.getId(), p.getVersion().toString()))
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(WorkflowProcess::getId)
                .map(workflowManager::getProcessVariableDefinitionList)
                .flatMap(Collection::stream)
                .map(WorkflowVariable::getId)
                .collect(Collectors.toSet());
    }
}
