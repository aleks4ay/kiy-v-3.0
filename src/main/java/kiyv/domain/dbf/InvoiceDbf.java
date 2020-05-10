package kiyv.domain.dbf;

import kiyv.domain.model.Invoice;

import java.util.Map;

public interface InvoiceDbf {

    Map<String, Invoice> getAll();

}
