package com.bg7yoz.ft8cn.rigs;

import static com.bg7yoz.ft8cn.GeneralVariables.QUERY_FREQ_TIMEOUT;
import static com.bg7yoz.ft8cn.GeneralVariables.START_QUERY_FREQ_DELAY;

import android.os.Handler;
import android.util.Log;

import com.bg7yoz.ft8cn.GeneralVariables;
import com.bg7yoz.ft8cn.R;
import com.bg7yoz.ft8cn.database.ControlMode;
import com.bg7yoz.ft8cn.ui.ToastMessage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * qrp-labs.com QDX, QMX, QMX+
 * 
 * CAT command set is similar to TS-480, but they implement native
 * digital mode which is quite different from USB.
 */
public class QrpLabsRig extends BaseRig {
    private static final String TAG = "QrpLabsRig";
    private final StringBuilder buffer = new StringBuilder();

    private Timer readFreqTimer = new Timer();
    private int swr = 0;
    private boolean swrAlert = false;

    private TimerTask readTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    if (!isConnected()) {
                        readFreqTimer.cancel();
                        readFreqTimer.purge();
                        readFreqTimer = null;
                        return;
                    }
                    if (isPttOn()) {
                        readMeters();//读METER
                    } else {
                        readFreqFromRig();//读频率
                    }

                } catch (Exception e) {
                    Log.e(TAG, "readFreq error:" + e.getMessage());
                }
            }
        };
    }

    /**
     * 读取Meter RM;
     */
    private void readMeters() {
        if (getConnector() != null) {
            clearBufferData();//清空一下缓存
            getConnector().sendData(QrpLabsRigConstant.setReadMeters());
        }
    }

    /**
     * 清空缓存数据
     */
    private void clearBufferData() {
        buffer.setLength(0);
    }

    @Override
    public void setPTT(boolean on) {
        super.setPTT(on);
        if (getConnector() != null) {
            switch (getControlMode()) {
                case ControlMode.CAT://以CIV指令
                    getConnector().setPttOn(QrpLabsRigConstant.setPTTState(on));
                    break;
                case ControlMode.RTS:
                case ControlMode.DTR:
                    getConnector().setPttOn(on);
                    break;
            }
        }
    }

    @Override
    public boolean isConnected() {
        if (getConnector() == null) {
            return false;
        }
        return getConnector().isConnected();
    }

    @Override
    public void setUsbModeToRig() {
        if (getConnector() != null) {
            getConnector().sendData(QrpLabsRigConstant.setDigiMode());
        }
    }

    @Override
    public void setFreqToRig() {
        if (getConnector() != null) {
            getConnector().sendData(QrpLabsRigConstant.setOperationFreq(getFreq()));
        }
    }

    @Override
    public void onReceiveData(byte[] data) {
        String s = new String(data);

        if (!s.contains("\r")) {
            buffer.append(s);
            if (buffer.length() > 1000) clearBufferData();
            //return;//说明数据还没接收完。
        } else {
            if (s.indexOf("\r") > 0) {//说明接到结束的数据了，并且不是第一个字符是;
                buffer.append(s.substring(0, s.indexOf("\r")));
            }
            //开始分析数据
            Yaesu3Command yaesu3Command = Yaesu3Command.getCommand(buffer.toString());
            clearBufferData();//清一下缓存
            //要把剩下的数据放到缓存里
            buffer.append(s.substring(s.indexOf("\r") + 1));

            if (yaesu3Command == null) {
                return;
            }
            String cmd = yaesu3Command.getCommandID();
            if (cmd.equalsIgnoreCase("FA")) {//频率
                long tempFreq = Yaesu3Command.getFrequency(yaesu3Command);
                if (tempFreq != 0) {//如果tempFreq==0，说明频率不正常
                    setFreq(Yaesu3Command.getFrequency(yaesu3Command));
                }
            } else if (cmd.equalsIgnoreCase("SW")) {//SWR
                swr = Yaesu3Command.getQrpLabsSWR(yaesu3Command);
                showAlert();
            }

        }

    }

    private void showAlert() {
        if ((swr >= QrpLabsRigConstant.swr_alert_max)
                && GeneralVariables.swr_switch_on) {
            if (!swrAlert) {
                swrAlert = true;
                ToastMessage.show(GeneralVariables.getStringFromResource(R.string.swr_high_alert));
            }
        } else {
            swrAlert = false;
        }

    }

    @Override
    public void readFreqFromRig() {
        if (getConnector() != null) {
            clearBufferData();//清空一下缓存
            getConnector().sendData(QrpLabsRigConstant.setReadOperationFreq());
        }
    }

    @Override
    public String getName() {
        return "qrp-labs.com QDX/QMX/QMX+";
    }

    public QrpLabsRig() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getConnector() != null) {
                    getConnector().sendData(QrpLabsRigConstant.setVFOMode());
                }
            }
        }, START_QUERY_FREQ_DELAY - 500);
        readFreqTimer.schedule(readTask(), START_QUERY_FREQ_DELAY, QUERY_FREQ_TIMEOUT);
    }
}
