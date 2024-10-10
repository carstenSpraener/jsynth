package de.spraener.jsynth.envelope;

public interface Envelope<T extends Envelope> {
    float value(float time);
    T reset(float time);
    T start(float time);
    T release(float time);
    boolean isActive();
}
