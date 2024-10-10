package de.spraener.jsynth.modular.reflection;

import java.lang.reflect.Method;
import java.util.Map;

public class MethodWrapper {
    private Method method;
    private Object obj;
    private String name;

    public MethodWrapper(String name, Method method, Object obj) {
        this.name = name;
        this.method = method;
        this.method.setAccessible(true);
        this.obj = obj;
    }

    public Float getValue(float time) {
        try {
            return (Float) this.method.invoke(obj, time);
        } catch( Exception e ) {}
        return null;
    }

    public String getName() {
        return name;
    }

    public void toMap(float time, Map<String, Float> valueMap) {
        valueMap.put(this.name, this.getValue(time));
    }
}
