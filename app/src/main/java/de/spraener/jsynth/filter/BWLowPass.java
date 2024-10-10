package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;

public class BWLowPass implements StreamFilter<BWLowPass> {
    private int n;
    private  SoundFormat soundFormat;
    private float[] A;
    private float[] d1;
    private float[] d2;
    private float[] w0;
    private float[] w1;
    private float[] w2;
    private float cutoff;

    public BWLowPass(int order, SoundFormat soundFormat, float cutoff) {
        this.soundFormat = soundFormat;
        this.n = order / 2;
        this.A = new float[n];
        this.d1 = new float[n];
        this.d2 = new float[n];
        this.w0 = new float[n];
        this.w1 = new float[n];
        this.w2 = new float[n];

        setupFilter(soundFormat, cutoff);
    }

    private void setupFilter(SoundFormat soundFormat, float cutoff) {
        float s = soundFormat.sampleRate;
        float a = (float) Math.tan((Math.PI * cutoff / s));
        float a2 = a * a;
        double r;

        int i;

        for (i = 0; i < n; ++i) {
            r = Math.sin((Math.PI * (2.0 * i + 1.0) / (4.0 * n)));
            s = (float) (a2 + 2.0 * a * r + 1.0);
            this.A[i] = a2 / s;
            this.d1[i] = (float) (2.0 * (1 - a2) / s);
            this.d2[i] = (float) (-(a2 - 2.0 * a * r + 1.0) / s);
        }
    }

    @Override
    public float filter(float input) {
        int i;
        for (i = 0; i < this.n; ++i) {
            this.w0[i] = this.d1[i] * this.w1[i] + this.d2[i] * this.w2[i] + input;
            input = this.A[i] * (this.w0[i] + 2.0f * this.w1[i] + this.w2[i]);
            this.w2[i] = this.w1[i];
            this.w1[i] = this.w0[i];
        }
        return input;
    }

    @Override
    public BWLowPass setCutoff(float cutoff) {
        this.cutoff = cutoff;
        return this;
    }

    public float getCutoff() {
        return this.cutoff;
    }
}
