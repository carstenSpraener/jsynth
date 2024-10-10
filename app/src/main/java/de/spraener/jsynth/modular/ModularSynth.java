package de.spraener.jsynth.modular;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.modular.annotations.SignalOutput;
import de.spraener.jsynth.modular.annotations.SignalRoot;
import de.spraener.jsynth.modular.reflection.FieldWrapper;
import de.spraener.jsynth.sequencer.Instrument;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModularSynth implements Instrument {
    private final MSVoice[] voices;
    private int nxtVoice = 0;
    private String outValue = "out";

    public ModularSynth(int nofVoices) {
        this.voices = new MSVoice[nofVoices];
        for( int i=0; i<nofVoices; i++ ) {
            this.voices[i] = new MSVoice();
        }
    }

    public MSVoice getVoice(int idx) {
        return voices[idx];
    }

    public ModularSynth setOutValue(String outValue) {
        for(MSVoice v : voices) {
            v.setOutputValue(outValue);
        }
        return this;
    }

    public ModularSynth addComponent(String name, Supplier<Object> cSupplier) {
        for( int i = 0; i < this.voices.length; i++ ) {
            this.voices[i].addComponent(name, cSupplier.get());
        }
        return this;
    }

    private ModularSynth fireTime(float t) {
        for( int i=0; i<voices.length; i++ ) {
            for (TimeResponsive tr : voices[i].timeResponsives) {
                tr.setTime(t);
            }
        }
        return this;
    }

    public float sample(float t) {
        float v = 0f;
        for( int i=0; i<voices.length; i++ ) {
            v += voices[i].sample(t);
        }
        return v;
    }

    public Map<String,Float> lastValueMap(int voice) {
        return this.voices[voice].valueMap;
    }

    public String listAllParams() {
        if( voices[0].pathResolver == null ) {
            build();
        }
        StringBuilder sb = new StringBuilder();
        for( String path : voices[0].pathResolver.listAll() ) {
            sb.append(path).append('\n');
        }
        return sb.toString();
    }

    public Map<String, Float> getRootValues(float time) {
        Map<String, Float> rootValues = new HashMap<>();
        for( Map.Entry<String, Object> e : voices[0].synthComponents.entrySet() ) {
            Object component = e.getValue();
            if( component.getClass().isAnnotationPresent(SignalRoot.class) ) {
                for( Method m : component.getClass().getDeclaredMethods() ) {
                    if( !m.isAnnotationPresent(SignalOutput.class) ) {
                        continue;
                    }
                    try {
                        String name = e.getKey()+"."+m.getAnnotation(SignalOutput.class).name();
                        Float value = (Float) m.invoke(component, time);
                        rootValues.put(name, value);
                    } catch( ReflectiveOperationException roXC ) {
                        // ignore
                    }
                }
            }
        }
        return rootValues;
    }

    public void build() {
        for( int i = 0; i < this.voices.length; i++ ) {
            this.voices[i].build();
        }
    }

    public String getComponentPath(Object obj) {
        for( MSVoice v : this.voices ) {
            String path = v.pathResolver.getComponentPath(obj);
            if( path != null ) {
                return path;
            }
        }
        return null;
    }

    public MSVoice pressKey(float time, PianoKeys k, float velocity) {
        if (k == PianoKeys.NONE) {
            return null;
        }
        MSVoice voice = this.voices[nxtVoice++];
        nxtVoice %= voices.length;
        if( voice.keyPlaying != null ) {
            releaseKey(time, voice, 1.0f);
        }
        voice.pressKey(time, k, velocity);
        return voice;
    }

    @Override
    public ModularSynth releaseKey(Object value, float time, PianoKeys k, float velocity) {
        MSVoice v = (MSVoice) value;
        v.releaseKey(time, velocity);
        return this;
    }

    public void releaseKey(float time, MSVoice voice, float velocity) {
        voice.releaseKey(time, velocity);
    }

    public void setValue(String path, float value) {
        for( MSVoice voice : voices ) {
            Object o = voice.pathResolver.resolve(path);
            if (o instanceof FieldWrapper fw) {
                fw.set(value);
            }
        }
    }
}
