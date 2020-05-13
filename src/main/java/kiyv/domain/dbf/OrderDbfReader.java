package kiyv.domain.dbf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Order;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class OrderDbfReader implements OrderDbf {

    private static Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_ORDER = "SELECT IDDOC, SP1899, SP14836, SP14695, SP14680, SP14684 " +
            "from DH1898 WHERE SP14694 = 1;";

    public OrderDbfReader() {
        connDbf = UtilDao.getConnDbf();
        log.debug("Get connection to 'dbf-files' 1C from {}.", OrderDbfReader.class);
    }

    public Map<String, Order> getAll() {

        Map<String, Order> mapOrder = new HashMap<>();

        try (Statement st = connDbf.createStatement()) {
            ResultSet rs2 = st.executeQuery(SQL_GET_ORDER); //SP1899, SP14836, SP14695, SP14680, SP14684
            log.debug("Select all rows 'Order' from 1C. SQL = {}.", SQL_GET_ORDER);
            while (rs2.next()) {
                String idDoc = rs2.getString("IDDOC");

                String idClient = rs2.getString("SP1899");
                Date date = rs2.getDate("SP14836");
                Timestamp dateToFactory;
                if (date == null) {
                    dateToFactory = null;
                }
                else if (date.getTime() < 1560805200000L) {
                        continue;
                    }
                else {
                    dateToFactory = new Timestamp(date.getTime());
                }
                int durationTime = rs2.getInt("SP14695");
                String idManager = rs2.getString("SP14680");
                double price = rs2.getDouble("SP14684");

                Order order = new Order(0, idDoc, idClient, idManager, durationTime, null, 0, null, null,
                        null, dateToFactory, null, price);

                mapOrder.put(idDoc, order);
            }
            log.debug("Was read {} orders from 1C 'DH1898'.", mapOrder.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'DH1898'. SQL = {}.", SQL_GET_ORDER, e);
        }
        return mapOrder;
    }
}
