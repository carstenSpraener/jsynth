package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.modular.annotations.SignalInput;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;

@SynthComponent(name="allpass")
public class AllpassFilter implements StreamFilter<AllpassFilter> {
    private SoundFormat soundFormat;

    private double dn1 = 0;
    private double rn1 = 0;
    @SynthParam(name = "cutoff")
    private float cutoff;
    private double alpha;
    @SynthParam(name = "amplitude")
    private float amplitude = 1.0f;
    private float sign = 1.0f;

    public AllpassFilter(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
        setCutoff(16000);
    }

    public AllpassFilter setMode(FilterMode mode) {
        if( mode==FilterMode.LOW_PASS) {
            this.sign = 1.0f;
        } if( mode == FilterMode.HIGH_PASS) {
            this.sign = -1.0f;
        } else {
            this.sign = 1.0f;
        }
        return this;
    }

    @Override
    public float filter(float input) {
        double aZ = alpha * input + dn1;
        dn1 = input - alpha * aZ;
        double out = 0.5 * (input + sign  * aZ);

        return (float)out * amplitude;
    }

    @Override
    public AllpassFilter setCutoff(float cutoff) {
        this.cutoff = cutoff;
        double tan = Math.tan(Math.PI * this.cutoff / this.soundFormat.sampleRate);
        this.alpha = (tan - 1.0) / (tan + 1.0);
        return this;
    }

    @Override
    public float getCutoff() {
        return this.cutoff;
    }

    public AllpassFilter setAmplitude(float v) {
        this.amplitude = v;
        return this;
    }
}
