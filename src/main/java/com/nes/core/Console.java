package com.nes.core;


import com.nes.goncurrent.Chan;

import java.awt.*;
import java.awt.image.BufferedImage;

import static com.nes.util.Constant.CPUFrequency;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class Console {

    public CPU CPU;
    public APU APU;
    public PPU PPU;
    public Cartridge Cartridge;
    public Controller Controller1;
    public Controller Controller2;
    public com.nes.core.mapper.Mapper Mapper;
    public byte[] RAM;

    public Console(String path){
        try{
            Cartridge = Ines.LoadNESFile(path);
        }catch (Exception e){

        }
        this.RAM = new byte[2048];
        this.Controller1 = new Controller();
        this.Controller2 = new Controller();

        this.Mapper = com.nes.core.mapper.Mapper.newMapper(this);
        this.CPU = new CPU(this);
        this.APU = new APU(this);
        this.PPU = new PPU(this);

    }

    public void reset(){
        this.CPU.Reset();
    }

    public int step(){
        int cpuCycles = this.CPU.Step();
        int ppuCycles = cpuCycles * 3;
        for(int i = 0; i < ppuCycles; i++){
            this.PPU.Step();
            this.Mapper.Step();
        }
        for(int i = 0; i < cpuCycles; i++){
            this.APU.Step();
        }
        return cpuCycles;
    }

    public int stepFrame(){
        int cpuCycles = 0;
        int frame = this.PPU.Frame;
        while(frame == this.PPU.Frame){
            cpuCycles += this.step();
        }
        return cpuCycles;
    }

    public void stepSeconds(float seconds) {
        int cycles = (int) (CPUFrequency * seconds);
        while (cycles > 0){
            cycles -= this.step();
        }
    }

    public BufferedImage buffer()  {
        return this.PPU.front;
    }

    public Color backgroundColor()  {
        return Palette.Palette[this.PPU.readPalette(0)%64];
    }

    public void setButtons1(boolean[] buttons) {
        this.Controller1.SetButtons(buttons);
    }

    public void setButtons2(boolean[] buttons) {
        this.Controller2.SetButtons(buttons);
    }

    public void setAudioChannel(Chan<Float> channel) {
        this.APU.channel = channel;
    }

}
