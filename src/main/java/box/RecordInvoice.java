package box;

public class RecordInvoice {
//    public int id = 0;
    public String idDoc = "";
    public String docNumber = "";
    public String idOrder = "";
    public long time22;
    public double price;

    public RecordInvoice(/*int id, */String idDoc, String docNumber, String idOrder, long time22, double price) {
//        this.id = id;
        this.idDoc = idDoc;
        this.docNumber = docNumber;
        this.idOrder = idOrder;
        this.time22 = time22;
        this.price = price;
    }

    public boolean compareToInvoice(RecordInvoice r2) {
        boolean b1 = this.idDoc.equalsIgnoreCase(r2.idDoc);
        boolean b2 = this.docNumber.equalsIgnoreCase(r2.docNumber);
        boolean b3 = this.idOrder.equalsIgnoreCase(r2.idOrder);
        boolean b4 = this.time22 == r2.time22;
        boolean b5 = this.price == r2.price;

        if (b1 & b2 & b3 & b4 & b5) {
            return true;
        }
        else {
            return false;
        }
    }
}
