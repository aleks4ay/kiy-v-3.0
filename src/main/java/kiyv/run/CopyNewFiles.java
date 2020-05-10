package kiyv.run;

import kiyv.copiller.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CopyNewFiles {

    private static String serverPath = null;
    private static String dbfPath = null;
    private static final Logger log = LoggerFactory.getLogger(CopyNewFiles.class);

    static {
        //load DB properties
        try (InputStream in = CopyNewFiles.class.getClassLoader().getResourceAsStream("persistence.properties")){
            Properties properties = new Properties();
            properties.load(in);
            serverPath = properties.getProperty("dbf.serverPath");
            dbfPath = properties.getProperty("dbf.path");
            log.debug("Loaded properties as Stream: dbf.serverPath = {}, dbf.path = {}.", serverPath, dbfPath);
        } catch (IOException e) {
            log.warn("Exception during Loaded properties from file {}.", new File("/persistence.properties").getPath(), e);
        }
    }



    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        update();
    }

    public static Map<String, Boolean> update() throws SQLException, IOException, ClassNotFoundException {

        Map<String, Boolean> mapNewFiles = new HashMap<>();
        boolean needWrite = false;

        while (true) {
            log.info("Start copy filas from 1C.");
            long t1 = System.currentTimeMillis();
            needWrite = false;

            boolean isNewClient = copyFiles("SC172");
            boolean isNewTmc = copyFiles("SC302");
            boolean isNewEmbodiment = copyFiles("SC14716");
            boolean isNewManager = copyFiles("SC1670");
            boolean isNewJournal = copyFiles("1SJOURN");
            boolean isNewOrder = copyFiles("DH1898");
            boolean isNewDescription = copyFiles("DT1898");
            boolean isNewManuf = copyFiles("DT2728");
            boolean isNewInvoice = copyFiles("DH3592");

            if (isNewTmc) {
                CopyTmc.getInstance().doCopyNewRecord();
                CopyTmcTechno.getInstance().doCopyNewRecord();
                needWrite = true;
            }
            if (isNewOrder) {
                CopyOrder.getInstance().doCopyNewRecord();
                needWrite = true;
            }
            if (isNewDescription) {
                CopyDescription.getInstance().doCopyNewRecord();
                needWrite = true;
            }
            if (isNewManuf) {
                CopyManuf.getInstance().doCopyNewRecord();
                needWrite = true;
            }
            if (isNewInvoice) {
                CopyInvoice.getInstance().doCopyNewRecord();
                needWrite = true;
            }

            if (needWrite) {
//                DataControl.writeTimeChange();
//                DataControl.writeTimeChangeFrom1C();
            }

            long t2 = System.currentTimeMillis();
            log.info("Total time for copy = {} c.", (double)((t2-t1)/1000));

            try {
                Thread.sleep(15 * 60 * 1000); // sleep 15 min
            } catch (InterruptedException e) {
                log.warn("Exception during sleep 15 min.", e);
            }
        }
    }

    public static boolean copyFiles(String filePath) {
        File fromFile1 = new File(serverPath + "\\" + filePath + ".DBF");
        File toFile1 = new File(dbfPath + "\\" + filePath + ".DBF");
        File fromFile2 = new File(serverPath + "\\" + filePath + ".CDX");
        File toFile2 = new File(dbfPath + "\\" + filePath + ".CDX");
        return copyOneFile(fromFile1, toFile1) & copyOneFile(fromFile2, toFile2);
    }

    public static boolean copyOneFile(File fromFile, File toFile) {
        if (fromFile.lastModified() != toFile.lastModified()) {
            try {
                log.debug("new file '{}'.", toFile.getName());
//                System.out.println("new file: " + toFile.getName());
                Files.copy(fromFile.toPath(), toFile.toPath(), REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                log.warn("Exception during copy '{}'.", toFile.getName(), e);
//                e.printStackTrace();
            }
        }
        return false;
    }
}
