package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;

public class AverageLowPass implements StreamFilter<AverageLowPass>, Filter {
    private SoundFormat soundFormat;
    private float cutoff;
    private float prevSample = 0;
    private float RC;
    private float dt;
    private float alpha;

    public AverageLowPass(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
        setCutoff(12000f);
    }

    public AverageLowPass setCutoff(float cutoff) {
        this.cutoff = cutoff;
        RC = 1.0f /cutoff; // Wellendauer der Filter-Frequenz in Sekunden
        dt = 1.0f / this.soundFormat.sampleRate; // Dauer eines Samples in Sekunden
        alpha = dt / (RC + dt);
        return this;
    }

    public float getCutoff() {
        return this.cutoff;
    }

    @Override
    public float filter(float input) {
        float output = prevSample + (alpha * (input - prevSample));
        prevSample = output;
        return output;
    }

    @Override
    public float[] filterBuffer(float input[]) {
        float dt = 1.0f / this.soundFormat.sampleRate;
        float alpha = dt / (RC + dt);
        float output[] = new float[input.length];
        output[0] = input[0];
        for (int i = 1; i < input.length; ++i) {
            output[i] = output[i - 1] + (alpha * (input[i] - output[i - 1]));
        }
        return output;
    }
}
