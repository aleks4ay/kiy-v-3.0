package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Tmc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class TmcDbfReader implements TmcDbf {

    private Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_TMC = "select ID, PARENTID, CODE, DESCR, ISFOLDER, SP276, SP277 from SC302;";

    public TmcDbfReader(Connection connDbf) {
        this.connDbf = connDbf;
        log.debug("Get connection to 'dbf-files' 1C from {}.", TmcDbfReader.class);
    }

    @Override
    public List<Tmc> getAll() {

        List<Tmc> listTmc = new ArrayList<>();

        try (Statement st = connDbf.createStatement()) {

            ResultSet rs = st.executeQuery(SQL_GET_TMC);
            log.debug("Select all 'Worker' from 1C. SQL = {}.", SQL_GET_TMC);
            while (rs.next()) {
                String id = rs.getString("ID");
                String parentId = rs.getString("PARENTID");
                String code = rs.getString("CODE");
                String descr = rs.getString("DESCR");
                int isFolder = rs.getInt("ISFOLDER");
                String descrAll = rs.getString("SP276");
                String type = rs.getString("SP277");

                if (descr != null) {
                    byte[] bytes = rs.getBytes("DESCR");
                    descr = new String(bytes, "Windows-1251");
                }
                else {
                    descr = "";
                }

                if (descrAll != null) {
                    byte[] bytes = rs.getBytes("SP276");
                    descrAll = new String(bytes, "Windows-1251");
                }
                else {
                    descrAll = "";
                }

                listTmc.add(new Tmc(id, parentId, code, descr, isFolder, descrAll, type));

            }
            log.debug("Was read {} Tmc.", listTmc.size());
            return listTmc;
        } catch (Exception e) {
            log.warn("Exception during writing all 'Tmc'. SQL = {}", SQL_GET_TMC, e);
        }
        log.debug("Tmc not found.");
        return null;
    }
}
