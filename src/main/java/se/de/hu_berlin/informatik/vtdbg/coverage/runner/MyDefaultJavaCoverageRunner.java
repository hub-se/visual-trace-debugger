// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package se.de.hu_berlin.informatik.vtdbg.coverage.runner;

import com.intellij.coverage.*;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.coverage.CoverageEnabledConfiguration;
import com.intellij.execution.configurations.coverage.JavaCoverageEnabledConfiguration;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.annotations.NotNull;
import se.de.hu_berlin.informatik.vtdbg.coverage.runner.MyCoverageExecutor;

public class MyDefaultJavaCoverageRunner extends DefaultJavaProgramRunner {
  @Override
  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    try {
      // the already existing runner does not recognize our executor, because this check fails!
      // therefore, we need to override this method and use our own executor's id...
      return executorId.equals(MyCoverageExecutor.EXECUTOR_ID) &&
             //profile instanceof ModuleBasedConfiguration &&
             !(profile instanceof RunConfigurationWithSuppressedDefaultRunAction) &&
             profile instanceof RunConfigurationBase &&
             CoverageEngine.EP_NAME.findExtensionOrFail(JavaCoverageEngine.class).isApplicableTo((RunConfigurationBase)profile);
    }
    catch (Exception e) {
      return false;
    }
  }

  @Override
  public RunnerSettings createConfigurationData(@NotNull ConfigurationInfoProvider settingsProvider) {
    return new CoverageRunnerData();
  }

  @NotNull
  @Override
  public String getRunnerId() {
    return "TraceCover";
  }

  @Override
  public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
    RunProfileState currentState = environment.getState();
    if (currentState == null) {
      return;
    }

    ExecutionManager executionManager = ExecutionManager.getInstance(environment.getProject());
    executionManager
            .executePreparationTasks(environment, currentState)
            .onSuccess(__ -> {
              executionManager.startRunProfile(environment, currentState, (ignored) -> {
                if (environment.getRunProfile() instanceof RunConfigurationBase) {
                  final JavaCoverageEnabledConfiguration coverageConfig = JavaCoverageEnabledConfiguration.getFrom((RunConfigurationBase)environment.getRunProfile());

                  // retrieve current set coverage runner and replace with our runner
                  assert coverageConfig != null;
                  CoverageRunner coverageRunner = coverageConfig.getCoverageRunner();
                  coverageConfig.setCoverageRunner(CoverageRunner.getInstance(IDEATraceCoverageRunner.class));

                  // execute!
                  RunContentDescriptor result = doExecute(currentState, environment);

                  // reset the runner to previous state
                  coverageConfig.setCoverageRunner(coverageRunner);

                  return result;
                } else {
                  // original behavior
                  return doExecute(currentState, environment);
                }
              });
            });
  }
}
