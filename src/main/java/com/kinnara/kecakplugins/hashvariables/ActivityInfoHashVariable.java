package com.kinnara.kecakplugins.hashvariables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class ActivityInfoHashVariable extends DefaultHashVariablePlugin{

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
        
        if(activity!=null) {
        	if(key.equals("VariableKey")) {
	        	Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activity.getId());
	        	for(WorkflowVariable wVar: variableList) {
	        		if(wVar.getName().equals(key)) {
	        			return (String) wVar.getVal();
	        		}
	        	}
	        }else {
	        	return getActivityAttribute(activity,key);
	        }
        }
        
		return null;
	}

	private String getActivityAttribute(WorkflowActivity activity, String attribute) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
			
			if(attribute.equals("FinishTime")||attribute.equals("Due")) {
//				LogUtil.info(getClassName(), "[Date1] "+sdf.format(activity.getFinishTime()));
				
				Method method = WorkflowActivity.class.getDeclaredMethod("get"+attribute);
				Date result = (Date) method.invoke(activity);
//				LogUtil.info(getClassName(), "[Date] "+sdf.format(result));
				return sdf.format(result);
			}else {
				Method method = WorkflowActivity.class.getDeclaredMethod("get"+attribute);
				return (String) method.invoke(activity);
			}
			
		} catch (NoSuchMethodException e) {
			LogUtil.error(getClassName(), e, e.getMessage());
		} catch (SecurityException e) {
			LogUtil.error(getClassName(), e, e.getMessage());
		} catch (IllegalAccessException e) {
			LogUtil.error(getClassName(), e, e.getMessage());
		} catch (IllegalArgumentException e) {
			LogUtil.error(getClassName(), e, e.getMessage());
		} catch (InvocationTargetException e) {
			LogUtil.error(getClassName(), e, e.getMessage());
		}
		return null;
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
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("activityInfo.ActId.VariableKey");
        syntax.add("activityInfo.ActId.ProcessDefId");
        syntax.add("activityInfo.ActId.ProcessId");
        syntax.add("activityInfo.ActId.ProcessName");
        syntax.add("activityInfo.ActId.Delay");
        syntax.add("activityInfo.ActId.Performer");
        syntax.add("activityInfo.ActId.Limit");
        syntax.add("activityInfo.ActId.Description");
        syntax.add("activityInfo.ActId.FinishTime");
        syntax.add("activityInfo.ActId.Due");
        syntax.add("activityInfo.ActId.Name");
        syntax.add("activityInfo.ActId.State");
        return syntax;
    }
}
