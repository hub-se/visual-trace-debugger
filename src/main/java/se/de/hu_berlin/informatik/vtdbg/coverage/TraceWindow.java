package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.util.Query;
import de.unisb.cs.st.sequitur.input.InputSequence;

import javax.swing.*;
import java.io.IOException;
import java.util.Map;

public class TraceWindow {
    private JPanel content;
    private JTextPane textPane1;
    private JButton button1;

    private Project project;

    private static final Logger LOG = Logger.getInstance(TraceWindow.class);


    public TraceWindow(Project project) {
        this.project = project;
        showButtonDemo();
    }

    public JPanel getContent() {
        return content;
    }

    public void setTextPane1(String trace) {
        textPane1.setText(trace);
    }


    //für @Enrico: hier kann man schon auf die Daten von @traces zugreifen
    public void setTrace(Map<Long, byte[]> traces, Map<Integer, String> idToClassNameMap) {
        if (traces != null && idToClassNameMap != null) {
            for (Map.Entry<Long, byte[]> entry : traces.entrySet()) {
                InputSequence<Long> sequence = null;
                try {
                    sequence = SequiturUtils.getInputSequenceFromByteArray(entry.getValue(), Long.class);
                } catch (IOException | ClassNotFoundException e) {
                }

                if (sequence != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Long encodedStatement : sequence) {
                        sb.append(ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap))
                                .append(": ")
                                .append(ClassLineEncoding.getLineNUmber(encodedStatement))
                                .append(System.lineSeparator());
                    }

                    //here only for testing / showing purposes
                    //these data should be passed to the navigateToClass() on click
                    setTextPane1("Thread " + entry.getKey() + " -> " + System.lineSeparator() +
                            sb.toString());
                }
            }
        }
    }


    /** function only for testing / showing purposes
     * jumps to given line (here: 13) of the given class (here: Main)
     * className and line come from the Trace-Diagram by clicking on it
     **/

    private void showButtonDemo() {
        button1.addActionListener(e -> {
            navigateToClass(project, "com.company.Main", 13);

        });

    }

    public void navigateToClass(Project project, String className, int line) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null) {
            LOG.warn("Class not found");
            return;
        }
        // we need to use line-1 because OpenFileDescriptor is indexing from 0
        new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), line-1, 0).navigate(true);
    }
}
