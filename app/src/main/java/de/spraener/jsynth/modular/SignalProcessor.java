package de.spraener.jsynth.modular;

import java.util.Map;

public interface SignalProcessor {
    boolean canProcess(MSVoice ms, Map<String, Float> values);
    float process(float time, Map<String, Float> values);
}
