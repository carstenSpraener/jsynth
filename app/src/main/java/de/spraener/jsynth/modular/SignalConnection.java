package de.spraener.jsynth.modular;

import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;

import java.util.Map;

@SynthComponent(name="connection")
public class SignalConnection implements SignalProcessor {
    private float time;

    @SynthParam(name="rate")
    private float rate = 1.0f;

    private float lastOutput = 0;
    private String input;
    private String output;

    public SignalConnection(ModularSynth synth, String from, String to) {
        input = from;
        output = to;
    }

    @Override
    public boolean canProcess(MSVoice ms, Map<String, Float> values) {
        return values.containsKey(input);
    }

    @Override
    public float process(float time, Map<String, Float> values) {
        float value = values.get(input) * this.rate;
        values.put(output, value);
        return value;
    }

    @Override
    public String toString() {
        return this.input + " -> " + this.output;
    }
}
