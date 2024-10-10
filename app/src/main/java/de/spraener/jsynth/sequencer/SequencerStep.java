package de.spraener.jsynth.sequencer;

import de.spraener.jsynth.PianoKeys;

import java.util.List;

public class SequencerStep {
    private StepSequencer sequencer;
    private PianoKeys[] keysToPlay;
    private float duration = 0.5f; // a half step
    private int stepIdx;

    public SequencerStep(StepSequencer sequencer, int stepIdx) {
        this.sequencer = sequencer;
    }

    public SequencerStep withKeys(PianoKeys... k) {
        this.keysToPlay = k;
        return this;
    }

    public SequencerStep forSteps(float nofSteps) {
        this.duration = sequencer.getTStep() * nofSteps;
        return this;
    }

    public SequencerEvent[] createEvents(List<SequencerEvent> eventList) {
        float tStepStart = this.sequencer.getLStep() * this.stepIdx;
        float tStepEnd = tStepStart + this.sequencer.getLStep() * this.duration;
        StopStepEvent stop =  new StopStepEvent(tStepEnd, this);
        StartStepEvent start = new StartStepEvent(tStepStart, this);
        start.setStopEvent(stop);
        return new SequencerEvent[] {start, stop};
    }

    public PianoKeys[] getKeys() {
        return this.keysToPlay;
    }

    public SequencerStep withStepDuration(float stepDuration) {
        this.duration = stepDuration;
        return this;
    }
}
