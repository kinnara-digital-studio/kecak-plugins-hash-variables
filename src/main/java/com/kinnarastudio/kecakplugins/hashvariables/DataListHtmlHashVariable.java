package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.kecakplugins.hashvariables.exception.DataListHtmlException;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class DataListHtmlHashVariable extends DefaultHashVariablePlugin {
    public final static String LABEL = "DataList HTML Hash Variable";

    @Override
    public String getPrefix() {
        return "dataListHtml";
    }

    @Override
    public String processHashVariable(String key) {
        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        try {
            final String[] split = key.split("\\.", 2);
            final String dataListName = Arrays.stream(split).findFirst().orElseThrow(() -> new DataListHtmlException("DataList not defined"));

            final DataList dataList = getDataList(appDefinition, dataListName);
            final DataListCollection<Map<String, String>> rows = dataList.getRows();

            final String thTd = Arrays.stream(dataList.getColumns())
                    .map(c -> {
                        final String name = c.getName();
                        final String label = c.getLabel();
                        return "<td id='" + name + "' >" + label + "</td>";
                    })
                    .collect(Collectors.joining(""));

            final String trTd = Optional.ofNullable(rows)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .map(row -> {
                        final String td = Arrays.stream(dataList.getColumns())
                                .map(c -> {
                                    final String name = c.getName();
                                    final String label = c.getLabel();
                                    final String value = row.getOrDefault(name, "");
                                    return "<td id='" + name + "' data-label='" + label + "'>" + value + "</td>";
                                })
                                .collect(Collectors.joining());
                        return td;
                    })
                    .map(s -> "<tr>" + s + "</tr>")
                    .collect(Collectors.joining());

            return "<table id='" + dataListName + "'><thead>" + thTd + "</thead><tbody>" + trTd + "</tbody></table>";
        } catch (DataListHtmlException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
            return null;
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
        return LABEL;
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

    @Nonnull
    protected DataList getDataList(@Nonnull AppDefinition appDefinition, @Nonnull String dataListId) throws DataListHtmlException {
        final DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) AppUtil.getApplicationContext().getBean("datalistDefinitionDao");
        final DataListService dataListService = (DataListService) AppUtil.getApplicationContext().getBean("dataListService");

        // get dataList definition
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(dataListId, appDefinition);
        if (datalistDefinition == null) {
            throw new DataListHtmlException("DataList Definition for dataList [" + dataListId + "] not found");
        }

        DataList dataList = Optional.of(datalistDefinition)
                .map(DatalistDefinition::getJson)
                .map(it -> AppUtil.processHashVariable(it, null, null, null))
                .map(it -> dataListService.fromJson(it))
                .orElseThrow(() -> new DataListHtmlException("Error generating dataList [" + dataListId + "]"));

        // check permission
//        if (!isAuthorize(dataList)) {
//            throw new DataListHtmlException("User [" + WorkflowUtil.getCurrentUsername() + "] is not authorized to access datalist [" + dataListId + "]");
//        }

        return dataList;
    }

    /**
     * Check datalist authorization
     * Restrict if no permission is set and user is anonymous
     *
     * @param dataList
     * @return
     */
    protected boolean isAuthorize(@Nonnull DataList dataList) {
        final DataListService dataListService = (DataListService) AppUtil.getApplicationContext().getBean("dataListService");

        final boolean isPermissionSet = dataList.getPermission() != null;
        return !isPermissionSet && isDefaultUserToHavePermission() || isPermissionSet && dataListService.isAuthorize(dataList);
    }

    protected boolean isDefaultUserToHavePermission() {
        return !WorkflowUtil.isCurrentUserAnonymous();
//        return WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN);
    }
}
