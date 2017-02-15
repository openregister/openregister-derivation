package uk.gov.register.derivation.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PairBufferedReader {

    private BufferedReader bufferedReader;

    public PairBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public Stream<String> readPairs() {
        Iterator<String> iter = new Iterator<String>() {
            String nextPair = null;

            @Override
            public boolean hasNext() {
                if (nextPair != null) {
                    return true;
                } else {
                    try {
                        String nextLine = bufferedReader.readLine();
                        if (nextLine == null) {
                            return false;
                        }
                        String lineAfterNext = bufferedReader.readLine();
                        if (lineAfterNext == null) {
                            return false;
                        }
                        StringBuilder builder = new StringBuilder(nextLine);
                        builder.append("\n");
                        builder.append(lineAfterNext);
                        nextPair = builder.toString();
                        return true;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public String next() {
                if (nextPair != null || hasNext()) {
                    String pair = nextPair;
                    nextPair = null;
                    return pair;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
