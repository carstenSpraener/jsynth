package de.spraener.jsynth.oscilator;

public interface OscillatorFuntion {
    /**
     * Generates Sample values of frequency f at time t. Values are in the range
     * of -1 to 1
     */
    float getSample(float f, float t);
}
