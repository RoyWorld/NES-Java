package com.nes.core.memory;


import com.nes.core.Console;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class CPUMemory implements Memory{

    Console console;
    
    public CPUMemory(Console console){
        this.console = console;
    }

    public byte Read(int address)  {
        if(address < 0x2000){
            return this.console.RAM[address%0x0800];
        }
        else if(address < 0x4000) {
            return this.console.PPU.readRegister(0x2000 + address % 8);
        }
        else if(address == 0x4014) {
            return this.console.PPU.readRegister(address);
        }
        else if(address == 0x4015) {
            return this.console.APU.readRegister(address);
        }
        else if(address == 0x4016) {
            return this.console.Controller1.Read();
        }
        else if(address == 0x4017) {
            return this.console.Controller2.Read();
        }
        else if(address < 0x6000) {
            // TODO) I/O registers
        }
        else if(address >= 0x6000) {
            return this.console.Mapper.Read(address);
        }
        else{
            System.out.println(String.format("unhandled cpu memory read at address) 0x%04X", address));
        }
        return 0;
    }

    public void Write(int address, byte value) {
        if(address < 0x2000) {
            this.console.RAM[address % 0x0800] = value;
        }
        else if(address < 0x4000) {
            this.console.PPU.writeRegister(0x2000 + address % 8, value);
        }
        else if(address < 0x4014) {
            this.console.APU.writeRegister(address, value);
        }
        else if(address == 0x4014) {
            this.console.PPU.writeRegister(address, value);
        }
        else if(address == 0x4015) {
            this.console.APU.writeRegister(address, value);
        }
        else if(address == 0x4016) {
            this.console.Controller1.Write(value);
            this.console.Controller2.Write(value);
        }
        else if(address == 0x4017) {
            this.console.APU.writeRegister(address, value);
        }
        else if(address < 0x6000) {
            // TODO) I/O registers
        }
        else if(address >= 0x6000) {
            this.console.Mapper.Write(address, value);
        }
        else{
            System.out.println(String.format("unhandled cpu memory read at address) 0x%04X", address));
        }
    }
}
