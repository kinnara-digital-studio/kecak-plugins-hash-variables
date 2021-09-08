package com.kinnara.kecakplugins.hashvariables;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(ActivityInfoHashVariable.class.getName(), new ActivityInfoHashVariable(), null));
        registrationList.add(context.registerService(IdGeneratorHashVariable.class.getName(), new IdGeneratorHashVariable(), null));
        registrationList.add(context.registerService(OrganizationHashVariable.class.getName(), new OrganizationHashVariable(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}