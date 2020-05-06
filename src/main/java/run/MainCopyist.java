package run;

import reader.ReaderTechnoProduct;
import reader.CopyDescrFrom1S;
import reader.CopyInvoiceFrom1S;
import reader.CopyManufactureFrom1S;
import reader.CopyOrderFrom1S;
import tools.DataControl;
import tools.MyConst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class MainCopyist {

    private static String serverPath = MyConst.getServerPath(); //"Z:\\1C Base\\Copy250106";
    private static String dbfPath = MyConst.getDbfPath(); //"\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy\\test";

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        update();

    }

    public static void update() throws SQLException, IOException, ClassNotFoundException {

        int oldIndexOrder = 0;
        int newIndexOrder = 0;
        int oldIndexDescr = 0;
        int newIndexDescr = 0;
        int oldIndexTechn = 0;
        int newIndexTechn = 0;
        int oldIndexManuf = 0;
        int newIndexManuf = 0;
        int oldIndexInvoice = 0;
        int newIndexInvoice = 0;
        int oldIndexJourn = 0;
        int newIndexJourn = 0;
        boolean needWrite = false;

        while (true) {
            long t1 = System.currentTimeMillis();
            needWrite = false;

            oldIndexJourn = 0;
            oldIndexTechn = 0;
            oldIndexOrder = 0;
            oldIndexDescr = 0;
            oldIndexManuf = 0;
            oldIndexInvoice = 0;

            newIndexJourn = copyFiles("1SJOURN");
            newIndexTechn = copyFiles("SC302");
            newIndexOrder = copyFiles("DH1898") + copyFiles("SC172") + newIndexJourn + copyFiles("SC1670");
            newIndexDescr = copyFiles("DT1898") + copyFiles("SC14716") + newIndexTechn;
            newIndexManuf = copyFiles("DT2728") + newIndexJourn;
            newIndexInvoice = copyFiles("DH3592") + newIndexJourn;

            if (oldIndexTechn != newIndexTechn) {
                ReaderTechnoProduct.doCopyNewRecord();
                needWrite = true;
            }
            if (oldIndexOrder != newIndexOrder) {
                CopyOrderFrom1S.readOrderFrom1S();
                needWrite = true;
            }
            if (oldIndexDescr != newIndexDescr) {
                CopyDescrFrom1S.readDescriptionFrom1S();
                needWrite = true;
            }
            if (oldIndexManuf != newIndexManuf) {
                CopyManufactureFrom1S.readManufactureFrom1S();
                needWrite = true;
            }
            if (oldIndexInvoice != newIndexInvoice) {
                CopyInvoiceFrom1S.readInvoiceFrom1S();
                needWrite = true;
            }

            if (needWrite) {
                DataControl.writeTimeChange();
                DataControl.writeTimeChangeFrom1C();
            }

            long t2 = System.currentTimeMillis();
            System.out.print("time: " + new Time(t2).toString() + "  ");
            System.out.println("Total working time is: " + (double)((t2-t1)/1000) + " c");

            try {
                Thread.sleep(15 * 60 * 1000); // sleep 5 min
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static int copyFiles(String filePath) {
        File fromFile1 = new File(serverPath + "\\" + filePath + ".DBF");
        File toFile1 = new File(dbfPath + "\\" + filePath + ".DBF");
        File fromFile2 = new File(serverPath + "\\" + filePath + ".CDX");
        File toFile2 = new File(dbfPath + "\\" + filePath + ".CDX");
        return copyOneFile(fromFile1, toFile1) + copyOneFile(fromFile2, toFile2);
    }

    public static int copyOneFile(File fromFile, File toFile) {
        int index = 0;
        if (fromFile.lastModified() != toFile.lastModified()) {
            index ++;
            try {
                System.out.println("new file: " + toFile.getName());
                Files.copy(fromFile.toPath(), toFile.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return index;
    }
}
