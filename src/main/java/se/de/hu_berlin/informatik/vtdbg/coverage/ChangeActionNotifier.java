package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.util.messages.Topic;

public interface ChangeActionNotifier {

    Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC = Topic.create("custom name", ChangeActionNotifier.class);

    void changeTrace(String text);
}
