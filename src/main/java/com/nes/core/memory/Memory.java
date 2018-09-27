package com.nes.core.memory;

/**
 * Created by RoyChan on 2018/3/8.
 */
public interface Memory {
    byte Read(int address);

    void Write(int address, byte value);
}
