package se.de.hu_berlin.informatik.vtdbg.samples.services;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface MyProjectService {
    static MyProjectService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, MyProjectService.class);
    }

    void someServiceMethod(String parameter, boolean b);
}
