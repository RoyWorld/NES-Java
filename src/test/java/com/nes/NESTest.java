package com.nes;

import com.nes.goncurrent.Chan;
import com.nes.goncurrent.Select;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by RoyChan on 2018/3/13.
 */
public class NESTest {

    @Test
    public void test(){
        final Chan<Integer> ch1 = Chan.create(0);
        final Chan<Integer> ch2 = Chan.create(0);
        final Chan<Integer> done = Chan.create(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    ch1.send(1);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Integer i : ch2) {
                    System.out.printf("ch2: %d\n", i);
                }
                done.send(0);
            }
        }).start();

        Select select = new Select();
        while (true) {
            select.receive(ch2); // index=1
            select.receive(ch1); // index=0

            int index = select.select(); // wait until one of the channels is ready.
            if (index == 1) {
                // data is read from ch1
                Integer value = (Integer)select.getData();
                System.out.println(value);
            }
        }
    }

    @Test
    public void filePath() throws IOException {
        String path = "E:\\chrome_download\\lwjgl-3.1.6\\native";
        File file = new File("E:\\chrome_download\\lwjgl-3.1.6");
        File[] files = file.listFiles();
        for (File file1 : files){
            if (file1.isDirectory() && !file1.getName().contains("native")){
                for (File f : file1.listFiles()){
                    if (f.getName().contains("native") && f.getName().contains("windows")){
                        String name = f.getName();
                        System.out.println(name);
                        FileUtils.copyFile(f, new File( path + "\\" + name));
                    }
                }
            }

        }
    }

    private void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }finally{
            sourceChannel.close();
            destChannel.close();
        }
    }

    @Test
    public void systemPath(){
        System.out.println(System.getProperty("java.library.path"));
    }

    @Test
    public void test1(){
        String s = "aaaadfa\\adfa\\";
        s.toUpperCase();
        System.out.println(s);
    }
}
