package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;

@SynthComponent(name="sawtooth")
public class SawToothWaveFunction  implements OscillatorFunction {
    private final SoundFormat soundFormat;
    private float phase = 0f;
    private float dT;
    public SawToothWaveFunction(SoundFormat format) {
        this.soundFormat = format;
        this.dT = 1.0f/ soundFormat.sampleRate;
    }

    @Override
    public final float getSample(float fHz, float tSec) {
        phase += dT * fHz;
        phase %= 1;
        return 1.0f - 2 * phase;
    }

}
