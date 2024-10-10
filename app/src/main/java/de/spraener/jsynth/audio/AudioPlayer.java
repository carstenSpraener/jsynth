package de.spraener.jsynth.audio;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.SynthContext;

import javax.sound.sampled.*;

public class AudioPlayer implements Runnable, AutoCloseable {
    private SoundFormat soundFormat;
    private boolean suspended = false;
    private boolean bigEndian = false;
    private boolean signed = true;
    private int bits = 16;
    private int channels = 1;
    private AudioBufferSource src;

    private long blockCount = 0;

    public AudioPlayer(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
    }

    public AudioPlayer() {
        this(SynthContext.soundFormat);
    }

    public AudioPlayer withAudioSource(AudioBufferSource src) {
        this.src = src;
        return this;
    }
    public void start() {
        Thread t = new Thread(this);
        t.setName("AudioPlayer");
        t.setDaemon(true);
        this.suspended = false;
        t.start();
    }

    public synchronized void waitForBlockPlayed() {
        System.out.println("Waiting for block played...");
        long playingBlock = blockCount;
        while( this.blockCount == playingBlock ) {
            try {wait();} catch(InterruptedException ignore) {}
        }
    }

    public synchronized void signalBlockPlayed() {
        System.out.println("Block played!");
        this.blockCount++;
        notifyAll();
    }

    public void run() {
        try {
            AudioFormat format = new AudioFormat(soundFormat.sampleRate, bits, channels, signed, bigEndian);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            final SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open();
            int bufferSize = (int)(soundFormat.sampleRate/0.5f);
            byte[] data = AudioBufferConverter.convert(this.src.next(bufferSize));
            sourceLine.addLineListener(le->{
                System.out.println(le);
                signalBlockPlayed();
            });
            sourceLine.start();
            while (!suspended) {
                sourceLine.write(data, 0, data.length);
                sourceLine.drain();
                byte[] nextData = AudioBufferConverter.convert(this.src.next(bufferSize));
                data = nextData;
            }
        } catch( Exception e ) {
            e.printStackTrace(System.err);
            this.suspended = true;
        }
    }

    @Override
    public void close() throws Exception {
        this.suspended = true;

    }
}
