package kiyv.domain.dbf;

import kiyv.domain.dao.UtilDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class EmbodimentDbfReader implements EmbodimentDbf {

    private static Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_EMBODIMENT = "select ID, DESCR from SC14716;";

    public EmbodimentDbfReader() {
        connDbf = UtilDao.getConnDbf();
        log.debug("Get connection to 'dbf-files' 1C from {}.", EmbodimentDbfReader.class);
    }

    @Override
    public Map<String, String> getAll() {

        Map<String, String> mapEmbodiment = new HashMap<>();

        try (Statement st = connDbf.createStatement()) {

            ResultSet rs = st.executeQuery(SQL_GET_EMBODIMENT);
            log.debug("Select all 'Embodiment' from 1C. SQL = {}.", SQL_GET_EMBODIMENT);
            while (rs.next()) {
                String name = rs.getString(2);
                if (name != null) {
                    byte[] bytes = rs.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                } else {
                    name = "-";
                }
                mapEmbodiment.put(rs.getString(1), name);
            }
            log.debug("Was read {} Embodiment.", mapEmbodiment.size());
            return mapEmbodiment;
        } catch (Exception e) {
            log.warn("Exception during writing all 'Embodiment'. SQL = {}", SQL_GET_EMBODIMENT, e);
        }
        log.debug("Embodiments not found.");
        return null;
    }
}
