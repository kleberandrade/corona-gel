package com.kleberandrade.coronagel.helpers;

import java.util.logging.Logger;

public final class LogHelper {
    private LogHelper() { }
    public static void write(String text) { Logger.getLogger("CoronaGel").info(text); }
}
