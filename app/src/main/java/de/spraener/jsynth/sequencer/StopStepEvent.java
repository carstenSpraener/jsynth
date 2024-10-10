package de.spraener.jsynth.sequencer;

import de.spraener.jsynth.PianoKeys;

public class StopStepEvent extends SequencerEvent {
    SequencerStep step;
    Object keyPressValue = null;
    public StopStepEvent(float time, SequencerStep sequencerStep) {
        super(time);
        this.step = sequencerStep;
    }

    @Override
    void trigger(StepSequencer sequencer, Instrument i) {
        for(PianoKeys k : step.getKeys() ) {
            i.releaseKey(this.keyPressValue, this.time, k, 1.0f);
        }
    }

    public StopStepEvent setKeyPressValue(Object o) {
        this.keyPressValue = o;
        return this;
    }
}
