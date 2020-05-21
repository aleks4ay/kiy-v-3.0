package kiyv.domain.dbf;

import kiyv.domain.dao.UtilDao;
import kiyv.domain.model.Invoice;
import kiyv.domain.model.Manufacture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static kiyv.log.ClassNameUtil.getCurrentClassName;

public class InvoiceDbfReader implements InvoiceDbf {

    private Connection connDbf;
    private static final Logger log = LoggerFactory.getLogger(getCurrentClassName());
    private static final String SQL_GET_INVOICE ="SELECT IDDOC,SP3561,SP3589 from  DH3592 WHERE SP3561 <>'   0     0';";

    public InvoiceDbfReader(Connection connDbf) {
        this.connDbf = connDbf;
        log.debug("Get connection to 'dbf-files' 1C from {}.", InvoiceDbfReader.class);
    }

    public Map<String, Invoice> getAll() {

        Map<String, Invoice> mapInvoice = new HashMap<>();

        try (Statement st = connDbf.createStatement()) {
            ResultSet rs = st.executeQuery(SQL_GET_INVOICE); //IDDOC,SP3561,SP3589
            log.debug("Select all rows 'Invoice' from 1C. SQL = {}.", SQL_GET_INVOICE);
            while (rs.next()) {

                String idDoc = rs.getString("IDDOC");
                String idOrder = rs.getString("SP3561").substring(4);
                double newPayment = rs.getDouble("SP3589");

                Invoice invoice = new Invoice(idDoc, null, idOrder, null, 0L, newPayment);

                mapInvoice.put(idDoc, invoice);
            }
            log.debug("Was read {} Invoices from 1C 'DH3592'.", mapInvoice.size());

        } catch (Exception e) {
            log.warn("Exception during reading all rows 'DH3592'. SQL = {}.", SQL_GET_INVOICE, e);
        }
        return mapInvoice;
    }
}
