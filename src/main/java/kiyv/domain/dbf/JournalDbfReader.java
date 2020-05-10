package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Journal;
import kiyv.domain.tools.TimeConverter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class JournalDbfReader implements JournalDbf {

    private static Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_JOURNAL = "SELECT IDDOC, DOCNO, DATE, TIME " +
            "from 1SJOURN WHERE YEAR (DATE) > 2018 AND CLOSED <> 4;"; //AND IDDOCDEF = ' 1GQ'

    public JournalDbfReader() {
        connDbf = UtilDao.getConnDbf();
        log.debug("Get connection to 'dbf-files' 1C from {}.", JournalDbfReader.class);
    }

    @Override
    public Map<String, Journal> getAllJournal() {
        Map<String, Journal> mapOrder = new HashMap<>();

            try (Statement st = connDbf.createStatement()) {
            ResultSet rs1 = st.executeQuery(SQL_GET_JOURNAL); //IDDOC, DOCNO, DATE, TIME
            log.debug("Select all rows 'Journal' from 1C. SQL = {}.", SQL_GET_JOURNAL);
            while (rs1.next()) {
                String idDoc = rs1.getString("IDDOC");
                byte[] bytes = rs1.getBytes("DOCNO");
                String docNumber = new String(bytes, "Windows-1251");
                long dateCreate = rs1.getDate("DATE").getTime();
                long timeCreate = TimeConverter.convertStrToTimeMillisecond(rs1.getString("TIME"));

                Journal journal = new Journal(0, idDoc, docNumber, new Timestamp(dateCreate + timeCreate));

                mapOrder.put(idDoc, journal);
            }
            log.debug("Was read {} temp rows from 1C Journal.", mapOrder.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'Journal'. SQL = {}.", SQL_GET_JOURNAL, e);
        }
        return mapOrder;
    }
}
