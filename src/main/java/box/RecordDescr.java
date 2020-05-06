package box;

public class RecordDescr {

    public int kod = 0;
    public int bigNumber;
    public String idDoc = "";
    public int position = 0;
    public String idTmc = "";
    public int amount = 0;
    public String descrSecond = "";
    public int sizeA = 0;
    public int sizeB = 0;
    public int sizeC = 0;
    public String embodiment = "";


    public RecordDescr(int kod, int bigNumber, String idDoc, int position, String idTmc, int amount,
                       String descrSecond, int sizeA, int sizeB, int sizeC, String embodiment) {
        this.kod = kod;
        this.bigNumber = bigNumber;
        this.idDoc = idDoc;
        this.position = position;
        this.idTmc = idTmc;
        this.amount = amount;
        this.descrSecond = descrSecond;
        this.sizeA = sizeA;
        this.sizeB = sizeB;
        this.sizeC = sizeC;
        this.embodiment = embodiment;
    }

    public boolean compareTo(RecordDescr r2) {
        boolean b1 = this.kod == r2.kod;
        boolean b2 = this.bigNumber == r2.bigNumber;
        boolean b3 = this.idDoc.equalsIgnoreCase(r2.idDoc);
        boolean b4 = this.position == r2.position;
        boolean b5 = this.idTmc.equalsIgnoreCase(r2.idTmc);
        boolean b6 = this.amount == r2.amount;
        boolean b7 = this.embodiment.equalsIgnoreCase(r2.embodiment);
        boolean b8 = this.descrSecond.equalsIgnoreCase(r2.descrSecond);
        boolean b9 = this.sizeA == r2.sizeA;
        boolean b10 = this.sizeB == r2.sizeB;
        boolean b11 = this.sizeC == r2.sizeC;
        if (b1 & b2 & b3 & b4 & b5 & b6 & b7 & b8 & b9 & b10 & b11) {
            return true;
        }
        else {
            return false;
        }
    }
}
