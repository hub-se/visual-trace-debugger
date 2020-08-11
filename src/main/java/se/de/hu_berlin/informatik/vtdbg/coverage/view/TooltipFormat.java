package se.de.hu_berlin.informatik.vtdbg.coverage.view;

import com.intellij.rt.coverage.traces.ClassLineEncoding;
import org.jetbrains.annotations.NotNull;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Map;

public class TooltipFormat extends NumberFormat {

    private Map<Integer, String> idToClassNameMap;
    private long[] indices;

    public TooltipFormat(Map<Integer, String> idToClassNameMap, long[] indices) {
        this.idToClassNameMap = idToClassNameMap;
        this.indices = indices;
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return getStringBuffer((long) number, toAppendTo);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return getStringBuffer(number, toAppendTo);
    }

    @NotNull
    private StringBuffer getStringBuffer(long number, StringBuffer toAppendTo) {
        if (number > Integer.MAX_VALUE) {
            return toAppendTo.append("error: index too large");
        } else if (number < 0) {
            return toAppendTo.append("error: index too small");
        } else if (number > indices.length) {
            return toAppendTo.append("error: no index available");
        } else {
            long encodedStatement = indices[(int) number];
            return toAppendTo.append(ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap))
                    .append(":").append(ClassLineEncoding.getLineNUmber(encodedStatement));
        }
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }
}
