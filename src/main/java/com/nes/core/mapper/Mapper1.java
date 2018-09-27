package com.nes.core.mapper;

import com.nes.core.Console;
import com.nes.core.Cartridge;

import static com.nes.core.mapper.Mirror.*;

/**
 * Created by RoyChan on 2018/3/13.
 */
public class Mapper1 extends Mapper {
    Cartridge Cartridge;
    byte shiftRegister;
    byte control;
    byte prgMode;
    byte chrMode;
    byte prgBank;
    byte chrBank0;
    byte chrBank1;
    int[] prgOffsets;
    int[] chrOffsets;

    public Mapper1(Console console){
//        super(console);
        this.Cartridge = console.Cartridge;
        this.shiftRegister = 0x10;
        this.prgOffsets[1] = this.prgBankOffset(-1);
    }


    public void Step() {
    }

    public byte Read(int address) {
        if(address < 0x2000){
            int bank = address / 0x1000;
            int offset = address % 0x1000;
            return this.Cartridge.CHR[this.chrOffsets[bank]+offset];
        } else if(address >= 0x8000){
            address = address - 0x8000;
            int bank = address / 0x4000;
            int offset = address % 0x4000;
            return this.Cartridge.PRG[this.prgOffsets[bank]+offset];

        }else if(address >= 0x6000){
            return this.Cartridge.SRAM[address-0x6000];
        }else {
            System.out.println(String.format("unhandled mapper1 read at address: 0x%04X", address));
        }
        return 0;
    }

    public void Write(int address, byte value) {
        if(address < 0x2000){
            int bank = address / 0x1000;
            int offset = address % 0x1000;
            this.Cartridge.CHR[this.chrOffsets[bank]+offset] = value;
        }else if(address >= 0x8000){
            this.loadRegister(address, value);
        }else if(address >= 0x6000){
            this.Cartridge.SRAM[address-0x6000] = value;
        }else {
            System.out.println(String.format("unhandled mapper1 write at address: 0x%04X", address));
        }
    }

    public void loadRegister(int address, byte value) {
        if((value&0x80) == 0x80) {
            this.shiftRegister = 0x10;
            this.writeControl((byte) (this.control | 0x0C));
        } else {
            boolean complete = (this.shiftRegister&1) == 1;
            this.shiftRegister >>= 1;
            this.shiftRegister |= (value & 1) << 4;
            if(complete){
                this.writeRegister(address, this.shiftRegister);
                this.shiftRegister = 0x10;
            }
        }
    }

    public void writeRegister(int address, byte value) {
        if(address <= 0x9FFF)
            this.writeControl(value);
        else if(address <= 0xBFFF)
            this.writeCHRBank0(value);
        else if(address <= 0xDFFF)
            this.writeCHRBank1(value);
        else if(address <= 0xFFFF)
            this.writePRGBank(value);
    }

    // Control (internal, $8000-$9FFF)
    public void writeControl(byte value) {
        this.control = value;
        this.chrMode = (byte) ((value >> 4) & 1);
        this.prgMode = (byte) ((value >> 2) & 3);
        int mirror = value & 3;
        switch(mirror){
            case 0:
                this.Cartridge.Mirror = MirrorSingle0;
            case 1:
                this.Cartridge.Mirror = MirrorSingle1;
            case 2:
                this.Cartridge.Mirror = MirrorVertical;
            case 3:
                this.Cartridge.Mirror = MirrorHorizontal;
        }
        this.updateOffsets();
    }

    // CHR bank 0 (internal, $A000-$BFFF)
    public void writeCHRBank0(byte value) {
        this.chrBank0 = value;
        this.updateOffsets();
    }

    // CHR bank 1 (internal, $C000-$DFFF)
    public void writeCHRBank1(byte value) {
        this.chrBank1 = value;
        this.updateOffsets();
    }

    // PRG bank (internal, $E000-$FFFF)
    public void writePRGBank(byte value) {
        this.prgBank = (byte) (value & 0x0F);
        this.updateOffsets();
    }

    public int prgBankOffset(int index)  {
        if(index >= 0x80){
            index -= 0x100;
        }
        index %= this.Cartridge.PRG.length / 0x4000;
        int offset = index * 0x4000;
        if(offset < 0){
            offset += this.Cartridge.PRG.length;
        }
        return offset;
    }

    public int chrBankOffset(int index)  {
        if(index >= 0x80){
            index -= 0x100;
        }
        index %= this.Cartridge.CHR.length / 0x1000;
        int offset = index * 0x1000;
        if(offset < 0){
            offset += this.Cartridge.PRG.length;
        }
        return offset;
    }

    // PRG ROM bank mode (0, 1: switch 32 KB at $8000, ignoring low bit of bank number;
//                    2: fix first bank at $8000 and switch 16 KB bank at $C000;
//                    3: fix last bank at $C000 and switch 16 KB bank at $8000)
// CHR ROM bank mode (0: switch 8 KB at a time; 1: switch two separate 4 KB banks)
    public void updateOffsets() {
        switch(this.prgMode){
            case 0:
            case 1:
                this.prgOffsets[0] = this.prgBankOffset(this.prgBank & 0xFE);
                this.prgOffsets[1] = this.prgBankOffset(this.prgBank | 0x01);
            case 2:
                this.prgOffsets[0] = 0;
                this.prgOffsets[1] = this.prgBankOffset(this.prgBank);
            case 3:
                this.prgOffsets[0] = this.prgBankOffset(this.prgBank);
                this.prgOffsets[1] = this.prgBankOffset(-1);
        }
        switch(this.chrMode){
            case 0:
                this.chrOffsets[0] = this.chrBankOffset(this.chrBank0 & 0xFE);
                this.chrOffsets[1] = this.chrBankOffset(this.chrBank0 | 0x01);
            case 1:
                this.chrOffsets[0] = this.chrBankOffset(this.chrBank0);
                this.chrOffsets[1] = this.chrBankOffset(this.chrBank1);
        }
    }

}
