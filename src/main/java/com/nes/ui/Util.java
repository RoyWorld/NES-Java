package com.nes.ui;

import com.nes.util.Constant;
import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 * Created by RoyChan on 2018/3/13.
 */
public class Util {

    static String homeDir;
    static boolean[] buttonsUtil = new boolean[9];

    public static void init() {
//        u, err = user.Current()
//        if err != nil {
//            log.Fatalln(err)
//        }
//        homeDir = u.HomeDir
    }

    public String thumbnailURL(String hash)  {
        return "http://www.michaelfogleman.com/static/nes/" + hash + ".png";
    }

    public String thumbnailPath(String hash)  {
        return homeDir + "/.nes/thumbnail/" + hash + ".png";
    }

    public static String sramPath(String hash) {
        return homeDir + "/.nes/sram/" + hash + ".dat";
    }

    public static boolean readKey(Window window, int key) {
        return window.GetKey(key) == GLFW_PRESS;
    }

    public static boolean[] readKeys(Window window, boolean turbo) {
        boolean[] result = new boolean[8];
        result[Constant.ButtonA] = readKey(window, GLFW_KEY_Z) || (turbo && readKey(window, GLFW_KEY_A));
        result[Constant.ButtonB] = readKey(window, GLFW_KEY_X) || (turbo && readKey(window, GLFW_KEY_S));
        result[Constant.ButtonSelect] = readKey(window, GLFW_KEY_RIGHT_SHIFT);
        result[Constant.ButtonStart] = readKey(window, GLFW_KEY_ENTER);
        result[Constant.ButtonUp] = readKey(window, GLFW_KEY_UP);
        result[Constant.ButtonDown] = readKey(window, GLFW_KEY_DOWN);
        result[Constant.ButtonLeft] = readKey(window, GLFW_KEY_LEFT);
        result[Constant.ButtonRight] = readKey(window, GLFW_KEY_RIGHT);
        return result;
    }

    public static boolean[] readJoystick(int joy, boolean turbo) {
        boolean[] result = new boolean[8];
        if(!glfwJoystickPresent(joy)){
            return result;
        }
        float[] axes = glfwGetJoystickAxes(joy).array();
        byte[] buttons = glfwGetJoystickButtons(joy).array();
        result[Constant.ButtonA] = buttons[0] == 1 || (turbo && buttons[2] == 1);
        result[Constant.ButtonB] = buttons[1] == 1 || (turbo && buttons[3] == 1);
        result[Constant.ButtonSelect] = buttons[6] == 1;
        result[Constant.ButtonStart] = buttons[7] == 1;
        result[Constant.ButtonUp] = axes[1] < -0.5;
        result[Constant.ButtonDown] = axes[1] > 0.5;
        result[Constant.ButtonLeft] = axes[0] < -0.5;
        result[Constant.ButtonRight] = axes[0] > 0.5;
        return result;
    }

    public static boolean joystickReset(int joy)  {
        if(!glfwJoystickPresent(joy)){
            return false;
        }
        byte[] buttons = glfwGetJoystickButtons(joy).array();
        return buttons[4] == 1 && buttons[5] == 1;
    }

    public static boolean[] combineButtons(boolean[] a, boolean[] b){
        boolean[] result = new boolean[8];
        for(int i = 0; i < 8; i++){
            result[i] = a[i] || b[i];
        }
        return result;
    }

    public static String hashFile(String path) {
        StringBuffer sb = new StringBuffer();
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(path);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1

            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }



    public static int createTexture()  {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureID;
    }

    public static ByteBuffer convert(BufferedImage image){
        ByteBuffer byteBuffer;
        DataBuffer dataBuffer = image.getRaster().getDataBuffer();

        if (dataBuffer instanceof DataBufferByte) {
            byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
            byteBuffer = ByteBuffer.wrap(pixelData);
        }
        else if (dataBuffer instanceof DataBufferUShort) {
            short[] pixelData = ((DataBufferUShort) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
            byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
        }
        else if (dataBuffer instanceof DataBufferShort) {
            short[] pixelData = ((DataBufferShort) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
            byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
        }
        else if (dataBuffer instanceof DataBufferInt) {
            int[] pixelData = ((DataBufferInt) dataBuffer).getData();
            byteBuffer = ByteBuffer.allocate(pixelData.length * 4);
            byteBuffer.asIntBuffer().put(IntBuffer.wrap(pixelData));
        }
        else {
            throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
        }
        return byteBuffer;
    }

    public static void setTexture(BufferedImage im) {
        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA, im.getWidth(), im.getHeight(),
                0, GL_RGBA, GL_UNSIGNED_BYTE, convert(im));
    }

    public static BufferedImage copyImage(BufferedImage src){
        ColorModel cm = src.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = src.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
//
//    public Image loadPNG(String path){
//        file, err = os.Open(path)
//        if err != nil {
//            return nil, err
//        }
//        defer file.Close()
//        return png.Decode(file);
//    }
//
//    public void savePNG(String path, Image im) {
//        file, err = os.Create(path)
//        if err != nil {
//            return err
//        }
//        defer file.Close()
//        return png.Encode(file, im);
//    }
//
//    public void saveGIF(String path, Image[] frames) {
//        Color[] palette;
//        for _, c = range Constant.Palette {
//            palette = append(palette, c)
//        }
//        g = gif.GIF{}
//        for i, src = range frames {
//            if i%3 != 0 {
//                continue
//            }
//            dst = image.NewPaletted(src.Bounds(), palette)
//            draw.Draw(dst, dst.Rect, src, image.ZP, draw.Src)
//            g.Image = append(g.Image, dst)
//            g.Delay = append(g.Delay, 5)
//        }
//        file, err = os.Create(path)
//        if err != nil {
//            return err
//        }
//        defer file.Close()
//        return gif.EncodeAll(file, &g)
//    }
//
//    public void screenshot(Image im) {
//        for(int i = 0; i < 1000; i++){
//            path = fmt.Sprintf("%03d.png", i);
//            if _, err = os.Stat(path); os.IsNotExist(err) {
//                savePNG(path, im);
//                return;
//            }
//        }
//    }
//
//    public void animation(Image[] frames) {
//        for(int i = 0; i < 1000; i++){
//            path = fmt.Sprintf("%03d.gif", i);
//            if _, err = os.Stat(path); os.IsNotExist(err) {
//                saveGIF(path, frames);
//                return;
//            }
//        }
//    }

    public static void writeSRAM(String filename, byte[] sram) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);

            ByteBuffer bb = ByteBuffer.wrap(sram);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            WritableByteChannel channel = Channels.newChannel(fos);

            channel.write(bb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readSRAM(String filename) {
        byte[] sram = new byte[0x20000];
        try {
            FileInputStream file = new FileInputStream(new File(filename));

            byte[] bytes = IOUtils.toByteArray(file);
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            bb.get(sram, 0, sram.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sram;
    }

}
