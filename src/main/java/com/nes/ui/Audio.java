package com.nes.ui;

import com.nes.goncurrent.Chan;
import com.nes.goncurrent.Select;

import javax.sound.sampled.*;

/**
 * Created by RoyChan on 2018/3/8.
 */
public class Audio {

    public SourceDataLine soundLine;
    public Chan<Float> channel;

    public Audio(){
        channel = Chan.create(441000);
    }

    public void Start() throws LineUnavailableException {
        AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        soundLine = (SourceDataLine) AudioSystem.getLine(info);
        final int bufferSize = 2200; // in Bytes
        soundLine.open(audioFormat, bufferSize);
        soundLine.start();
    }

    public void Stop() throws LineUnavailableException {
        soundLine.close();
    }

    public void Callback(float[] out){
        for (int i = 0; i < out.length; i++) {
            Select select = new Select();
            select.receive(channel); // index=1
            int index = select.select(); // wait until one of the channels is ready.
            if (index == 1) {
                // data is read from ch1
                float value = (float) select.getData();
                out[i] = value;
            }else {
                out[i] = 0;
            }
        }
    }
}
