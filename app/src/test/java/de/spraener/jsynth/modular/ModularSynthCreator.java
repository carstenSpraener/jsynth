package de.spraener.jsynth.modular;

import de.spraener.jsynth.SoundFormat;
import de.spraener.jsynth.SynthContext;
import de.spraener.jsynth.VCA;
import de.spraener.jsynth.envelope.ADSREnvelope;
import de.spraener.jsynth.filter.AllpassFilter;
import de.spraener.jsynth.filter.StackedFilter;
import de.spraener.jsynth.filter.StreamFilter;
import de.spraener.jsynth.filter.VCF;
import de.spraener.jsynth.oscilator.*;

import java.util.function.Consumer;

public class ModularSynthCreator {

    public static ModularSynth buildStandardSynth(int nofVoices) {
        ModularSynth ms = new ModularSynth(nofVoices);
        SquareWaveFunction pulseWave = new SquareWaveFunction(SynthContext.soundFormat);

        StreamFilter filter = new StackedFilter(
                new AllpassFilter(SynthContext.soundFormat), // 6db
                new AllpassFilter(SynthContext.soundFormat), // 12db
                new AllpassFilter(SynthContext.soundFormat), // 18db
                new AllpassFilter(SynthContext.soundFormat) // 24db
        );
        ms.addComponent("osc1", () -> new Oscillator(SynthContext.soundFormat, new SawToothWaveFunction(SynthContext.soundFormat)));
        ms.addComponent("osc2", () -> new Oscillator(SynthContext.soundFormat, pulseWave));
        ms.addComponent("osc3", ()->new Oscillator(SynthContext.soundFormat, new SineWaveFunction(SynthContext.soundFormat)));
        ms.addComponent("lfo1", () -> new Oscillator(SynthContext.soundFormat, new SineWaveFunction(SynthContext.soundFormat)));
        ms.addComponent("vcf", () -> new VCF(filter, new ADSREnvelope()));
        ms.addComponent("vca", () -> new VCA(new ADSREnvelope()));

        ms.addComponent("connect_osc->vcf", () -> new SignalSummer(ms, "vcf.in", "osc1.out", "osc2.out", "osc3.out"));
        ms.addComponent("connect_vcf->vca", () -> new SignalConnection(ms, "vcf.out", "vca.in"));
        ms.addComponent("connect_lfo->pw", ()->new SignalConnection(ms, "lfo1.out", "osc1.WaveForm[pulsewave].pulsewidth"));

        ms.addComponent("ringmod", () -> new SignalConnection(ms, "osc3.out", "osc1.volume"));

        ms.setOutValue("vca.out");
        ms.build();

        ms.setValue("osc2.volume", 0.5f);
        ms.setValue("osc2.finetune", 0.985f);
        ms.setValue("osc1.volume", 0.5f);

        ms.setValue("vcf.envelope[ADSR].attack", 0.15f);
        ms.setValue("vcf.envelope[ADSR].decay", 0.02f);
        ms.setValue("vcf.envelope[ADSR].sustain", 0.2f);
        ms.setValue("vcf.envelope[ADSR].release", 0.3f);

        ms.setValue("vca.envelope[ADSR].attack", 0.01f);
        ms.setValue("vca.envelope[ADSR].decay", 0.02f);
        ms.setValue("vca.envelope[ADSR].sustain", 0.5f);
        ms.setValue("vca.envelope[ADSR].release", 0.5f);
        ms.setValue("vcf.cutoff", 12000f);
        ms.setValue("vcf.intensity", 1f);

        return ms;
    }

    public static ModularSynth createRingModulatedSynth(SoundFormat sf, Consumer<ModularSynth>... modifiers) {
        ModularSynth ms = new ModularSynth(1);
        ms.addComponent("osc1", ()->new Oscillator(sf, new SineWaveFunction(sf)));
        ms.addComponent("osc2", ()->new Oscillator(sf, new SineWaveFunction(sf)));
        // Connect Oscillator 1 out to Synth out
        ms.setOutValue("osc1.out");
        // Do the ring modulation: Connect output of Oscillator 2 to volume input of Oscillator 1
        ms.addComponent("ringmod", ()->new SignalConnection(ms, "osc2.out", "osc1.volume"));
        applyModifiers(modifiers, ms);
        // Build the synth (analyze signal flow, cache reflective access etc..)
        ms.build();
        return ms;
    }

    private static void applyModifiers(Consumer<ModularSynth>[] modifiers, ModularSynth ms) {
        if( modifiers !=null) {
            for( Consumer<ModularSynth> mod : modifiers) {
                mod.accept(ms);
            }
        }
    }

    /**
     * Builds a Synth with a Sinewave oscillator and one LFO. The LFO has a frequency of 2Hz and does not follow
     * a keyboard. The output of the LFO is connected to the frequency modulation input of oscillator 1.
     *
     * @param sf
     * @param modifiers
     * @return
     */
    public static ModularSynth createSimpleSineFModFromLFOSynth(SoundFormat sf, Consumer<ModularSynth>... modifiers) {
        ModularSynth ms = new ModularSynth(1);
        ms.addComponent("osc1", ()->new Oscillator(sf, new PhasedSineFunction(sf)));
        ms.addComponent("lfo", ()->new Oscillator(sf, new SineWaveFunction(sf)));
        ms.addComponent("lfoFmod", () -> new SignalConnection(ms, "lfo.out", "osc1.fmod"));
        applyModifiers(modifiers, ms);
        ms.setOutValue("osc1.out");
        ms.build();

        ms.setValue("lfo.keyFollow", 0f);
        ms.setValue("lfo.frequence", 220f);
        ms.setValue("lfoFmod.rate", 0.08f);
        return ms;
    }

    public static ModularSynth createSineSynth(SoundFormat sf, Consumer<ModularSynth>... modifiers) throws Exception {
        ModularSynth ms = new ModularSynth(1);
        ms.addComponent("osc1", ()->new Oscillator(sf, new SineWaveFunction(sf)));
        applyModifiers(modifiers, ms);
        ms.setOutValue("osc1.out");
        ms.build();

        return ms;
    }
}
