package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MyToolWindowFactory implements ToolWindowFactory {
    public static final String ID = "MyToolWindowFactory";
    TraceWindow myToolWindow;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        myToolWindow = new TraceWindow(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(ChangeActionNotifier.CHANGE_ACTION_TOPIC, new ChangeActionNotifier() {
            @Override
            public void changeTrace(Map<Long, byte[]> traces, Map<Integer, String> idToClassNameMap) {
                //traces und idToClassMap hier zum ToolWindow übertragen, damit man vom Plugin aus
                //darauf Zugriff hat
                myToolWindow.setTrace(traces, idToClassNameMap);
            }
        });
    }
}