package de.spraener.jsynth.modular;

import de.spraener.jsynth.modular.annotations.SynthComponent;
import de.spraener.jsynth.modular.reflection.FieldWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathResolver {
    private MSVoice ms;
    private Map<String, Object> roots = null;
    private Map<String, Object> pathToComponentMap = new HashMap<>();
    private Map<Object, String> componentToPath = new HashMap<>();

    public PathResolver(MSVoice ms) {
        this.ms = ms;
        this.roots = ms.synthComponents;
        initializeMaps();
    }

    private void initializeMaps() {
        List<String> pathList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : roots.entrySet()) {
            String path = entry.getKey();
            Object root = entry.getValue();
            listAll(root, path, pathList);
        }
    }

    public List<String> listAll() {
        List<String> pathList = new ArrayList<>();
        pathList.addAll(pathToComponentMap.keySet());
        pathList.sort(String::compareTo);
        return pathList;
    }

    private void listAll(Object parent, String path, List<String> pathList) {
        pathList.add(path);
        componentToPath.put(parent, path);
        pathToComponentMap.put(path, parent);

        for (Field field : parent.getClass().getDeclaredFields()) {
            String componentName = getSynthComponentName(field.getDeclaredAnnotations());
            if (componentName != null) {
                if (field.getType().isAnnotationPresent(SynthComponent.class)) {
                    listAll(field.getType(), path + "." + field.getName(), pathList);
                } else {
                    String fieldPath =path + "." + componentName;
                    pathList.add(fieldPath);
                    FieldWrapper fw = new FieldWrapper(fieldPath, parent, field);
                    componentToPath.put(fw, fieldPath);
                    pathToComponentMap.put(fieldPath, fw);
                }
                try {
                    field.setAccessible(true);
                    Object value = field.get(parent);
                    if (value != null) {
                        String name = getSynthComponentName(value.getClass().getAnnotations());
                        if (name != null) {
                            listAll(value, path + "." + componentName + "[" + name + "]", pathList);
                        }
                    }
                } catch( ReflectiveOperationException roXC) {}
            }
        }
        for (Method m : parent.getClass().getDeclaredMethods()) {
            String componentName = getSynthComponentName(m.getDeclaredAnnotations());
            if (componentName != null) {
                pathList.add(path + "." + componentName);
            }
        }
    }

    private String getSynthComponentName(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (isSynthAnnotation(annotation)) {
                return readName(annotation);
            }
        }
        return null;
    }

    public Object resolve(String path) {
        return this.pathToComponentMap.get(path);
    }

    private Object resolve(Object parent, String name) {
        Object result = resolveByField(parent, name);
        if (result == null) {
            result = resolveByMethod(parent, name);
        }
        return result;
    }

    private static Field resolveByField(Object parent, String name) {
        for (Field field : parent.getClass().getDeclaredFields()) {
            for (Annotation a : field.getDeclaredAnnotations()) {
                if (isSynthAnnotation(a)) {
                    String componentName = readName(a);
                    if (name.equals(componentName)) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    private static Method resolveByMethod(Object parent, String name) {
        for (Method method : parent.getClass().getDeclaredMethods()) {
            for (Annotation a : method.getDeclaredAnnotations()) {
                if (isSynthAnnotation(a)) {
                    String componentName = readName(a);
                    if (name.equals(componentName)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSynthAnnotation(Annotation a) {
        return a.annotationType().getPackage().getName().equals(SynthComponent.class.getPackage().getName());
    }

    private static String readName(Annotation a) {
        try {
            Method m = a.getClass().getMethod("name");
            return "" + m.invoke(a);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public String getPath(Object... objs) {
        String path = null;
        for( Object o : objs ) {
            Annotation[]annotations = readAnnotation(o);
            String name = getSynthComponentName(annotations);
            if( path != null ) {
                path += ".";
            }
            path += name;
        }
        return path;
    }

    private Annotation[] readAnnotation(Object o) {
        if( o instanceof Method m ) {
            return m.getAnnotations();
        } else if( o instanceof Field f ) {
            return f.getAnnotations();
        }
        return o.getClass().getAnnotations();
    }

    public String getComponentPath(Object component) {
        return this.componentToPath.get(component);
    }
}
