package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.AudioTestSupport;
import de.spraener.jsynth.SynthContext;
import org.junit.jupiter.api.Test;

public class PhaseSineFunctionTests {
    private PhasedSineFunction uut = new PhasedSineFunction(SynthContext.soundFormat);

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
}
