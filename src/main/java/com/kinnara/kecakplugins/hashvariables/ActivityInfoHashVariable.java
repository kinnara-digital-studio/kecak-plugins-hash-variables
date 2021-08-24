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
		String activityId = temp[0];
		String tableName = temp[1].trim();
        String columnName = temp[2].trim();
        
        
    	LogUtil.info(getClassName(), "[ACTID] "+activityId);
        LogUtil.info(getClassName(), "[Column Name] "+columnName);
        ApplicationContext appContext = AppUtil.getApplicationContext();
        DataSource ds = (DataSource) appContext.getBean("setupDataSource");
        
		WorkflowManager wfManager = (WorkflowManager)appContext.getBean("workflowManager");
        WorkflowActivity act = wfManager.getActivityById(activityId);
        if(act!=null) {
        	LogUtil.info(getClassName(), "[ACT] "+act.getId());
        }
        String processId = null;
        String query=
        		"SELECT a.ProcessId "+ 
        		"FROM shkactivities a "+ 
        		"WHERE a.Id= ? ";
        try(Connection con = ds.getConnection();PreparedStatement pstmt = con.prepareStatement(query)) {
        	pstmt.setString(1, activityId);
        	try (ResultSet rs = pstmt.executeQuery();) {
        		while (rs.next()) {
        			processId = rs.getString("ProcessId");
        		}
        	}catch (Exception e) {
    			LogUtil.error(getClassName(), e, e.getMessage());
    		}
        	
        	if(processId!=null) {
        		// iterate wf process link
        		// get form data
        		String columnValue = null;
        		query = "SELECT c_"+columnName+" AS "+columnName+" FROM app_fd_"+tableName+" WHERE id=? ";
        		try(PreparedStatement ps = con.prepareStatement(query)){
        			ps.setString(1, processId);
        			try (ResultSet rs = ps.executeQuery();) {
                		while (rs.next()) {
                			columnValue = rs.getString(columnName);
                		}
                	}catch (Exception e) {
            			LogUtil.error(getClassName(), e, e.getMessage());
            		}
                	if(columnValue!=null) {
                		LogUtil.info(getClassName(), "[VALUE] "+columnValue);
                		return columnValue;
                	}
        		}catch (Exception e) {
        			LogUtil.error(getClassName(), e, e.getMessage());
        		}
        	}
		} catch (Exception e) {
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
        syntax.add("activityInfo.KEY.TABLENAME.COLUMNNAME");
        
        return syntax;
    }
}
