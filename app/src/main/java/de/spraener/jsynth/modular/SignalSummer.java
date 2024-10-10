package de.spraener.jsynth.modular;

import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SynthComponent(name="combine")
public class SignalSummer implements TimeResponsive, SignalProcessor {
    private float time;

    private List<Function<Float,Float>> inputs = new ArrayList<>();
    private BiConsumer<Float,Float> output;
    @SynthParam(name="rate")
    private float rate = 1.0f;
    private String[] neededInputs;
    private String outputName;

    public SignalSummer(ModularSynth synth, String to, String... from) {
        this.neededInputs = from;
        this.outputName = to;
    }

    public SignalSummer addInput(Function<Float,Float> input) {
        this.inputs.add(input);
        return this;
    }

    public SignalSummer setOutput(BiConsumer<Float,Float> output) {
        this.output = output;
        return this;
    }

    @Override
    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public boolean canProcess(MSVoice ms, Map<String, Float> values) {
        for( String neededInput : neededInputs ) {
            if( !values.containsKey(neededInput) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float process(float time, Map<String, Float> values) {
        float sum = 0f;
        for( String input : neededInputs ) {
            sum += values.get(input);
        }
        sum /= neededInputs.length;
        sum *= this.rate;
        values.put(outputName, sum);
        return sum;
    }
}
