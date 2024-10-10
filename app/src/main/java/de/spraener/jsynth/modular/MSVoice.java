package de.spraener.jsynth.modular;

import de.spraener.jsynth.PianoKeys;
import de.spraener.jsynth.TimeResponsive;
import de.spraener.jsynth.modular.annotations.SignalOutput;
import de.spraener.jsynth.modular.annotations.SignalRoot;
import de.spraener.jsynth.modular.reflection.MethodWrapper;
import de.spraener.jsynth.oscilator.Oscillator;
import de.spraener.jsynth.voice.KeyListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MSVoice {
    public Map<String, Object> synthComponents = new HashMap<>();
    public List<TimeResponsive> timeResponsives = new ArrayList<>();
    public List<KeyListener> keyListeners = new ArrayList<>();
    public PathResolver pathResolver;
    private SignalFlow signalFlow = null;
    private String outValue = "out";
    private List<MethodWrapper> rootValueMethods = new ArrayList<>();
    private Consumer<Map<String,Float>> valueListener = null;

    Map<String, Float> valueMap;
    PianoKeys keyPlaying = null;

    public MSVoice addComponent(String name, Object component) {
        this.synthComponents.put(name, component);
        if( component instanceof TimeResponsive tr) {
            timeResponsives.add(tr);
        }
        if( component instanceof KeyListener kl) {
            keyListeners.add(kl);
        }
        return this;
    }

    public MSVoice setOutputValue(String outValue) {
        this.outValue = outValue;
        return this;
    }
    private MSVoice fireTime(float t) {
        for (TimeResponsive tr : timeResponsives) {
            tr.setTime(t);
        }
        return this;
    }

    public MSVoice setValueListener(Consumer<Map<String,Float>> valueListener) {
        this.valueListener = valueListener;
        return this;
    }

    public Map<String,Float> lastValueMap() {
        return this.valueMap;
    }

    public String listAllParams() {
        if( pathResolver == null ) {
            build();
        }
        StringBuilder sb = new StringBuilder();
        for( String path : pathResolver.listAll() ) {
            sb.append(path).append('\n');
        }
        return sb.toString();
    }

    public Map<String, Float> getRootValues(float time) {
        Map<String, Float> rootValues = new HashMap<>();
        if( rootValueMethods.isEmpty() ) {
            for (Map.Entry<String, Object> e : this.synthComponents.entrySet()) {
                Object component = e.getValue();
                if (component.getClass().isAnnotationPresent(SignalRoot.class)) {
                    for (Method m : component.getClass().getDeclaredMethods()) {
                        if (!m.isAnnotationPresent(SignalOutput.class)) {
                            continue;
                        }
                        try {
                            String name = e.getKey() + "." + m.getAnnotation(SignalOutput.class).name();
                            Float value = (Float) m.invoke(component, time);
                            rootValues.put(name, value);
                            rootValueMethods.add( new MethodWrapper(name, m, component));
                        } catch (ReflectiveOperationException roXC) {
                            // ignore
                        }
                    }
                }
            }
        } else {
            for( MethodWrapper mw : rootValueMethods ) {
                mw.toMap(time, rootValues);
            }
        }
        return rootValues;
    }


    public void build() {
        this.pathResolver = new PathResolver(this);
        this.signalFlow = new SignalFlow(this);
        getRootValues(0);
    }

    MSVoice fireKeyEvent(KeyListener.EventType type, float t, PianoKeys k, float v) {
        Consumer<KeyListener> cKL = null;
        switch (type) {
            case KEY_DOWN:
                cKL = (kl)->kl.keyDown(t,k,v);
                break;
            case KEY_UP:
                cKL = (kl)->kl.keyUp(t,k,v);
                break;
            case AFTERTOUCH:
                cKL = (kl)->kl.aftertouch(t,(int)v);
                break;
        }
        for( KeyListener kl : keyListeners ) {
            cKL.accept(kl);
        }
        return this;
    }

    public float sample(float t) {
        fireTime(t);
        this.valueMap = this.signalFlow.process(t);
        if( this.valueListener != null ) {
            this.valueListener.accept(this.valueMap);
        }
        return this.valueMap.get(this.outValue);
    }

    public String getComponentPath(Object component) {
        return pathResolver.getComponentPath(component);
    }

    public void pressKey(float time, PianoKeys k, float velocity) {
        if( this.keyPlaying != null ) {
            releaseKey(time, velocity);
        }
        this.keyPlaying = k;
        fireKeyEvent(KeyListener.EventType.KEY_DOWN, time, k, velocity);
    }

    public MSVoice releaseKey(float time, float velocity) {
        fireKeyEvent(KeyListener.EventType.KEY_UP, time, this.keyPlaying, 1.0f);
        return this;
    }

    public<T> T getComponent(String path) {
        return (T)this.pathResolver.resolve(path);
    }
}
