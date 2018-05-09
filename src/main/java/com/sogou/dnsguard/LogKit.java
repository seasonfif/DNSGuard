package com.sogou.dnsguard;

import android.util.Log;

/**
 * 目前只是简单地使用系统log打印
 * 后续可以接入log平台，云端监控或者存本地文件分析
 */
class LogKit {

    private static final String TAG = "DNSGuard";

    /**
     * 日志打印级别
     */
    @LogLevel
    private static int sLevel;

    /**
     * 日志打印级别设置
     * @param level
     */
    static void init(@LogLevel int level){
        sLevel = level;
    }

    static void log(String msg) {
        log(TAG, msg);
    }

    static void log(String tag, String msg) {
        log(tag, msg, null);
    }

    static void log(String tag, String msg, Throwable tr) {

        if (sLevel == LogLevel.LOG_NONE) return;

        switch (sLevel){
            case LogLevel.LOG_V:
                Log.v(tag, msg, tr);
                break;
            case LogLevel.LOG_D:
                Log.d(tag, msg, tr);
                break;
            case LogLevel.LOG_I:
                Log.i(tag, msg, tr);
                break;
            case LogLevel.LOG_W:
                Log.w(tag, msg, tr);
                break;
            case LogLevel.LOG_E:
                Log.e(tag, msg, tr);
                break;
        }
    }
}
