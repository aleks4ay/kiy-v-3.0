package kiyv.copiller;

import kiyv.domain.dao.*;
import kiyv.domain.dbf.*;
import kiyv.domain.model.Invoice;
import kiyv.domain.model.Journal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class CopyInvoice {

    private static final CopyInvoice copyInvoice = new CopyInvoice();
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());

    private static final StatusDao statusDao = new StatusDaoJdbc(UtilDao.getConnPostgres());
    private static final InvoiceDao invoiceDao = new InvoiceDaoJdbc(UtilDao.getConnPostgres());
    private static final OrderDao orderDao = new OrderDaoJdbc(UtilDao.getConnPostgres());
    private static final JournalDbf journalDbfReader = new JournalDbfReader();
    private static final InvoiceDbf invoiceDbfReader = new InvoiceDbfReader();

    private CopyInvoice() {
    }

    public static CopyInvoice getInstance() {
        return copyInvoice;
    }

    public static void main(String[] args) {

        CopyInvoice.getInstance().doCopyNewRecord();

    }

    public void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        log.info("Start writing 'I N V O I C E'.");

        List<String> listIdOrder = orderDao.getAllId();
        Map<String, Journal> mapJournal = journalDbfReader.getAllJournal();
        Map<String, Invoice> mapInvoice = invoiceDbfReader.getAll();

        List<Invoice> listNewInvoice = new ArrayList<>();
        List<Invoice> listUpdatingInvoice = new ArrayList<>();

        Map<String, Invoice> oldInvoice = invoiceDao.getAll()
                .stream()
                .collect(Collectors.toMap(Invoice::getIdDoc, Invoice::getInvoice));

        for (Invoice invoice : mapInvoice.values()) {
//            String id = invoice.getIdDoc();
            String idDoc = invoice.getIdDoc();
            String idOrder = invoice.getIdOrder();

            if (mapJournal.containsKey(idDoc) && listIdOrder.contains(idOrder)) {
                Journal journal = mapJournal.get(idDoc);

                Timestamp dateInvoice = journal.getDateCreate();

                invoice.setDocNumber(journal.getDocNumber());
                invoice.setTimeInvoice(dateInvoice);
                invoice.setTime22(dateInvoice.getTime());

                if (!oldInvoice.containsKey(idDoc)) {
                    listNewInvoice.add(invoice);
                } else if (!oldInvoice.get(idDoc).equals(invoice)) {
                    log.info("UPDATE Manufacture with Id = '{}', '{}'. Different fields: {}.",
                            invoice.getIdDoc(),
                            invoice.getDocNumber(),
                            invoice.getDifferences(oldInvoice.get(idDoc))
                    );
                    listUpdatingInvoice.add(invoice);
                    oldInvoice.remove(idDoc);
                }
                else {
                    oldInvoice.remove(idDoc);
                }
            }
        }

        if (listNewInvoice.size() > 0) {
            log.info("Save to DataBase. Must be added {} new Manufactures.", listNewInvoice.size());
            invoiceDao.saveAll(listNewInvoice);
            List<Invoice> invoicesAfterFilter = sumInvoiceWithTheSameOrder(listNewInvoice);
            orderDao.savePraceFromInvoice(invoicesAfterFilter, 5.0);
            statusDao.saveFromInvoice(invoicesAfterFilter);
        }
        if (listUpdatingInvoice.size() > 0) {
            log.info("Write change to DataBase. Must be updated {} Manufactures.", listUpdatingInvoice.size());
            invoiceDao.updateAll(listUpdatingInvoice);
        }
        if (oldInvoice.size() > 0) {
            log.info("Delete old Manufactures from DataBase. Must be deleted {} Manufactures.", oldInvoice.size());
            for (Invoice invoice : oldInvoice.values()) {
                log.info("DELETE Manufacture with id '{}', '{}'.", invoice.getIdDoc(), invoice.getDocNumber());
            }
            invoiceDao.deleteAll(oldInvoice.keySet());
        }

        long end = System.currentTimeMillis();
        log.info("End writing 'I N V O I C E'. Time = {} c.", (double)(end-start)/1000);
    }


    public List<Invoice> sumInvoiceWithTheSameOrder(List<Invoice> invoices) {
        Map<String, Invoice> invoicesAfterFilter = new HashMap<>();
        if (invoices.isEmpty()) {
            return invoices;
        }
        for (Invoice invoice : invoices) {
            String idOrder = invoice.getIdOrder();
            long time22 = invoice.getTime22();

            if (! invoicesAfterFilter.containsKey(idOrder)) {
                invoicesAfterFilter.put(idOrder, invoice);
            }
            else {
                Invoice oldInvoice = invoicesAfterFilter.get(idOrder);
                oldInvoice.setPrice(oldInvoice.getPrice() + invoice.getPrice());

                if (oldInvoice.getTime22() < time22) {
                    oldInvoice.setTime22(time22);
                    oldInvoice.setTimeInvoice(invoice.getTimeInvoice());
                    oldInvoice.setDocNumber(invoice.getDocNumber());
                }
            }
        }
        List<Invoice> result = new ArrayList<>();
        result.addAll(invoicesAfterFilter.values());
        return result;
    }
}
