package de.spraener.jsynth.modular;

import de.spraener.jsynth.modular.reflection.FieldWrapper;

import java.util.*;

/**
 * Calculates the sample of a Modular Synth by following the signal path.
 * It starts with the root values of a synth (for example the oscillators) and
 * looks for component having this values as input.
 * It then triggers these components and records their output.
 * This is done in a loop until there is no more component to trigger.
 *
 * It than returns the last created value. (That should be only one! The sample at time t)
 */
public class SignalFlow {
    private MSVoice ms;
    private List<SignalProcessor> processingList = new ArrayList<>();

    public SignalFlow(MSVoice ms) {
        this.ms = ms;
    }

    public SignalFlow reset() {
        processingList.clear();
        return this;
    }

    public Map<String, Float> process(float time) {
        if (this.processingList.isEmpty()) {
            return fillProcessingList(time);
        } else {
            Map<String, Float> values = ms.getRootValues(time);
            for( SignalProcessor processor : this.processingList ) {
                processor.process(time, values);
                applyValues(values);
            }
            return values;
        }
    }

    private Map<String, Float> fillProcessingList(float time) {
        Map<String, Float> values = ms.getRootValues(time);
        Set<Object> toProcess = new HashSet<>();
        toProcess.addAll(ms.synthComponents.values().stream().filter(o -> o instanceof SignalProcessor).toList());

        boolean hasProcessed;
        do {
            hasProcessed = false;
            Set<Object> toRemove = new HashSet<>();
            for( Object component : toProcess ) {
                if( component instanceof SignalProcessor sp && sp.canProcess(ms, values)) {
                    sp.process(time, values);
                    processingList.add(sp);
                    hasProcessed = true;
                    toRemove.add(sp);
                }
            }
            toProcess.removeAll(toRemove);
            if( hasProcessed ) {
                applyValues(values);
            }
        } while(hasProcessed && !toProcess.isEmpty());
        return values;
    }

    private void applyValues(Map<String, Float> values) {
        for( String path : values.keySet() ) {
            Object component = ms.pathResolver.resolve(path);
            if( component instanceof FieldWrapper fw ) {
                fw.set(values.get(path));
            }
        }
    }
}
