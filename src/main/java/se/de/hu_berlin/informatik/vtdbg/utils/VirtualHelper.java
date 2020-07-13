package se.de.hu_berlin.informatik.vtdbg.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class VirtualHelper {

    //for resolving a minor problem: we can not get exact same path from our project as
    //the path we are working with in the trace and coverage data so we need to reshape it
    //to enable coloring after color button was clicked on and a new editor is opened
    public static String getShortPath(Project project, VirtualFile file) {
        String shortPath = file.getPresentableUrl().replace(project.getBasePath(), "")
                .replace("/src/", "")
                .replace("/test/", "")
                .replace("." + file.getExtension(), "");
        return shortPath;
    }
}
