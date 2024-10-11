package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.AudioTestSupport;
import de.spraener.jsynth.SynthContext;
import de.spraener.jsynth.WaveDisplay;
import org.junit.jupiter.api.Test;

public class PhaseSineFunctionTests {
    private PhasedSineFunction uut = new PhasedSineFunction(SynthContext.soundFormat);
    private AudioTestSupport audioTestSupport = new AudioTestSupport();

    @Test
    public void testSine() throws Exception {
        float f = 440;
        float dauer = 4;
        float[] data = new float[(int)(SynthContext.soundFormat.sampleRate * dauer)];
        float t = 0;
        for( int s=0; s<data.length; s++) {
            data[s] = uut.getSample(f,t);
            t += SynthContext.soundFormat.tSample;
        }

        new AudioTestSupport().playSoundData(data, SynthContext.soundFormat.sampleRate);
    }

    @Test
    public void testPhaseVsSine() throws Exception {
        OscillatorFunction swf = new SquareWaveFunction(SynthContext.soundFormat);
        OscillatorFunction pswf = new SawToothWaveFunction(SynthContext.soundFormat);
        float f = 86.132812f;
        float dauer = 2 * 1/f;
        float[] data = new float[(int)(SynthContext.soundFormat.sampleRate * dauer)];
        float[] phasedData = new float[(int)(SynthContext.soundFormat.sampleRate * dauer)];
        float t=0;
        for( int s=0; s<data.length; s++) {
            data[s] = swf.getSample(f,t);
            phasedData[s] = pswf.getSample(f,t);
            if( s == data.length/2 ) {
                f = 120;
            }
            t+=SynthContext.soundFormat.tSample;
        }
        WaveDisplay wd = new WaveDisplay(data);
        new WaveDisplay(phasedData);
        Thread.sleep(10000);
    }
}
