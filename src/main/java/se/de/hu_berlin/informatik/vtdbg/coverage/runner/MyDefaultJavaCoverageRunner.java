// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package se.de.hu_berlin.informatik.vtdbg.coverage.runner;

import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageRunner;
import com.intellij.coverage.CoverageRunnerData;
import com.intellij.coverage.JavaCoverageEngine;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.configurations.coverage.JavaCoverageEnabledConfiguration;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.NotNull;

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
