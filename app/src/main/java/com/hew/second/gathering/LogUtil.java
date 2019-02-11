package com.hew.second.gathering;

public class LogUtil {

    private static final int TRACE_CALLER_COUNT = 2;
    public static String getClassName() {
        String fn = "";
        try {
            fn = new Throwable().getStackTrace()[TRACE_CALLER_COUNT].getClassName();
        } catch (Exception e) {
        }

        return fn;
    }

    public static String getFunctionName() {
        String fn = "";
        try {
            fn = new Throwable().getStackTrace()[TRACE_CALLER_COUNT].getMethodName();
        } catch (Exception e) {
        }

        return fn;
    }
    public static int getLine() {
        int fn = 0;
        try {
            fn = new Throwable().getStackTrace()[TRACE_CALLER_COUNT].getLineNumber();
        } catch (Exception e) {
        }

        return fn;
    }

    public static String getLog(){
        StackTraceElement calledClass = Thread.currentThread().getStackTrace()[3];
        return calledClass.getFileName() + ":" + calledClass.getLineNumber();
    }
}
