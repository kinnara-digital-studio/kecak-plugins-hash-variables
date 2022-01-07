package com.kinnara.kecakplugins.hashvariables;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.DefaultHashVariablePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StringHashVariable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "string";
    }

    @Override
    public String processHashVariable(String key) {
        final String[] keySplit = key.split("\\.", 2);
        final String operation = Arrays.stream(keySplit).findFirst().orElse("");
        final String parameter = Arrays.stream(keySplit).skip(1).findFirst().orElse("");

        if(parameter.isEmpty()) {
            return "";
        }

        final String[] parameterSplit = parameter.split("/");
        final String source = Arrays.stream(parameterSplit).findFirst().orElse("");
        final String pattern = Arrays.stream(parameterSplit).skip(1).findFirst().orElse("");

        if("replaceAll".equalsIgnoreCase(operation)) {
            final String with = Arrays.stream(parameterSplit).skip(2).findFirst().orElse("");
            return source.replaceAll(pattern, with);
        }

        if("split".equalsIgnoreCase(operation)) {
            final int index = Arrays.stream(parameterSplit)
                    .skip(2)
                    .findFirst()
                    .map(Try.onFunction(Integer::valueOf))
                    .orElse(0);

            return Arrays.stream(source.split(pattern))
                    .skip(index)
                    .findFirst()
                    .orElse("");
        }

        return "";
    }

    @Override
    public String getName() {
        return "String Hash Variable";
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
    public String getLabel() {
        return "String Hash Variable";
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
        final List<String> syntax = new ArrayList<>();
        syntax.add(getPrefix() + ".replaceAll.SOURCE/PATTERN/REPLACE_WITH");
        syntax.add(getPrefix() + ".split.SOURCE/PATTERN/GET_INDEX");
        return syntax;
    }
}
