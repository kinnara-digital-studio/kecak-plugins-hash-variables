package com.kinnara.kecakplugins.hashvariables;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
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
        return null;
    }

    @Override
    public String getDescription() {
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
        return "";
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> list = new ArrayList();
        list.add(this.getPrefix() + ".STRING[DELIMITER][INDEX]");
        return list;
    }
}
