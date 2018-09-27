package com.nes.ui;

import com.nes.goncurrent.Chan;
import com.nes.goncurrent.Select;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.nes.ui.Util.convert;

/**
 * Created by RoyChan on 2018/3/14.
 */
public class Texture {

    public static final int textureSize = 4096;

    public static final int textureDim = textureSize / 256;

    public static final int textureCount = textureDim * textureDim;

    int texture;
    Map<String, Integer> lookup;
    String[] reverse = new String[textureCount];
    int[] access = new int[textureCount];
    int counter;
    Chan<String> ch;

    public Texture(){
        texture = Util.createTexture();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                textureSize, textureSize, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        this.texture = texture;
        this.lookup = new HashMap<>();
        this.ch = Chan.create(1024);
    }


    public void Purge() {
        while (true){
            Select select = new Select();
            select.receive(this.ch);
            int index = select.select(); // wait until one of the channels is ready.
            if (index == 1) {
                // data is read from ch1
                String path = (String) select.getData();
                this.lookup.remove(path);
            }else {
                return;
            }
        }
    }

    public void Bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
    }

    public void Unbind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public float[] Lookup(String path){
        Integer index = this.lookup.get(path);
        if(index != null) {
            return this.coord(index);
        } else {
            return this.coord(this.load(path));
        }
    }

    public void mark(int index) {
        this.counter++;
        this.access[index] = this.counter;
    }

    public int lru() {
        int minIndex = 0;
        int minValue = this.counter + 1;
        for (int i = 0; i < this.access.length; i++) {
            if(this.access[i] < minValue){
                minIndex = i;
                minValue = this.access[i];
            }
        }
        return minIndex;
    }

    public float[] coord(int index)  {
        float[] array = new float[4];
        float x = array[0] = index%textureDim / textureDim;
        float y = array[1] = index/textureDim / textureDim;
        float dx = array[2] = (float) (1.0 / textureDim);
        float dy = array[3] = dx * 240 / 256;
        return array;
    }

    public int load(String path) {
        int index = this.lru();
        this.lookup.remove(this.reverse[index]);
        this.mark(index);
        this.lookup.put(path, index);
        this.reverse[index] = path;
        int x = (index % textureDim) * 256;
        int y = (index / textureDim) * 256;
        BufferedImage im = Util.copyImage(this.loadThumbnail(path));
        GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0, x, y, im.getWidth(), im.getHeight(),
                GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, convert(im));
        return index;
    }

    public BufferedImage loadThumbnail(String romPath) {
//        File file = new File(romPath);
//        String name = file.getName();
//        name = strings.TrimSuffix(name, ".nes");
//        name = strings.Replace(name, "_", " ", -1);
//        name = strings.Title(name);
//        im = CreateGenericThumbnail(name);
//        String hash, err = Util.hashFile(romPath);
//        if err != nil {
//            return im;
//        }
//        filename = thumbnailPath(hash);
//        if _, err = os.Stat(filename); os.IsNotExist(err) {
//            go this.downloadThumbnail(romPath, hash);
//            return im;
//        } else {
//            thumbnail, err = loadPNG(filename);
//            if err != nil {
//                return im;
//            }
//            return thumbnail;
//        }
        return null;
    }

    public void downloadThumbnail(String romPath, String hash) {
//        url = thumbnailURL(hash);
//        filename = thumbnailPath(hash);
//        dir, _ = path.Split(filename);
//
//        resp, err = http.Get(url);
//        if err != nil {
//            return err
//        }
//        defer resp.Body.Close()
//
//        if err = os.MkdirAll(dir, 0755); err != nil {
//            return err
//        }
//
//        file, err = os.Create(filename)
//        if err != nil {
//            return err
//        }
//        defer file.Close()
//
//        if _, err = io.Copy(file, resp.Body); err != nil {
//            return err
//        }
//
//        this.ch <- romPath
//
//        return nil;
    }
}
