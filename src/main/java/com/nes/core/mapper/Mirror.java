package com.nes.core.mapper;

/**
 * Created by RoyChan on 2018/3/13.
 */
public class Mirror {

   public static final int MirrorHorizontal = 0;
   public static final int MirrorVertical   = 1;
   public static final int MirrorSingle0    = 2;
   public static final int MirrorSingle1    = 3;
   public static final int MirrorFour       = 4;

    static int[][] MirrorLookup = new int[][]{
        {0, 0, 1, 1},
        {0, 1, 0, 1},
        {0, 0, 0, 0},
        {1, 1, 1, 1},
        {0, 1, 2, 3},
    };

    public static int MirrorAddress(byte mode, int address){
        address = (address - 0x2000) % 0x1000;
        int table = address / 0x0400;
        int offset = address % 0x0400;
        return 0x2000 + MirrorLookup[mode][table]*0x0400 + offset;
    }
}
