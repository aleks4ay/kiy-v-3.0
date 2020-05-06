package tools;

import java.io.*;

public final class DataControl {

    public static String fileName = MyConst.getFileName();
    public static String fileName1C = MyConst.getFileName1C();

    // APPEND new time
    public static boolean writeTimeChange(){

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(fileName), true), "UTF8"))) {
            long millis = DateConverter.getNowDate();
            String date = DateConverter.dateToString(millis);
            String time = DateConverter.timeToString(millis);
            String s = date + "  " + time + " < 1C  >";
            bw.write( s + "\r\n");
            bw.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
            return false;
        }
        return true;

    }

    // REWRITE time from 1C
    public static boolean writeTimeChangeFrom1C(){

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(fileName1C), false), "UTF8"))) {
            long millis = DateConverter.getNowDate();
            String date = DateConverter.dateToString(millis);
            String time = DateConverter.timeToString(millis);
            String s = date + "  " + time + " < 1C  >";
            bw.write( s );
            bw.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

}

