package de.spraener.jsynth.sequencer;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.SynthContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StepSequencer {
    private SoundFormat soundFormat = SynthContext.soundFormat;

    private int bpm = 120;
    private float tBeat = 60.0f/bpm;
    private float lStep = 1/16f;
    private float tStep = lStep*tBeat;

    private List<SequencerStep> steps = new ArrayList<>();

    public SoundFormat getSoundFormat() {
        return soundFormat;
    }

    public void setSoundFormat(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public float getTBeat() {
        return tBeat;
    }

    public void setTBeat(float tBeat) {
        this.tBeat = tBeat;
    }

    public float getLStep() {
        return lStep;
    }

    public void setLStep(float lStep) {
        this.lStep = lStep;
    }

    public float getTStep() {
        return tStep;
    }

    public void setTStep(float tStep) {
        this.tStep = tStep;
    }

    public List<SequencerStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SequencerStep> steps) {
        this.steps = steps;
    }

    public StepSequencer addStep(float stepDuration, PianoKeys... k) {
        this.steps.add(
                new SequencerStep(this, this.steps.size())
                        .withStepDuration(stepDuration)
                        .withKeys(k)
        );
        return this;
    }

    public StepSequencer addStep(PianoKeys... k) {
        this.steps.add(
                new SequencerStep(this, this.steps.size())
                        .withStepDuration(0.5f)
                        .withKeys(k)
        );
        return this;
    }

    public float[] record(Instrument instrument, int nofLoops) {
        float totalDuration = tStep * steps.size() * nofLoops;
        int bufferSize = (int)(totalDuration*soundFormat.sampleRate);
        float[] buffer = new float[bufferSize];

        SequencerEvent[] events = scheduleEvents();
        float seqTime = 0;
        int eventIdx = 0;
        float loopOffset = 0;
        for( int sampleIdx=0; sampleIdx<bufferSize; sampleIdx++ ) {
            float t = sampleIdx * soundFormat.tSample;
            while( events[eventIdx].time < t-loopOffset) {
                events[eventIdx++].trigger(this, instrument);
                eventIdx++;
                if( eventIdx >= events.length ) {
                    eventIdx = 0;
                    loopOffset += tStep * steps.size();
                }
            }
            buffer[sampleIdx] = instrument.sample(t);
        }
        return buffer;
    }

    private SequencerEvent[] scheduleEvents() {
        List<SequencerEvent> eventList = new ArrayList<>();
        for( SequencerStep step : steps ) {
            step.createEvents(eventList);
        }
        SequencerEvent[] events = eventList.toArray(new SequencerEvent[eventList.size()]);
        Arrays.sort(events, (e1,e2)-> e1.time < e2.time ? -1 : 1);
        return events;
    }
}
