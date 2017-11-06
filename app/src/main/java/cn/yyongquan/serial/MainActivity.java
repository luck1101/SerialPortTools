package cn.yyongquan.serial;

import android.app.Activity;
import android.os.Bundle;
import android.serialport.api.SerialPort;
import android.serialport.api.SerialPortFinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText send_text;
    public ReadThread mReadThread;
    private SerialPort mSerialPort;
    public InputStream mInputStream;
    public OutputStream mOutputStream;
    private TextView id_text, id_switch;

    private int[] baudRateIntArr;
    private String[] serialArr, baudRateArr, checkBitArr, dataBitArr, stopBitArr;

    private char mParity;
    private String mPath;
    private int mBaudRate, mDataBits, mStopBits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initView();
    }

    private void init() {
        SerialPortFinder spf = new SerialPortFinder();
        serialArr = spf.getAllDevicesPath();
        baudRateIntArr = new int[]{150, 200, 300, 600, 1200, 1800, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 460800, 500000, 576000, 921600, 1000000, 1152000, 1500000, 2000000, 2500000, 3000000, 3500000, 4000000};
        checkBitArr = new String[]{"无NONE", "奇ODD", "偶EVEN"};
        dataBitArr = new String[]{"8位", "7位", "6位"};
        stopBitArr = new String[]{"1位", "2位"};
        baudRateArr = new String[baudRateIntArr.length];
        for (int i = 0; i < baudRateArr.length; i++)
            baudRateArr[i] = String.valueOf(baudRateIntArr[i]);

        if (serialArr != null && serialArr.length > 0)
            mPath = serialArr[0];
        mBaudRate = baudRateIntArr[0];
        mParity = SerialPort.PARITY_NONE;
        mDataBits = SerialPort.DATA_BITS_8;
        mStopBits = SerialPort.STOP_BITS_1;
    }

    private void initView() {
        id_text = (TextView) this.findViewById(R.id.id_text);
        id_switch = (TextView) this.findViewById(R.id.id_switch);
        send_text = (EditText) this.findViewById(R.id.send_text);
        Spinner sp_data = (Spinner) this.findViewById(R.id.sp_data);
        Spinner sp_check = (Spinner) this.findViewById(R.id.sp_check);
        Spinner sp_serial = (Spinner) this.findViewById(R.id.sp_serial);
        Spinner sp_stop_bit = (Spinner) this.findViewById(R.id.sp_stop_bit);
        Spinner sp_baud_rate = (Spinner) this.findViewById(R.id.sp_baud_rate);

        id_switch.setOnClickListener(this);
        this.findViewById(R.id.btn_send).setOnClickListener(this);
        this.findViewById(R.id.btn_empty).setOnClickListener(this);

        sp_serial.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, serialArr));
        sp_baud_rate.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, baudRateArr));
        sp_check.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, checkBitArr));
        sp_data.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataBitArr));
        sp_stop_bit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stopBitArr));

        sp_serial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mPath = serialArr[position];
                close();
                resetSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_baud_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBaudRate = baudRateIntArr[position];
                close();
                resetSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_data.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mDataBits = SerialPort.DATA_BITS_8;
                } else if (position == 1) {
                    mDataBits = SerialPort.DATA_BITS_7;
                } else if (position == 2) {
                    mDataBits = SerialPort.DATA_BITS_6;
                } else {
                    mDataBits = SerialPort.DATA_BITS_8;
                }
                close();
                resetSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_stop_bit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mStopBits = SerialPort.STOP_BITS_1;
                } else if (position == 1) {
                    mStopBits = SerialPort.STOP_BITS_2;
                } else {
                    mStopBits = SerialPort.DATA_BITS_8;
                }
                close();
                resetSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_check.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mParity = SerialPort.PARITY_NONE;
                } else if (position == 1) {
                    mParity = SerialPort.PARITY_ODD;
                } else if (position == 2) {
                    mParity = SerialPort.PARITY_EVEN;
                } else {
                    mParity = SerialPort.PARITY_NONE;
                }
                close();
                resetSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_switch:
                if (mSerialPort != null) {
                    close();
                } else {
                    open();
                }
                resetSwitch();
                break;
            case R.id.btn_empty:
                id_text.setText("");
                break;
            case R.id.btn_send:
                send();
                break;
        }
    }

    private void resetSwitch() {
        id_switch.setText(mSerialPort != null ? "关闭" : "打开");
    }

    private void send() {
        try {
            if (mSerialPort != null && mOutputStream != null) {
                String sendStr = send_text.getText().toString().trim();
                List<Byte> list = new ArrayList<>();
                while (sendStr.length() > 2) {
                    list.add((byte) (Integer.parseInt(sendStr.substring(0, 2), 16) & 0xFF));
                    sendStr = sendStr.substring(2);
                }
                list.add((byte) (Integer.parseInt(sendStr, 16) & 0xFF));

                byte[] sendBs = new byte[list.size()];
                for (int i = 0; i < sendBs.length; i++) sendBs[i] = list.get(i);

                mOutputStream.write(sendBs);
                mOutputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void open() {
        System.out.println(">>>>> " + mPath + " => " + mBaudRate + " => " + mDataBits + " => " + mStopBits + " => " + mParity);
        try {
            try {
                try {
                    if (mPath != null && mPath.length() > 0) {
                        mSerialPort = SerialPort.getSerialPort(mPath, mBaudRate, mDataBits, mStopBits, mParity);
                        if (mSerialPort != null) {
                            mInputStream = mSerialPort.getInputStream();
                            mOutputStream = mSerialPort.getOutputStream();
                            mReadThread = new ReadThread();
                            mReadThread.start();
                        }
                    }
                } catch (SecurityException e1) {
                    e1.printStackTrace();
                    Toast.makeText(this, "设备不存在或没有权限", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                Toast.makeText(this, "打开串口失败", Toast.LENGTH_SHORT).show();
            }
        } catch (InvalidParameterException e3) {
            e3.printStackTrace();
            Toast.makeText(this, e3.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void close() {
        if (mReadThread != null) {
            mReadThread.interrupt();//中断
            mReadThread = null;
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
                mInputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
                mOutputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    private class UiRunnable implements Runnable {
        private String data;

        private UiRunnable(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            id_text.append(Utils.getData());
            id_text.append("：");
            id_text.append(this.data);
            id_text.append("\n");
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted() && mInputStream != null) {
                try {
                    int size = mInputStream.available(); // 获取流的数据长度
                    if (size > 0) {
                        byte[] buffer = new byte[size];
                        size = mInputStream.read(buffer);

                        if (size > 0) {
                            runOnUiThread(new UiRunnable(Utils.toHexString(buffer, size)));
                        }
                    } else {
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
