package com.nes.core;

import com.nes.util.NESException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.nes.util.Constant.iNESFileMagic;

/**
 * Created by RoyChan on 2018/3/8.
 */

class iNESFileHeader{
    int Magic;// iNES magic number
    byte NumPRG;// number of PRG-ROM banks (16KB each)
    byte NumCHR;// number of CHR-ROM banks (8KB each)
    byte Control1;// control bits
    byte Control2;// control bits
    byte NumRAM;// PRG-RAM size (x 8KB)
    byte[] padding;// unused padding 7

    public iNESFileHeader(ByteBuffer bb){
        this.Magic = bb.getInt();
        this.NumPRG = bb.get();
        this.NumCHR = bb.get();
        this.Control1 = bb.get();
        this.Control2 = bb.get();
        this.NumRAM = bb.get();
//        this.padding = bb.get(bytes, 9, 7).array();
    }

    @Override
    public String toString() {
        return "iNESFileHeader{" +
                "Magic=" + Integer.toHexString(Magic) +
                ", NumPRG=" + Integer.toHexString(NumPRG) +
                ", NumCHR=" + Integer.toHexString(NumCHR) +
                ", Control1=" + Integer.toHexString(Control1) +
                ", Control2=" + Integer.toHexString(Control2) +
                ", NumRAM=" + Integer.toHexString(NumRAM) +
                ", padding=" + Arrays.toString(padding) +
                '}';
    }
}

public class Ines {

    // LoadNESFile reads an iNES file (.nes) and returns a Cartridge on success.
    // http://wiki.nesdev.com/w/index.php/INES
    // http://nesdev.com/NESDoc.pdf (page 28)
    public static Cartridge LoadNESFile(String path){
        try {
            // open file
            FileInputStream file = new FileInputStream(new File(path));
            byte[] bytes = IOUtils.toByteArray(file);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            // read file header
            iNESFileHeader header = new iNESFileHeader(bb);


            // verify header magic number
            if(header.Magic != iNESFileMagic){
                throw new NESException("invalid .nes file");
            }

            // mapper type
            byte mapper1 = (byte)(header.Control1 >> 4);
            byte mapper2 = (byte)(header.Control2 >> 4);
            byte mapper = (byte)(mapper1 | mapper2<<4);

            // mirroring type
            byte mirror1 = (byte)(header.Control1 & 1);
            byte mirror2 = (byte)((header.Control1 >> 3) & 1);
            byte mirror = (byte)(mirror1 | mirror2<<1);

            // battery-backed RAM
            byte battery = (byte)((header.Control1 >> 1) & 1);

            int offset = 16;

            // read trainer if present (unused)
            if((header.Control1&4) == 4){
                byte[] trainer = new byte[512];
                bb.get(trainer, offset, 512);
                offset += 512;
            }

            // read prg-rom bank(s)
            byte[] prg = new byte[header.NumPRG*16384];
            bb.get(prg, offset, header.NumPRG*16384);
            offset += header.NumPRG*16384;

            byte[] chr;
            // provide chr-rom/ram if not in file
            if(header.NumCHR == 0){
                chr = new byte[8192];
            }else {
                // read chr-rom bank(s)
                chr = new byte[header.NumCHR*8192];
                bb.get(prg, offset, header.NumCHR*8192);
                offset += header.NumCHR*8192;
            }

            // success
            return new Cartridge(prg, chr, mapper, mirror, battery);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String path = "E:\\chrome_download\\Super Bat Puncher Demo\\Super Bat Puncher Demo.nes";

        FileInputStream file = new FileInputStream(new File(path));
        byte[] bytes = IOUtils.toByteArray(file);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        // read file header
        iNESFileHeader header = new iNESFileHeader(bb);

        System.out.println(header.toString());
    }
}
