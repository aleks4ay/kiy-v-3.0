package kiyv.copiller;

import kiyv.domain.model.Journal;
import kiyv.domain.tools.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import kiyv.domain.dao.OrderDao;
import kiyv.domain.dao.OrderDaoJdbc;
import kiyv.domain.dao.UtilDao;
import kiyv.domain.dbf.*;
import kiyv.domain.model.Client;
import kiyv.domain.model.Order;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class CopyOrder {


    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());


    public static void main(String[] args) {

        new CopyOrder().doCopyNewRecord();

    }

    public void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        log.info("Start writing 'O R D E R S'.");

        UtilDao utilDao = new UtilDao();
        Connection connPostgres = utilDao.getConnPostgres();
        Connection connDbf = utilDao.getConnDbf();

        OrderDao orderDao = new OrderDaoJdbc(connPostgres);

        JournalDbf journalDbfReader = new JournalDbfReader(connDbf);
        OrderDbf orderDbfReader = new OrderDbfReader(connDbf);
        ClientDbf clientDbfReader = new ClientDbfReader(connDbf);
        WorkerDbf managerDbfReader = new WorkerDbfReader(connDbf);

        Map<String, String> mapManagerName = managerDbfReader.getAll();
        Map<String, String> mapClientName = clientDbfReader.getAll()
                .stream()
                .collect(Collectors.toMap(Client::getId, Client::getName));

        Map<String, Journal> mapJournal = journalDbfReader.getAllJournal();
        Map<String, Order> mapOrder = orderDbfReader.getAll();

        List<Order> listNewOrder = new ArrayList<>();
        List<Order> listUpdatingOrder = new ArrayList<>();

        Map<String, Order> oldOrder = orderDao.getAll()
                .stream()
                .collect(Collectors.toMap(Order::getIdDoc, Order::getOrder));

        for (Order newOrder : mapOrder.values()) {
            String idOrder = newOrder.getIdDoc();

            if (mapJournal.containsKey(idOrder)) {

                Journal journal = mapJournal.get(idOrder);
                String docNumber = journal.getDocNumber();

                Timestamp dateCreate = journal.getDateCreate();
                Timestamp dateToFactory = newOrder.getDateToFactory();
                Timestamp dateEnd = newOrder.getDateToShipment();
                int duration = newOrder.getDurationTime();
                int bigNumber = (DateConverter.getYearShort(dateCreate.getTime()) ) * 100000 + Integer.valueOf(docNumber.substring(5));

                if ( dateToFactory == null) {
                    dateToFactory = dateCreate;
                }
/*                if (dateToFactory.getTime() < 1560805200000L) {
                    continue;
                }*/
                if (dateEnd == null) {
                    Timestamp maximum = dateCreate.after(dateToFactory) ? dateCreate : dateToFactory;
                    dateEnd = new Timestamp(DateConverter.offset(maximum.getTime(), duration));
                }

                String idClient = newOrder.getIdClient();
                String idManager = newOrder.getIdManager();

                String managerName;
                if (mapManagerName.get(idManager) == null) {
                    managerName = "";
                }
                else {
                    managerName = mapManagerName.get(idManager);
                }
                String clientName;
                if (mapClientName.get(idClient) == null){
                    clientName = "";
                }
                else {
                    clientName = mapClientName.get(idClient);
                }

                newOrder.setBigNumber(bigNumber);
                newOrder.setDocNumber(docNumber);
                newOrder.setDateCreate(dateCreate);
                newOrder.setManager(managerName);
                newOrder.setClient(clientName);
                newOrder.setDateToShipment(dateEnd);

                if (!oldOrder.containsKey(idOrder)) {
                    listNewOrder.add(newOrder);
                } else if (!oldOrder.get(idOrder).equals(newOrder)) {
                    log.info("UPDATE Order with idDoc '{}', '{}'. Different fields: {}.",
                            newOrder.getIdDoc(),
                            newOrder.getDocNumber(),
                            newOrder.getDifferences(oldOrder.get(idOrder))
                    );
                    listUpdatingOrder.add(newOrder);
                    oldOrder.remove(idOrder);
                }
                else {
                    oldOrder.remove(idOrder);
                }
            }
        }

        if (listNewOrder.size() > 0) {
            log.info("Save to DataBase. Must be added {} new Orders.", listNewOrder.size());
            orderDao.saveAll(listNewOrder);
        }
        if (listUpdatingOrder.size() > 0) {
            log.info("Write change to DataBase. Must be updated {} Orders.", listUpdatingOrder.size());
            orderDao.updateAll(listUpdatingOrder);
        }
        if (oldOrder.size() > 0) {
            log.info("Delete old orders from DataBase. Must be deleted {} Orders.", oldOrder.size());
            for (Order order : oldOrder.values()) {
                log.info("DELETE Order with idDoc '{}', '{}'.", order.getIdDoc(), order.getDocNumber());
            }
            orderDao.deleteAll(oldOrder.keySet());
        }

        long end = System.currentTimeMillis();
        log.info("End writing 'O R D E R S'. Time = {} c.", (double)(end-start)/1000);

        utilDao.closeConnection(connPostgres);
        utilDao.closeConnection(connDbf);
    }
}
