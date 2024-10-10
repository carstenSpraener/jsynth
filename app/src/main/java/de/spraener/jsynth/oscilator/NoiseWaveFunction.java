package de.spraener.jsynth.oscilator;

public class NoiseWaveFunction implements OscillatorFunction {
    @Override
    public float getSample(float f, float t) {
        return (float) (Math.random() * 2.0 - 1.0);
    }
}
