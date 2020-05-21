package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Client;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class ClientDbfReader implements ClientDbf {

    private Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_ALL = "select ID, DESCR from SC172;";

    public ClientDbfReader(Connection connDbf) {
        this.connDbf = connDbf;
        log.debug("Get connection to 'dbf-files' 1C from {}.", ClientDbfReader.class);
    }

    @Override
    public List<Client> getAll() {

        List<Client> listClient = new ArrayList<>();

        try (Statement st = connDbf.createStatement()) {
            ResultSet rs = st.executeQuery(SQL_GET_ALL);
            log.debug("Select all 'Clients' from 1C. SQL = {}.", SQL_GET_ALL);
            while (rs.next()) {
                String name = rs.getString(2);
                if (name != null) {
                    byte[] bytes = rs.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                } else {
                    name = "-";
                }
                listClient.add(new Client(rs.getString(1), name));
            }
            log.debug("Was read {} Clients.", listClient.size());
            return listClient;
        } catch (Exception e) {
            log.warn("Exception during writing all 'Clients'. SQL = {}.", SQL_GET_ALL, e);
        }
        log.debug("Clients not found.");
        return null;
    }
}
