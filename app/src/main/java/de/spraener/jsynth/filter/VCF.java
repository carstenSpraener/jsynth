package de.spraener.jsynth.filter;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.envelope.Envelope;
import de.spraener.jsynth.modular.MSVoice;
import de.spraener.jsynth.modular.SignalProcessor;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;
import de.spraener.jsynth.voice.KeyListener;

import java.util.Map;

@SynthComponent
public class VCF implements StreamFilter<VCF>, KeyListener, TimeResponsive, SignalProcessor {
    @SynthParam(name = "filter", type = StreamFilter.class)
    private StreamFilter<?> filter;

    @SynthParam(name="envelope", type = Envelope.class)
    private Envelope envelope;

    @SynthParam(name="cutoff")
    private float startCutoff;

    @SynthParam(name="intensity")
    private float intensity = 1.0f;

    public VCF(StreamFilter<?> filter, Envelope envelope) {
        this.filter = filter;
        this.envelope = envelope;
        this.startCutoff = filter.getCutoff();
    }

    @Override
    public float filter(float input) {
        return this.filter.filter(input);
    }

    @Override
    public VCF setCutoff(float cutoff) {
        this.startCutoff = cutoff;
        return this;
    }

    @Override
    public float getCutoff() {
        return this.startCutoff;
    }

    @Override
    public void keyUp(float time, PianoKeys k, float velocity) {
        if( this.envelope instanceof KeyListener kl ) {
            kl.keyUp(time, k, velocity);
        }
    }

    @Override
    public void keyDown(float time, PianoKeys k, float velocity) {
        if( this.envelope instanceof KeyListener kl ) {
            kl.keyDown(time, k, velocity);
        }
    }

    @Override
    public void aftertouch(float time, int value) {
        if( this.envelope instanceof KeyListener kl ) {
            kl.aftertouch(time, value);
        }
    }

    @Override
    public void setTime(float time) {
        this.filter.setCutoff(
                this.startCutoff * (1.0f-intensity + intensity * this.envelope.value(time))
        );
    }

    public VCF setIntensity(float intensity) {
        this.intensity = intensity;
        return this;
    }

    private String componentPath;

    @Override
    public boolean canProcess(MSVoice ms, Map<String, Float> values) {
        this.componentPath = ms.getComponentPath(this);
        return values.containsKey(componentPath+".in");
    }

    @Override
    public float process(float time, Map<String, Float> values) {
        setTime(time);
        float input = values.get(componentPath+".in");
        float output = this.filter(input);
        values.put(this.componentPath+".out", output);
        return output;
    }
}
