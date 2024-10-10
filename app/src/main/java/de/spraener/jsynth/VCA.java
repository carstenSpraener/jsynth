package de.spraener.jsynth;

import de.spraener.jsynth.envelope.Envelope;
import de.spraener.jsynth.modular.MSVoice;
import de.spraener.jsynth.modular.SignalProcessor;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;
import de.spraener.jsynth.voice.KeyListener;

import java.util.Map;

@SynthComponent(name="vca")
public class VCA implements KeyListener, TimeResponsive, SignalProcessor {
    @SynthParam(name="envelope", type= Envelope.class)
    private Envelope envelope;

    @SynthParam(name="volume")
    private float volume = 1.0f;

    private float time;

    private String componentPath = null;

    public VCA(Envelope envelope) {
        this.envelope = envelope;
    }

    public float processSample(float sample) {
        sample *= envelope.value(time);
        sample *= volume;
        return sample;
    }

    @Override
    public void setTime(float time) {
        this.time = time;
    }

    @Override
    public boolean canProcess(MSVoice ms, Map<String, Float> values) {
        this.componentPath = ms.getComponentPath(this);
        return values.containsKey(this.componentPath+".in");
    }

    @Override
    public float process(float time, Map<String, Float> values) {
        float input = values.get(this.componentPath+".in");
        float out = processSample(input);
        values.put(this.componentPath+".out", out);
        return out;
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
}
