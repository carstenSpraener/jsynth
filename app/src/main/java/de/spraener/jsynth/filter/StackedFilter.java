package de.spraener.jsynth.filter;

import java.util.function.Supplier;

public class StackedFilter implements StreamFilter<StackedFilter> {
    private StreamFilter<?>[] filters;
    private float amplitude = 1.0f;

    public StackedFilter(StreamFilter... filters) {
        this.filters = filters;
    }

    @Override
    public float filter(float input) {
        float out =input;
        for( StreamFilter<?> f : filters ) {
            out = f.filter(out);
        }
        return out * amplitude;
    }

    @Override
    public StackedFilter setCutoff(float cutoff) {
        for( StreamFilter<?> f : filters ) {
            f.setCutoff(cutoff);
        }
        return this;
    }

    @Override
    public float getCutoff() {
        return filters[0].getCutoff();
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }
}
