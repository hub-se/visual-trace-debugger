package se.de.hu_berlin.informatik.vtdbg.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.ui.JBColor;
import com.intellij.util.Query;
import se.de.hu_berlin.informatik.vtdbg.coverage.view.SBFLScoreBarRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditorUtils {

    private static final Logger LOG = Logger.getInstance(EditorUtils.class);

    final static TextAttributes BACKGROUND = new TextAttributes(null, JBColor.cyan,
            null, EffectType.LINE_UNDERSCORE, Font.PLAIN);
    final static TextAttributes BACKGROUND2 = new TextAttributes(null, JBColor.yellow,
            null, EffectType.LINE_UNDERSCORE, Font.PLAIN);

    final static Map<Float, TextAttributes> colorScheme = new HashMap<>();

    /**
     * @param line      which line we want to jump to
     * @param className class in which line is contained
     * @param project   which project user of the plugin is currently working on
     * @Brief: Implements the "jump" / navigation to a given line.
     * @returns true if class found; false otherwise
     **/
    public static boolean navigateToClass(Project project, String className, int line) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null) {
            LOG.warn("Class not found");
            return false;
        }
        // we need to use line-1 because OpenFileDescriptor is indexing from
        new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), line - 1, 0).navigate(true);
        return true;
    }

    /**
     * @param project which project user of the plugin is currently working on
     * @Brief: sets background for a given line
     * Note: color is set by default to CYAN and line=13 for showing purposes
     **/
    public static void colorRandomLine(Project project) {

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor == null) {
            return;
        }

        // colors a random line
        colorLineInEditor(editor, new Random().nextInt(editor.getDocument().getLineCount()) + 1, true);
    }


//    /**
//     * Colors all the open class based on (SBFL) score
//     *
//     * @param project               current project
//     * @param score                 SBFL score for each executed line
//     * @param removeOldHighlighters whether to remove old highlighters
//     */
//    public static void colorAllOpenClassSBFL(Project project, Map<String, List<Score>> score, boolean removeOldHighlighters) {
//        for (Map.Entry<String, List<Score>> item : score.entrySet()) {
//            EditorUtils.colorClassSBFL(project, item.getKey(), score, false);
//        }
//    }

//    /**
//     * Colors the specified class based on (SBFL) score in the given editor
//     *
//     * @param project               current project
//     * @param score                 SBFL score for each executed line
//     * @param removeOldHighlighters whether to remove old highlighters
//     */
//    public static void colorClassSBFL(Project project, String className, Map<String, List<Score>> score, boolean removeOldHighlighters) {
//        VirtualFile[] open = FileEditorManager.getInstance(project).getOpenFiles();
//        boolean contains = false;
//        for (VirtualFile editor : open) {
//            if (editor.getPresentableUrl().contains(className)) {
//                contains = true;
//                break;
//            }
//        }
//        if (!contains)
//            return;
//        //navigate to class is needed because otherwise it is not possible to color it
//        navigateToClass(project, className, 0);
//
//        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
//
//        if (editor == null) {
//            return;
//        }
//        List<Score> currentScore = score.get(className);
//        colorClassInEditor(editor, currentScore, removeOldHighlighters);
//    }

    public static void colorLineInEditor(Project project, int line, boolean removeOldHighlighters) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (editor == null) {
            return;
        }

        colorLineInEditor(editor, line, removeOldHighlighters);
    }

    /**
     * Colors the specified line in the given editor
     *
     * @param editor                an editor
     * @param line                  the line to color (starts at 1)
     * @param removeOldHighlighters whether to remove old highlighters
     */
    public static void colorLineInEditor(Editor editor, int line, boolean removeOldHighlighters) {
        if (editor == null) {
            return;
        }
        if (removeOldHighlighters) {
            // removes any old highlighters
            editor.getMarkupModel().removeAllHighlighters();
        }
        // decrement line number (index starts at 0)
        --line;
        // returns the start of the line, but doesn't skip whitespaces
        int startOffset = editor.getDocument().getLineStartOffset(line);
        // returns the actual end of the specified line
        int endOffset = editor.getDocument().getLineEndOffset(line);
        // skip whitespace chars at the start of the line
        String text = editor.getDocument().getText(new TextRange(startOffset, endOffset));
        startOffset += text.length() - text.trim().length();

        editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset,
                HighlighterLayer.CARET_ROW, BACKGROUND, HighlighterTargetArea.EXACT_RANGE);
    }

    /**
     * Colors all the open class based on (SBFL) score
     *
     * @param project               current project
     * @param score                 SBFL score for each executed line
     * @param removeOldHighlighters whether to remove old highlighters
     */
    public static void colorAllClassesSBFL(Project project, Map<String, Map<Integer, Float>> score, boolean removeOldHighlighters) {
        // removes any old highlighters
        for (Map.Entry<String, Map<Integer, Float>> item : score.entrySet()) {
            EditorUtils.colorClassSBFL(project, item.getKey(), item.getValue(), removeOldHighlighters);
        }
    }

    /**
     * Colors the current class based on (SBFL) score in the given editor
     *
     * @param editor                an editor
     * @param scores                SBFL score for each executed line
     * @param removeOldHighlighters whether to remove old highlighters
     */
    private static void colorClassInEditor(Editor editor, Map<Integer, Float> scores, boolean removeOldHighlighters) {
        if (editor == null) {
            return;
        }
        if (removeOldHighlighters) {
            // removes any old highlighters
            editor.getMarkupModel().removeAllHighlighters();
        }
        for (Map.Entry<Integer, Float> score : scores.entrySet()) {
            // returns the start of the line, but doesn't skip whitespaces
            int startOffset = editor.getDocument().getLineStartOffset(score.getKey() - 1);
            // returns the actual end of the specified line
            int endOffset = editor.getDocument().getLineEndOffset(score.getKey() - 1);
            // skip whitespace chars at the start of the line
            String text = editor.getDocument().getText(new TextRange(startOffset, endOffset));
            startOffset += text.length() - text.trim().length();

            // assumes scores between 0 and 1, currently
            TextAttributes color = colorScheme.computeIfAbsent((float) Math.round(score.getValue() * 10), k ->
                    new TextAttributes(null, SBFLScoreBarRenderer.getColorForNormalizedScore(k / 10.0f),
                            null, EffectType.LINE_UNDERSCORE, Font.PLAIN));

            editor.getMarkupModel().addRangeHighlighter(startOffset, endOffset,
                    HighlighterLayer.CARET_ROW, color, HighlighterTargetArea.EXACT_RANGE);
        }
    }

    /**
     * Colors the specified class based on (SBFL) score in the given editor
     *
     * @param project               current project
     * @param scores                SBFL score for each executed line
     * @param removeOldHighlighters whether to remove old highlighters
     */
    public static void colorClassSBFL(Project project, String className, Map<Integer, Float> scores, boolean removeOldHighlighters) {
//        VirtualFile[] open = FileEditorManager.getInstance(project).getOpenFiles();
//        boolean contains = false;
//        for (VirtualFile editor : open) {
//            if (editor.getPresentableUrl().contains(className)) {
//                contains = true;
//                break;
//            }
//        }
//        if (!contains)
//            return;

        // navigate to class is needed because otherwise it is not possible to color it
        boolean found = navigateToClass(project, className, 0);

        if (found) {
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (removeOldHighlighters) {
                // removes any old highlighters
                editor.getMarkupModel().removeAllHighlighters();
            }

            if (editor == null) {
                return;
            }
            colorClassInEditor(editor, scores, false);
        }
    }
}
