package de.spraener.jsynth;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.function.Consumer;

public class AudioTestSupport {

    public void playSoundData(float[] buffer, float sampleRate, Consumer<WaveDisplay>... displayConsumers) throws Exception {
        final byte[] byteBuffer = new byte[buffer.length * 2];

        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = (int) (buffer[bufferIndex++] * 32767.0);

            byteBuffer[i++] = (byte) x;
            byteBuffer[i] = (byte) (x >>> 8);
        }

        final boolean bigEndian = false;
        final boolean signed = true;

        final int bits = 16;
        final int channels = 1;

        AudioFormat format = new AudioFormat(sampleRate, bits, channels, signed, bigEndian);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, byteBuffer.length);
        WaveDisplay wd = new WaveDisplay(buffer);
        if( displayConsumers!=null) {
            for(Consumer<WaveDisplay> consumer : displayConsumers) {
                consumer.accept(wd);
            }
        }
        int sleep = (int) (byteBuffer.length * 1.0 / sampleRate) * 500;
        if( sleep < 3000 ) {
            sleep = 3000;
        }
        play(audioInputStream, sleep);
        wd.setVisible(false);
        audioInputStream = new AudioInputStream(new ByteArrayInputStream(byteBuffer), format, buffer.length);
        AudioSystem.write(
                audioInputStream
                , AudioFileFormat.Type.WAVE
                ,new File("test.wav")
        );

    }

    public void play(AudioInputStream ais, int sleep) throws Exception {
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clip.start();
        clip.drain();
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ignore) {
        }
        clip.flush();
        clip.close();
    }

    public float[] normalize(float[] buffer, float pMin, float pMax) {
        float range = pMax - pMin;
        float scale = 2.0f / (pMax - pMin);
        for( int i = 0; i < buffer.length; i++ ) {
            buffer[i] *= scale;
        }
        return buffer;
    }

    public float[] normalize(float[] buffer) {
        float pMin = Float.MAX_VALUE;
        float pMax = Float.MIN_VALUE;
        for( int i = 0; i < buffer.length; i++ ) {
            if( buffer[i] < pMin ) {
                pMin = buffer[i];
            }
            if( buffer[i] > pMax ) {
                pMax = buffer[i];
            }
        }
        return normalize(buffer, pMin, pMax);
    }
}
