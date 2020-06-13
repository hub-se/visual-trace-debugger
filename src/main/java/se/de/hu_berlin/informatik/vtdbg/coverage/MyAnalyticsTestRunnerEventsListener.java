package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Get testData from the onTestingFinished function to see which tests failed and which passed
 *
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */
public class MyAnalyticsTestRunnerEventsListener implements SMTRunnerEventsListener {
    TraceWindow myToolWindow;

    public MyAnalyticsTestRunnerEventsListener(TraceWindow myToolWindow) {
        this.myToolWindow = myToolWindow;
    }

    @Override
    public void onTestingStarted(@NotNull SMTestProxy.SMRootTestProxy testsRoot) {

    }

    @Override
    public void onTestingFinished(@NotNull SMTestProxy.SMRootTestProxy testsRoot) {
        myToolWindow.setTestResult(testsRoot.getChildren());
    }

    @Override
    public void onTestsCountInSuite(int count) {

    }

    @Override
    public void onTestStarted(@NotNull SMTestProxy test) {

    }

    @Override
    public void onTestFinished(@NotNull SMTestProxy test) {

    }

    @Override
    public void onTestFailed(@NotNull SMTestProxy test) {

    }

    @Override
    public void onTestIgnored(@NotNull SMTestProxy test) {

    }

    @Override
    public void onSuiteFinished(@NotNull SMTestProxy suite) {

    }

    @Override
    public void onSuiteStarted(@NotNull SMTestProxy suite) {

    }

    @Override
    public void onCustomProgressTestsCategory(@Nullable String categoryName, int testCount) {

    }

    @Override
    public void onCustomProgressTestStarted() {

    }

    @Override
    public void onCustomProgressTestFailed() {

    }

    @Override
    public void onCustomProgressTestFinished() {

    }

    @Override
    public void onSuiteTreeNodeAdded(SMTestProxy testProxy) {

    }

    @Override
    public void onSuiteTreeStarted(SMTestProxy suite) {

    }
}
