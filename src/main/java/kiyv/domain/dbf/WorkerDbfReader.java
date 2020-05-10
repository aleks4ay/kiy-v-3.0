package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class WorkerDbfReader implements WorkerDbf {

    private static Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_WORKER = "select ID, DESCR from SC1670;";

    public WorkerDbfReader() {
        connDbf = UtilDao.getConnDbf();
        log.debug("Get connection to 'dbf-files' 1C from {}.", WorkerDbfReader.class);
    }

    @Override
    public Map<String, String> getAll() {

        Map<String, String> mapWorker = new HashMap<>();
        mapWorker.put("     0", "-");

        try (Statement st = connDbf.createStatement()) {

            ResultSet rs = st.executeQuery(SQL_GET_WORKER);
            log.debug("Select all 'Worker' from 1C. SQL = {}.", SQL_GET_WORKER);
            while (rs.next()) {
                String name = rs.getString(2);
                if (name != null) {
                    byte[] bytes = rs.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                } else {
                    name = "-";
                }
                mapWorker.put(rs.getString(1), name);
            }
            log.debug("Was read {} Worker.", mapWorker.size());
            return mapWorker;
        } catch (Exception e) {
            log.warn("Exception during writing all 'Worker'. SQL = {}", SQL_GET_WORKER, e);
        }
        log.debug("Workers not found.");
        return null;
    }
}
