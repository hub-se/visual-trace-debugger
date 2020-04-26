package se.de.hu_berlin.informatik.vtdbg.samples.services;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ProjectCountingService {
    static ProjectCountingService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ProjectCountingService.class);
    }

    void incrProjectCount();

    void decrProjectCount();

    boolean projectLimitExceeded();

    int getProjectCount();

    int getProjectCountLimit();

}
