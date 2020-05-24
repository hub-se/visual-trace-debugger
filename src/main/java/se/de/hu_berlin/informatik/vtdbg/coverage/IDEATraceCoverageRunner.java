// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.CoverageBundle;
import com.intellij.coverage.CoverageSuite;
import com.intellij.coverage.IDEACoverageRunner;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.icons.AllIcons;
import com.intellij.ide.impl.ContentManagerWatcher;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.*;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import com.intellij.rt.coverage.traces.ExecutionTraceCollector;
import com.intellij.rt.coverage.traces.FileUtils;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.MessageBusUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.messages.MessageBus;
import de.unisb.cs.st.sequitur.input.InputSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public class IDEATraceCoverageRunner extends IDEACoverageRunner {
    private static final Logger LOG = Logger.getInstance(IDEATraceCoverageRunner.class);

    @Override
    public void appendCoverageArgument(final String sessionDataFilePath,
                                       final String[] patterns,
                                       final String[] excludePatterns,
                                       final SimpleJavaParameters javaParameters,
                                       final boolean collectLineInfo,
                                       final boolean isSampling,
                                       @Nullable String sourceMapPath) {
        StringBuilder argument = new StringBuilder("-javaagent:");
        String agentPath = handleSpacesInAgentPath(PathUtil.getJarPathForClass(ExecutionTraceCollector.class));
        if (agentPath == null) return;
        argument.append(agentPath);
        argument.append("=");
        try {
            final File tempFile = createTempFile();
            tempFile.deleteOnExit();
            write2file(tempFile, sessionDataFilePath);
            write2file(tempFile, String.valueOf(collectLineInfo));
            write2file(tempFile, Boolean.FALSE.toString()); //append unloaded
            write2file(tempFile, Boolean.FALSE.toString());//merge with existing
            write2file(tempFile, String.valueOf(isSampling));
            if (sourceMapPath != null) {
                write2file(tempFile, Boolean.TRUE.toString());
                write2file(tempFile, sourceMapPath);
            }
            if (patterns != null) {
                writePatterns(tempFile, patterns);
            }
            if (excludePatterns != null) {
                write2file(tempFile, "-exclude");
                writePatterns(tempFile, excludePatterns);
            }
            argument.append(tempFile.getCanonicalPath());
        } catch (IOException e) {
            LOG.info("Coverage was not enabled", e);
            return;
        }

        javaParameters.getVMParametersList().add(argument.toString());
    }

    private static void writePatterns(File tempFile, String[] patterns) throws IOException {
        for (String coveragePattern : patterns) {
            coveragePattern = coveragePattern.replace("$", "\\$").replace(".", "\\.").replaceAll("\\*", ".*");
            if (!coveragePattern.endsWith(".*")) { //include inner classes
                coveragePattern += "(\\$.*)*";
            }
            write2file(tempFile, coveragePattern);
        }
    }


    @Override
    @NotNull
    public String getPresentableName() {
        return "IntelliJ Trace IDEA";
    }

    @Override
    @NotNull
    public String getId() {
        return "traceidea";
    }

    @Override
    @NotNull
    public String getDataFileExtension() {
        return "itc";
    }

    @Override
    public boolean isCoverageByTestApplicable() {
        return true;
    }

    @Override
    public ProjectData loadCoverageData(@NotNull File sessionDataFile, @Nullable CoverageSuite baseCoverageSuite) {
        System.out.println("loadCoverageData");

        Map<Long, byte[]> traces = null;
        Map<Integer, String> idToClassNameMap = null;

        String file = FileUtils.getFilePathUniqueToSessionFile(
                sessionDataFile, ExecutionTraceCollector.TRACE_FILE_ID);
        try (FileInputStream streamIn = new FileInputStream(file)) {
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            traces = (Map<Long, byte[]>) objectinputstream.readObject();
            idToClassNameMap = (Map<Integer, String>) objectinputstream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Could not read file " + file, e);
        }

        Project project = ProjectManager.getInstance().getDefaultProject();
   /*     final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow consoleToolWindow = toolWindowManager.getToolWindow(MyToolWindowFactory.ID);
        if (consoleToolWindow == null) {
            consoleToolWindow = toolWindowManager.registerToolWindow(MyToolWindowFactory.ID, true, ToolWindowAnchor.RIGHT);
            consoleToolWindow.setIcon(AllIcons.General.Modified);
        }
    */
        //     MyViewManager.getInstance(project).activateToolwindow(new TraceWindow(),true);


        if (traces != null && idToClassNameMap != null) {
            for (Map.Entry<Long, byte[]> entry : traces.entrySet()) {
                InputSequence<Long> sequence = null;
                try {
                    sequence = SequiturUtils.getInputSequenceFromByteArray(entry.getValue(), Long.class);
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error("Could not read execution trace.", e);
                }

                if (sequence != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Long encodedStatement : sequence) {
                        sb.append(ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap))
                                .append(": ")
                                .append(ClassLineEncoding.getLineNUmber(encodedStatement))
                                .append(System.lineSeparator());
                    }
                    MessageBus bus = ApplicationManager.getApplication().getMessageBus();
                    ChangeActionNotifier changeActionNotifier = bus.syncPublisher(ChangeActionNotifier.CHANGE_ACTION_TOPIC);
                    changeActionNotifier.changeTrace("Thread " + entry.getKey() + " -> " + System.lineSeparator() +
                            sb.toString());
                    result = "Thread " + entry.getKey() + " -> " + System.lineSeparator() +
                            sb.toString();
                    System.out.println("Thread " + entry.getKey() + " -> " + System.lineSeparator() +
                            sb.toString());
                }
            }
        }

        return super.loadCoverageData(sessionDataFile, baseCoverageSuite);
    }

    static String result;

}