package kiyv.domain.dbf;

import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Manufacture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class ManufDbfReader implements ManufDbf {

    private static Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_MANUFACTURE = "SELECT IDDOC, SP2722, LINENO, SP2725, " +
            "SP2721, SP14726, SP14722, SP14723, SP14724, SP14725 from DT2728 WHERE  SP2722 <> '     0';";

    public ManufDbfReader() {
        connDbf = UtilDao.getConnDbf();
        log.debug("Get connection to 'dbf-files' 1C from {}.", ManufDbfReader.class);
    }

    public Map<String, Manufacture> getAll() {

        Map<String, Manufacture> mapManufacture = new HashMap<>();

        try (Statement st = connDbf.createStatement()) {
            ResultSet rs = st.executeQuery(SQL_GET_MANUFACTURE);
            log.debug("Select all rows 'Manufacture' from 1C. SQL = {}.", SQL_GET_MANUFACTURE);
            while (rs.next()) { //IDDOC, SP2722, LINENO, SP2725, SP2721, SP14726, SP14722, SP14723, SP14724, SP14725

                String idDoc = rs.getString("IDDOC");
                String idOrder = rs.getString("SP2722");
                int pos = rs.getInt("LINENO");
                String id = idDoc + "-" + pos;
                int quantity = rs.getInt("SP2725");

                String idTmc = rs.getString("SP2721");
                String descrSecond = rs.getString("SP14726");
                int sizeA = rs.getInt("SP14722");
                int sizeB = rs.getInt("SP14723");
                int sizeC = rs.getInt("SP14724");
                String embodiment = rs.getString("SP14725");

                if (descrSecond != null) {
                    byte[] bytes2 = rs.getBytes("SP14726");
                    descrSecond = new String(bytes2, "Windows-1251");
                } else {
                    descrSecond = "";
                }

                Manufacture manufacture = new Manufacture(id, idDoc, pos, null, idOrder, null, 0L,
                        quantity, idTmc, descrSecond, sizeA, sizeB, sizeC, embodiment);

                mapManufacture.put(id, manufacture);
            }
            log.debug("Was read {} Manufactures from 1C 'DT2728'.", mapManufacture.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'DT2728'. SQL = {}.", SQL_GET_MANUFACTURE, e);
        }
        return mapManufacture;
    }
}
