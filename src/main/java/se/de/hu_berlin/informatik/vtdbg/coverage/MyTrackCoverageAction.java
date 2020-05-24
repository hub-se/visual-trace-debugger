package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageExecutor;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.actions.TrackCoverageAction;
import com.intellij.execution.Executor;
import com.intellij.execution.Location;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.ToggleModelAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.util.ArrayList;
import java.util.List;

public class MyTrackCoverageAction extends TrackCoverageAction {
  private final TestConsoleProperties myProperties;
  private TestFrameworkRunningModel myModel;
  private TreeSelectionListener myTreeSelectionListener;

  public MyTrackCoverageAction(TestConsoleProperties properties) {
    super(properties);
    myProperties = properties;

  }
  @Override
  protected boolean isEnabled() {
    final CoverageSuitesBundle suite = getCurrentCoverageSuite();
    return suite != null && suite.isCoverageByTestApplicable() && suite.isCoverageByTestEnabled();
  }

  @Override
  protected boolean isVisible() {
    final CoverageSuitesBundle suite = getCurrentCoverageSuite();
    return suite != null && suite.isCoverageByTestApplicable();
  }


  @Nullable
  private CoverageSuitesBundle getCurrentCoverageSuite() {
    if (myModel == null) {
      return null;
    }

    final RunProfile runConf = myModel.getProperties().getConfiguration();
    if (runConf instanceof ModuleBasedConfiguration) {

      // if coverage supported for run configuration
      if (CoverageEnabledConfiguration.isApplicableTo((ModuleBasedConfiguration) runConf)) {

        // Get coverage settings
        Executor executor = myProperties.getExecutor();
        if (executor != null && executor.getId().equals(MyCoverageExecutor.EXECUTOR_ID)) {
          return CoverageDataManager.getInstance(myProperties.getProject()).getCurrentSuitesBundle();
        }
      }
    }
    return null;
  }
}
