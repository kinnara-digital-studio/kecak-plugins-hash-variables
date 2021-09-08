package com.kinnara.kecakplugins.hashvariables;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrganizationHashVariable extends DefaultHashVariablePlugin {
    @Override
    public String getPrefix() {
        return "organization";
    }

    @Override
    public String processHashVariable(String key) {
        final String getter = "get" + key.substring(0,1).toUpperCase() + key.substring(1);

        ApplicationContext applicationContext = AppUtil.getApplicationContext();
        DirectoryManager directoryManager = (DirectoryManager)applicationContext.getBean("directoryManager");
        String username = WorkflowUtil.getCurrentUsername();

        return Optional.of(username)
                .map(directoryManager::getUserByUsername)
                .map(User::getEmployments)
                .map(o -> (Set<Employment>)o)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .map(Employment::getOrganization)
                .map(Try.onFunction( o -> {
                    Method method = Organization.class.getDeclaredMethod(getter);
                    return String.valueOf(method.invoke(o));
                }))
                .orElse("");
    }

    @Override
    public String getName() {
        return getLabel();
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
        return "Organization Hash Variable";
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
        final Collection<String> key = getGetterMethods(Organization.class)
                .stream()
                .map(s -> s.replaceAll("^get", ""))
                .map(s -> s.substring(0, 1).toLowerCase() + s.substring(1))
                .map(s -> getPrefix() + "." + s)
                .collect(Collectors.toList());
        return key;
    }

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
}
