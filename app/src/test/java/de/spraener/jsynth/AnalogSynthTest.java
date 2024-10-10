package de.spraener.jsynth;

import de.spraener.jsynth.voice.Voice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AnalogSynthTest {
    private PianoKeys[] sequence;

    private AudioTestSupport audioTestSupport = new AudioTestSupport();

    @BeforeEach
    public void setup() {
        PianoKeys[] org = TestConstants.sequence;
        this.sequence = new PianoKeys[org.length * 2];
        for( int i = 0; i < org.length; i++ ) {
            this.sequence[2 * i] = PianoKeys.keyOf(org[i].n() - 12);
            this.sequence[2 * i + 1 ] = org[i];
        }
    }
    @Test
    public void testPlaySequence() throws Exception {
        SoundFormat sf = new SoundFormat();
        float dauer = 10.0f;
        int bufferSize = (int) (dauer * sf.sampleRate);
        float[] buffer = new float[bufferSize];

        AnalogSynth as = new AnalogSynth(new SoundFormat());
        int noteLength = buffer.length / sequence.length;
        int oldNoteIdx = 0;
        float pMax = Float.MIN_VALUE;
        float pMin = Float.MAX_VALUE;
        boolean chordTrigered = false;
        boolean chordReleased = false;
        Voice[] cv = new Voice[4];
        Voice activeVoice = as.pressKey(0, sequence[oldNoteIdx], 1.0f);
        float tNoteStart = 0;
        long tStart = System.currentTimeMillis();
        for (int sample = 0; sample < buffer.length; sample++) {
            float time = sample / sf.sampleRate;
            int noteIdx = (sample / noteLength) % sequence.length;
            if (oldNoteIdx != noteIdx) {
                // as.releaseVoice(time, activeVoice, 1.0f);
                oldNoteIdx = noteIdx;
                activeVoice = as.pressKey(time, sequence[oldNoteIdx], 1.0f);
            }
            buffer[sample] = as.sample(time);
            if( activeVoice != null && time - tNoteStart > 0.1) {
                as.releaseVoice(time, activeVoice, 1.0f);
                activeVoice = null;
            }
            if( time > dauer * 0.7f && !chordTrigered) {
                cv[0] = as.pressKey(time, PianoKeys.C4, 1.0f);
                cv[1] = as.pressKey(time, PianoKeys.Ais4, 1.0f);
                cv[2] = as.pressKey(time, PianoKeys.G4, 1.0f);
                cv[3] = as.pressKey(time, PianoKeys.E4, 1.0f);
                chordTrigered = true;
            }
            if( time > dauer * 0.7f + 0.5f && !chordReleased) {
                for( Voice v : cv ) {
                    as.releaseVoice(time, v, 1.0f);
                }
                chordReleased = true;
            }
            if( buffer[sample] > pMax ) {
                pMax = buffer[sample];
            }
            if( buffer[sample] < pMin ) {
                pMin = buffer[sample];
            }
        }
        long tEnd = System.currentTimeMillis();
        System.out.printf("Für %.3fs Sound wurden %3fs CPU benötigt.%n", dauer, (tEnd-tStart)/1000.0);
        buffer = audioTestSupport.normalize(buffer, pMin, pMax);
        audioTestSupport.playSoundData(buffer, sf.sampleRate, wd -> wd.scaleWidth());
    }
}
