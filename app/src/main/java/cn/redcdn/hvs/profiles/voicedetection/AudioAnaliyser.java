package cn.redcdn.hvs.profiles.voicedetection;

import java.util.Vector;

/**
 * Created by Administrator on 2017/2/27.
 */

class Peak   //Peak是数组需要任意频段的能量
{
    public double frequency; // 频率

    public double amplitude; // 振幅==能量

    public Peak(double frequency, double amplitude)
    {
        this.frequency = frequency;
        this.amplitude = amplitude;
    }
}

public class AudioAnaliyser
{
    static private AudioAnaliyser inst = null;

    // 数据分析单元
    private double[] unitFrame = null;

    // 假定采用率均为8000
    private double rate = 4000.0; // 8000/2

    private int blockSize = 128;

    private AudioAnaliyser()
    {
        if (unitFrame == null)
            unitFrame = new double[blockSize];
    }

    static public AudioAnaliyser instance()
    {
        if (inst == null)
            inst = new AudioAnaliyser();
        return inst;
    }

    static boolean isBaseFreq(double freq, double lfreq, double rfreq)
    {
        return ((freq > lfreq) && (freq < rfreq));
    }

    static boolean isHarmonicFreq(double freq, double[] freqs)
    {
        for (int i = 0; i < freqs.length; i += 2)
        {
            if ((freq > freqs[i]) && (freq < freqs[i + 1]))
                return true;
        }
        return false;
    }

    public Peak maxPeak(Peak[] peaks)
    {
        Peak maxPeak = null;
        if (peaks.length > 0)
        {
            maxPeak = peaks[0];
            for (int i = 0; i < peaks.length; i++)
            {
                if (peaks[i].amplitude > maxPeak.amplitude)
                {
                    maxPeak = peaks[i];
                }
            }
        }
        return maxPeak;
    }

    public Peak[] maxPeak2(Peak[] peaks)
    {
        Peak[] maxPeak = new Peak[3];
        if (peaks.length > 0)
        {
            maxPeak[0] = peaks[0];
            maxPeak[1] = null;
            maxPeak[2] = null;
            for (int i = 1; i < peaks.length; i++)
            {
                if (peaks[i].amplitude > maxPeak[0].amplitude)
                {
                    maxPeak[2] = maxPeak[1];
                    maxPeak[1] = maxPeak[0];
                    maxPeak[0] = peaks[i];
                }
                else if((maxPeak[1] == null) || (peaks[i].amplitude > maxPeak[1].amplitude))
                {
                    maxPeak[2] = maxPeak[1];
                    maxPeak[1] = peaks[i];
                }
                else if((maxPeak[2] == null) || (peaks[i].amplitude > maxPeak[2].amplitude))
                {
                    maxPeak[2] = peaks[i];
                }
            }
        }
        return maxPeak;
    }

    /**
     * @param samples
     *            长度等于unitFrame
     */
    public Peak[] parseSamples(int[] samples)
    {
        int i = 0;
        for (i = 0; i < samples.length; i++)
            unitFrame[i] = ((double) samples[i]) / 32768;

        int N = unitFrame.length;
        for (i = 0; i < N; i++)
        {
            unitFrame[i] = (0.42 - 0.5 * Math.cos(2 * Math.PI * i / (N - 1)) + 0.08 * Math
                    .cos(4 * Math.PI * i / (N - 1)))
                    * unitFrame[i];
        }

        magnitudeSpectrum(unitFrame);

        // only take the first half of the spectrum
        int halfLen = unitFrame.length / 2;
        double[] magnitudeHalf = new double[halfLen];
        for (i = 0; i < halfLen; i++)
        {
            magnitudeHalf[i] = unitFrame[i];
        }

        Peak[] peaks = detectSpectralPeaks(magnitudeHalf, rate);
        return peaks;
    }

    private static double[] magnitudeSpectrum(double[] realPart)
    {
        double[] imaginaryPart = new double[realPart.length];

        for (int i = 0; i < imaginaryPart.length; i++)
        {
            imaginaryPart[i] = 0;
        }
        forwardFFT(realPart, imaginaryPart, true);

        for (int i = 0; i < realPart.length; i++)
        {
            realPart[i] = Math.sqrt(realPart[i] * realPart[i]
                    + imaginaryPart[i] * imaginaryPart[i]);
        }

        return realPart;
    }

    private static final Peak[] detectSpectralPeaks(double[] point, double rate)
    {
        int nMaxPeaks = 10;

        Vector<Peak> peaks = new Vector<Peak>();
        int N = point.length;

        double[] pointDB = new double[N];

        for (int i = 0; i < point.length; i++)
        {
            pointDB[i] = lin2dB(point[i]);
        }

        for (int i = 1; i < N - 1; i++)
        {
            double freq = (i * rate) / point.length;
            if(i == 45)
            {
                int j = 0;
                j++;
            }
            if (pointDB[i] > pointDB[i - 1] && pointDB[i + 1] < pointDB[i]
                    && pointDB[i] > threshold(freq, rate)
                    && peaks.size() < nMaxPeaks)
            {
                peaks.add(new Peak(freq, point[i]));
            }
        }
        if (pointDB[N - 1] > pointDB[N - 2]
                && pointDB[N - 1] > threshold(4000.0, rate)) // 在测试1k时，4k是谐波
            peaks.add(new Peak(4000.0, point[N - 1]));

        peaks.trimToSize();
        Peak[] ret = new Peak[peaks.size()];
        return peaks.toArray(ret);
    }

    private static final double threshold(double frequency, double rate)
    {
        double peakThreshold = 0.08;
        double peakThresholdStiffness = 4.0;
        return peakThreshold
                * Math.exp(-peakThresholdStiffness * frequency / rate)
        /*+ peakThreshold * 0.1*/;
    }

    private static void forwardFFT(double in_r[], double in_i[], boolean forward)
    {
        int id;

        int localN;
        double wtemp, Wjk_r, Wjk_i, Wj_r, Wj_i;
        double theta, tempr, tempi;
        // int ti, tj;

        int numBits = (int) log2(in_r.length);
        if (forward)
        {
            // centering(in_r);
        }

        // Truncate input data to a power of two
        int length = 1 << numBits; // length = 2**nu
        int n = length;
        int nby2;

        // Copy passed references to variables to be used within
        // fft routines & utilities
        double[] r_data = in_r;
        double[] i_data = in_i;

        bitReverse2(r_data, i_data);
        for (int m = 1; m <= numBits; m++)
        {
            // localN = 2^m;
            localN = 1 << m;

            nby2 = localN / 2;
            Wjk_r = 1;
            Wjk_i = 0;

            theta = Math.PI / nby2;

            // for recursive comptutation of sine and cosine
            Wj_r = Math.cos(theta);
            Wj_i = -Math.sin(theta);
            if (forward == false)
            {
                Wj_i = -Wj_i;
            }

            for (int j = 0; j < nby2; j++)
            {
                // This is the FFT innermost loop
                // Any optimizations that can be made here will yield
                // great rewards.
                for (int k = j; k < n; k += localN)
                {
                    id = k + nby2;
                    tempr = Wjk_r * r_data[id] - Wjk_i * i_data[id];
                    tempi = Wjk_r * i_data[id] + Wjk_i * r_data[id];

                    // Zid = Zi -C
                    r_data[id] = r_data[k] - tempr;
                    i_data[id] = i_data[k] - tempi;
                    r_data[k] += tempr;
                    i_data[k] += tempi;
                }

                // (eq 6.23) and (eq 6.24)

                wtemp = Wjk_r;

                Wjk_r = Wj_r * Wjk_r - Wj_i * Wjk_i;
                Wjk_i = Wj_r * Wjk_i + Wj_i * wtemp;
            }
        }
        // normalize output of fft.
        // if (forward)
//    if (false)
//      for (int i = 0; i < r_data.length; i++)
//      {
//        r_data[i] = r_data[i] / (double) n;
//        i_data[i] = i_data[i] / (double) n;
//      }
        in_r = r_data;
        in_r = i_data;
    }

    private static void bitReverse2(double[] r_data, double[] i_data)
    {
    /* bit reversal */
        int n = r_data.length;
        int j = 1;

        int k;

        for (int i = 1; i < n; i++)
        {

            if (i < j)
                swapInt(r_data, i_data, i, j);
            k = n / 2;
            while (k >= 1 && k < j)
            {

                j = j - k;
                k = k / 2;
            }
            j = j + k;
        }
    }

    // swap Zi with Zj
    private static void swapInt(double[] r_data, double[] i_data, int i, int j)
    {
        double tempr;
        int ti;
        int tj;
        ti = i - 1;
        tj = j - 1;
        tempr = r_data[tj];
        r_data[tj] = r_data[ti];
        r_data[ti] = tempr;
        tempr = i_data[tj];
        i_data[tj] = i_data[ti];
        i_data[ti] = tempr;
    }

    private static double log2(double x)
    {
        return Math.log10(x) / Math.log10(2.0);
    }

    private static final double lin2dB(double lin)
    {
        return Math.log10(lin + 1);
    }
}


