package android.serialport.api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * @author tina
 * @package_name android_serialport_api
 * @date 2016-09-01
 * @desc TODO
 */
public class SerialPort {

    public static final int STOP_BITS_1 = 1;//1位停止位
    public static final int STOP_BITS_2 = 2;//2位停止位

    public static final int DATA_BITS_8 = 8;//8位数据位
    public static final int DATA_BITS_7 = 7;//7位数据位
    public static final int DATA_BITS_6 = 6;//6位数据位

    public static final char PARITY_NONE = 'N';//无校验
    public static final char PARITY_ODD = 'O';//奇校验
    public static final char PARITY_EVEN = 'E';//偶校验

    private static final String TAG = "SerialPort";

    static {
        System.loadLibrary("serial_port");
    }

    private native FileDescriptor open(String path, int baudrate, int databits, int stopbits, char parity);

    private native void close(FileDescriptor fd);

    /**
     * 获取串口连接
     *
     * @param path     串口
     * @param baudrate 波特率
     * @return SerialPort 串口连接
     * @throws SecurityException         设备不存在
     * @throws IOException               连接串口失败
     * @throws InvalidParameterException 参数错误
     */
    public static SerialPort getSerialPort(String path, int baudrate)
            throws SecurityException, IOException, InvalidParameterException {
        return SerialPort.getSerialPort(path, baudrate, DATA_BITS_8, STOP_BITS_1, PARITY_NONE);
    }

    /**
     * 获取串口连接
     *
     * @param path     串口
     * @param baudrate 波特率
     * @param databits 数据位
     * @param stopbits 停止位
     * @param parity   校验方式 无校验:PARITY_NONE 奇校验:PARITY_ODD  偶校验:PARITY_EVEN
     * @return SerialPort 串口连接
     * @throws SecurityException         设备不存在
     * @throws IOException               连接串口失败
     * @throws InvalidParameterException 参数错误
     */
    public static SerialPort getSerialPort(String path, int baudrate, int databits, int stopbits, char parity)
            throws SecurityException, IOException, InvalidParameterException {
        if ((path.length() == 0) || (baudrate == -1)) {
            throw new InvalidParameterException("波特率不正确");
        }

        if (databits != DATA_BITS_8 && databits != DATA_BITS_7 && databits != DATA_BITS_6) {
            throw new InvalidParameterException("数据位不正确");
        }

        if (stopbits != STOP_BITS_1 && stopbits != STOP_BITS_2) {
            throw new InvalidParameterException("停止位不正确");
        }

        if (parity != PARITY_NONE && parity != PARITY_ODD && parity != PARITY_EVEN) {
            throw new InvalidParameterException("校验方式不正确");
        }
            /* Open the serial port */
        return new SerialPort(new File(path), baudrate, databits, stopbits, parity);
    }

    /*
     * Do not remove or rename the field mFd: it is used by native method
     * close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int databits, int stopbits, char parity)
            throws SecurityException, IOException {

		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        // 打开串口
        mFd = open(device.getAbsolutePath(), baudrate, databits, stopbits, parity);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    //关闭
    public void close() {
        if (mFileInputStream != null) {
            try {
                mFileInputStream.close();
                mFileInputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.close();
                mFileOutputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mFd != null) {
            close(mFd);
            mFd = null;
        }
    }
}
