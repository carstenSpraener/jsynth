package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;

@SynthComponent(name="sawtooth")
public class SawToothWaveFunction  implements OscillatorFuntion {
    private final SoundFormat soundFormat;

    public SawToothWaveFunction(SoundFormat format) {
        this.soundFormat = format;
    }

    @Override
    public final float getSample(float fHz, float tSec) {
        float pLength = (float)1.0/fHz;
        float tWave = tSec % pLength;
        return -2.0f/pLength * tWave + 1.0f;
    }

}
