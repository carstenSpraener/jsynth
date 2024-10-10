package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllpassFilterTests {
    private SoundFormat soundFormat = new SoundFormat();
    AllpassFilter uut = new AllpassFilter(soundFormat);

   @Test
   public void testAlphaCalculation() throws Exception {
       uut.setCutoff(20000);
       float result = uut.filter(0.9f);
       assertTrue(result>=-1.0f && result <= 1.0f,"Value out of range");
   }
}
