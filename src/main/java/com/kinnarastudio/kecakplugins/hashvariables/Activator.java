package com.kinnarastudio.kecakplugins.hashvariables;

import java.util.ArrayList;
import java.util.Collection;

import com.kinnarastudio.kecakplugins.hashvariables.formatter.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(ActivityInfoHashVariable.class.getName(), new ActivityInfoHashVariable(), null));
        registrationList.add(context.registerService(ApplicationDefinitionHashVariable.class.getName(), new ApplicationDefinitionHashVariable(), null));
        registrationList.add(context.registerService(ConvertCase.class.getName(), new ConvertCase(), null));
        registrationList.add(context.registerService(DataListHtmlHashVariable.class.getName(), new DataListHtmlHashVariable(), null));
        registrationList.add(context.registerService(FieldLabel.class.getName(), new FieldLabel(), null));
        registrationList.add(context.registerService(GridHtmlTable.class.getName(), new GridHtmlTable(), null));
        registrationList.add(context.registerService(IdGeneratorHashVariable.class.getName(), new IdGeneratorHashVariable(), null));
        registrationList.add(context.registerService(OptionsLabel.class.getName(), new OptionsLabel(), null));
        registrationList.add(context.registerService(OrganizationHashVariable.class.getName(), new OrganizationHashVariable(), null));
        registrationList.add(context.registerService(StringEscape.class.getName(), new StringEscape(), null));
        registrationList.add(context.registerService(StringHashVariable.class.getName(), new StringHashVariable(), null));
        registrationList.add(context.registerService(StringSplitHashVariable.class.getName(), new StringSplitHashVariable(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}