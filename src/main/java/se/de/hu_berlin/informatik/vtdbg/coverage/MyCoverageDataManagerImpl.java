// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ven
 */
public class MyCoverageDataManagerImpl extends com.intellij.coverage.CoverageDataManagerImpl {
  private static final Logger LOG = Logger.getInstance(MyCoverageDataManagerImpl.class);

  private final Project myProject;

  public MyCoverageDataManagerImpl(@NotNull Project project) {
    super(project);
    myProject = project;

    final MyCoverageViewSuiteListener coverageViewListener = createMyCoverageViewListener();
    if (coverageViewListener != null) {
      addSuiteListener(coverageViewListener, myProject);
    }
  }

  @Nullable
  protected MyCoverageViewSuiteListener createMyCoverageViewListener() {
    return new MyCoverageViewSuiteListener(this, myProject);
  }

}
