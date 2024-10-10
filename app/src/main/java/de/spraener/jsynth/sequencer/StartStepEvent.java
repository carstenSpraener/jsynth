package de.spraener.jsynth.sequencer;

import de.spraener.jsynth.PianoKeys;

public class StartStepEvent extends SequencerEvent {
    SequencerStep step;
    private StopStepEvent    stopEvent;

    public StartStepEvent(float time, SequencerStep sequencerStep) {
        super(time);
        this.step = sequencerStep;
    }

    @Override
    void trigger(StepSequencer sequencer, Instrument i) {
        for(PianoKeys k : step.getKeys() ) {
            this.stopEvent.setKeyPressValue(i.pressKey(this.time, k, 1.0f));
        }
    }

    public SequencerEvent setStopEvent(StopStepEvent stop) {
        this.stopEvent = stop;
        return this;
    }

}
