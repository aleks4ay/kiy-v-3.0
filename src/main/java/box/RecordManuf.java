package box;

public class RecordManuf {
    public int id = 0;
    public String idDoc = "";
    public int position = 0;
    public String docNumber = "";
    public String idOrder = "";
    public long time21;

    public int amount;
    public String idTmc;
    public String descrSecond;
    public int sizeA;
    public int sizeB;
    public int sizeC;
    public String embodiment;
//    private double price;


    public RecordManuf(int id, String idDoc, int position, String docNumber, String idOrder, long time21, int amount, String idTmc,
                       String descrSecond, int sizeA, int sizeB, int sizeC, String embodiment) {
        this.id = id;
        this.idDoc = idDoc;
        this.position = position;
        this.docNumber = docNumber;
        this.idOrder = idOrder;
        this.time21 = time21;
        this.amount = amount;
        this.idTmc = idTmc;
        this.descrSecond = descrSecond;
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.sizeC = sizeC;
        this.embodiment = embodiment;
    }

    public boolean compareTo(RecordManuf r2) {
        boolean b1 = this.idDoc.equalsIgnoreCase(r2.idDoc);
        boolean b2 = this.position == r2.position;
        boolean b3 = this.docNumber.equalsIgnoreCase(r2.docNumber);
        boolean b4 = this.idOrder.equalsIgnoreCase(r2.idOrder);
        boolean b5 = this.time21 == r2.time21;
        boolean b6 = this.amount == r2.amount;
        boolean b7 = this.idTmc.equalsIgnoreCase(r2.idTmc);
        boolean b8 = this.descrSecond.equalsIgnoreCase(r2.descrSecond);
        boolean b9 = this.sizeA == r2.sizeA;
        boolean b10 = this.sizeB == r2.sizeB;
        boolean b11 = this.sizeC == r2.sizeC;
        boolean b12 = this.embodiment == r2.embodiment;

        if (b1 & b2 & b3 & b4 & b5 & b6 & b7 & b8 & b9 & b10 & b11 & b12) {
            return true;
        }
        else {
            return false;
        }
    }
}
