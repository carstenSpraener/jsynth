package de.spraener.jsynth.modular;

import de.spraener.jsynth.*;
import de.spraener.jsynth.oscilator.Oscillator;
import de.spraener.jsynth.oscilator.SineWaveFunction;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModularSynthTests {
    private ModularSynth uut;
    private PianoKeys[] sequence = TestConstants.sequence;
    private AudioTestSupport audioTestSupport = new AudioTestSupport();
    private PrintWriter pw;
    private SoundFormat sf = SynthContext.soundFormat;

    @Test
    public void testSimpleSine() throws Exception{
        uut = ModularSynthCreator.createSineSynth(sf);
        final Oscillator o = (Oscillator) uut.getVoice(0).getComponent("osc1");
        uut.setValue("osc1.fmod", 0.05f);
        float dauer = 15;
        final float[] directBuffer = new float[(int)(dauer * sf.sampleRate)];

        // Erstelle die Samples mit 440HZ Sinus frequenzmoduliert mit
        // 4HZ;
        float fLFO = 4.0f;
        float fCarrier = 440.0f;
        float fmIntensity = 0.05f;
        recordSamples(dauer, 50, sf, uut, (t,s)->{
            float t2PI = (float)(t * Math.PI * 2);
            float pMod = fmIntensity * (float)Math.sin(t2PI * fLFO);
            float fOscillator = fCarrier * (1.0f+pMod);
            directBuffer[s] = (float)Math.sin(t2PI * fOscillator);
        });
        audioTestSupport.playSoundData(directBuffer,sf.sampleRate);
    }

    @Test
    public void testListParams() throws Exception {
        ModularSynth ms = ModularSynthCreator.buildStandardSynth(4);
        ms.setValue("ringmod.rate", 0.6f);
        ms.setValue("vca.envelope[ADSR].sustain", 0.5f);

        System.out.println(ms.listAllParams());
        System.out.println("Root Value: ");
        for (Map.Entry<String, Float> e : ms.getRootValues(0).entrySet()) {
            System.out.println("  >" + e.getKey() + ": " + e.getValue());
        }

        float t = SynthContext.soundFormat.getTSample();
        System.out.printf("All processed Values(t=%.4fms.)%n", t * 1000);

        ms.sample(0);
        MSVoice v = ms.pressKey(t, PianoKeys.A_TUNE, 1.0f);
        ms.sample(t);
        for (Map.Entry<String, Float> e : ms.lastValueMap(0).entrySet()) {
            System.out.println("  >" + e.getKey() + ": " + e.getValue());
        }
        ms.releaseKey(t, v, 1.0f);
    }

    @Test
    public void testSingleNote() throws Exception {
        ModularSynth ms = ModularSynthCreator.buildStandardSynth(1);
        float dauer = 0.8f;
        SoundFormat sf = SynthContext.soundFormat;
        int bufferSize = (int) (dauer * sf.sampleRate);
        float[] buffer = new float[bufferSize];

        MSVoice v = null;
        boolean keyPressed = false;
        boolean keyReleased = false;
        for (int sample = 0; sample < buffer.length; sample++) {
            float time = sample / sf.sampleRate;
            buffer[sample] = ms.sample(time);
            if( time >= 0.01 && !keyPressed ) {
                v = ms.pressKey(time, PianoKeys.C4, 1.0f);
                keyPressed = true;
            }
            if( time >= 0.2 && v!=null && !keyReleased ) {
                ms.releaseKey(time, v, 1.0f);
                v = null;
                keyReleased = true;
            }
        }
        buffer = audioTestSupport.normalize(buffer);
        audioTestSupport.playSoundData(buffer, sf.sampleRate, wd -> wd.scaleWidth());
    }

    @Test
    public void testPlaySequence() throws Exception {
        ModularSynth ms = ModularSynthCreator.buildStandardSynth(6);

        float dauer = 7.5f;
        SoundFormat sf = SynthContext.soundFormat;
        int bufferSize = (int) (dauer * SynthContext.soundFormat.sampleRate);
        float[] buffer = new float[bufferSize];

        int noteLength = buffer.length / sequence.length;
        int oldNoteIdx = 0;
        float pMax = Float.MIN_VALUE;
        float pMin = Float.MAX_VALUE;

        MSVoice activeVoice = ms.pressKey(0, PianoKeys.NONE, 1.0f);
        float tNoteStart = 0;
        long tStart = System.currentTimeMillis();
        for (int sample = 0; sample < buffer.length; sample++) {
            float time = sample / sf.sampleRate;
            int noteIdx = (sample / noteLength) % sequence.length;
            if (oldNoteIdx != noteIdx) {
                if (activeVoice != null) {
                    ms.releaseKey(time, activeVoice, 1.0f);
                }
                tNoteStart = time;
                oldNoteIdx = noteIdx;
                activeVoice = ms.pressKey(time, sequence[oldNoteIdx], 1.0f);
            }
            buffer[sample] = ms.sample(time);
            if (activeVoice != null && time - tNoteStart > 0.3) {
                ms.releaseKey(time, activeVoice, 1.0f);
                activeVoice = null;
            }
            if (buffer[sample] > pMax) {
                pMax = buffer[sample];
            }
            if (buffer[sample] < pMin) {
                pMin = buffer[sample];
            }
        }
        long tEnd = System.currentTimeMillis();
        System.out.printf("Für %.3fs Sound wurden %.3fs cpu benötigt.%n", dauer, (tEnd - tStart)/1000.0);

        buffer = audioTestSupport.normalize(buffer, pMin, pMax);
        audioTestSupport.playSoundData(buffer, sf.sampleRate, wd -> wd.scaleWidth());

    }

    @Test
    public void testRingModulation() throws Exception {
        SoundFormat sf = SynthContext.soundFormat;
        // Create a Synth with two Sine-Wave Oscillators
        ModularSynth ms = ModularSynthCreator.createRingModulatedSynth(sf);

        // Modulate C4 as Carrier with G4 as Modulator
        ms.setValue("osc1.frequence", PianoKeys.C4.f());
        ms.setValue("osc2.frequence", PianoKeys.G4.f());
        // Sample 15 carrier periods of audio
        float duration = 1.0f/PianoKeys.C4.f() * 15.0f;
        float[] buffer = recordSamples(duration, sf, ms);
        // Play and visualize the audio
        audioTestSupport.playSoundData(buffer, sf.sampleRate);

        // Sample 5 seconds to hear something
        buffer = recordSamples(5.0f, sf, ms);
        // Play and visualize the audio
        audioTestSupport.playSoundData(buffer, sf.sampleRate);
    }

    @Test
    public void testModulatedRingModulation() throws Exception {
        SoundFormat sf = SynthContext.soundFormat;
        ModularSynth ms = ModularSynthCreator.createRingModulatedSynth(sf, m -> {
            m.addComponent("lfo", ()->new Oscillator(sf, new SineWaveFunction(sf)));
            m.addComponent("lfo->osc2.fmod", ()->new SignalConnection(m, "lfo.out", "osc2.fmod"));
        });
        ms.setValue("osc1.frequence", PianoKeys.C4.f());
        ms.setValue("osc2.frequence", PianoKeys.G4.f());
        ms.setValue("lfo.frequence", 1.5f);
        ms.setValue("lfo.keyFollow", 0f);
        ms.setValue("lfo->osc2.fmod.rate", 0.2f);
        // Sample 5 seconds to hear something
        float[] buffer = recordSamples(5.0f, sf, ms);
        // Play and visualize the audio
        audioTestSupport.playSoundData(buffer, sf.sampleRate);

    }

    @Test
    public void testFrequencyModulation() throws Exception {
        SoundFormat sf = SynthContext.soundFormat;
        final ModularSynth ms = ModularSynthCreator.createSimpleSineFModFromLFOSynth(sf);
        uut = ms;
        final Oscillator osc1 = ms.getVoice(0).getComponent("osc1");
        final MSVoice v = ms.getVoice(0);//.setValueListener(this::recordValues);
        float duration = 2f;
        float[] fOuts = new float[(int)(duration * sf.sampleRate)];
        float[] fMods = new float[(int)(duration * sf.sampleRate)];
        pw = new PrintWriter(new FileWriter("voice-state.csv"));
        float[] buffer = recordSamples(duration, 8f, sf, ms, (t,s) -> {
            int i=0;
            float fOut = osc1.getFOut();
            fOuts[s] = 1.0f/osc1.getFrequence() * fOut - 1.0f;
            assertTrue( fOut <= 440*1.05f && fOut >= 440 * (1.0 - 0.05), "Bei Sample "+s+": FOut außerhalb des Bereichs");
            float fMod = osc1.getFMod();
            assertTrue( fMod <= 0.05f && fMod >= -0.05f, "Bei Sample "+s+": FMod außerhalb des Bereichs");
            fMods[s] = 1.0f/0.1f * fMod;
            if( s % ((int)(sf.sampleRate/2)) == 0 ) {
                i++;
            }
            float peak = v.lastValueMap().get("osc1.out");
            if( Math.abs(peak) >= 1.01 ) {
                System.err.println("Bei Sample " + s + ": Sample außerhalb des Bereichs");
            }
        });
        pw.flush();
        pw.close();
        pw = null;
        WaveDisplay wd = new WaveDisplay(fOuts);
        wd.scaleWidth();

        WaveDisplay wd2 = new WaveDisplay(fMods);
        wd2.scaleWidth();
        audioTestSupport.playSoundData(buffer, sf.sampleRate, w -> w.scaleWidth());
    }

    private List<String> keys = new ArrayList<>();

    private void recordValues(Map<String, Float> stringFloatMap) {
        if( pw == null ) return;
        if(keys.isEmpty() ) {
            for( String key : stringFloatMap.keySet() ) {
                keys.add(key);
                pw.printf("%s;", key);
            }
            pw.println("osc1.f;");
        }
        for( String key : keys ) {
            pw.printf("%4f;", stringFloatMap.get(key));
        }
        Oscillator osc =  ((Oscillator)uut.getVoice(0).getComponent("osc1"));
        pw.printf("%4f;",osc.getFOut());
        pw.printf("%d;",osc.getOctave());
        pw.printf("%4f;",osc.getTune());
        pw.printf("%4f;",osc.getFineTune());
        pw.printf("%4f;",osc.getFMod());
        pw.println();
    }

    private static float[] recordSamples(float duration, SoundFormat sf, ModularSynth ms, BiConsumer<Float, Integer>... recorders) {
        return recordSamples(duration, 0f, sf, ms, recorders);
    }

    private static float[] recordSamples(float duration, float tStart, SoundFormat sf, ModularSynth ms, BiConsumer<Float, Integer>... recorders) {
        float[] buffer = new float[(int)(duration * sf.sampleRate)];
        float t = 4.5f;
        for( int sample=0; sample<buffer.length; sample++) {
            buffer[sample] = ms.sample(t);
            t += sf.tSample;
            if( recorders != null ) {
                for( BiConsumer<Float,Integer> r : recorders) {
                    r.accept(t,sample);
                }
            }
        }
        return buffer;
    }
}
