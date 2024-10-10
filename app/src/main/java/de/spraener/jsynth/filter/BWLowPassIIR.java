package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;
import uk.me.berndporr.iirj.Butterworth;
import uk.me.berndporr.iirj.DirectFormAbstract;

public class BWLowPassIIR implements StreamFilter<BWLowPassIIR> {
    private SoundFormat soundFormat;
    private Butterworth butterworth = new Butterworth();
    private int order;
    private float[] lastInputsRingBuffer;
    private int lastInputIdx = 0;

    private float cutoff;

    public BWLowPassIIR(SoundFormat soundFormat, float cutoff, int order) {
        this.soundFormat = soundFormat;
        withOrder(order);
        setCutoff(cutoff);
        lastInputsRingBuffer = new float[20];
        butterworth.lowPass(order, this.soundFormat.sampleRate, this.cutoff, DirectFormAbstract.DIRECT_FORM_II);
    }

    @Override
    public float filter(float input) {
        lastInputsRingBuffer[lastInputIdx++] = input;
        lastInputIdx %= lastInputsRingBuffer.length;
        return (float)butterworth.filter(input);
    }

    @Override
    public BWLowPassIIR setCutoff(float cutoff) {
        this.cutoff = cutoff;
        butterworth = new Butterworth();
        butterworth.lowPass(order, this.soundFormat.sampleRate, this.cutoff, DirectFormAbstract.DIRECT_FORM_II);
        for( int i=0; i<lastInputsRingBuffer.length; i++ ) {
            butterworth.filter(lastInputsRingBuffer[lastInputIdx++]);
            lastInputIdx %= lastInputsRingBuffer.length;
        }
        return this;
    }

    public float getCutoff() {
        return cutoff;
    }

    public StreamFilter<?> withOrder(int n) {
        this.order = n;
        this.lastInputsRingBuffer = new float[n];
        this.lastInputIdx = 0;
        return this;
    }
}
