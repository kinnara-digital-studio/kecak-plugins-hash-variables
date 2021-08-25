package com.kinnara.kecakplugins.hashvariables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfActivity;
import org.enhydra.shark.api.client.wfmodel.WfActivityIterator;
import org.enhydra.shark.api.client.wfservice.AdminMisc;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

public class ActivityInfoHashVariable extends DefaultHashVariablePlugin{

	@Override
	public String getPrefix() {
		return "activityInfo";
	}

	@Override
	public String processHashVariable(String variableKey) {
		String temp[] = variableKey.split("\\.");
		String activityId = temp[0].trim();
        String wVarKey = temp[1].trim();
        
        
    	LogUtil.info(getClassName(), "[ACTID] "+activityId);
        LogUtil.info(getClassName(), "[WVar Key] "+wVarKey);
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowActivity activity = workflowManager.getActivityById(activityId);
        if(activity!=null) {
        	Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activity.getId());
        	for(WorkflowVariable wVar: variableList) {
        		if(wVar.getName().equals(wVarKey)) {
        			LogUtil.info(getClassName(), "[Value] "+(String) wVar.getVal());
        			return (String) wVar.getVal();
        		}
        	}
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

	protected SharkConnection connect() throws Exception {
        return connect(null);
    }
	
	protected SharkConnection connect(String username) throws Exception {
        SharkConnection sConn = Shark.getInstance().getSharkConnection();
        if (username == null) {
            username = WorkflowUtil.getCurrentUsername();
        }
        WMConnectInfo wmconnInfo = new WMConnectInfo(username, username, "WorkflowManager", "");
        sConn.connect(wmconnInfo);
        return sConn;
    }
	
	@Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("activityInfo.ActId.VariableKey");
        
        return syntax;
    }
}
