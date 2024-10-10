package de.spraener.jsynth.voice;

import de.spraener.jsynth.PianoKeys;

public interface KeyListener {
    enum EventType{
        KEY_UP,
        KEY_DOWN,
        AFTERTOUCH
    }

    void keyUp(float time, PianoKeys k, float velocity);
    void keyDown(float time, PianoKeys k, float velocity);
    void aftertouch(float time, int value);
}
