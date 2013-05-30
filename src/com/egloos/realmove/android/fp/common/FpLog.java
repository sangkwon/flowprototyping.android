
package com.egloos.realmove.android.fp.common;

import com.egloos.realmove.android.fp.BuildConfig;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class FpLog {

    public static boolean _DEBUGMODE = BuildConfig.DEBUG;

    public static void d(String tag, Object... args) {
        println(android.util.Log.DEBUG, tag, args);
    }

    public static void i(String tag, Object... args) {
        println(android.util.Log.INFO, tag, args);
    }

    public static void w(String tag, Object... args) {
        println(android.util.Log.WARN, tag, args);
    }

    public static void e(String tag, Object... args) {
        println(android.util.Log.ERROR, tag, args);
    }

    public static void v(String tag, Object... args) {
        println(android.util.Log.VERBOSE, tag, args);
    }

    /**
     * 실제로 로그를 찍는 부분.
     * 
     * @param priority
     * @param tag
     * @param args
     */
    private static void println(int level, String tag, Object... args) {
        if (_DEBUGMODE) {
            if (tag != null && args != null && args.length > 0) {
                StringBuffer sb = new StringBuffer();
                StringBuffer sbLater = null; // Intent와 Throwable 은 뒤쪽에 좀 자세히
                                             // 출력되도록 수정했다.
                for (Object arg : args) {
                    if (arg == null) {
                        if (sb.length() > 0)
                            sb.append(' ');
                        sb.append("null");
                    } else if (arg instanceof Intent) {
                        /* Intent 는 그 내용들을 자세히 출력한다. */
                        if (sbLater == null)
                            sbLater = new StringBuffer();
                        Intent intent = (Intent) arg;
                        sbLater.append("Intent action:" + intent.getAction());
                        sbLater.append("\n   type:" + intent.getType());
                        sbLater.append("\n   data:" + intent.getData());
                        sbLater.append("\n   extra:");
                        Bundle extras = intent.getExtras();
                        if (extras != null) {
                            Iterator<String> keys = extras.keySet().iterator();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                sbLater.append(" " + key + "=>" + extras.get(key));
                            }
                        }
                    } else if (arg instanceof Throwable) {
                        /* Exception 은 CallStack 을 전부 출력해준다. */
                        Throwable tr = (Throwable) arg;
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        tr.printStackTrace(pw);
                        if (sbLater == null)
                            sbLater = new StringBuffer();
                        sbLater.append(sw.toString());
                    } else {
                        if (sb.length() > 0)
                            sb.append(' ');
                        sb.append(arg.toString());
                    }
                }

                if (sb != null)
                    android.util.Log.println(level, tag, sb.toString());
                if (sbLater != null)
                    android.util.Log.println(level, tag, sbLater.toString());

            }
        }
    }

    @SuppressLint("NewApi")
    @TargetApi(9)
    public static void setStrictPolicy() {
        if (_DEBUGMODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();
                builder.detectNetwork();
                builder.penaltyLog();
                // builder.penaltyDialog();
                StrictMode.setThreadPolicy(builder.build());
            }
        }
    }

}
