package se.de.hu_berlin.informatik.vtdbg.samples.services;

import com.intellij.openapi.components.Service;

// https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html#light-services
@Service
public final class AnotherService {

    public String anotherServiceMethod(String parameter, boolean b) {
        return b ? parameter + " is visible" : parameter + " is not visible";
    }

}
