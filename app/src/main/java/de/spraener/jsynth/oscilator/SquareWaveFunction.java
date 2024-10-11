package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;

@SynthComponent(name="pulsewave")
public class SquareWaveFunction implements OscillatorFunction {
    private final SoundFormat soundFormat;
    private final float dT;
    private float phase = 0f;
    @SynthParam(name="pulsewidth")
    private float pulseLengthPercent = 0.5f;

    public SquareWaveFunction(SoundFormat format) {
        this.soundFormat = format;
        this.dT = 1.0f/soundFormat.sampleRate;
    }

    @Override
    public final float getSample(float fHz, float tSec) {
        phase += dT * fHz;
        phase %= 1.0f;
        return phase < pulseLengthPercent ? 1f : -1f;
    }

    public float getPulseLengthPercent() {
        return pulseLengthPercent;
    }

    public SquareWaveFunction setPulseLengthPercent(float pulseLengthPercent) {
        this.pulseLengthPercent = pulseLengthPercent;
        return this;
    }
}
