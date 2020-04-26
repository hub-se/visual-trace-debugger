package se.de.hu_berlin.informatik.vtdbg.samples.services.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import se.de.hu_berlin.informatik.vtdbg.samples.services.AnotherService;
import se.de.hu_berlin.informatik.vtdbg.samples.services.MyProjectService;

public class MyProjectServiceImpl implements MyProjectService {

    private final Project myProject;

    public MyProjectServiceImpl(Project project) {
        myProject = project;
    }

    @Override
    public void someServiceMethod(String parameter, boolean b) {
        AnotherService anotherService = myProject.getService(AnotherService.class);
        String result = anotherService.anotherServiceMethod(parameter, b);

        String dlgTitle = "Some Service Test";
        Messages.showMessageDialog(myProject, result, dlgTitle, Messages.getInformationIcon());
    }

}
