package de.spraener.jsynth.sequencer;

public abstract class SequencerEvent {
    float time;

    public SequencerEvent(float time) {
        this.time = time;
    }

    abstract void trigger(StepSequencer sequencer, Instrument i);
}
