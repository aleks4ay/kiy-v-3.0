package reader_test;

import tools.Converter;
import box.RecordOrder;
import tools.DateConverter;
import tools.MyConstTest;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

public final class CopyOrderFrom1S_test {
    private static String dbfPath = MyConstTest.getDbfPath(); //"\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy";

    private static String url = MyConstTest.getURL();
    private static String user = MyConstTest.getUSER();
    private static String password = MyConstTest.getPASSWORD();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        readOrderFrom1S();

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)(end-start)/1000 + " c." );
    }

    public static void readOrderFrom1S() throws ClassNotFoundException, SQLException, IOException {
        long start = System.currentTimeMillis();
        System.out.print("write  'O R D E R S', ");

        Map<Integer, RecordOrder> listOrder = new TreeMap<>();
        Map<String, String> listClient = new TreeMap<>();
        Map<String, String> listWorker = new TreeMap<>();

        readAdditionalData(listClient, listWorker);
        readOldOrders(listOrder);

        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);
        conn2.setAutoCommit(false);
//        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        String idDoc, docNumber, idClient, idManager, managerName, clientName;
        int positionCount, durationTime;
        Date dateCreate, dateToFactory;
        double price;

        try {
            Statement st = conn1.createStatement();
            ResultSet rs = st.executeQuery("SELECT j.IDDOC, j.DOCNO, j.DATE, j.ACTCNT, o.SP1899, o.SP14836, o.SP14695, o.SP14680, o.SP14684 " +
                    "from 1SJOURN j, DH1898 o WHERE o.IDDOC = j.IDDOC AND YEAR (j.DATE) > 2018 AND j.IDDOCDEF = ' 1GQ' AND o.SP14694 = 1;"); // j.CLOSED IN (1, 5)
            while (rs.next()) {

                idDoc = rs.getString("IDDOC");
                byte[] bytes = rs.getBytes("DOCNO");
                docNumber = new String(bytes, "Windows-1251");

                dateCreate = rs.getDate("DATE");
                positionCount = Converter.convertStrToInt(rs.getString("ACTCNT"));
                idClient = rs.getString("SP1899");
                dateToFactory = rs.getDate("SP14836");
                durationTime = rs.getInt("SP14695");
                idManager = rs.getString("SP14680");
                price = rs.getDouble("SP14684");

                if (dateToFactory == null) {
                    dateToFactory = dateCreate;
                }

//                if (dateToFactory.getTime() < 1560805200000L) {
//                    continue;
//                }

                if (listWorker.get(idManager) == null) {
                    managerName = "";
                }
                else {
                    managerName = listWorker.get(idManager);
                }
                if (listClient.get(idClient) == null){
                    clientName = "";
                }
                else {
                    clientName = listClient.get(idClient);
                }

                int bigNumber = DateConverter.getYearShort(dateCreate.getTime()) * 100000 + Integer.valueOf(docNumber.substring(5));
//                System.out.println("3");
                long timeEnd = DateConverter.offset(dateToFactory.getTime() ,durationTime);

                RecordOrder newOrder = new RecordOrder(bigNumber, idDoc, idClient, idManager, durationTime, docNumber, positionCount, clientName,
                        managerName, new Timestamp(dateCreate.getTime()), new Timestamp(dateToFactory.getTime()), new Timestamp(timeEnd), price);
//                System.out.println(listOrder.containsKey(bigNumber));
//                System.out.println("4");
                if ( ! listOrder.containsKey(bigNumber) ) {
                    ps2 = conn2.prepareStatement(
                            "INSERT INTO kiyv.test.orders_test (big_number, iddoc, idclient, idmanager, duration, docno, pos_count, client_name, manager_name, " +
                                    "t_create, t_factory, t_end, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, bigNumber);
                    ps2.setString(2, idDoc);
                    ps2.setString(3, idClient);
                    ps2.setString(4, idManager);
                    ps2.setInt(5, durationTime);
                    ps2.setString(6, docNumber);
                    ps2.setInt(7, positionCount);
                    ps2.setString(8, clientName);
                    ps2.setString(9, managerName);
                    ps2.setTimestamp(10, new Timestamp(dateCreate.getTime()));
                    ps2.setTimestamp(11, new Timestamp(dateToFactory.getTime()));
                    ps2.setTimestamp(12, new Timestamp(timeEnd));
                    ps2.setDouble(13, price);

                    ps2.addBatch();
                    ps2.executeBatch();
//                    listOrder.put(bigNumber, newOrder);

                }
                else if (listOrder.get(bigNumber).compareTo(newOrder) == false) {
                    ps2 = conn2.prepareStatement("UPDATE kiyv.test.orders_test SET iddoc = ?, idclient = ?, idmanager = ?, duration = ?, docno = ?, " +
                            " pos_count = ?, client_name = ?, manager_name = ?, t_create = ?, t_factory = ?, t_end = ?, price =? WHERE big_number = ?;");
                    ps2.setString(1, idDoc);
                    ps2.setString(2, idClient);
                    ps2.setString(3, idManager);
                    ps2.setInt(4, durationTime);
                    ps2.setString(5, docNumber);
                    ps2.setInt(6, positionCount);
                    ps2.setString(7, clientName);
                    ps2.setString(8, managerName);
                    ps2.setTimestamp(9, new Timestamp(dateCreate.getTime()));
                    ps2.setTimestamp(10, new Timestamp(dateToFactory.getTime()));
                    ps2.setTimestamp(11, new Timestamp(timeEnd));
                    ps2.setDouble(12, price);

                    ps2.setInt(13, bigNumber);

                    ps2.addBatch();
                    ps2.executeBatch();
                    conn2.commit();
                }
//                else {
//                    continue;
//                }

            }
            conn2.commit();
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            conn1.close();
            conn2.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("t = " + (double)(end-start)/1000 + " c" );
    }

    public static void readAdditionalData(Map<String, String> listClient, Map<String, String> listWorker)
                    throws ClassNotFoundException, SQLException, IOException  {

        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection con = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);
        try {
            Statement st = con.createStatement();
            ResultSet rs1 = st.executeQuery("select ID, DESCR from SC172;");

            while (rs1.next()) {
                String name = rs1.getString(2);
                if (name != null) {
                    byte[] bytes = rs1.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                }
                else {
                    name = "-";
                }
                listClient.put(rs1.getString(1), name);
            }
            ResultSet rs2 = st.executeQuery("select ID, DESCR from SC1670;");
            while (rs2.next()) {
                String name = rs2.getString(2);
                if (name != null) {
                    byte[] bytes = rs2.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                }
                else {
                    name = "-";
                }
                listWorker.put(rs2.getString(1), name);
            }
            rs1.close();
            rs2.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readOldOrders (Map<Integer, RecordOrder> listOrder) {

        int bigNumber, durationTime, posCount;
        String docNumber, idDoc, idClient, idManager, client, manager;
        Timestamp dateCreate, dateToFactory, dateToShipment;
        double price;

        RecordOrder tempOrder;

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select * from kiyv.test.orders_test;");

            while (rs.next()) {
                bigNumber = rs.getInt("big_number");
                idDoc = rs.getString("iddoc");
                idClient = rs.getString("idclient");
                idManager = rs.getString("idmanager");
                durationTime = rs.getInt("duration");
                docNumber = rs.getString("docno");
                posCount = rs.getInt("pos_count");
                client = rs.getString("client_name");
                manager = rs.getString("manager_name");

                dateCreate = rs.getTimestamp("t_create");
                dateToFactory = rs.getTimestamp("t_factory");
                dateToShipment = rs.getTimestamp("t_end");
                price = rs.getDouble("price");
                tempOrder = new RecordOrder(bigNumber, idDoc, idClient, idManager, durationTime, docNumber, posCount, client, manager,
                        dateCreate, dateToFactory, dateToShipment, price);
                listOrder.put(bigNumber, tempOrder);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
