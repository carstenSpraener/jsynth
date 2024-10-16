package de.spraener.jsynth.filter;

import de.spraener.jsynth.SoundFormat;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FFTLowPass implements Filter {
    private SoundFormat soundFormat;
    private double cutoff = 800f;

    public FFTLowPass(SoundFormat soundFormat) {
        this.soundFormat = soundFormat;
    }

    public double getCutoff() {
        return cutoff;
    }

    public FFTLowPass setCutoff(double cutoff) {
        this.cutoff = cutoff;
        return this;
    }

    @Override
    public float[] filterBuffer(float[] input) {
        return fourierLowPassFilter(input, this.cutoff, this.soundFormat.sampleRate);
    }

    public float[] fourierLowPassFilter(float[] data, double lowPass, double frequency){
        //data: input data, must be spaced equally in time.
        //lowPass: The cutoff frequency at which
        //frequency: The frequency of the input data.

        //The apache Fft (Fast Fourier Transform) accepts arrays that are powers of 2.
        int minPowerOf2 = 1;
        while(minPowerOf2 < data.length)
            minPowerOf2 = 2 * minPowerOf2;

        //pad with zeros
        double[] padded = new double[minPowerOf2];
        for(int i = 0; i < data.length; i++)
            padded[i] = data[i];


        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fourierTransform = transformer.transform(padded, TransformType.FORWARD);

        //build the frequency domain array
        double[] frequencyDomain = new double[fourierTransform.length];
        for(int i = 0; i < frequencyDomain.length; i++)
            frequencyDomain[i] = frequency * i / (double)fourierTransform.length;

        //build the classifier array, 2s are kept and 0s do not pass the filter
        double[] keepPoints = new double[frequencyDomain.length];
        keepPoints[0] = 1;
        for(int i = 1; i < frequencyDomain.length; i++){
            if(frequencyDomain[i] < lowPass)
                keepPoints[i] = 2;
            else
                keepPoints[i] = 0;
        }

        //filter the fft
        for(int i = 0; i < fourierTransform.length; i++)
            fourierTransform[i] = fourierTransform[i].multiply((double)keepPoints[i]);

        //invert back to time domain
        Complex[] reverseFourier = transformer.transform(fourierTransform, TransformType.INVERSE);

        //get the real part of the reverse
        float[] result = new float[data.length];
        for(int i = 0; i< result.length; i++){
            result[i] = (float)reverseFourier[i].getReal();
        }

        return result;
    }
}
