package box;

import java.sql.Timestamp;

public class RecordOrder {
    public int bigNumber;
    public String idDoc = "";
    public String idClient = "";
    public String idManager = "";
    public int durationTime;
    public String docNumber = "";
    public int posCount = 0;
    public String client = ""; // Don't compare
    public String manager = ""; // Don't compare
    public Timestamp dateCreate;
    public Timestamp dateToFactory;
    public Timestamp dateToShipment; // Don't compare
    public double price;

    public RecordOrder(int bigNumber, String idDoc, String idClient, String idManager, int durationTime, String docNumber,
                       int posCount, String client, String manager, Timestamp dateCreate, Timestamp dateToFactory, Timestamp dateToShipment, double price) {
        this.bigNumber = bigNumber;
        this.idDoc = idDoc;
        this.idClient = idClient;
        this.idManager = idManager;
        this.durationTime = durationTime;
        this.docNumber = docNumber;
        this.posCount = posCount;
        this.client = client;
        this.manager = manager;
        this.dateCreate = dateCreate;
        this.dateToFactory = dateToFactory;
        this.dateToShipment = dateToShipment;
        this.price = price;
    }

    public boolean compareTo(RecordOrder r2) {
        boolean b1 = this.bigNumber == r2.bigNumber;
        boolean b2 = this.idDoc.equalsIgnoreCase(r2.idDoc);
        boolean b3 = this.idClient.equalsIgnoreCase(r2.idClient);
        boolean b4 = this.idManager.equalsIgnoreCase(r2.idManager);
        boolean b5 = this.durationTime == r2.durationTime;
        boolean b6 = this.docNumber.equalsIgnoreCase(r2.docNumber);
        boolean b7 = this.price == r2.price;
        boolean b8 = this.posCount == r2.posCount;
        boolean b9 = this.dateCreate.getTime() == r2.dateCreate.getTime();
        boolean b10 = this.dateToFactory.getTime() == r2.dateToFactory.getTime();
        boolean b11 = this.client.equalsIgnoreCase(r2.client);
        boolean b12 = this.manager.equalsIgnoreCase(r2.manager);
        if (b1 & b2 & b3 & b4 & b5 & b6 & b7 & b8 & b9 & b10 & b11 & b12) {
            return true;
        }
        else {
            return false;
        }
    }
}
