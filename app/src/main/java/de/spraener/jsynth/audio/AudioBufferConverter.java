package de.spraener.jsynth.audio;

public class AudioBufferConverter {

    public static byte[] convert(float[] buffer) {
        final byte[] byteBuffer = new byte[buffer.length * 2];

        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = (int) (buffer[bufferIndex++] * 32767.0);

            byteBuffer[i++] = (byte) x;
            byteBuffer[i] = (byte) (x >>> 8);
        }
        return byteBuffer;
    }
}
