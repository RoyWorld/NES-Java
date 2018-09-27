package com.nes.util;

/**
 * Created by RoyChan on 2018/3/9.
 */
public class NESException extends Exception {
    private static final String TAG = "[NESException]";

    public NESException() {
        super();
    }

    public NESException(String message) {
        super("[EngineException] ERROR:" + message);
    }
}
