package de.spraener.jsynth;

import de.spraener.jsynth.envelope.ADSREnvelope;
import de.spraener.jsynth.envelope.Envelope;
import de.spraener.jsynth.filter.AllpassFilter;
import de.spraener.jsynth.filter.StackedFilter;
import de.spraener.jsynth.filter.StreamFilter;
import de.spraener.jsynth.filter.VCF;
import de.spraener.jsynth.oscilator.Oscillator;
import de.spraener.jsynth.oscilator.SawToothWaveFunction;
import de.spraener.jsynth.oscilator.SineWaveFunction;
import de.spraener.jsynth.oscilator.SquareWaveFunction;
import de.spraener.jsynth.voice.Voice;

import java.util.function.Consumer;

public class AnalogSynth {
    private SoundFormat soundFormat;

    private Voice[] voices = new Voice[6];
    private int vNext = 0;

    public AnalogSynth(SoundFormat soundFormat, Consumer<Voice>... voiceModifiers) {
        this.soundFormat = soundFormat;
        for (int i = 0; i < voices.length; i++) {
            voices[i] = buildVoice(i);
            for (Consumer<Voice> modifier : voiceModifiers) {
                modifier.accept(voices[i]);
            }
        }
    }

    public AnalogSynth setFilterCutoff(float f) {
        for (Voice voice : voices) {
            voice.getFilter().setCutoff(f);
        }
        return this;
    }

    private Voice buildVoice(int id) {
        Oscillator o1 = new Oscillator(soundFormat, new SawToothWaveFunction(this.soundFormat))
                .setFrequence(0);

        SquareWaveFunction sqf = new SquareWaveFunction(this.soundFormat);
        sqf.setPulseLengthPercent(0.15f);
        Oscillator lfo = new Oscillator(soundFormat, new SineWaveFunction(this.soundFormat))
                .setFrequence(3f);
        Oscillator o2 = new Oscillator(soundFormat, new SawToothWaveFunction(soundFormat))
                .setOctave(-1)
                .setFineTune(1.015f)
                .setFrequence(0);

        Oscillator o3 = new Oscillator(soundFormat, new SawToothWaveFunction(this.soundFormat))
                .setOctave(-1)
                .setFineTune(0.985f)
                .setFrequence(0);
        ;

        StreamFilter<?> filter = new StackedFilter(
                new AllpassFilter(soundFormat).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setAmplitude(1.0f)
        );

        filter.setCutoff(16000);

        Envelope vca = new ADSREnvelope()
                .setAttackSecs(0.01f)
                .setDecaySecs(0.03f)
                .setSustain(0.9f)
                .setReleaseSecs(1.5f)
                .reset(Float.MIN_VALUE);

        Envelope filterEnv = new ADSREnvelope()
                .setAttackSecs(0.0f)
                .setDecaySecs(0.2f)
                .setSustain(0.6f)
                .setReleaseSecs(0.6f);

        return new Voice.VoiceBuilder(soundFormat)
                .withID(id)
                .withOscilator(o1)
                .withOscilator(o2)
                .withOscilator(o3)
                .withTimeResponsive(t->sqf.setPulseLengthPercent(0.5f - 0.45f * lfo.getSample(t)))
                .withFilter(new VCF(filter, filterEnv).setIntensity(0.95f))
                .withVca(vca)
                .build();
    }

    public Voice pressKey(float time, PianoKeys k, float velocity) {
        if (k == PianoKeys.NONE) {
            return null;
        }
        Voice v = voices[vNext++];
        System.out.println("Using voice "+v.getID());
        vNext %= voices.length;
        v.startKey(time, k, velocity);
        return v;
    }

    public AnalogSynth releaseVoice(float time, Voice v, float velocity) {
        if (v == null) {
            return this;
        }
        v.release(time, v.getKey(), velocity);
        return this;
    }

    public float sample(float time) {
        float sample = 0;
        for (Voice v : voices) {
            if (v.getVca().value(time) > 0) {
                sample += v.sample(time);
            }
        }
        return sample;
    }
}
