package de.spraener.jsynth.audio;

public interface AudioBufferSource {
    float[] next(int nofSamples);
}
