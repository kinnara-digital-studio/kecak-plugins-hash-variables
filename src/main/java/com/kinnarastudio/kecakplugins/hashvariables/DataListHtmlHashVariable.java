package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.kecakplugins.hashvariables.exception.DataListHtmlException;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.*;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Usage:
 * dataListHtml.{dataListId}.[{filterName1}={filterValue1}&{filterName2}={filterValue2}&...]
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
            final Map<String, String[]> filters = Arrays.stream(split)
                    .skip(1)
                    .findFirst()
                    .map(s -> s.replaceAll("^\\[|]$", ""))
                    .map(s -> s.split("&"))
                    .stream()
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toMap(s -> s.replaceAll("=.+$", ""), s -> {
                        String value = s.replaceFirst("^[^=]+=", "");
                        return value.split(";");
                    }));

            filters.forEach((k, v) -> LogUtil.info(getClassName(), "Key [" + k + "] Val [" + String.join(" | ", v) + "]"));

            final DataList dataList = getDataList(appDefinition, dataListName);
            getCollectFilters(dataList, filters);

            final DataListCollection<Map<String, String>> rows = dataList.getRows();
            final DataListColumn[] columns = dataList.getColumns();
            final String thTd = Arrays.stream(columns)
                    .map(c -> {
                        final String name = c.getName();
                        final String label = c.getLabel();
                        return "<td id='" + name + "' style=\"border: 1px solid black; padding: 8px;\">" + label + "</td>";
                    })
                    .collect(Collectors.joining(""));

            final String trTd = Optional.ofNullable(rows)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(row -> {
                        final String td = Arrays.stream(columns)
                                .map(c -> {
                                    final String name = c.getName();
                                    final String label = c.getLabel();
                                    final String value = row.getOrDefault(name, "");
                                    return "<td id='" + name + "' data-label='" + label + "' style=\"border: 1px solid black; padding: 8px;\">" + value + "</td>";
                                })
                                .collect(Collectors.joining());
                        return td;
                    })
                    .map(s -> "<tr>" + s + "</tr>")
                    .collect(Collectors.joining());

            String htmlDatalist = "<table id='" + dataListName + "' style=\"border-collapse: collapse; width: 100%;\"><thead><tr>" + thTd + "</tr></thead><tbody>" + trTd + "</tbody></table>";

            return htmlDatalist;
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
     * Check datalist authorization Restrict if no permission is set and user is
     * anonymous
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

    protected void getCollectFilters(@Nonnull final DataList dataList, @Nonnull final Map<String, String[]> filters) {
        Optional.of(dataList)
                .map(DataList::getFilters)
                .stream()
                .flatMap(Arrays::stream)
                .filter(f -> Optional.of(f)
                        .map(DataListFilter::getName)
                        .map(filters::get)
                        .map(l -> l.length > 0)
                        .orElse(false))
                .forEach(f -> {
                    final DataListFilterType type = f.getType();
                    final String name = f.getName();
                    final String defaultValue = String.join(";", filters.get(name));
                    type.setProperty("defaultValue", defaultValue);
                });

        dataList.getFilterQueryObjects();
        dataList.setFilters(null);
    }
}
