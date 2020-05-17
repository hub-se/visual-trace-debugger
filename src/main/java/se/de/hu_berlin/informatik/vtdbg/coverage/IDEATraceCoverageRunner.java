// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.coverage.*;
import com.intellij.execution.configurations.SimpleJavaParameters;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.traces.ExecutionTraceCollector;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

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

        try {
            FileInputStream streamIn = new FileInputStream(sessionDataFile.getParent()+"traces.ser");
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Map<Long, byte[]> readCase = (Map<Long, byte[]>) objectinputstream.readObject();
            for (Map.Entry<Long, byte[]> entry : readCase.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());
            }
            objectinputstream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.loadCoverageData(sessionDataFile, baseCoverageSuite);
    }
}