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
import se.de.hu_berlin.informatik.vtdbg.coverage.view.TraceWindow;

import java.awt.*;
import java.util.Random;

public class EditorUtils {

    private static final Logger LOG = Logger.getInstance(EditorUtils.class);

    final static TextAttributes BACKGROUND = new TextAttributes(null, JBColor.cyan,
            null, EffectType.LINE_UNDERSCORE, Font.PLAIN);

    /**
     * @param line      which line we want to jump to
     * @param className class in which line is contained
     * @param project   which project user of the plugin is currently working on
     * @Brief: Implements the "jump" / navigation to a given line.
     * note: here is line=13 (for showing purposes)
     **/
    public static void navigateToClass(Project project, String className, int line) {
        Query<PsiClass> search = AllClassesSearch.search(GlobalSearchScope.projectScope(project), project, className::endsWith);
        PsiClass psiClass = search.findFirst();
        if (psiClass == null) {
            LOG.warn("Class not found");
            return;
        }
        // we need to use line-1 because OpenFileDescriptor is indexing from
        new OpenFileDescriptor(project, psiClass.getContainingFile().getVirtualFile(), line - 1, 0).navigate(true);
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

        // removes any old highlighters
        editor.getMarkupModel().removeAllHighlighters();
        // colors a random line
        colorLineInEditor(editor, new Random().nextInt(editor.getDocument().getLineCount()) + 1);
    }

    /**
     * Colors the specified line in the given editor
     * @param editor an editor
     * @param line the line to color (starts at 1)
     */
    public static void colorLineInEditor(Editor editor, int line) {
        if (editor == null) {
            return;
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

}
