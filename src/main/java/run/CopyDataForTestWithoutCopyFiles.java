package run;

import reader_test.*;
import tools.DataControl;
import tools.MyConst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Time;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class CopyDataForTestWithoutCopyFiles {


    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        long t1 = System.currentTimeMillis();

        ReaderTechnoProduct_test.doCopyNewRecord();
        CopyOrderFrom1S_test.readOrderFrom1S();
        CopyDescrFrom1S_test.readDescriptionFrom1S();
        CopyManufactureFrom1S_test.readManufactureFrom1S();
        CopyInvoiceFrom1S_test.readInvoiceFrom1S();
//        DataControl.writeTimeChange();

        long t2 = System.currentTimeMillis();
        System.out.print("time: " + new Time(t2).toString() + "  ");
        System.out.println("Total working time is: " + (double)((t2-t1)/1000) + " c");

    }

}
