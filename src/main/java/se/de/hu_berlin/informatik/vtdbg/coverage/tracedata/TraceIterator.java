package se.de.hu_berlin.informatik.vtdbg.coverage.tracedata;

import com.intellij.openapi.util.Pair;
import com.intellij.rt.coverage.traces.ClassLineEncoding;
import de.unisb.cs.st.sequitur.input.InputSequence;

import java.util.ListIterator;
import java.util.Map;

public class TraceIterator implements ListIterator<Pair<String, Integer>> {

    private ListIterator<Long> iterator;
    private Map<Integer, String> idToClassNameMap;

    public TraceIterator(InputSequence<Long> sequence, Map<Integer, String> idToClassNameMap) {
        this(sequence, idToClassNameMap, 0L);
    }

    public TraceIterator(InputSequence<Long> sequence, Map<Integer, String> idToClassNameMap, long position) {
        this.iterator = sequence.iterator(position);
        this.idToClassNameMap = idToClassNameMap;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Pair<String, Integer> next() {
        Long encodedStatement = iterator.next();
        return new Pair<>(
                ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap),
                ClassLineEncoding.getLineNUmber(encodedStatement));
    }

    @Override
    public boolean hasPrevious() {
        return iterator.hasPrevious();
    }

    @Override
    public Pair<String, Integer> previous() {
        Long encodedStatement = iterator.previous();
        return new Pair<>(
                ClassLineEncoding.getClassName(encodedStatement, idToClassNameMap),
                ClassLineEncoding.getLineNUmber(encodedStatement));
    }

    @Override
    public int nextIndex() {
        return iterator.nextIndex();
    }

    @Override
    public int previousIndex() {
        return iterator.previousIndex();
    }

    @Override
    public void remove() {

    }

    @Override
    public void set(Pair<String, Integer> stringIntegerPair) {

    }

    @Override
    public void add(Pair<String, Integer> stringIntegerPair) {

    }
}
