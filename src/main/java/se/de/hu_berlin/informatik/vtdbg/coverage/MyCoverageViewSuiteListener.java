/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.*;
import com.intellij.coverage.view.CoverageViewManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class MyCoverageViewSuiteListener implements CoverageSuiteListener {
  private final CoverageDataManager myDataManager;
  private final Project myProject;

  public MyCoverageViewSuiteListener(CoverageDataManager dataManager, Project project) {
    myDataManager = dataManager;
    myProject = project;
  }

  @Override
  public void beforeSuiteChosen() {
    final CoverageSuitesBundle suitesBundle = myDataManager.getCurrentSuitesBundle();
    if (suitesBundle != null) {
      MyViewManager.getInstance(myProject).closeView();
    }
  }

  @Override
  public void afterSuiteChosen() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return;
    }
    final CoverageSuitesBundle suitesBundle = myDataManager.getCurrentSuitesBundle();
    if (suitesBundle == null) return;
    final MyViewManager viewManager = MyViewManager.getInstance(myProject);
    viewManager.createToolWindow(true);
  }

  private static boolean shouldActivate(CoverageSuitesBundle suitesBundle) {
    final CoverageSuite[] suites = suitesBundle.getSuites();
    for (CoverageSuite suite : suites) {
      if (!(suite.getCoverageDataFileProvider() instanceof DefaultCoverageFileProvider)) return false;
    }
    return true;
  }

}
