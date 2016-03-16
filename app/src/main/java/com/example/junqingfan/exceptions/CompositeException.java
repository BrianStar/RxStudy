package com.example.junqingfan.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by junqing.fan on 2016/3/11.
 */
public final class CompositeException extends RuntimeException {
    private static final long serialVersionUID = 3026362227162912146L;

    private final List<Throwable> exceptions;
    private final String message;

    public CompositeException(String messagePrefix, Collection<? extends Throwable> errors) {
        Set<Throwable> deDupedExceptions = new LinkedHashSet<Throwable>();
        List<Throwable> _exceptions = new ArrayList<Throwable>();
        if (errors != null) {
            for (Throwable ex :
                    errors) {
                if (ex instanceof CompositeException) {
                    deDupedExceptions.addAll(((CompositeException) ex).getExceptions());
                } else if (ex != null) {
                    deDupedExceptions.add(ex);
                } else {
                    deDupedExceptions.add(new NullPointerException());
                }
            }
        } else {
            deDupedExceptions.add(new NullPointerException());
        }

        _exceptions.addAll(deDupedExceptions);

        this.exceptions = Collections.unmodifiableList(_exceptions);
        this.message = exceptions.size() + "exceptions occurred.";

    }

    public CompositeException(Collection<? extends Throwable> errors) {
        this(null, errors);
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private Throwable cause = null;

    @Override
    public synchronized Throwable getCause() {
        //return cause;
        if (cause == null) {
            CompositeExceptionCausalChain _cause = new CompositeExceptionCausalChain();
            Set<Throwable> seenCauses = new HashSet<Throwable>();

            Throwable chain = _cause;
            for (Throwable e :
                    exceptions) {
                if (seenCauses.contains(e)) {
                    continue;
                }

                seenCauses.add(e);
                List<Throwable> listOfCauses = getListOfCauses(e);

                for (Throwable child :
                        listOfCauses) {
                    if (seenCauses.contains(child)) {
                        e = new RuntimeException("Duplicate found in causal chain so cropping to prevent loop...");
                        continue;
                    }
                    seenCauses.add(child);
                }
                try {
                    chain.initCause(e);
                } catch (Throwable t) {

                }
                chain = chain.getCause();
            }
            cause = _cause;
        }
        return cause;
    }

    private final List<Throwable> getListOfCauses(Throwable ex) {
        ArrayList<Throwable> list = new ArrayList<Throwable>();
        Throwable root = ex.getCause();
        if (root == null) {
            return list;
        } else {
            while (true) {
                list.add(root);
                if (root.getCause() == null) {
                    return list;
                } else {
                    root = root.getCause();
                }
            }
        }
    }


    final static class CompositeExceptionCausalChain extends RuntimeException {
        private static final long serialVersionUID = 3875212506787802066L;
        static String MESSAGE = "Chain of Causes for CompositeException In Order Received =>";

        @Override
        public String getMessage() {
            return MESSAGE;
        }
    }


    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream err) {
        printStackTrace(new WrappedPrintStream(err));
    }

    @Override
    public void printStackTrace(PrintWriter err) {
        printStackTrace(new WrappedPrintWriter(err));
    }


    private void printStackTrace(PrintStreamOrWriter s) {
        StringBuilder bldr = new StringBuilder();
        bldr.append(this).append("\n");
        for (StackTraceElement myStackElement : getStackTrace()) {
            bldr.append("\tat ").append(myStackElement).append("\n");
        }
        int i = 1;
        for (Throwable ex : exceptions) {
            bldr.append("  ComposedException ").append(i).append(" :").append("\n");
            appendStackTrace(bldr, ex, "\t");
            i++;
        }
        synchronized (s.lock()) {
            s.println(bldr.toString());
        }
    }

    private void appendStackTrace(StringBuilder bldr, Throwable ex, String prefix) {
        bldr.append(prefix).append(ex).append("\n");
        for (StackTraceElement stackElement : ex.getStackTrace()) {
            bldr.append("\t\tat ").append(stackElement).append("\n");
        }
        if (ex.getCause() != null) {
            bldr.append("\tCaused by: ");
            appendStackTrace(bldr, ex.getCause(), "");
        }
    }


    private static class WrappedPrintStream extends PrintStreamOrWriter {

        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        @Override
        Object lock() {
            return printStream;
        }

        @Override
        void println(Object o) {
            printStream.println(o);
        }
    }

    private static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }


        @Override
        Object lock() {
            return printWriter;
        }

        @Override
        void println(Object o) {
            printWriter.println(o);
        }
    }

    private abstract static class PrintStreamOrWriter {
        /**
         * Returns the object to be locked when using this StreamOrWriter
         */
        abstract Object lock();

        /**
         * Prints the specified string as a line on this StreamOrWriter
         */
        abstract void println(Object o);
    }

}
