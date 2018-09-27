package com.nes.core.mapper;

import com.nes.core.Cartridge;
import com.nes.core.Console;

/**
 * Created by RoyChan on 2018/3/8.
 */
public abstract class Mapper {

//    public Mapper(Console console){
//        Cartridge cartridge = console.Cartridge;
//        switch(cartridge.Mapper){
////            case 0:
////                return Mapper2(cartridge);
//            case 1:
//                new Mapper1(cartridge);
////            case 2:
////                return Mapper2(cartridge);
////            case 3:
////                return Mapper3(cartridge);
////            case 4:
////                return Mapper4(console, cartridge);
////            case 7:
////                return Mapper7(cartridge);
//        }
//    }

    public static Mapper newMapper(Console console){
        Cartridge cartridge = console.Cartridge;
        switch(cartridge.Mapper){
//            case 0:
//                return Mapper2(cartridge);
            case 1:
                return  new Mapper1(console);
//            case 2:
//                return Mapper2(cartridge);
//            case 3:
//                return Mapper3(cartridge);
//            case 4:
//                return Mapper4(console, cartridge);
//            case 7:
//                return Mapper7(cartridge);
        }
        return null;
    }

    public abstract byte Read(int address);

    public abstract void Write(int address, byte value);

    public abstract void Step();
}
