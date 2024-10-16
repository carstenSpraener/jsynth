package de.spraener.jsynth.oscilator;

import de.spraener.jsynth.SynthContext;
import de.spraener.jsynth.modular.annotations.ProvidedValue;
import de.spraener.jsynth.modular.annotations.SynthParamProvider;

@SynthParamProvider(type = OscillatorFunctionProvider.class)
public class OscillatorFunctionProvider {

    @ProvidedValue(value="SawTooth", icon="sawtooth")
    public OscillatorFunction sawTooth() {
        return new SawToothWaveFunction(SynthContext.soundFormat);
    }

    @ProvidedValue(value="Pulse", icon="pulsewave")
    public OscillatorFunction pulsewave() {
        return new SquareWaveFunction(SynthContext.soundFormat);
    }

    @ProvidedValue(value="Sine", icon="sinewave")
    public OscillatorFunction sinewave() {
        return new SineWaveFunction(SynthContext.soundFormat);
    }

    @ProvidedValue(value="Noise", icon="noise")
    public OscillatorFunction noise() {
        return new NoiseWaveFunction();
    }
}
