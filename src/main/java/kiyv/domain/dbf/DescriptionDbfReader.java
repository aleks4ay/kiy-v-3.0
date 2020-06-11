package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Description;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class DescriptionDbfReader implements DescriptionDbf {

    private Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_DESCRIPTION =
            "select IDDOC, LINENO, SP1902, SP1905, SP14676, SP14686, SP14687, SP14688, SP14717, SP14681 from DT1898;";
//    private static final String SQL_GET_EMBODIMENT = "SELECT ID, DESCR from SC14716;";

    public DescriptionDbfReader(Connection connDbf) {
        this.connDbf = connDbf;
        log.debug("Get connection to 'dbf-files' 1C from {}.", DescriptionDbfReader.class);
    }


    @Override
    public List<Description> getAll() {
/*        Map<String, String> mapEmbodiment = new HashMap<>();

        try (Statement st = connDbf.createStatement()) {
            ResultSet rs1 = st.executeQuery(SQL_GET_EMBODIMENT);
            log.debug("Select all 'Embodiment' from 1C. SQL = {}.", SQL_GET_EMBODIMENT);
            while (rs1.next()) {
                String id = rs1.getString(1);
                String descr = rs1.getString(2);

                if (descr != null) {
                    byte[] bytes = rs1.getBytes(2);
                    descr = new String(bytes, "Windows-1251");
                }
                else {
                    descr = "";
                }
                mapEmbodiment.put(id, descr);
            }
            log.debug("Was read {} rows from 1C SC14716.", mapEmbodiment.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'Journal'. SQL = {}.", SQL_GET_DESCRIPTION, e);
        }*/

        List<Description> result = new ArrayList<>();

        try (Statement st = connDbf.createStatement()) {
            //Get  IDDOC, LINENO, SP1902, SP1905, SP14676, SP14686, SP14687, SP14688, SP14717, SP14681
            ResultSet rs = st.executeQuery(SQL_GET_DESCRIPTION);
            log.debug("Select all 'Description' from 1C. SQL = {}.", SQL_GET_DESCRIPTION);
            while (rs.next()) {

                String idDoc = rs.getString("IDDOC");
                String idTmc = rs.getString("SP1902");//Must not be: 'Go designer to size measurement', 'Shipment', 'Fixing', 'DELETED'
                if (idTmc.equalsIgnoreCase("   CBN") || idTmc.equalsIgnoreCase("   7LH") ||
                        idTmc.equalsIgnoreCase("   9VQ") || idTmc.equalsIgnoreCase("     0")) {
                    continue;
                }
                int position = rs.getInt("LINENO");
                int quantity = rs.getInt("SP1905");
                int sizeA = rs.getInt("SP14686");
                int sizeB = rs.getInt("SP14687");
                int sizeC = rs.getInt("SP14688");
                String embodiment = rs.getString("SP14717");
                double price = rs.getDouble("SP14681");

                String descrSecond = rs.getString("SP14676");
                if (descrSecond != null) {
                    byte[] bytes = rs.getBytes("SP14676");
                    descrSecond = new String(bytes, "Windows-1251");
                } else {
                    descrSecond = "";
                }

                String kod = idDoc + "-" + position;

                result.add(new Description(kod, idDoc, position, idTmc, quantity, descrSecond, sizeA, sizeB, sizeC, embodiment));
            }
            log.debug("Was read {} Description from 1C 'DT1898'.", result.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'DT1898'. SQL = {}.", SQL_GET_DESCRIPTION, e);
        }
        return result;
    }
}
