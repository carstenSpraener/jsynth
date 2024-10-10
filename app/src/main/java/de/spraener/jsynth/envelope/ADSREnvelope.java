package de.spraener.jsynth.envelope;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.annotations.SynthParam;
import de.spraener.jsynth.voice.KeyListener;

@SynthComponent(name="ADSR")
public class ADSREnvelope implements Envelope<ADSREnvelope>, KeyListener {
    @SynthParam(name="attack")
    private float attackSecs;
    @SynthParam(name="decay")
    private float decaySecs;
    @SynthParam(name="sustain")
    private float sustain;
    @SynthParam(name="release")
    private float releaseSecs;

    private float tStart;
    private float tRelease = Float.MAX_VALUE;
    private float qRelease = -1f;
    private boolean active = false;

    public ADSREnvelope() {
        this.attackSecs = 0;
        this.attackSecs = 0;
        this.sustain = 1.0f;
        this.decaySecs = 0f;
    }

    @Override
    public float value(float time) {
        if( !active ) {
            return 0;
        }
        float tEnv = time - tStart;
        boolean released = time >= tRelease;
        float value = 0;
        if( released ) {
            if( releaseSecs<=0) {
                active = false;
                return 0;
            }
            tEnv = time - tRelease;
            if( tEnv < 0 ) {
                this.active = false;
                value = 0;
            } else {
                if (qRelease < 0) {
                    qRelease = sustain / releaseSecs;
                }
                value = Math.max(sustain - tEnv * qRelease, 0);
            }
            if( value == 0 ) {
                active = false;
            }
        } else if( tEnv < attackSecs ) {
            value = tEnv * 1.0f/attackSecs;
        } else if( tEnv < attackSecs+decaySecs ) {
            tEnv -= attackSecs;
            float qDecay = (1.0f - sustain)/decaySecs;
            value = 1.0f - tEnv * qDecay;
        } else {
            value = sustain;
        }
        return value;
    }

    @Override
    public ADSREnvelope reset(float time) {
        this.tStart = Float.MIN_VALUE;
        this.active = false;
        return this;
    }

    @Override
    public ADSREnvelope start(float time) {
        this.tStart = time;
        this.tRelease = Float.MAX_VALUE;
        this.active = true;
        return this;
    }

    @Override
    public ADSREnvelope release(float time) {
        this.tRelease = time;
        return this;
    }

    public float getAttackSecs() {
        return attackSecs;
    }

    public ADSREnvelope setAttackSecs(float attackSecs) {
        this.attackSecs = attackSecs;
        return this;
    }

    public float getDecaySecs() {
        return decaySecs;
    }

    public ADSREnvelope setDecaySecs(float decaySecs) {
        this.decaySecs = decaySecs;
        return this;
    }

    public float getSustain() {
        return sustain;
    }

    public ADSREnvelope setSustain(float sustain) {
        this.sustain = sustain;
        this.qRelease = -1;
        return this;
    }

    public float getReleaseSecs() {
        return releaseSecs;
    }

    public ADSREnvelope setReleaseSecs(float releaseSecs) {
        this.releaseSecs = releaseSecs;
        this.qRelease = -1;
        return this;
    }

    @Override
    public void keyUp(float time, PianoKeys k, float velocity) {
        this.release(time);
    }

    @Override
    public void keyDown(float time, PianoKeys k, float velocity) {
        this.start(time);
    }

    @Override
    public void aftertouch(float time, int value) {
    }

    @Override
    public boolean isActive() {
        return this.active;
    }
}
