package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import com.intellij.rt.coverage.traces.SequiturUtils;
import com.intellij.ui.JBColor;
import com.intellij.util.Query;
import de.unisb.cs.st.sequitur.input.InputSequence;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Represents the execution Traces of selected tests
 *
 * @author Dorottya Kregl
 * @author kregldor@hu-berlin.de
 * @version 1.0
 * @since 1.0
 */

public class TraceWindow {

    final static TextAttributes BACKGROUND = new TextAttributes(null, JBColor.cyan,
            null, EffectType.LINE_UNDERSCORE, Font.PLAIN);

    final static TextAttributes BACKGROUND2 = new TextAttributes(null, JBColor.yellow,
            null, EffectType.LINE_UNDERSCORE, Font.PLAIN);

    private JPanel content;
    private JTextPane textPane1;
    private JButton button1;
    private JTextPane textPane2;


    List<? extends SMTestProxy> testResults;

    //projectData includes Hits for each line, will need later for the SBFL scores
    private Project project;

    private static final Logger LOG = Logger.getInstance(TraceWindow.class);

    public TraceWindow(Project project) {
        this.project = project;
        showButtonDemo();
    }

    /**
     * @return everything (that is visible) from the TraceWindow
     */
    public JPanel getContent() {
        return content;
    }

    /**
     * @param trace execution trace which is built after running a selection of tests
     *              in the debugging plugin
     */
    public void setTextPane1(String trace) {
        textPane1.setText(trace);
    }

    public void setTestResult(List<? extends SMTestProxy> testInfo) {
        testResults = testInfo;
        textPane2.setText(testInfo.get(0).getStacktrace() == null ? "success" : "failed");
    }

    /**
     * @param traces           compressed data including execution traces, which was
     *                         collected, built and compressed with the sequitur algorithm through an Agent
     * @param idToClassNameMap Map for organized saving of the class names
     * @Brief: re-building/ decompressing and setting the execution trace visible in the TraceWindow
     */

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

                    //printing the traces here only for testing / showing purposes
                    //these data should be passed to the navigateToClass() on click
                    setTextPane1("Thread " + entry.getKey() + " -> " + System.lineSeparator() +
                            sb.toString());
                }
            }
        }
    }


    /**
     * function only for testing / showing purposes
     * jumps to given line (here: 13) of the given class (here: Main)
     * className and line come from the Trace-Diagram by clicking on it
     **/

    private void showButtonDemo() {
        button1.addActionListener(e -> {
            Map<String, List<Score>> map = new HashMap<>();
            map.put("com.company.Main", Arrays.asList(new Score(13, 0.5), new Score(15, 0.2)));
            map.put("com.company.TestClass", Arrays.asList(new Score(13, 0.1), new Score(21, 0.7)));
            for (Map.Entry<String, List<Score>> item : map.entrySet()) {
                navigateToClass(project, item.getKey(), item.getValue());
            }

            //colorLineInFile(project, "com.company.TestClass", Arrays.asList(new Score(13, 0.5), new Score(15, 0.2)));
        });

    }

    /**
     * @param score     which line we want to jump to with score
     * @param className class in which line is contained
     * @param project   which project user of the plugin is currently working on
     * @Brief: Implements the "jump" / navigation to a given line.
     * note: here is line=13 (for showing purposes)
     **/
    public void navigateToClass(Project project, String className, List<Score> score) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null) {
            LOG.warn("Class not found");
            return;
        }
        // we need to use line-1 because OpenFileDescriptor is indexing from
        // navigate to file to logicalLine currently 0
        new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), 0, 0).navigate(true);
        colorLine(project, score);
    }

    /**
     * @param score   which line we want to jump to
     * @param project which project user of the plugin is currently working on
     * @Brief: sets background for a given line
     * Note: color is set by default to CYAN and line=13 for showing purposes
     **/
    public void colorLine(Project project, List<Score> score) {

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        colorLineEditor(editor, score);
    }

    /**
     * This does not work
     **/
    public void colorLineInFile(Project project, String className, List<Score> score) {

        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null) {
            LOG.warn("Class not found");
            return;
        }
        PsiFile file = psiClass.getContainingFile();
        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);

        Editor editor = EditorFactory.getInstance().createEditor(document, project, file.getVirtualFile(), true);

        colorLineEditor(editor, score);
    }

    private void colorLineEditor(Editor editor, List<Score> scores) {
        if (editor == null) {
            return;
        }
        for (Score score : scores) {
            // returns the start of the line, but doesn't skip whitespaces
            int startOffset = editor.getDocument().getLineStartOffset(score.line - 1);
            // returns the actual end of the specified line
            int endOffset = editor.getDocument().getLineEndOffset(score.line - 1);
            // skip whitespace chars at the start of the line
            String text = editor.getDocument().getText(new TextRange(startOffset, endOffset));
            startOffset += text.length() - text.trim().length();


            TextAttributes color = BACKGROUND;
            if (score.value > 0.4)
                color = BACKGROUND2;
            editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset,
                    HighlighterLayer.CARET_ROW, color, HighlighterTargetArea.EXACT_RANGE);
        }
    }

}
