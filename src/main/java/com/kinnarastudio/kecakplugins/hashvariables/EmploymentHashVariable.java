package com.kinnarastudio.kecakplugins.hashvariables;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.*;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Usage: employment.[USERNAME].METHOD
 *
 * USERNAME is optional, default to current username
 * METHOD is if the available property from {@link Employment}
 */
public class EmploymentHashVariable extends DefaultHashVariablePlugin implements HashVariablePlugin {
    public final static String LABEL = "Employment Hash Variable";

    @Override
    public String getPrefix() {
        return "employment";
    }

    @Override
    public String processHashVariable(String key) {
        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final UserDao userDao = (UserDao) applicationContext.getBean("userDao");
        final EmploymentDao employmentDao = (EmploymentDao) applicationContext.getBean("employmentDao");

        final String[] split = key.split("\\.", 2);
        final String username = (split.length == 1) ? WorkflowUtil.getCurrentUsername() : split[0];
        final String method = split[split.length - 1];

        final int index = -1;

        final Stream<String> resultStream = Optional.of(username)
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(Predicate.not(String::isEmpty))
                .map(userDao::getUser)
                .map(User::getEmployments)
                .flatMap(Collection<Employment>::stream)
                .map(Try.onFunction(e -> getGetterMethods(Employment.class)
                        .stream()
                        .filter(s -> s.equalsIgnoreCase("get" + method))
                        .findFirst()
                        .map(Try.onFunction(Employment.class::getMethod))
                        .map(Try.onFunction(m -> m.invoke(e)))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(obj -> {
                    if (obj instanceof Department) {
                        return ((Department) obj).getId();
                    } else if (obj instanceof Organization) {
                        return ((Organization) obj).getId();
                    } else if (obj instanceof EmploymentReportTo) {
                        return ((EmploymentReportTo) obj).getReportTo().getUserId();
                    } else if (obj instanceof Grade) {
                        return ((Grade) obj).getId();
                    } else if (obj instanceof Collection) {
                        return ((Set<?>) obj).stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(";"));
                    } else {
                        return String.valueOf(obj);
                    }
                });

        if (index >= 0) {
            return resultStream.skip(index)
                    .findFirst()
                    .orElseGet(() -> {
                        LogUtil.warn(getClassName(), "Missing employment value for username [" + username + "] method [" + method + "]");
                        return "";
                    });
        } else {
            return resultStream.collect(Collectors.joining(";"));
        }
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
    public Collection<String> availableSyntax() {
        return getGetterMethods(Employment.class)
                .stream()
                .map(s -> s.replaceAll("^get", ""))
                .map(s -> s.substring(0, 1).toLowerCase() + s.substring(1))
                .flatMap(s -> Stream.of(String.join(".", getPrefix(), s), String.join(".", getPrefix(), "[USERNAME]", s)))
                .sorted()
                .collect(Collectors.toList());
    }

    protected Set<String> getGetterMethods(Class<?> cls) {
        return Optional.of(cls.getName())
                .map(Try.onFunction(Class::forName))
                .map(Class::getMethods)
                .stream()
                .flatMap(Arrays::stream)
                .filter(m -> m.getParameterCount() == 0)
                .map(Try.onFunction(Method::getName))
                .filter(s -> s.startsWith("get"))
                .collect(Collectors.toSet());
    }
}
