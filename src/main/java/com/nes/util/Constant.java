package com.nes.util;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class Constant {
    public static final int iNESFileMagic = 0x1a53454e;
    public static final int CPUFrequency = 1789773;
    public static final double frameCounterRate = CPUFrequency / 240.0;
    public static final double sampleRate = CPUFrequency / 44100.0 / 2;

    public static final int ButtonA = 0;
    public static final int ButtonB = 1;
    public static final int ButtonSelect = 2;
    public static final int ButtonStart = 3;
    public static final int ButtonUp = 4;
    public static final int ButtonDown = 5;
    public static final int ButtonLeft = 6;
    public static final int ButtonRight = 7;
}
