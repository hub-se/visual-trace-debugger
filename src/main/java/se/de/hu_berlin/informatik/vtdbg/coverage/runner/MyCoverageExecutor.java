package se.de.hu_berlin.informatik.vtdbg.coverage.runner;

import com.intellij.execution.Executor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.TextWithMnemonic;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;
import se.de.hu_berlin.informatik.vtdbg.coverage.MyCoverageBundle;

import javax.swing.*;

public class MyCoverageExecutor extends Executor {

  public static final String EXECUTOR_ID = "MyCoverage";

  @Override
  @NotNull
  public String getStartActionText() {
   return MyCoverageBundle.message("run.with.coverage.trace");
  }

  @NotNull
  @Override
  public String getStartActionText(@NotNull String configurationName) {
    String configName = StringUtil.isEmpty(configurationName) ? "" : " '" + shortenNameIfNeeded(configurationName) + "'";
    return TextWithMnemonic.parse(MyCoverageBundle.message("run.with.coverage.mnemonic.trace")).replaceFirst("%s", configName).toString();
  }

  @NotNull
  @Override
  public String getToolWindowId() {
    return ToolWindowId.RUN;
  }

  @NotNull
  @Override
  public Icon getToolWindowIcon() {
    return AllIcons.Toolwindows.ToolWindowRun;
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return AllIcons.General.RunWithCoverage;
  }

  @Override
  public Icon getDisabledIcon() {
    return IconLoader.getDisabledIcon(getIcon());
  }

  @Override
  public String getDescription() {
    return MyCoverageBundle.message("run.selected.configuration.with.coverage.enabled");
  }

  @Override
  @NotNull
  public String getActionName() {
    return MyCoverageBundle.message("action.name.cover");
  }

  @Override
  @NotNull
  public String getId() {
    return EXECUTOR_ID;
  }

  @Override
  public String getContextActionId() {
    return "RunMyCoverage";
  }

  @Override
  public String getHelpId() {
    return null;//todo
  }
}