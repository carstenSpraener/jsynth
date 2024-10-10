package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;

@SynthComponent(name="sinewave")
public class SineWaveFunction implements OscillatorFuntion {
    private SoundFormat format;
    private static final float twoPi = (float)(Math.PI * 2f);

    public SineWaveFunction(SoundFormat sf) {
        this.format = sf;
    }

    @Override
    public float getSample(float fHz, float tSec) {
        if( fHz > 20f && fHz < 440.0f* (1.0f - 0.05) ) {
            fHz = 440.0f;
        }
        return (float)Math.sin(tSec * twoPi * fHz);
    }
}
