package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;

public class ChebyshevII implements StreamFilter<ChebyshevII> {
    private SoundFormat soundFormat;
    private float cutoff;
    private uk.me.berndporr.iirj.ChebyshevII chebyshevII;

    public ChebyshevII(SoundFormat soundFormat, float cutoff) {
        this.soundFormat = soundFormat;
        setCutoff(cutoff);
    }

    public float filter(float x) {
        return (float)chebyshevII.filter(x);
    }

    @Override
    public ChebyshevII setCutoff(float cutoff) {
        this.cutoff = cutoff;
        chebyshevII = new uk.me.berndporr.iirj.ChebyshevII();
        chebyshevII.lowPass(2, soundFormat.sampleRate, cutoff, 12);
        return this;
    }


    @Override
    public float getCutoff() {
        return this.cutoff;
    }


}
