// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.codeInspection.export.ExportToHTMLDialog;
import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageEngine;
import com.intellij.coverage.CoverageSuitesBundle;
import com.intellij.coverage.view.CoverageViewManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyGenerateCoverageReportAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Project project = e.getProject();
    assert project != null;
    MyViewManager.getInstance(project).createToolWindow(true);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();
    presentation.setEnabledAndVisible(false);
    final Project project = e.getProject();
    if (project != null) {
      final CoverageSuitesBundle currentSuite = CoverageDataManager.getInstance(project).getCurrentSuitesBundle();
      if (currentSuite != null) {
        final CoverageEngine coverageEngine = currentSuite.getCoverageEngine();
        if (coverageEngine.isReportGenerationAvailable(project, dataContext, currentSuite)) {
          presentation.setEnabledAndVisible(true);
        }
      }
    }
  }

}
