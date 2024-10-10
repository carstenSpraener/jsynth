package de.spraener.jsynth.filter;

public interface StreamFilter<T extends StreamFilter> {

    float filter(float input);
    T setCutoff(float cutoff);
    float getCutoff();
}
