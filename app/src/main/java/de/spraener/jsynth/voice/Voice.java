package de.spraener.jsynth.voice;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.filter.StreamFilter;
import de.spraener.jsynth.oscilator.Oscillator;
import de.spraener.jsynth.envelope.Envelope;

import java.util.ArrayList;
import java.util.List;

public class Voice {
    private int id;
    private SoundFormat soundFormat;
    private List<KeyListener> keyListeners = new ArrayList<>();
    private List<Oscillator> oscilators = new ArrayList<>();
    private List<TimeResponsive> timeListener = new ArrayList<>();

    private StreamFilter<?> filter;
    private Envelope vca;
    private PianoKeys playedKey;

    public Envelope<Envelope> getVca() {
        return this.vca;
    }

    public static class VoiceBuilder {
        private Voice voice;

        public VoiceBuilder(SoundFormat soundFormat) {
            this.voice = new Voice(soundFormat);
        }

        public VoiceBuilder withOscilator(Oscillator oscilator) {
            this.voice.oscilators.add(oscilator);
            this.voice.keyListeners.add(oscilator);
            this.voice.timeListener.add(oscilator);
            return this;
        }

        public VoiceBuilder withFilter(StreamFilter<?> filter) {
            this.voice.filter = filter;
            if( filter instanceof KeyListener kl) {
                this.voice.keyListeners.add(kl);
            }
            if( filter instanceof TimeResponsive tr) {
                this.voice.timeListener.add(tr);
            }
            return this;
        }

        public VoiceBuilder withVca(Envelope vca) {
            this.voice.vca = vca;
            if( vca instanceof KeyListener kl) {
                this.voice.keyListeners.add(kl);
            }
            if( vca instanceof TimeResponsive tr) {
                this.voice.timeListener.add(tr);
            }
            return this;
        }

        public Voice build() {
            return voice;
        }

        public VoiceBuilder withID(int id) {
            this.voice.id = id;
            return this;
        }

        public VoiceBuilder withTimeResponsive(TimeResponsive tr) {
            this.voice.timeListener.add(tr);
            return this;
        }
    }

    public Voice(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
    }

    public Voice startKey(float time, PianoKeys key, float velocity) {
        fireKeyStart(time, key, velocity);
        this.playedKey = key;
        return this;
    }

    private void fireKeyStart(float time, PianoKeys key, float velocity) {
        for( KeyListener kl : keyListeners ) {
            kl.keyDown(time, key, velocity);
        }
    }

    public Voice release(float time, PianoKeys key, float velocity) {
        fireKeyRelease(time, key, velocity);
        this.playedKey = null;
        return this;
    }

    private void fireKeyRelease(float time, PianoKeys key, float velocity) {
        for( KeyListener kl : keyListeners ) {
            kl.keyUp(time,key, velocity);
        }
    }

    private void fireTime(float t) {
        for( TimeResponsive tr : this.timeListener ) {
            tr.setTime(t);
        }
    }

    public float sample(float t) {
        if( !this.vca.isActive() ) {
            return 0;
        }
        fireTime(t);
        float ampQ = 1.0f/this.oscilators.size();
        float sample = 0;
        for( Oscillator osc : oscilators ) {
            sample += osc.getSample(t) * ampQ;
        }
        sample = this.filter.filter(sample);
        return sample * vca.value(t);
    }

    public PianoKeys getKey() {
        return playedKey;
    }

    public boolean isPlaying() {
        return this.vca.isActive();
    }

    public StreamFilter<?> getFilter() {
        return filter;
    }

    public int getID() {
        return this.id;
    }
}
