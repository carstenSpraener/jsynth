package de.spraener.jsynth.envelope;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.WaveDisplay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ADSREnvelopeTests {

    ADSREnvelope uut = new ADSREnvelope();

    @Test
    public void testRelease() throws Exception {
        uut.setReleaseSecs(1.0f);
        uut.setSustain(0.5f);
        uut.release(0);
        assertEquals(0.5f, uut.value(0));
        assertEquals(0.25f, uut.value(0.5f));
        assertEquals(0.125f, uut.value(0.75f));
        assertEquals(0, uut.value(1.0f));
        assertEquals(0, uut.value(1.5f));
    }

    @Test
    public void testZeroRelease() throws Exception {
        uut.setSustain(1.0f);
        uut.setReleaseSecs(0);
        uut.release(0);
        assertEquals(0, uut.value(0.3f));
    }

    @Test
    public void testAttack() throws Exception {
        uut.setAttackSecs(0.5f);
        uut.start(0f);
        uut.release(1.0f);
        assertEquals(0, uut.value(0));
        assertEquals(0.5f, uut.value(0.25f));
        assertEquals(1.0f, uut.value(0.5f));
    }

    @Test
    public void testDecay() throws Exception {
        uut.setAttackSecs(1.0f);
        uut.setDecaySecs(0.5f);
        uut.setSustain(0.5f);
        uut.setReleaseSecs(0.25f);
        uut.start(0f);
        uut.release(2.0f);
        assertEquals(1.0f, uut.value(1.0f));
        assertEquals(0.75f, uut.value(1.25f));
        assertEquals(0.5f, uut.value(1.5f));
        assertEquals(0.5f, uut.value(1.9f));
    }

    @Test
    public void testZeroDecay() throws Exception {
        uut.setAttackSecs(1.0f);
        uut.setDecaySecs(0f);
        uut.setSustain(0.5f);
        uut.setReleaseSecs(0.25f);
        uut.start(0f);
        uut.release(2.0f);
        assertEquals(0.5f, uut.value(1.0f));
    }

    @Test
    public void displayEnvelope() throws Exception {
        uut.setAttackSecs(0.01f);
        uut.setDecaySecs(0.05f);
        uut.setSustain(0.75f);
        uut.setReleaseSecs(0.5f);
        uut.start(0);
        uut.release(1.5f);

        ADSREnvelope vca = uut;
        vca.setAttackSecs(0.01f);
        vca.setDecaySecs(0.05f);
        vca.setSustain(0.75f);
        vca.setReleaseSecs(0.3f);
        vca.start(0);
        vca.release(0.7f);

        float tDauer = 2.0f;
        SoundFormat sf = new SoundFormat().setSampleRate(1024);
        float[] values = new float[(int)(1.0f/sf.tSample)];
        for( int i=0; i<values.length; i++ ) {
            float t = i * sf.tSample;
            values[i] = uut.value(t);
        }
        WaveDisplay wd = new WaveDisplay(values);
        Thread.sleep(3000);
    }
}
