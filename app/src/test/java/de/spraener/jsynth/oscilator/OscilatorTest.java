package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.TestConstants;
import de.spraener.jsynth.WaveDisplay;
import de.spraener.jsynth.envelope.ADSREnvelope;
import de.spraener.jsynth.filter.*;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;

import java.io.*;

class OscilatorTest {
    private static final PianoKeys[] sequence = TestConstants.sequence;
    @Test
    public void testSimpleSineOscilator() throws Exception {
        SoundFormat soundFormat = new SoundFormat();
        OscillatorFunction[] functions = new OscillatorFunction[]{
                new SineWaveFunction(soundFormat),
                new SquareWaveFunction(soundFormat).setPulseLengthPercent(0.3f),
                new SawToothWaveFunction(soundFormat),
                new NoiseWaveFunction()
        };


        StreamFilter<?> filter = new StackedFilter(
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f),
                new AllpassFilter(soundFormat).setCutoff(5500).setAmplitude(1.0f)
        );
        ADSREnvelope vca = new ADSREnvelope();
        vca.setAttackSecs(0.0f);
        vca.setDecaySecs(0.01f);
        vca.setSustain(0.5f);
        vca.setReleaseSecs(0.7f);
        vca.start(0);
        vca.release(0.2f);


        for (OscillatorFunction function : functions) {
            Oscillator uut = new Oscillator(soundFormat, function);
            uut.setKey(PianoKeys.A_TUNE);

            final double seconds = 1.0;
            int bufferSize = (int) (seconds * soundFormat.sampleRate);
            bufferSize = (bufferSize / soundFormat.bufferSize + 1) * soundFormat.bufferSize;

            float[] buffer = new float[bufferSize];
            float t = 0;
            filter.setCutoff(12000);

            for (int sample = 0; sample < buffer.length; sample++) {
                buffer[sample] = vca.value(t) * filter.filter(uut.getSample(t));
                t += soundFormat.tSample;
                filter.setCutoff(filter.getCutoff()*0.999857f);
            }
            playSoundData(buffer, soundFormat.sampleRate);
        }
    }

    @Test
    public void testWav() throws Exception {
        SoundFormat soundFormat = new SoundFormat();
        final float sampleRate = soundFormat.sampleRate;
        final double amplitude = 1.0;
        final double seconds = 4.5;
        int bufferSize = (int) (seconds * sampleRate);
        bufferSize = (bufferSize / soundFormat.bufferSize + 1) * soundFormat.bufferSize;

        float[] buffer = new float[bufferSize];

        SquareWaveFunction sqf = new SquareWaveFunction(soundFormat);
        sqf.setPulseLengthPercent(0.5f);
        Oscillator uut = new Oscillator(soundFormat, sqf);//new SawToothWaveFunction(soundFormat));

        Oscillator uut2 = new Oscillator(soundFormat, new SawToothWaveFunction(soundFormat));
        uut2.setFineTune(1.015f);
        uut2.setOctave(-1);

        Oscillator sub = new Oscillator(soundFormat, new SawToothWaveFunction(soundFormat));
        sub.setFineTune(0.985f);
        sub.setOctave(-2);

        Oscillator lfo = new Oscillator(soundFormat, new SineWaveFunction(soundFormat));
        lfo.setFrequence(1f);

        float fStart = 200f;
        StreamFilter<?> streamingFilter = new StackedFilter(
                /*new AllpassFilter(soundFormat),
                new AllpassFilter(soundFormat),
                new AllpassFilter(soundFormat),*/
                new AllpassFilter(soundFormat)
        );

        int noteLength = buffer.length / sequence.length;
        float fEnd = 50f;
        float fStep = (fStart - fEnd) / buffer.length;
        int oldNoteIdx = 0;
        float fCutoff = fStart;

        ADSREnvelope env = new ADSREnvelope();
        env.setAttackSecs(0.002f);
        env.setDecaySecs(0.015f);
        env.setDecaySecs(0.75f);
        env.setReleaseSecs(0.3f);

        streamingFilter.setCutoff(fCutoff);
        env.start(0);
        for (int sample = 0; sample < buffer.length; sample++) {
            float time = sample / sampleRate;
            int noteIdx = (sample / noteLength) % sequence.length;
            if (oldNoteIdx != noteIdx) {
                oldNoteIdx = noteIdx;
                fCutoff = fStart;
                //streamingFilter.setCutoff(fCutoff);
                if( noteIdx> 0 && sequence[noteIdx]!=sequence[oldNoteIdx-1]) {
                    env.start(time);
                    env.release(time+0.08f);
                }
            }
            uut.setKey(sequence[noteIdx]);
            uut2.setKey(sequence[noteIdx]);
            sub.setKey(sequence[noteIdx]);

            sqf.setPulseLengthPercent(0.5f - 0.45f * lfo.getSample(time));

            float sampleValue = (float) (amplitude / 3f * uut.getSample(time));
            sampleValue += (float) (amplitude / 3f * uut2.getSample(time));
            sampleValue += (float) (amplitude / 3f * sub.getSample(time));

            sampleValue = streamingFilter.filter(sampleValue);
            buffer[sample] = env.value(time) * sampleValue;
            float cutoff = streamingFilter.getCutoff() * 0.999f;
            //streamingFilter.setCutoff(cutoff);
        }
        playSoundData(buffer, (float) sampleRate);
    }

    private void playSoundData(float[] buffer, float sampleRate) throws Exception {
        final byte[] byteBuffer = new byte[buffer.length * 2];

        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = (int) (buffer[bufferIndex++] * 32767.0);

            byteBuffer[i++] = (byte) x;
            byteBuffer[i] = (byte) (x >>> 8);
        }

        final boolean bigEndian = false;
        final boolean signed = true;

        final int bits = 16;
        final int channels = 1;

        AudioFormat format = new AudioFormat(sampleRate, bits, channels, signed, bigEndian);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, byteBuffer.length);
        WaveDisplay wd = new WaveDisplay(buffer);
        int sleep = (int) (byteBuffer.length * 1.0 / sampleRate) * 500;
        play(audioInputStream, sleep);
        wd.setVisible(false);
        audioInputStream = new AudioInputStream(new ByteArrayInputStream(byteBuffer), format, buffer.length);
        AudioSystem.write(
                audioInputStream
                ,AudioFileFormat.Type.WAVE
                ,new File("test.wav")
        );

    }

    private void play(AudioInputStream ais, int sleep) throws Exception {
        Clip clip = AudioSystem.getClip();
        clip.open(ais);
        clip.start();
        clip.drain();
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ignore) {
        }
        clip.flush();
        clip.close();
    }
}