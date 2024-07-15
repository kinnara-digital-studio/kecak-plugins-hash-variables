package com.kinnarastudio.kecakplugins.hashvariables;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Usage:
 * <li>
 *     <ul>split.string[delimiter][index]</ul>
 * </li>
 */
public class StringSplitHashVariable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "split";
    }

    @Override
    public String processHashVariable(String key) {
        Pattern pOptions = Pattern.compile("(?<=\\[)[^\\]]+(?=\\])");
        Matcher mOptions = pOptions.matcher(key);
        String delimiter = mOptions.find() ? mOptions.group() : ";";
        int index = mOptions.find() ? Integer.parseInt(mOptions.group()) : 0;

        Pattern pStr = Pattern.compile("[^\\[]+(?=\\[)");
        Matcher mStr = pStr.matcher(key);
        String str = mStr.find() ? mStr.group() : "";

        return Optional.of(str)
                .map(s -> s.split(delimiter))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .skip(index)
                .findFirst()
                .orElseGet(() -> {
                    LogUtil.warn(getClassName(), "Error splitting with key [" + key + "]");
                    return "";
                });
    }

    @Override
    public String getName() {
        return "Split String Hash Variable";
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
        return getName();
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
        Collection<String> list = new ArrayList();
        list.add(this.getPrefix() + ".STRING[DELIMITER][INDEX]");
        return list;
    }
}
