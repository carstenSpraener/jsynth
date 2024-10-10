package de.spraener.jsynth.sequencer;

import de.spraener.jsynth.PianoKeys;

public interface Instrument {
    float sample(float time);

    Object pressKey(float time, PianoKeys k, float velocity);
    Object releaseKey(Object value, float time, PianoKeys k, float velocity);
}
