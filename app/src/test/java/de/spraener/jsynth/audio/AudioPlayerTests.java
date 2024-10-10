package de.spraener.jsynth.audio;

import de.spraener.jsynth.SynthContext;
import de.spraener.jsynth.modular.ModularSynth;
import de.spraener.jsynth.modular.ModularSynthCreator;
import de.spraener.jsynth.oscilator.Oscillator;
import org.junit.jupiter.api.Test;

public class AudioPlayerTests {
    private float time = 0;
    private ModularSynth ms;

    @Test
    public void testBackgroundPlaying() throws Exception {
        ms = ModularSynthCreator.createRingModulatedSynth(SynthContext.soundFormat);
        Oscillator o1 = (Oscillator) ms.getVoice(0).getComponent("osc1");
        try (AudioPlayer player = new AudioPlayer()) {
            player.withAudioSource(this::createAudioBlock);
            player.start();
            int i=0;
            while( true ) {
                try {Thread.sleep(125);} catch (InterruptedException e) {}
                float f = 440;
                if( i%2==0 )  {
                    f = 220f;
                }
                o1.setFrequence(f);
            }
        }
    }

    private float[] createAudioBlock(int nofSamples) {
        float[] block = new float[nofSamples];
        for (int i = 0; i < nofSamples; i++) {
            block[i] = ms.sample(time);
            time += SynthContext.soundFormat.tSample;
        }
        return block;
    }
}
