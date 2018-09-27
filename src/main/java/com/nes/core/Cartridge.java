package com.nes.core;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class Cartridge {
    public byte[] PRG;// PRG-ROM banks
    public byte[] CHR;// CHR-ROM banks
    public byte[] SRAM;// Save RAM
    public byte Mapper;// mapper type
    public byte Mirror;// mirroring mode
    public byte Battery;// battery present

    public Cartridge(byte[] prg, byte[] chr,  byte mapper,  byte mirror,  byte battery){
        this.SRAM = new byte[0x2000];
        this.PRG = prg;
        this.CHR = chr;
        this.Mapper = mapper;
        this.Mirror = mirror;
        this.Battery = battery;
    }
}
