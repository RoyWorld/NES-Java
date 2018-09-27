package com.nes.core;

import com.nes.core.memory.PPUMemory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class PPU {
    com.nes.core.memory.Memory Memory;// memory interface
    Console console;// reference to parent object

    public int Cycle;// 0-340
    public int ScanLine;// 0-261, 0-239=visible, 240=post, 241-260=vblank, 261=pre
    public int Frame;// frame counter

    // storage variables
   public byte[] paletteData;
   public byte[] nameTableData;
   public byte[] oamData;

    BufferedImage front;
    BufferedImage back;

    // PPU registers
    int v; // current vram address (15 bit)
    int t;// temporary vram address (15 bit)
    byte x;// fine x scroll (3 bit)
    byte w;// write toggle (1 bit)
    byte f;// even/odd frame flag (1 bit)

    byte register;

    // NMI flags
    boolean nmiOccurred;
    boolean nmiOutput;
    boolean nmiPrevious;
    byte nmiDelay;

    // background temporary variables
    byte nameTableByte;
    byte attributeTableByte;
    byte lowTileByte;
    byte highTileByte;
    long tileData;

    // sprite temporary variables
    int spriteCount;
    int[] spritePatterns;
    byte[] spritePositions;
    byte[] spritePriorities;
    byte[] spriteIndexes;

    // $2000 PPUCTRL
    byte flagNameTable;// 0: $2000; 1: $2400; 2: $2800; 3: $2C00
    byte flagIncrement;// 0: add 1; 1: add 32
    byte flagSpriteTable;// 0: $0000; 1: $1000; ignored in 8x16 mode
    byte flagBackgroundTable;// 0: $0000; 1: $1000
    byte flagSpriteSize;// 0: 8x8; 1: 8x16
    byte flagMasterSlave;// 0: read EXT; 1: write EXT

    // $2001 PPUMASK
    byte flagGrayscale;          // 0: color; 1: grayscale
    byte flagShowLeftBackground; // 0: hide; 1: show
    byte flagShowLeftSprites;    // 0: hide; 1: show
    byte flagShowBackground;     // 0: hide; 1: show
    byte flagShowSprites;        // 0: hide; 1: show
    byte flagRedTint;            // 0: normal; 1: emphasized
    byte flagGreenTint;// 0: normal; 1: emphasized
    byte flagBlueTint;// 0: normal; 1: emphasized

    // $2002 PPUSTATUS
    byte flagSpriteZeroHit;
    byte flagSpriteOverflow;

    // $2003 OAMADDR
    byte oamAddress;

    // $2007 PPUDATA
    byte bufferedData;// for(buffered reads

    public PPU(Console console){
        Memory = new PPUMemory(console);
        
        Reset();
    }

    public void Reset() {
        this.Cycle = 340;
        this.ScanLine = 240;
        this.Frame = 0;
        this.writeControl((byte) 0);
        this.writeMask((byte) 0);
        this.writeOAMAddress((byte) 0);
    }

    public byte readPalette(int address){
        if(address >= 16 && address%4 == 0){
            address -= 16;
        }
        return this.paletteData[address];
    }

    public void writePalette(int address, byte value) {
        if(address >= 16 && address%4 == 0){
            address -= 16;
        }
        this.paletteData[address] = value;
    }

    public byte readRegister(int address){
        switch(address){
            case 0x2002:
                return this.readStatus();
            case 0x2004:
                return this.readOAMData();
            case 0x2007:
                return this.readData();
        }
        return 0;
    }

    public void writeRegister(int address, byte value) {
        this.register = value;
        switch(address){
            case 0x2000:
                this.writeControl(value);
            case 0x2001:
                this.writeMask(value);
            case 0x2003:
                this.writeOAMAddress(value);
            case 0x2004:
                this.writeOAMData(value);
            case 0x2005:
                this.writeScroll(value);
            case 0x2006:
                this.writeAddress(value);
            case 0x2007:
                this.writeData(value);
            case 0x4014:
                this.writeDMA(value);
        }
    }

    // $2000: PPUCTRL
    public void writeControl(byte value) {
        this.flagNameTable = (byte) ((value >> 0) & 3);
        this.flagIncrement = (byte) ((value >> 2) & 1);
        this.flagSpriteTable = (byte) ((value >> 3) & 1);
        this.flagBackgroundTable = (byte) ((value >> 4) & 1);
        this.flagSpriteSize = (byte) ((value >> 5) & 1);
        this.flagMasterSlave = (byte) ((value >> 6) & 1);
        this.nmiOutput = ((value>>7)&1) == 1;
        this.nmiChange();
        // t: ....BA.. ........ = d: ......BA
        this.t = (this.t & 0xF3FF) | (value & 0x03) << 10;
    }

    // $2001: PPUMASK
    public void writeMask(byte value) {
        this.flagGrayscale = (byte) ((value >> 0) & 1);
        this.flagShowLeftBackground = (byte) ((value >> 1) & 1);
        this.flagShowLeftSprites = (byte) ((value >> 2) & 1);
        this.flagShowBackground = (byte) ((value >> 3) & 1);
        this.flagShowSprites = (byte) ((value >> 4) & 1);
        this.flagRedTint = (byte) ((value >> 5) & 1);
        this.flagGreenTint = (byte) ((value >> 6) & 1);
        this.flagBlueTint = (byte) ((value >> 7) & 1);
    }

    // $2002: PPUSTATUS
    public byte readStatus() {
        byte result = (byte) (this.register & 0x1F);
        result |= this.flagSpriteOverflow << 5;
        result |= this.flagSpriteZeroHit << 6;
        if(this.nmiOccurred){
            result |= 1 << 7;
        }
        this.nmiOccurred = false;
        this.nmiChange();
        // w:                   = 0
        this.w = 0;
        return result;
    }

    // $2003: OAMADDR
    public void writeOAMAddress(byte value) {
        this.oamAddress = value;
    }

    // $2004: OAMDATA (read)
    public byte readOAMData() {
        return this.oamData[this.oamAddress];
    }

    // $2004: OAMDATA (write)
    public void writeOAMData(byte value) {
        this.oamData[this.oamAddress] = value;
        this.oamAddress++;
    }

    // $2005: PPUSCROLL
    public void writeScroll(byte value) {
        if(this.w == 0){
            // t: ........ ...HGFED = d: HGFED...
            // x:               CBA = d: .....CBA
            // w:                   = 1
            this.t = (this.t & 0xFFE0) | (value >> 3);
            this.x = (byte) (value & 0x07);
            this.w = 1;
        } else {
            // t: .CBA..HG FED..... = d: HGFEDCBA
            // w:                   = 0
            this.t = (this.t & 0x8FFF) | (((value) & 0x07) << 12);
            this.t = (this.t & 0xFC1F) | (((value) & 0xF8) << 2);
            this.w = 0;
        }
    }

    // $2006: PPUADDR
    public void writeAddress(byte value) {
        if(this.w == 0){
            // t: ..FEDCBA ........ = d: ..FEDCBA
            // t: .X...... ........ = 0
            // w:                   = 1
            this.t = (this.t & 0x80FF) | (((value) & 0x3F) << 8);
            this.w = 1;
        } else {
            // t: ........ HGFEDCBA = d: HGFEDCBA
            // v                    = t
            // w:                   = 0
            this.t = (this.t & 0xFF00) | (value);
            this.v = this.t;
            this.w = 0;
        }
    }

    // $2007: PPUDATA (read)
    public byte readData() {
        byte value = this.Memory.Read(this.v);
        // emulate buffered reads
        if(this.v%0x4000 < 0x3F00){
            byte buffered = this.bufferedData;
            this.bufferedData = value;
            value = buffered;
        } else {
            this.bufferedData = this.Memory.Read(this.v - 0x1000);
        }
        // increment address
        if(this.flagIncrement == 0){
            this.v += 1;
        } else {
            this.v += 32;
        }
        return value;
    }

    // $2007: PPUDATA (write)
    public void writeData(byte value) {
        this.Memory.Write(this.v, value);
        if(this.flagIncrement == 0){
            this.v += 1;
        } else {
            this.v += 32;
        }
    }

    // $4014: OAMDMA
    public void writeDMA(byte value) {
        CPU cpu = this.console.CPU;
        byte address = (byte) (value << 8);
        for(int i = 0; i < 256; i++){
            this.oamData[this.oamAddress] = cpu.Memory.Read(address);
            this.oamAddress++;
            address++;
        }
        cpu.stall += 513;
        if(cpu.Cycles%2 == 1){
            cpu.stall++;
        }
    }

// NTSC Timing Helper Functions

    public void incrementX() {
        // increment hori(v)
        // if(coarse X == 31
        if((this.v&0x001F) == 31){
            // coarse X = 0
            this.v &= 0xFFE0;
            // switch(horizontal nametable
            this.v ^= 0x0400;
        } else {
            // increment coarse X
            this.v++;
        }
    }

    public void incrementY() {
        // increment vert(v)
        // if(fine Y < 7
        if((this.v&0x7000) != 0x7000){
            // increment fine Y
            this.v += 0x1000;
        } else {
            // fine Y = 0
            this.v &= 0x8FFF;
            // let y = coarse Y
            int y = (this.v & 0x03E0) >> 5;
            if(y == 29){
                // coarse Y = 0
                y = 0;
                // switch(vertical nametable
                this.v ^= 0x0800;
            } else if(y == 31){
                // coarse Y = 0, nametable not switched
                y = 0;
            } else {
                // increment coarse Y
                y++;
            }
            // put coarse Y back into v
            this.v = (this.v & 0xFC1F) | (y << 5);
        }
    }

    public void copyX(){
        // hori(v) = hori(t)
        // v: .....F.. ...EDCBA = t: .....F.. ...EDCBA
        this.v = (this.v & 0xFBE0) | (this.t & 0x041F);
    }

    public void copyY() {
        // vert(v) = vert(t)
        // v: .IHGF.ED CBA..... = t: .IHGF.ED CBA.....
        this.v = (this.v & 0x841F) | (this.t & 0x7BE0);
    }

    public void nmiChange() {
        boolean nmi = this.nmiOutput && this.nmiOccurred;
        if(nmi && !this.nmiPrevious){
            // TODO: this fixes some games but the delay shouldn't have to be so
            // long, so the timings are off somewhere
            this.nmiDelay = 15;
        }
        this.nmiPrevious = nmi;
    }

    public void setVerticalBlank() {
        this.front = this.back;
        this.back = this.front;
        this.nmiOccurred = true;
        this.nmiChange();
    }

    public void clearVerticalBlank() {
        this.nmiOccurred = false;
        this.nmiChange();
    }

    public void fetchNameTableByte() {
        byte address = (byte) (0x2000 | (v & 0x0FFF));
        this.nameTableByte = this.Memory.Read(address);
    }

    public void fetchAttributeTableByte() {
        byte address = (byte) (0x23C0 | (v & 0x0C00) | ((v >> 4) & 0x38) | ((v >> 2) & 0x07));
        int shift = ((v >> 4) & 4) | (v & 2);
        this.attributeTableByte = (byte) (((this.Memory.Read(address) >> shift) & 3) << 2);
    }

    public void fetchLowTileByte() {
        int fineY = (this.v >> 12) & 7;
        byte table = this.flagBackgroundTable;
        int tile = this.nameTableByte;
        byte address = (byte) (0x1000*table + tile*16 + fineY);
        this.lowTileByte = this.Memory.Read(address);
    }

    public void fetchHighTileByte() {
        int fineY = (this.v >> 12) & 7;
        byte table = this.flagBackgroundTable;
        int tile = this.nameTableByte;
        byte address = (byte) (0x1000*table + tile*16 + fineY);
        this.highTileByte = this.Memory.Read(address + 8);
    }

    public void storeTileData() {
        int data = 0;
        for(int i = 0; i < 8; i++){
            int a = this.attributeTableByte;
            int p1 = (this.lowTileByte & 0x80) >> 7;
            int p2 = (this.highTileByte & 0x80) >> 6;
            this.lowTileByte <<= 1;
            this.highTileByte <<= 1;
            data <<= 4;
            data |= a | p1 | p2;
        }
        this.tileData |= Long.valueOf(data);
    }

    public int fetchTileData()  {
        return (int) (this.tileData >> 32);
    }

    public byte backgroundPixel(){
        if(this.flagShowBackground == 0){
            return 0;
        }
        int data = this.fetchTileData() >> ((7 - this.x) * 4);
        return (byte) (data & 0x0F);
    }

    public int[] spritePixel(){
        int[] b = new int[2];
        b[0] = 0;
        b[1] = 0;
        if(this.flagShowSprites == 0){

            return b;
        }
        for(int i = 0; i < this.spriteCount; i++){
            int offset = (this.Cycle - 1) - this.spritePositions[i];
            if(offset < 0 || offset > 7){
                continue;
            }
            offset = 7 - offset;
            int color = (this.spritePatterns[i] >> offset*4) & 0x0F;
            if(color%4 == 0){
                continue;
            }
            b[0] = i;
            b[1] = color;
            return b;
        }
        return b;
    }

    public void renderPixel() {
        int x = this.Cycle - 1;
        int y = this.ScanLine;
        byte background = this.backgroundPixel();
        int[] spritePixel = this.spritePixel();
        int i = spritePixel[0], sprite = spritePixel[1];
        if(x < 8 && this.flagShowLeftBackground == 0){
            background = 0;
        }
        if(x < 8 && this.flagShowLeftSprites == 0){
            sprite = 0;
        }
        boolean b = (background%4) != 0;
        boolean s = sprite%4 != 0;
        byte color;
        if(!b && !s){
            color = 0;
        } else if(!b && s){
            color = (byte) (sprite | 0x10);
        } else if(b && !s){
            color = background;
        } else {
            if(this.spriteIndexes[i] == 0 && x < 255){
                this.flagSpriteZeroHit = 1;
            }
            if(this.spritePriorities[i] == 0){
                color = (byte) (sprite | 0x10);
            } else {
                color = background;
            }
        }
        Color c = Palette.Palette[this.readPalette(color)%64];
        this.back.setRGB(x, y, c.getRGB());
    }

    public int fetchSpritePattern(int i, int row) {
        byte tile = this.oamData[i*4+1];
        byte attributes = this.oamData[i*4+2];
        int address;
        if(this.flagSpriteSize == 0){
            if((attributes&0x80) == 0x80){
                row = 7 - row;
            }
            int table = this.flagSpriteTable;
            address = 0x1000*table + tile*16 + row;
        } else {
            if((attributes&0x80) == 0x80){
                row = 15 - row;
            }
            int table = tile & 1;
            tile &= 0xFE;
            if(row > 7){
                tile++;
                row -= 8;
            }
            address = 0x1000*table + tile*16 + row;
        }
        byte a = (byte) ((attributes & 3) << 2);
        byte lowTileByte = this.Memory.Read(address);
        byte highTileByte = this.Memory.Read(address + 8);
        int data = 0;
        for(int j = 0; j < 8; j++){
            byte p1, p2;
            if((attributes&0x40) == 0x40){
                p1 = (byte) ((lowTileByte & 1) << 0);
                p2 = (byte) ((highTileByte & 1) << 1);
                lowTileByte >>= 1;
                highTileByte >>= 1;
            } else {
                p1 = (byte) ((lowTileByte & 0x80) >> 7);
                p2 = (byte) ((highTileByte & 0x80) >> 6);
                lowTileByte <<= 1;
                highTileByte <<= 1;
            }
            data <<= 4;
            data |= a | p1 | p2;
        }
        return data;
    }

    public void evaluateSprites() {
        int h;
        if(this.flagSpriteSize == 0){
            h = 8;
        } else {
            h = 16;
        }
        int count = 0;
        for(int i = 0; i < 64; i++){
            byte y = this.oamData[i*4+0];
            byte a = this.oamData[i*4+2];
            byte x = this.oamData[i*4+3];
            byte row = (byte) (this.ScanLine - y);
            if(row < 0 || row >= h){
                continue;
            }
            if(count < 8){
                this.spritePatterns[count] = this.fetchSpritePattern(i, row);
                this.spritePositions[count] = x;
                this.spritePriorities[count] = (byte) ((a >> 5) & 1);
                this.spriteIndexes[count] = (byte) i;
            }
            count++;
        }
        if(count > 8){
            count = 8;
            this.flagSpriteOverflow = 1;
        }
        this.spriteCount = count;
    }

    // tick updates Cycle, ScanLine and Frame counters
    public void tick() {
        if(this.nmiDelay > 0){
            this.nmiDelay--;
            if(this.nmiDelay == 0 && this.nmiOutput && this.nmiOccurred){
                this.console.CPU.triggerNMI();
            }
        }

        if(this.flagShowBackground != 0 || this.flagShowSprites != 0){
            if(this.f == 1 && this.ScanLine == 261 && this.Cycle == 339){
                this.Cycle = 0;
                this.ScanLine = 0;
                this.Frame++;
                this.f ^= 1;
                return;
            }
        }
        this.Cycle++;
        if(this.Cycle > 340){
            this.Cycle = 0;
            this.ScanLine++;
            if(this.ScanLine > 261){
                this.ScanLine = 0;
                this.Frame++;
                this.f ^= 1;
            }
        }
    }

    // Step executes a single PPU cycle
    public void Step() {
        this.tick();

        boolean renderingEnabled = this.flagShowBackground != 0 || this.flagShowSprites != 0;
        boolean preLine = this.ScanLine == 261;
        boolean visibleLine = this.ScanLine < 240;
        // postLine = this.ScanLine == 240
        boolean renderLine = preLine || visibleLine;
        boolean preFetchCycle = this.Cycle >= 321 && this.Cycle <= 336;
        boolean visibleCycle = this.Cycle >= 1 && this.Cycle <= 256;
        boolean fetchCycle = preFetchCycle || visibleCycle;

        // background logic
        if(renderingEnabled){
            if(visibleLine && visibleCycle){
                this.renderPixel();
            }
            if(renderLine && fetchCycle){
                this.tileData <<= 4;
                switch(this.Cycle % 8){
                    case 1:
                        this.fetchNameTableByte();
                    case 3:
                        this.fetchAttributeTableByte();
                    case 5:
                        this.fetchLowTileByte();
                    case 7:
                        this.fetchHighTileByte();
                    case 0:
                        this.storeTileData();
                }
            }
            if(preLine && this.Cycle >= 280 && this.Cycle <= 304){
                this.copyY();
            }
            if(renderLine){
                if(fetchCycle && this.Cycle%8 == 0){
                    this.incrementX();
                }
                if(this.Cycle == 256){
                    this.incrementY();
                }
                if(this.Cycle == 257){
                    this.copyX();
                }
            }
        }

        // sprite logic
        if(renderingEnabled){
            if(this.Cycle == 257){
                if(visibleLine){
                    this.evaluateSprites();
                } else {
                    this.spriteCount = 0;
                }
            }
        }

        // vblank logic
        if(this.ScanLine == 241 && this.Cycle == 1){
            this.setVerticalBlank();
        }
        if(preLine && this.Cycle == 1){
            this.clearVerticalBlank();
            this.flagSpriteZeroHit = 0;
            this.flagSpriteOverflow = 0;
        }
    }

}
