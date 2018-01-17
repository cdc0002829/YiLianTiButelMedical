package cn.redcdn.hvs.profiles.voicedetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.redcdn.log.CustomLog;

/**
 *
 * 分析系统时延
 *
 * 调用伪代码：
 * int ret = parseAudioDelay.parse("xxxx.pcm");
 * if(ret > 0)
 * {//ret为系统时延
 * }
 * else if(ret == parseAudioDelay.PR_NOISERR)
 * {//噪音过大，选择安静环境重测
 * }
 * else if(ret == parseAudioDelay.PR_LOWVOL)
 * {//音量过低，调高音量重测
 * }
 * else if(ret == parseAudioDelay.PR_FILEERR)
 * {//文件错误，重录
 * }else if(ret == parseAudioDelay.PR_AEC )
 * {//设备自带回声
 * }
 * @author huangzl
 *
 */
public class parseAudioDelay {

    public final static int PR_FILEERR = -1;

    public final static int PR_NOISERR = -2;

    private final static int PR_NOISEOK = 0;

    private final static int PR_NOISESUBOK = 1;

    private final static int PR_NOISETHDOK = 2;

    public final static int PR_LOWVOL = -3;

    public final static int ERR_DELAY = 0;

    public final static int PR_AEC = -4;

    private final static float lowlevel2812gate = 0.15f; //噪音判断中,对2812做判断,如果小于这个值,认为能量过低

    private final static float aecgate = 0.2f; //噪音判断后,如果能量过低,判断是否自带回声消除.如果人声小于这个值,认为自带回声消除

    private final static float noisegate = 0.043f;

    private static class TAmp
    {
        public int t; // 时刻

        public double amplitude; // 振幅

        public TAmp(int t, double amplitude)
        {
            this.t = t;
            this.amplitude = amplitude;
        }
    }

    private static class AAmp
    {
        public int t; // 时刻

        Peak[] peak = null;

        public AAmp(int t, Peak[] peak)
        {
            this.t = t;
            this.peak = peak;
        }
    }

    private static class AAmp2
    {
        public int t; // 时刻

        Peak peak = null;

        public AAmp2(int t, Peak peak)
        {
            this.t = t;
            this.peak = peak;
        }
    }

    private static void myPrintln(String s)
    {
        System.out.println(s);
    }

    private static void myPrintlnx(String s)
    {
        System.out.println(s);
    }

    /**
     * 文件解析
     * @param filepath 语音文件绝对路径
     * @return
     */
    public static int parse(String filepath)
    {
        int ret = PR_NOISERR;
        try
        {
            float xfreq[] = new float[] { (float) 1562.5, (float) 2812.5, (float) 3437.5 };
            int noise[] = new int[]{PR_NOISERR, PR_NOISERR, PR_NOISERR};
            int delay[] = new int[]{ERR_DELAY, ERR_DELAY, ERR_DELAY};
            for(int i = 0; i < xfreq.length; i++)
            {
                noise[i] = noiseAna(filepath, xfreq[i]);
                if(noise[i] == PR_NOISEOK ||
                        noise[i] == PR_NOISESUBOK ||
                        noise[i] == PR_NOISETHDOK)
                {
                    delay[i] = getDelay(filepath, xfreq[i]);
                }
                myPrintln("freq:" + xfreq[i] + " noise:" + noise[i] + " delay" + delay[i]);
            }

            int oksum = 0;
            int subsumok = 0;
            int thdsumok = 0;
            for(int i = 0; i < xfreq.length; i++)
            {
                if(delay[i] != ERR_DELAY)
                {
                    if(noise[i] == PR_NOISEOK)
                        oksum++;
                    else if(noise[i] == PR_NOISESUBOK)
                        subsumok++;
                    else //PR_NOISETHDOK
                        thdsumok++;
                }
            }
            if (oksum == 0 &&
                    subsumok == 0 &&
                    thdsumok != 3 && (subsumok + thdsumok) != 3)
            {
                if(noise[0] == PR_NOISERR || noise[0] == PR_FILEERR) //如果是噪音污染，则三个频段应该一样
                    ret = noise[0];
                else //PR_LOWVOL
                {//能量过低，判断是否自带回声; 判断600ms后的声音
                    ret = aecAna(filepath, 600);
                }
            }
            else
            {
                if (oksum == 1 || oksum == 2 || oksum == 3)
                {// 采纳优先级：2812.5 -> // 3437.5 -> // 1562.5
                    if (delay[1] == 0)
                    {
                        if (delay[2] == 0)
                        {
                            ret = delay[0];
                        }
                        else
                        {
                            ret = delay[2];
                        }
                    }
                    else
                    {
                        ret = delay[1];
                    }
                }
                else if (/*subsumok == 1 || */subsumok == 2 || subsumok == 3)
                {// 采纳优先级：2812.5 -> 3437.5 -> 1562.5
                    ret = minI(delay);
                }
                else if(thdsumok == 3 || (subsumok + thdsumok) == 3)
                {
                    StdI si = StdI.getStd(delay);
                    if(si.sd < 5)//不同频段标差<5ms
                        ret = si.av;
                    else
                        ret = PR_NOISERR;
                }
                else
                {//位置问题都当成噪声
                    ret = PR_NOISERR;
                }
                StdI si = StdI.getStd(delay);
                myPrintln("delay:" + si.av + " std:" + si.sd);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        CustomLog.d("ParseAudioDelay", "ParseAudioDelay::parse() 检测结果 ret: " + ret);

        return ret;
    }

    private static int getDelay(String filepath, float xfreq) throws IOException
    {
        parseAudioDelay.TAmp[] taque = new parseAudioDelay.TAmp[30];
        double te = 0.0;
        double ae = 0.0;
        int tn = 0;
        for(int i = 0; i < 30; i++)
        {
            taque[i] = audioAna(filepath, i, xfreq);
            if(taque[i] != null)
            {
                myPrintln("[" + i + "] t=" + taque[i].t + " e=" + taque[i].amplitude);
                te += taque[i].amplitude;
                tn++;
            }
        }
        //丢弃低能量值
        int rm = 1;
        while (rm != 0 && tn != 0) {
            ae = te / tn; //能量平均值
            double egain = ae / 10; //丢弃门限
            te = 0.0;
            tn = 0;
            rm = 0;
            myPrintln("average e=" + ae + " gain=" + egain);
            for (int i = 0; i < 30; i++)
            {
                if (taque[i] != null)
                {
                    if(taque[i].amplitude < egain)
                    {
                        myPrintln("remove : [" + i + "] t=" + taque[i].t + " e=" + taque[i].amplitude);
                        taque[i] = null;
                        rm++;
                    }
                    else
                    {
                        te += taque[i].amplitude;
                        tn++;
                    }
                }
            }
        }

        //寻找递增列
        int maxt = 0;
        int mint = 0;
        for (int i = 0; i < 30; i++)
        {
            if(taque[i] != null)
            {
                myPrintln("[" + i + "] t=" + taque[i].t + " e=" + taque[i].amplitude);
                if(maxt == 0 || taque[i].t > maxt)
                    maxt = taque[i].t;
                if(mint == 0 || taque[i].t < mint)
                    mint = taque[i].t;
            }
        }
        myPrintln("maxt=" + maxt + " mint=" + mint);
        int maxn = 0;
        int maxnt = 0;
        for(int curt = mint; curt <= maxt; curt+=16)
        {
            int tnum = 0;
            for (int i = 0; i < 30; i++)
            {
                if(taque[i] != null)
                {
                    if(taque[i].t == curt)
                        tnum++;
                }
            }
            myPrintln("curt=" + curt + " num=" + tnum);
            if(maxn == 0 || tnum > maxn)
            {
                maxn = tnum;
                maxnt = curt;
            }
        }
        //计算队列能量均值
        te = 0;
        double maxe = 0.0;
        for (int i = 0; i < 30; i++)
        {
            if(taque[i] != null && taque[i].t == maxnt)
            {
                te += taque[i].amplitude;
                maxe = taque[i].amplitude;
            }
        }
        ae = te/maxn;
        double factor = 0.618;
        double axe = maxe/16*factor;
        double pree = 0.0;
        int ib = 0;
        for (int i = 0; i < 30; i++)
        {
            if(taque[i] != null && taque[i].t == maxnt)
            {
                if(pree == 0.0)
                    pree = taque[i].amplitude;
                if(taque[i].amplitude-pree < 0)
                    continue;
                myPrintln("[" + i + "] e=" + taque[i].amplitude + " xe=" + (taque[i].amplitude-pree));
                pree = taque[i].amplitude;
                if(ib == 0)
                    ib = i;
            }
        }
        int realt = 0;
        int prei = 0;
        pree = 0.0;
        for (int i = ib; i < 30; i++)
        {
            if(taque[i] != null && taque[i].t == maxnt)
            {
                if(prei != 0 && (taque[i].amplitude-pree)/(i-prei) < axe)
                    break;
                pree = taque[i].amplitude;
                realt = taque[i].t + i;
                prei = i;
            }
        }
        myPrintlnx("maxn=" + maxn + " maxnt=" + maxnt + " ae=" + ae + " axe=" + axe + " realt=" + realt);
        return realt;
    }

    private static TAmp audioAna(String filepath, int offset, float xfreq) throws IOException
    {
        int step = 16;
        int blockSize = step*8;
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        byte[] dataFromMic = new byte[blockSize * 2];
        int[] masterOut = new int[blockSize];
        if(offset > 0)
        {
            byte[] offsetPcm = new byte[offset*16];
            fis.read(offsetPcm);
        }
        int time = 0;
        while (fis.read(dataFromMic) != -1)
        {
            for (int i = 0, j = 0; j < blockSize; i += 2, j++)
            {
                masterOut[j] = 0;
                masterOut[j] += (dataFromMic[i] & 0xFF) | (dataFromMic[i + 1] << 8);
            }
            Peak[] peaks = AudioAnaliyser.instance().parseSamples(masterOut);
            if(peaks.length > 0)
            {
//				for(int i = 0; i < peaks.length ; i++)
//				{
//					if(peaks[i] != null)
//						myPrintln("" + time*step + " " + peaks[i].frequency + " " + peaks[i].amplitude);
//				}
//				Peak[] peak = AudioAnaliyser.instance().maxPeak2(peaks);
//				if(peak[0].frequency == 2812.5 && peak[0].amplitude > baseE)
//				{
//					return new parseAudioDelay.TAmp(time*step, peak[0].amplitude);
//				}
                Peak peak =findFreq(peaks, xfreq);
                if(peak != null && peak.amplitude > baseE)
                {
                    return new parseAudioDelay.TAmp(time*step, peak.amplitude);
                }
            }
            time++;
        }
        return null;
    }

    private static int noiseAna(String filepath, float xfreq) throws IOException
    {
        parseAudioDelay.AAmp[] aaque = new parseAudioDelay.AAmp[40]; //分析640ms的数据

        int step = 16;
        int blockSize = step*8;
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        byte[] dataFromMic = new byte[blockSize * 2];
        int[] masterOut = new int[blockSize];
        int time = 0;
        //获取各帧能量前三的频段
        while (fis.read(dataFromMic) != -1 && time < aaque.length)
        {
            for (int i = 0, j = 0; j < blockSize; i += 2, j++)
            {
                masterOut[j] = 0;
                masterOut[j] += (dataFromMic[i] & 0xFF)
                        | (dataFromMic[i + 1] << 8);
            }
            Peak[] peaks = AudioAnaliyser.instance().parseSamples(masterOut);
            if(peaks.length > 0)
            {
//				Peak[] peak = AudioAnaliyser.instance().maxPeak2(peaks);
//				aaque[time] = new parseAudioDelay.AAmp(time*step, peak);
                aaque[time] = new parseAudioDelay.AAmp(time*step, peaks);
            }
            time++;
        }

        int f2812s = 0;//统计2812.5出现次数
        int a2812i = 0;
        double min2812e = 0.0;
        double max2812e = 0.0;
        parseAudioDelay.AAmp2[] a2812aque = new parseAudioDelay.AAmp2[10]; //2812.5能量前十
        int ai = 0;
        double mine = 0.0;
        double maxe = 0.0;
        parseAudioDelay.AAmp2[] axaque = new parseAudioDelay.AAmp2[10]; //所有频段能量前十
        for(int k = 0; k < aaque.length; k++)
        {
            if(aaque[k] != null)
            {
                {
                    {
                        Peak peak = maxP(aaque[k].peak);
                        if(ai < 10)
                        {
                            axaque[ai] = new AAmp2(aaque[k].t, peak);
                            ai++;
                            if(mine == 0.0 || mine > peak.amplitude)
                                mine = peak.amplitude;
                            if(maxe == 0.0 || maxe < peak.amplitude)
                                maxe = peak.amplitude;
                        }
                        else
                        {
                            if(mine < peak.amplitude)
                            {
                                if(maxe < peak.amplitude)
                                    maxe = peak.amplitude;

                                double tmine = axaque[0].peak.amplitude;
                                if(tmine == mine)
                                    tmine = axaque[1].peak.amplitude;
                                for(int n = 0; n < axaque.length; n++)
                                {
                                    if(axaque[n].peak.amplitude == mine)
                                    {
                                        axaque[n] = new AAmp2(aaque[k].t, peak);
                                    }
                                    if(tmine > axaque[n].peak.amplitude)
                                        tmine = axaque[n].peak.amplitude;
                                }
                                mine = tmine;
                            }
                        }
                    }
                }
            }
        }
        for(int k = 0; k < aaque.length; k++)
        {
            if(aaque[k] != null)
            {
                Peak peak = findFreq(aaque[k].peak, xfreq);
                if(peak != null)
                {
                    {
                        myPrintln("" +  aaque[k].t + "ms : " + peak.frequency + "["+ peak.amplitude + "] ");
                        f2812s++;
                        {
                            if(a2812i < 10)
                            {
                                a2812aque[a2812i] = new AAmp2(aaque[k].t, peak);
                                a2812i++;
                                if(min2812e == 0.0 || min2812e > peak.amplitude)
                                    min2812e = peak.amplitude;
                                if(max2812e == 0.0 || max2812e < peak.amplitude)
                                    max2812e = peak.amplitude;
                            }
                            else
                            {
                                if(min2812e < peak.amplitude)
                                {
                                    if(max2812e < peak.amplitude)
                                        max2812e = peak.amplitude;

                                    double tmine = a2812aque[0].peak.amplitude;
                                    if(tmine == mine)
                                        tmine = a2812aque[1].peak.amplitude;
                                    for(int n = 0; n < a2812aque.length; n++)
                                    {
                                        if(a2812aque[n].peak.amplitude == min2812e)
                                            a2812aque[n] = new AAmp2(aaque[k].t, peak);
                                        if(tmine > a2812aque[n].peak.amplitude)
                                            tmine = a2812aque[n].peak.amplitude;
                                    }
                                    min2812e = tmine;
                                }
                            }
                        }
                    }
                }
            }
        }

        double aaxe = 0.0;
        double aa2812xe = 0.0;
        double min2812xe = 0.0;
        int n2812c = 0; //2812连续个数，不小于6。因为测试的语音100ms，理论上探测到的声音不少于100ms。
        int td = 0;  //约接近于16，能量分布越集中
        int t2812d = 0; //约接近于16，能量分布越集中，噪音可能性越小
        double ed = 0;
        double e2812d = 0;
        int pret = 0;
        double pree = 0.0;
        System.out.println("");
        sequeByT(axaque);
        for(int n = 0; n < axaque.length; n++)
        {
            if(axaque[n] == null)
                continue;
            myPrintln("" + axaque[n].t + "ms : " + axaque[n].peak.frequency + "["+ axaque[n].peak.amplitude + "] ");
            aaxe += axaque[n].peak.amplitude;

            if(pret == 0)
                pret = axaque[n].t;
            td += (axaque[n].t - pret);
            pret = axaque[n].t;

            if(pree == 0)
                pree = axaque[n].peak.amplitude;
            ed += Math.abs(axaque[n].peak.amplitude - pree);
            pree = axaque[n].peak.amplitude;
        }
        aaxe /= 10;
        td /= 9;
        ed /= 9;
        myPrintln("mine=" + mine + " maxe=" + maxe + " aaxe=" + aaxe + " td=" + td + " ed=" + ed);

        myPrintln("");
        sequeByT(a2812aque);
        int erresc = a2812aque.length;
        while(erresc-- != 0)
        {
            pret = 0;
            pree = 0;
            aa2812xe = 0.0;
            min2812xe = 0.0;
            t2812d = 0;
            e2812d = 0.0;
            n2812c = 0;
            for(int n = 0; n < a2812aque.length; n++)
            {
                if(a2812aque[n] == null)
                    continue;
                n2812c++;
                myPrintln("" + a2812aque[n].t + "ms : " + a2812aque[n].peak.frequency + "["+ a2812aque[n].peak.amplitude + "] ");
                aa2812xe += a2812aque[n].peak.amplitude;
                if(min2812xe == 0.0 || min2812xe > a2812aque[n].peak.amplitude)
                    min2812xe = a2812aque[n].peak.amplitude;

                if(pret == 0)
                    pret = a2812aque[n].t;
                t2812d += (a2812aque[n].t - pret);
                pret = a2812aque[n].t;

                if(pree == 0)
                    pree = a2812aque[n].peak.amplitude;
                e2812d += Math.abs(a2812aque[n].peak.amplitude - pree);
                pree = a2812aque[n].peak.amplitude;
            }
            if(n2812c < 4)
                break;
            aa2812xe /= n2812c;
            t2812d /= (n2812c-1);
            e2812d /= (n2812c-1);
            myPrintln("mine=" + min2812e + " maxe=" + max2812e + " aaxe=" + aa2812xe + " td=" + t2812d + " ed=" + e2812d);
            if(t2812d == 16)
                break;
            //后去掉异常点
            for(int n = a2812aque.length-1; n > 0; n--)
            {
                if(a2812aque[n] != null)
                {
                    if(a2812aque[n-1] == null
                            || (a2812aque[n-1] != null && a2812aque[n].t - a2812aque[n-1].t > 16))
                    {
                        myPrintln("remove " + a2812aque[n].t + "ms : " + a2812aque[n].peak.frequency + "["+ a2812aque[n].peak.amplitude + "] ");
                        a2812aque[n] = null;
                        break;
                    }
                }
            }
        }

        //2812能量分布连续性判断
        if(t2812d == 16 && aa2812xe > noisegate)
        {
            if(n2812c >= 6)
            {
                baseE = min2812xe/2;//aa2812xe / 10; //x1异常 asyRecord-0.pcm
                myPrintlnx("it's ok! baseE:" + baseE);
                return PR_NOISEOK;
            }
            else if(n2812c == 5)
            {
                baseE = min2812xe/2;//aa2812xe / 10; //x1异常 asyRecord-0.pcm
                myPrintlnx("it's sub ok! baseE:" + baseE);
                return PR_NOISESUBOK;
            }
            else if(n2812c == 4)
            {
                baseE = min2812xe/2;//aa2812xe / 10; //x1异常 asyRecord-0.pcm
                myPrintlnx("it's thd ok! baseE:" + baseE);
                return PR_NOISETHDOK;
            }
        }

        if(aa2812xe < lowlevel2812gate/* || aaxe < 0.1*/) //判断回声能量
        {
            myPrintlnx("not ok!");
            return PR_LOWVOL;
        }

        //TODO:所有无法分析的情景都当作噪音污染；如果在无噪声场景下返回PR_NOISERR，则需要演进算法。
        myPrintlnx("not ok!");
        return PR_NOISERR;
    }

    private static double baseE = 0.0;

    /**
     *
     * @param filepath
     * @param offset 单位ms
     * @return
     * @throws IOException
     */
    private static int aecAna(String filepath, int offset) throws IOException
    {//aecgate
        int step = 16;
        int blockSize = step * 8;
        File file = new File(filepath);
        FileInputStream fis = new FileInputStream(file);
        int flag1 = 0;
        byte[] dataFromMic = new byte[blockSize * 2];
        int[] masterOut = new int[blockSize];

        if(file.length() <= offset * 16)
            return PR_FILEERR;
        fis.skip(offset * 16);
        while (fis.read(dataFromMic) != -1)
        {
            for (int i = 0, j = 0; j < blockSize; i += 2, j++)
            {
                masterOut[j] = 0;
                masterOut[j] += (dataFromMic[i] & 0xFF)
                        | (dataFromMic[i + 1] << 8);
            }
            Peak[] peaks = AudioAnaliyser.instance().parseSamples(masterOut);

            if (peaks.length > 0)
            {
                peaks = AudioAnaliyser.instance().maxPeak2(peaks);
                System.out.print("" + peaks[0].amplitude + " ");
                if (peaks[0].amplitude > aecgate)
                {//TODO:可能存在噪声; 目前判断只判断一帧,可能需要连续判断(分析:测试声音)
                    flag1 = 0;
                }
                else
                {
                    flag1++;
                }
            }
            else
            {
                flag1++;
            }
            if ( flag1 > 28)
            {
                return PR_AEC;
            }
        }
        return PR_LOWVOL;
    }

    //按时间排序
    private static void sequeByT(parseAudioDelay.AAmp2[] que)
    {
        for(int m = 0; m < que.length; m++)
        {
            if(que[m] == null)
                continue;
            for(int n = m; n < que.length; n++)
            {
                if(que[n] == null)
                    continue;
                if(que[m].t > que[n].t)
                {
                    parseAudioDelay.AAmp2 aa = que[n];
                    que[n] = que[m];
                    que[m] = aa;
                }
            }
        }
    }

    private static Peak maxP(Peak[] peaks)
    {
        Peak maxPeak = null;
        if (peaks.length > 0)
        {
            maxPeak = peaks[0];
            for (int i = 1; i < peaks.length; i++)
            {
                if (peaks[i].amplitude > maxPeak.amplitude)
                {
                    maxPeak = peaks[i];
                }
            }
        }
        return maxPeak;
    }
    private static Peak findFreq(Peak[] peaks, float xfreq)
    {
        Peak xPeak = null;
        if (peaks.length > 0)
        {
            for (int i = 0; i < peaks.length; i++)
            {
                if (peaks[i].frequency == xfreq)
                {
                    xPeak = peaks[i];
                }
            }
        }
        return xPeak;
    }

    private static int minI(int[] sample)
    {
        int min = sample[0];
        for(int i = 1; i < sample.length; i++)
        {
            if(sample[i] == 0)
                continue;
            if(min == 0)
                min = sample[i];
            else
            {
                if(min > sample[i])
                    min = sample[i];
            }
        }
        return min;
    }

    private static class StdI
    {
        public int av;
        public float sd;
        public StdI()
        {
            av = 0;
            sd = 0;
        }
        public static StdI getStd(int[] sample)
        {
            StdI ret = new StdI();
            int num = 0;
            int sum = 0;
            int sq = 0;
            for(int i = 0; i < sample.length; i++)
            {
                if(sample[i] != 0)
                {
                    num++;
                    sum += sample[i];
                }
            }
            if(num == 0)
                return ret;
            ret.av = sum/num;
            for(int i = 0; i < sample.length; i++)
            {
                if(sample[i] != 0)
                {
                    sq += (sample[i]-ret.av)*(sample[i]-ret.av);
                }
            }
            ret.sd = (float) Math.sqrt(sq/num);
            return ret;
        }
    }
}

