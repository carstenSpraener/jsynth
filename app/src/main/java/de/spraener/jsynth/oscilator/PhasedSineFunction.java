package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;

public class PhasedSineFunction implements OscillatorFunction{
    private SoundFormat sf;
    private float phase = 0;
    private float twoPiOverR;
    private static final float TWO_PI = (float) Math.PI * 2;

    public PhasedSineFunction(SoundFormat sf) {
        this.sf = sf;
        this.twoPiOverR = (float)(Math.PI * 2 * sf.tSample);
    }

    @Override
    public float getSample(float f, float t) {
        phase += twoPiOverR*f;
        phase %= TWO_PI;
        return (float) Math.sin(phase);
    }
}
