package de.spraener.jsynth.filter;

public class NoneFilter implements StreamFilter<NoneFilter> {
    @Override
    public float filter(float input) {
        return input;
    }

    @Override
    public NoneFilter setCutoff(float cutoff) {
        return this;
    }

    @Override
    public float getCutoff() {
        return 20000;
    }
}
