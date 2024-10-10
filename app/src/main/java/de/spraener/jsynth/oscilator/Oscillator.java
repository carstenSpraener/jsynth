package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.modular.annotations.*;
import de.spraener.jsynth.voice.KeyListener;

@SynthComponent(name="osc")
@SignalRoot
public class Oscillator implements KeyListener, TimeResponsive {
    private SoundFormat format;
    @SynthParam(name="WaveForm", type= OscillatorFuntion.class)
    private final OscillatorFuntion function;
    @SynthParam(name="octave")
    private int octave = 0;
    @SynthParam(name="tune")
    private float tune = 1.0f;
    @SynthParam(name="finetune")
    private float fineTune = 1.0f;
    @SynthParam(name="frequence")
    private float frequence = 440;
    @SynthParam(name="volume")
    private float volume = 1.0f;
    @SynthParam(name="keyFollow")
    private float keyFollow = 1.0f;
    @SynthParam(name="fmod")
    private float fMod = 0f;
    @SignalOutput(name="fOut")
    private float fOut;

    private float time;

    public Oscillator(SoundFormat format) {
        this(format, new SquareWaveFunction(format));
    }

    public Oscillator(SoundFormat format, OscillatorFuntion function) {
        this.format = format;
        this.function = function;
    }

    @SignalOutput(name="out")
    public float getSample(float t) {
        fOut = this.frequence;
        fOut *= fineTune * Math.pow(2, this.octave) * tune;
        fOut = fOut * (1.0f- fMod);
        return this.volume * this.function.getSample(fOut, t);
    }

    public Oscillator setKey(PianoKeys k) {
        this.frequence = k.f()  * keyFollow;
        return this;
    }

    public int getOctave() {
        return octave;
    }

    public Oscillator setOctave(int octave) {
        this.octave = octave;
        return this;
    }

    public float getTune() {
        return tune;
    }

    public void setTune(float tune) {
        this.tune = tune;
    }

    public float getFineTune() {
        return fineTune;
    }

    public Oscillator setFineTune(float fineTune) {
        this.fineTune = fineTune;
        return this;
    }

    public float getFOut() {
        return this.fOut;
    }

    public Oscillator setFMod(float fmod) {
        this.fMod = fmod;
        return this;
    }

    public float getFMod() {
        return this.fMod;
    }

    @SignalInput(name="frequency")
    public Oscillator setFrequence(float frequence) {
        this.frequence = frequence;
        return this;
    }

    @Override
    public void keyUp(float time, PianoKeys k, float velocity) {
    }

    @Override
    public void keyDown(float time, PianoKeys k, float velocity) {
        this.setKey(k);
    }

    @Override
    public void aftertouch(float time, int value) {
    }

    @Override
    public void setTime(float time) {
        this.time = time;
    }

    public float getFrequence() {
        return this.frequence;
    }
}
