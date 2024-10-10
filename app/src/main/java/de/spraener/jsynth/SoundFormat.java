package de.spraener.jsynth;

public class SoundFormat {
    public float sampleRate = 44100f;
    public float tSample = 1.0f/sampleRate;
    public int bufferSize = 256;

    public SoundFormat setSampleRate( float sampleRate ) {
        this.sampleRate = sampleRate;
        this.tSample = 1.0f/sampleRate;
        return this;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public float getTSample() {
        return tSample;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
