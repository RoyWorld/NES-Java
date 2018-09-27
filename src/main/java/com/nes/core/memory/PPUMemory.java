package com.nes.core.memory;

import com.nes.core.Console;

/**
 * Created by RoyChan on 2018/3/12.
 */
public class PPUMemory implements Memory {
    
    Console console;
    
    public PPUMemory(Console console){
        this.console = console;
    }

    public final int MirrorHorizontal = 0;
    public final int MirrorVertical   = 1;
    public final int MirrorSingle0    = 2;
    public final int MirrorSingle1    = 3;
    public final int MirrorFour       = 4;


    int[][] MirrorLookup = new int[][]{
        {0, 0, 1, 1},
        {0, 1, 0, 1},
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 1, 2, 3},
    };

    public int MirrorAddress(byte mode, int address) {
        address = (address - 0x2000) % 0x1000;
        int table = address / 0x0400;
        int offset = address % 0x0400;
        return 0x2000 + MirrorLookup[mode][table]*0x0400 + offset;
    }


    @Override
    public byte Read(int address) {
        address = address % 0x4000;
        if(address < 0x2000){
            return this.console.Mapper.Read(address);
        }else if(address < 0x3F00){
            byte mode = this.console.Cartridge.Mirror;
            return this.console.PPU.nameTableData[MirrorAddress(mode, address)%2048];
        }else if(address < 0x4000){
            return this.console.PPU.readPalette(address % 32);
        }else {
            System.out.println(String.format("unhandled ppu thisory read at address) 0x%04X", address));
        }
        return 0;
    }

    @Override
    public void Write(int address, byte value) {
        address = address % 0x4000;
        if(address < 0x2000){
            this.console.Mapper.Write(address, value);
        }
        else if(address < 0x3F00){
            byte mode = this.console.Cartridge.Mirror;
            this.console.PPU.nameTableData[MirrorAddress(mode, address)%2048] = value;
        }
        else if(address < 0x4000){
            this.console.PPU.writePalette(address%32, value);
        }else {
            System.out.println(String.format("unhandled ppu thisory write at address) 0x%04X", address));
        }
    }
}
