package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;

@SynthComponent(name="sinewave")
public class SineWaveFunction implements OscillatorFunction {
    private static final float twoPi = (float)(Math.PI * 2f);

    public SineWaveFunction(SoundFormat sf) {
    }

    @Override
    public float getSample(float fHz, float tSec) {
        return (float)Math.sin(tSec * twoPi * fHz);
    }
}
