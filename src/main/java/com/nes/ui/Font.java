package com.nes.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Created by RoyChan on 2018/3/16.
 */
public class Font {
    int[] fontData = new int[]{
                0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x60,
                0x02, 0x03, 0x00, 0x00, 0x00, 0x8F, 0x9F, 0x44, 0x1B, 0x00, 0x00, 0x00,
                0x06, 0x50, 0x4C, 0x54, 0x45, 0x00, 0x00, 0x01, 0xFF, 0xFF, 0xFE, 0x6A,
                0x62, 0xC8, 0x2E, 0x00, 0x00, 0x00, 0x01, 0x62, 0x4B, 0x47, 0x44, 0x00,
                0x88, 0x05, 0x1D, 0x48, 0x00, 0x00, 0x00, 0x09, 0x70, 0x48, 0x59, 0x73,
                0x00, 0x00, 0x0E, 0xC4, 0x00, 0x00, 0x0E, 0xC4, 0x01, 0x95, 0x2B, 0x0E,
                0x1B, 0x00, 0x00, 0x02, 0xD2, 0x49, 0x44, 0x41, 0x54, 0x78, 0xDA, 0xC5,
                0x98, 0x49, 0x62, 0x23, 0x21, 0x0C, 0x45, 0xB5, 0xE1, 0x7E, 0xDA, 0xFC,
                0xFB, 0x5F, 0xA5, 0x4B, 0xE8, 0x4B, 0x88, 0x72, 0x2A, 0x1D, 0x07, 0xB9,
                0x9B, 0xC4, 0x2E, 0x8C, 0xE1, 0x19, 0x8D, 0x0C, 0x22, 0x56, 0x86, 0x5E,
                0xFF, 0x98, 0x2F, 0x81, 0x78, 0x81, 0xB5, 0xC5, 0x77, 0xF2, 0x50, 0xE2,
                0x9B, 0x63, 0x00, 0x90, 0x80, 0x71, 0xD5, 0xAD, 0xE6, 0x9F, 0xC5, 0x81,
                0x90, 0xD9, 0x92, 0xF0, 0x2C, 0xF9, 0x6B, 0x4D, 0x00, 0x00, 0xEA, 0xFD,
                0x31, 0x45, 0x40, 0x4C, 0xD2, 0xDE, 0x50, 0xC4, 0xBB, 0x4B, 0xD0, 0x02,
                0xF0, 0xCF, 0x26, 0x82, 0xBA, 0xE2, 0xE0, 0xDF, 0x06, 0x00, 0x5E, 0xD8,
                0x1D, 0xBB, 0x04, 0xE7, 0x00, 0x4E, 0xC5, 0x44, 0x98, 0xAD, 0xF6, 0x3F,
                0x20, 0xF2, 0x17, 0x11, 0x96, 0x79, 0xBB, 0x00, 0xAE, 0x48, 0xA5, 0x11,
                0xCB, 0x34, 0xAB, 0x19, 0xB1, 0x2C, 0x08, 0x69, 0x03, 0x24, 0x87, 0xD3,
                0xB7, 0xEE, 0x48, 0xEA, 0xEE, 0xCA, 0x05, 0xA0, 0x7D, 0x80, 0xE7, 0x68,
                0x79, 0x0C, 0x9F, 0x9F, 0xB4, 0xFE, 0x10, 0x90, 0x8E, 0x33, 0x03, 0xC9,
                0x2A, 0xEE, 0x28, 0x70, 0xD7, 0x66, 0xBB, 0xBC, 0x2A, 0x5C, 0xDC, 0xE9,
                0x8E, 0x01, 0x23, 0x03, 0x67, 0xD0, 0x89, 0x90, 0x41, 0x54, 0x82, 0x7C,
                0x99, 0x70, 0x33, 0x23, 0x1A, 0x00, 0x48, 0x3B, 0x79, 0x0E, 0x63, 0x58,
                0xB3, 0x1F, 0xE4, 0x0E, 0x70, 0xAF, 0x66, 0xA5, 0x17, 0x10, 0x8A, 0xA3,
                0x00, 0xD1, 0x29, 0x95, 0x7B, 0xF3, 0xA3, 0x45, 0xEB, 0x02, 0xB8, 0x4D,
                0xE7, 0x40, 0x95, 0x32, 0x6D, 0x57, 0xEE, 0x4B, 0xE0, 0xC6, 0xE0, 0x06,
                0xC0, 0x15, 0x36, 0x9E, 0xB2, 0x62, 0x36, 0x37, 0xC5, 0x2D, 0xF3, 0x2E,
                0x33, 0xA2, 0x26, 0x97, 0x63, 0xC0, 0xB4, 0x9E, 0xA5, 0x73, 0xBA, 0xED,
                0x1C, 0x12, 0xEE, 0x6B, 0x7A, 0x02, 0x13, 0x6D, 0x00, 0x8A, 0x2B, 0x3B,
                0xE4, 0x18, 0xF0, 0x46, 0x79, 0x23, 0xEA, 0xDF, 0x00, 0xF8, 0x54, 0xB5,
                0x84, 0xB0, 0xFA, 0x12, 0xE7, 0x15, 0xF5, 0xF4, 0x36, 0xE5, 0xF4, 0x7A,
                0x09, 0xB2, 0x39, 0xF6, 0x18, 0x80, 0x4C, 0xE2, 0xF6, 0xF4, 0x5A, 0xD5,
                0xD7, 0x0A, 0xB6, 0xE8, 0x65, 0xBF, 0x67, 0x7F, 0x69, 0xEA, 0x53, 0x00,
                0xA7, 0xC6, 0x0D, 0x8E, 0xAC, 0xE7, 0xEE, 0xEA, 0x13, 0x90, 0x71, 0x67,
                0x22, 0xB6, 0x03, 0x80, 0x1C, 0x00, 0x44, 0x52, 0xA1, 0x32, 0x03, 0xB0,
                0x12, 0x8A, 0x82, 0x0A, 0x6E, 0x02, 0x00, 0x0F, 0x22, 0x70, 0x9A, 0x54,
                0x76, 0xB0, 0x30, 0x4C, 0x9C, 0x36, 0x40, 0xE9, 0xF8, 0x6A, 0xC6, 0x1B,
                0x60, 0xA0, 0xA4, 0xFB, 0x2E, 0xC0, 0x80, 0xA6, 0xE2, 0x36, 0x57, 0xF6,
                0x3D, 0x4F, 0xBA, 0xF2, 0xA0, 0x52, 0x07, 0x96, 0xD9, 0x19, 0x0B, 0x87,
                0x80, 0xFF, 0x5F, 0xC0, 0xF4, 0x05, 0xA6, 0xAE, 0x3A, 0xC5, 0x4D, 0xB9,
                0x53, 0x0C, 0x6E, 0x7F, 0x7D, 0x59, 0xEF, 0x02, 0x6C, 0x9D, 0xDD, 0x57,
                0xB7, 0xB6, 0x19, 0xB8, 0x1E, 0xC4, 0xB9, 0xC4, 0xF8, 0xC7, 0x8F, 0x00,
                0x64, 0x07, 0x0C, 0x37, 0xD5, 0x04, 0x8C, 0x72, 0x00, 0xE1, 0x36, 0xA0,
                0x1B, 0xC0, 0x85, 0x25, 0x00, 0x1A, 0x89, 0x35, 0x96, 0xFE, 0x75, 0x08,
                0x8B, 0x3E, 0x4D, 0x00, 0x33, 0xE1, 0x0C, 0xE9, 0x75, 0xC6, 0x0B, 0x37,
                0xCD, 0xE5, 0xCE, 0x25, 0xE0, 0x10, 0xEC, 0x2B, 0xCC, 0x39, 0x80, 0x5B,
                0x29, 0x0B, 0xD5, 0x1A, 0xBA, 0x73, 0xB1, 0x71, 0x41, 0x34, 0x96, 0xFF,
                0x3C, 0x04, 0xB5, 0x03, 0x2E, 0x09, 0x36, 0x25, 0xFA, 0xF3, 0x96, 0xD2,
                0xE8, 0xCA, 0x57, 0x89, 0x67, 0x13, 0xE0, 0x30, 0x92, 0xD1, 0x00, 0xC0,
                0xC3, 0x11, 0x8E, 0xD1, 0x52, 0xF5, 0x95, 0x8E, 0xF4, 0xD5, 0x01, 0xBC,
                0x1F, 0x50, 0x1F, 0x7C, 0xFB, 0x10, 0x80, 0x1B, 0x6D, 0xDF, 0xAE, 0xF8,
                0xB6, 0x46, 0xD6, 0x2A, 0xAA, 0x37, 0x11, 0x34, 0x0E, 0xE6, 0xEC, 0xDF,
                0x01, 0xE0, 0xE4, 0x06, 0x13, 0x57, 0xD6, 0x81, 0x78, 0x16, 0x11, 0x06,
                0x3D, 0xD9, 0xF6, 0x1A, 0x31, 0xE2, 0x23, 0x00, 0xC4, 0xF1, 0x87, 0xAD,
                0x09, 0xD0, 0x7F, 0x05, 0x58, 0x97, 0x0E, 0x3B, 0x20, 0x82, 0xAD, 0x1D,
                0x60, 0x99, 0xE3, 0x66, 0xC6, 0x32, 0xED, 0xF1, 0x22, 0x4E, 0x00, 0xD2,
                0x8C, 0xE7, 0x80, 0xE7, 0x78, 0xFD, 0xBA, 0xFE, 0x46, 0xC0, 0xFF, 0x02,
                0xF0, 0x5D, 0x3F, 0xE6, 0xF6, 0x99, 0x60, 0xF1, 0x74, 0x7E, 0xF9, 0x28,
                0x60, 0x9D, 0x01, 0xFC, 0xA2, 0x6A, 0x74, 0x00, 0xC2, 0x7C, 0xB6, 0x9C,
                0x4A, 0x6C, 0x75, 0x96, 0x63, 0xC1, 0x5F, 0x74, 0xEF, 0x02, 0x58, 0xF7,
                0x2F, 0xC7, 0x80, 0x0C, 0x5F, 0x8A, 0x84, 0xEA, 0xDA, 0xB6, 0xDE, 0x84,
                0xCB, 0x22, 0x77, 0x35, 0x32, 0xD6, 0xF5, 0x61, 0x23, 0x40, 0x25, 0xAF,
                0xFC, 0x36, 0x80, 0x75, 0x56, 0xD4, 0xE3, 0xBE, 0x48, 0xD9, 0xDD, 0x34,
                0x00, 0x52, 0x89, 0xB9, 0xC3, 0xF2, 0x04, 0x7E, 0xA5, 0xAF, 0x10, 0xA1,
                0xDC, 0x56, 0x7C, 0x02, 0xA0, 0x92, 0x8B, 0x06, 0xF7, 0xFF, 0xB2, 0xD2,
                0x3A, 0xAF, 0x21, 0xFD, 0x88, 0xAC, 0xD5, 0x95, 0xF3, 0x0A, 0xB5, 0x0D,
                0xF0, 0xEB, 0x7B, 0x83, 0x53, 0xC0, 0x1F, 0xEF, 0x0D, 0xA2, 0x4D, 0x77,
                0x69, 0xB8, 0xB7, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
                0x42, 0x60, 0x82,
    };

    BufferedImage fontMask;

    public void init() {
        int[] bitMasks = new int[]{0xFF0000, 0xFF00, 0xFF, 0xFF000000};
        DataBufferInt data = new DataBufferInt(fontData, fontData.length);
        SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(
                DataBuffer.TYPE_INT, 256, 240, bitMasks);
        WritableRaster wr = Raster.createWritableRaster(sm, data, new Point());
        BufferedImage image = new BufferedImage(ColorModel.getRGBdefault(), wr, false, null);
        Raster raster = image.getData();
        Dimension size = raster.getBounds().getSize();
        BufferedImage mask = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < size.getHeight(); y++){
            for(int x = 0; x < size.getWidth(); x++){
                int r = image.getRGB(x, y);
                if(r > 0){
                    mask.setRGB(x, y, Color.OPAQUE);
                }
            }
        }
        fontMask = mask;
    }


    public BufferedImage CreateGenericThumbnail(String text) {
        BufferedImage im = new BufferedImage(256, 240, BufferedImage.TYPE_INT_ARGB);
        Raster raster = im.getRaster();
//        draw.Draw(im, im.Rect, &image.Uniform{color.Black}, image.ZP, draw.Src);
        DrawCenteredText(im, text, 1, 2, new Color(128, 128, 128, 255));
        DrawCenteredText(im, text, 0, 0, Color.white);
        return im;
    }

    public String[] WordWrap(String text, int maxLength) {
        java.util.List<String> rows = new ArrayList<>();
        String words = text;
        if(words.length() == 0){
            return (String[]) rows.toArray();
        }
        String row = String.valueOf(words.charAt(0));
        for (int i = 1; i < words.length(); i++) {
            String word = String.valueOf(words.charAt(i));
            String newRow = row + " " + word;
            if(newRow.length() <= maxLength){
                row = newRow;
            } else {
                rows.add(row);
                row = word;
            }
        }
        rows.add(row);
        return (String[]) rows.toArray();
    }

    public void DrawCenteredText(BufferedImage dst, String text, int dx, int dy, Color c) {
        String[] rows = WordWrap(text, 15);
        for (int i = 0; i < rows.length; i++) {
            int x = 128 - rows.length*8;
            int y = 120 - rows.length*12 + i*24;
            DrawText(dst, x+dx, y+dy, rows[i], c);
        }
    }

    public void DrawCharacter(BufferedImage dst, int x, int y, byte ch, Color c) {
        if(ch < 32 || ch > 128){
            return;
        }
        int cx = ((ch-32)%16) * 16;
        int cy = ((ch-32)/16) * 16;
        Rectangle r = new Rectangle(x, y, 16, 16);
//        draw.DrawMask(dst, r, &image.Uniform{c}, image.Pt(cx, cy), fontMask, sp, draw.Over);
    }

    public void DrawText(BufferedImage dst, int x, int y, String text, Color c) {
        for (int i = 0; i < text.length(); i++) {
            DrawCharacter(dst, x, y, Byte.valueOf(Character.toString(text.charAt(i))), c);
            x += 16;
        }
    }


}
