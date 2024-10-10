package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;

@SynthComponent(name="pulsewave")
public class SquareWaveFunction implements OscillatorFunction {
    private final SoundFormat soundFormat;
    @SynthParam(name="pulsewidth")
    private float pulseLengthPercent = 0.5f;

    public SquareWaveFunction(SoundFormat format) {
        this.soundFormat = format;
    }

    @Override
    public final float getSample(float fHz, float tSec) {
        float pLength = (float)1.0/fHz;
        float tWave = tSec % pLength;
        if( tWave < pLength* pulseLengthPercent) {
            return 1.0f;
        }
        return -1.0f;
    }

    public float getPulseLengthPercent() {
        return pulseLengthPercent;
    }

    public SquareWaveFunction setPulseLengthPercent(float pulseLengthPercent) {
        this.pulseLengthPercent = pulseLengthPercent;
        return this;
    }
}
