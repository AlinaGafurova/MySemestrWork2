package com.meo;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;

public class SMSSender {
    public SMSSender() {


    }
    private static SerialPort serialPort;

    public static void main(String args[]) {
        try {
            smsSend("Test", "79012345678");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String model;
    static String latsSendResult = "";

    static String getResult() {
        return model + " " + latsSendResult;
    }



    private static String reversePhone(String phone) {
        phone += "F";
        String phoneRev = "";
        phoneRev += phone.charAt(1);
        phoneRev += phone.charAt(0);
        phoneRev += phone.charAt(3);
        phoneRev += phone.charAt(2);
        phoneRev += phone.charAt(5);
        phoneRev += phone.charAt(4);
        phoneRev += phone.charAt(7);
        phoneRev += phone.charAt(6);
        phoneRev += phone.charAt(9);
        phoneRev += phone.charAt(8);
        phoneRev += phone.charAt(11);
        phoneRev += phone.charAt(10);

        return phoneRev;
    }


    private static String StringToUSC2(String text) throws IOException {
        String str = "";

        byte[] msgb = text.getBytes("UTF-16");
        String msgPacked = "";
        for (int i = 2; i < msgb.length; i++) {
            String b = Integer.toHexString((int) msgb[i]);
            if (b.length() < 2) msgPacked += "0";
            msgPacked += b;
        }

        String msglenPacked = Integer.toHexString(msgPacked.length() / 2);
        if (msglenPacked.length() < 2) str += "0";

        str += msglenPacked;
        str += msgPacked;

        str = str.toUpperCase();

        return str;

    }


    private static int getSMSLength(String sms) {
        return (sms.length() / 2 - 1);
    }

    public static boolean smsSend(String sms, String phone) throws IOException {

        serialPort = new SerialPort("COM37");
        try {

            serialPort.openPort();

            serialPort.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);


            String message = "0011000B91" + reversePhone(phone) + "0008A7" + StringToUSC2(sms);


            char c = 0x0D;
            String str0 = "AT+CGMM" + c;
            serialPort.writeString(str0);
            Thread.sleep(500);

            String responce = serialPort.readString();
            model = responce;
            System.out.println(responce);


            serialPort.purgePort(serialPort.PURGE_RXCLEAR | serialPort.PURGE_TXCLEAR);

            String strt = "AT#NITZ" + c;

            serialPort.writeString(strt);
            Thread.sleep(2000);
            responce = serialPort.readString();
            System.out.println(responce);


            String str = "AT+CMGF=1" + c;
            serialPort.writeString(str);
            Thread.sleep(2000);
            responce = serialPort.readString();
            System.out.println(responce);
            serialPort.purgePort(serialPort.PURGE_RXCLEAR | serialPort.PURGE_TXCLEAR);

            str = "AT+CMGS=\"+" + phone + "\"" + c;

            serialPort.writeString(str);
            Thread.sleep(2000);
            responce = serialPort.readString();
            System.out.println(responce);

            serialPort.purgePort(serialPort.PURGE_RXCLEAR | serialPort.PURGE_TXCLEAR);

            c = 26;//Символ CTRL+Z

            serialPort.writeString(sms + c);
            Thread.sleep(2000);
            responce = serialPort.readString();
            latsSendResult = responce;
            System.out.println("Send mesage result: " + responce);
            serialPort.purgePort(serialPort.PURGE_RXCLEAR | serialPort.PURGE_TXCLEAR);
            serialPort.closePort();

            return true;
        } catch (SerialPortException ex) {
            System.out.println(ex);
            return false;
        } catch (InterruptedException e) {

            return false;
        }

    }
}
