package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.ToggleModelAction;
import com.intellij.execution.testframework.ToggleModelActionProvider;

public class MyTrackCoverageActionProvider implements ToggleModelActionProvider {
  @Override
  public ToggleModelAction createToggleModelAction(TestConsoleProperties properties) {
    return new MyTrackCoverageAction(properties);
  }
}