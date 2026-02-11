package com.bg7yoz.ft8cn.rigs;

import android.annotation.SuppressLint;

public class QrpLabsRigConstant {
    private static final String TAG = "QrpLabsRigConstant";
    //LSB:1,USB:2,CW:3,DIGI:6CW_R:7,DIGI_LSB:9
    public static final int LSB = 0x01;
    public static final int USB = 0x02;
    public static final int CW = 0x03;
    public static final int DATA = 0x06;

    public static final int swr_alert_max = 300;//1:3.00

    //PTT状态

    //指令集
    private static final String DIGI_MODE = "MD6;";

    private static final String VFO_A = "FR0;";//KENWOOD TS590,设置VFO -A
    private static final String PTT_ON = "TX;";//KENWOOD TS590,PTT
    private static final String READ_FREQ = "FA;";//KENWOOD 读频率
    private static final String READ_METERS = "SW;";//SWR meter
    private static final String PTT_OFF = "RX;";


    public static String getModeStr(int mode) {
        switch (mode) {
            case LSB:
                return "LSB";
            case USB:
                return "USB";
            case CW:
                return "CW";
            case DATA:
                return "DATA";
            default:
                return "UNKNOWN";
        }
    }

    public static byte[] setPTTState(boolean on) {
        if (on) {
            return PTT_ON.getBytes();
        } else {
            return PTT_OFF.getBytes();
        }

    }
    public static byte[] setVFOMode() {
        return VFO_A.getBytes();
    }

    public static byte[] setDigiMode() {
        return DIGI_MODE.getBytes();
    }

    @SuppressLint("DefaultLocale")
    public static byte[] setOperationFreq(long freq) {
        return String.format("FA%011d;", freq).getBytes();
    }

    public static byte[] setReadMeters() {
        return READ_METERS.getBytes();
    }

    public static byte[] setReadOperationFreq() {
        return READ_FREQ.getBytes();
    }
}
