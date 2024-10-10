package de.spraener.jsynth.modular.reflection;

import java.lang.reflect.Field;

public class FieldWrapper {
    private Object obj;
    private Field field;
    private String path;

    public FieldWrapper( String path, Object obj, Field field) {
        this.obj = obj;
        this.field = field;
        this.field.setAccessible(true);
        this.path = path;
    }

    public Object get() {
        try {
            return this.field.get(this.obj);
        } catch(Exception e){}
        return null;
    }

    public void set(Object value) {
        try {
            this.field.set(obj, value);
        } catch( Exception e ) {}
    }
}
