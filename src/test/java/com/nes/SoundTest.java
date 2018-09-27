package com.nes;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;

/**
 * Created by RoyChan on 2018/3/13.
 */
public class SoundTest {
    private static int sliderValue = 500;

    public static void main(String[] args) throws Exception {
        final JFrame frame = new JFrame();
        final JSlider slider = new JSlider(500, 1000);
        frame.add(slider);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderValue = slider.getValue();
            }
        });
        frame.pack();
        frame.setVisible(true);

        Vector<AudioFormat> formats = getSupportedFormats(SourceDataLine.class);
        for (int i = 0; i < formats.size(); i++) {
            System.out.println(formats.get(i).toString());
        }
        System.out.println(formats.size());

        final AudioFormat audioFormat = new AudioFormat(44100, 16, 1, true, false);
        final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        final SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
        final int bufferSize = 2200; // in Bytes
        soundLine.open(audioFormat, bufferSize);
        soundLine.start();
        byte counter = 0;
        final byte[] buffer = new byte[bufferSize];
        byte sign = 1;
        while (frame.isVisible()) {
            int threshold = (int) (audioFormat.getFrameRate() / sliderValue);
            for (int i = 0; i < bufferSize; i++) {
                if (counter > threshold) {
                    sign = (byte) -sign;
                    counter = 0;
                }
                buffer[i] = (byte) (sign * 30);
                counter++;
            }
            // the next call is blocking until the entire buffer is
            // sent to the SourceDataLine
            soundLine.write(buffer, 0, bufferSize);
        }
    }

    public static Vector<AudioFormat> getSupportedFormats(Class<?> dataLineClass) {
    /*
     * These define our criteria when searching for formats supported
     * by Mixers on the system.
     */
        float sampleRates[] = { (float) 8000.0, (float) 16000.0, (float) 44100.0 };
        int channels[] = { 1, 2 };
        int bytesPerSample[] = { 2 };

        AudioFormat format;
        DataLine.Info lineInfo;

//        SystemAudioProfile profile = new SystemAudioProfile(); // Used for allocating MixerDetails below.
        Vector<AudioFormat> formats = new Vector<AudioFormat>();

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            for (int a = 0; a < sampleRates.length; a++) {
                for (int b = 0; b < channels.length; b++) {
                    for (int c = 0; c < bytesPerSample.length; c++) {
                        format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                sampleRates[a], 8 * bytesPerSample[c], channels[b], bytesPerSample[c],
                                sampleRates[a], false);
                        lineInfo = new DataLine.Info(dataLineClass, format);
                        if (AudioSystem.isLineSupported(lineInfo)) {
                        /*
                         * TODO: To perform an exhaustive search on supported lines, we should open
                         * TODO: each Mixer and get the supported lines. Do this if this approach
                         * TODO: doesn't give decent results. For the moment, we just work with whatever
                         * TODO: the unopened mixers tell us.
                         */
                            if (AudioSystem.getMixer(mixerInfo).isLineSupported(lineInfo)) {
                                formats.add(format);
                            }
                        }
                    }
                }
            }
        }
        return formats;
    }
}
