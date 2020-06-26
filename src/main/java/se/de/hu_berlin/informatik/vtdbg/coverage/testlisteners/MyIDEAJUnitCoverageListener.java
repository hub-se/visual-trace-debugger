package se.de.hu_berlin.informatik.vtdbg.coverage.testlisteners;

import com.intellij.coverage.listeners.CoverageListener;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.execution.junit.IDEAJUnitListener;

@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class MyIDEAJUnitCoverageListener extends CoverageListener implements IDEAJUnitListener {

  public void testStarted(String className, String methodName) {
//    final Object data = getData();
//    ((com.intellij.rt.coverage.data.ProjectData)data).testStarted(sanitize(className, methodName));
  }

  public void testFinished(String className, String methodName) {
//    final Object data = getData();
//    ((com.intellij.rt.coverage.data.ProjectData)data).testEnded(sanitize(className, methodName));
  }


}