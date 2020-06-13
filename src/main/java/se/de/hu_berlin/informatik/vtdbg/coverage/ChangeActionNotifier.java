package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.util.messages.Topic;

import java.util.Map;

/**
 * Enables used on the MessageBus to pass data from IDEACoverageRunner to MyToolindowFactory (like a callback)
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */

public interface ChangeActionNotifier {

    Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC = Topic.create("custom name", ChangeActionNotifier.class);

    void changeTrace(Map<Long, byte[]> traces,Map<Integer, String> idToClassNameMap);

}
