package BGUServer;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class messageEncoderDecoderBGR implements MessageEncoderDecoder<String> {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    private int roundCounter = 0; //counting the parts i counted until now
    private int amountOfZero = 0;
    private int zeroCounter =0;
    private byte[] OpCodebytes = new byte[2];
    private int needBytes = 0;
    private short opCode;
    public boolean finished = false;

    @Override
    public String decodeNextByte(byte nextByte) {
        /*char input = (char) nextByte;
        System.out.println("decode next byte..." + input);*/
        if(finished || (roundCounter == 1 && nextByte == 0)) {
            roundCounter = 0;
            finished = false;
        }
        if (nextByte == 0 && roundCounter > 2  /*&& needBytes == 0*/) {
            zeroCounter++;
            if (zeroCounter != amountOfZero) {
                nextByte = (byte) ' '; //space char
            }
        }
        int temp = roundCounter + 1;
        if (roundCounter < 2 && roundCounter < OpCodebytes.length) {
            OpCodebytes[roundCounter] = nextByte;
            roundCounter++;
            if (roundCounter == 2) {
                OpCodebytes[0] = 0;
                opCode = bytesToShort(OpCodebytes);
                amountOfZero = 2 * getZeros();
                needBytes = getBytesNeeded();
                nextByte = (byte) ' ';
            }
        }
        if (zeroCounter % 2 == 1) {
            return null;
        }

        if (roundCounter >= 2 && nextByte != (byte) ';') {
            pushByte(nextByte);
            if (((amountOfZero == zeroCounter) || amountOfZero == 0) && needBytes != 0) {
                needBytes--;
            }
            if (roundCounter != temp) {
                roundCounter++;
            }

        } else if (/*amountOfZero == zeroCounter && needBytes == 0 &&*/ nextByte == (byte) ';') {
           // System.out.println("popstring");
            return popString();
        }
        return null; //not a line yet
    }

    private int getBytesNeeded() {
        if(opCode == 2 || opCode == 4)
            return 1;
        return 0;
    }


    @Override
    public byte[] encode(String message) {
        //message = message.substring(0,message.length());
        bytes =new byte[1<<10];
        message = message + ' ' + '\n';
        String [] msg = message.split(" ");
        OpCodebytes = shortToBytes(getOpCode(msg[0]));
        /*for(byte c:OpCodebytes){
            pushByte(c);
        }*/
        for(int i = 0; i<msg.length;i++){
            if(i>=1 && i!= msg.length-1){
                pushByte((byte)' ');
            }
            for(byte b:msg[i].getBytes(StandardCharsets.UTF_8)){
                pushByte(b);
            }
            //pushByte((byte) 0);

        }
        return bytes;
    }

    private short getOpCode(String opMsg){
        switch (opMsg) {
            case "REGISTER":
                return 1;
            case "LOGIN":
                return 2;
            case "LOGOUT":
                return 3;
            case "FOLLOW":
                return 4;
            case "POST":
                return 5;
            case "PM":
                return 6;
            case "LOGSTAT":
                return 7;
            case "STAT":
                return 8;
            case "NOTIFICATION":
                return 9;
            case "ACK":
                return 10;
            case "ERROR":
                return 11;
            case "BLOCK":
                return 12;
        }
        return 0;
    }
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }
    public int getZeros(){
        if(opCode == 1|| opCode == 6){
            return 3; // register - 01 , pm - 06
        }
        else if(opCode == 2 || opCode == 9){
            return 2; //login - 02, notification - 09
        }
        else if(opCode == 3 || opCode == 7 || opCode == 4 || opCode ==  10 || opCode == 11){
            return 0; //logout - 03, logstat - 07, follow - 04 , ack - 10, error - 11
        }
        else if(opCode == 5 || opCode == 8 || opCode == 12){
            return 1; // post - 05, stat - 08 , block - 12
        }
        return 0;
    }

    public short bytesToShort(byte []byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        len = 0;
        return result;
    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = getMsg();
        reset();
        finished = true;
        /*System.out.println(result);
        System.out.println("we got to popstring with the message above");*/
        return result;
    }
    public String getMsg(){
        String msg = new String(bytes, 0, len, StandardCharsets.UTF_8);
        if(opCode == 1){
            msg = "REGISTER" +msg;
        }
        else if(opCode ==2 ){
            msg = "LOGIN" + msg;
        }
        else if(opCode == 3){
            msg = "LOGOUT" + msg;
        }
        else if(opCode == 4){
            msg = "FOLLOW" + msg;
        }
        else if(opCode == 5){
            msg = "POST" + msg;
        }
        else if(opCode == 6){
            msg = "PM" + msg;
        }
        else if(opCode == 7){
            msg = "LOGSTAT" + msg; //TODO SAME AS LOGOUT
        }
        else if(opCode == 8){
            msg = "STAT" + msg;
        }
        else if(opCode == 9){
            msg = "NOTIFICATION" + msg;
        }
        else if(opCode == 10){
            msg = "ACK" + msg;
        }
        else if(opCode == 11){
            msg = "ERROR" + msg; //TODO SAME AS LOGOUT
        }
        else if(opCode == 12){
            msg = "BLOCK" + msg;
        }
        return msg;
    }
    public void reset(){
        len = 0;
        roundCounter = 0;
        amountOfZero = 0;
        zeroCounter = 0;
        needBytes = 0;
        bytes =new byte[1<<10];
        OpCodebytes = new byte[2];
        opCode = -1;
    }
}
