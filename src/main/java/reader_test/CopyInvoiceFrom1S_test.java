package reader_test;

import box.RecordInvoice;
import tools.MyConstTest;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public final class CopyInvoiceFrom1S_test {

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        readInvoiceFrom1S();
//        doUpdate();
    }

    private static String dbfPath = MyConstTest.getDbfPath(); //"\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy";

    private static String url = MyConstTest.getURL();
    private static String user = MyConstTest.getUSER();
    private static String password = MyConstTest.getPASSWORD();


    public static void readInvoiceFrom1S() throws ClassNotFoundException, SQLException, IOException {
        long start = System.currentTimeMillis();
        System.out.print("write  'I N V O I C E', ");
        Map<String, RecordInvoice> listInvoice = new TreeMap<>();

        Set<String> setOrderChange = new TreeSet<>();
        readOldInvoice(listInvoice);

        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);
        conn2.setAutoCommit(false);
        PreparedStatement ps2 = null;

        String idOrder, idDoc, docNum;
        long time22;
        double newPayment;
        boolean needInsert = false;
        boolean needChange = false;

        RecordInvoice tempInvoice;

        try {
            Statement st = conn1.createStatement();
            Statement st2 = conn2.createStatement();
            Set<String> setIdOrders = new TreeSet<>();
            ResultSet resultSet = st2.executeQuery("SELECT iddoc FROM kiyv.test.orders_test;");
            while (resultSet.next()) {
                setIdOrders.add(resultSet.getString("iddoc"));
            }

            ResultSet rs = st.executeQuery("SELECT j.IDDOC, j.DOCNO, j.DATE, i.SP3561, i.SP3589 from 1SJOURN j, DH3592 i WHERE i.IDDOC = j.IDDOC AND YEAR (j.DATE) > 2018 AND j.CLOSED <> 4 AND i.SP3561 <> '   0     0';");


            while (rs.next()) {
                needInsert = false;
                needChange = false;
                time22 = rs.getDate("DATE").getTime();
                idOrder = rs.getString("SP3561").substring(4);
                idDoc = rs.getString("IDDOC");
                byte[] bytes = rs.getBytes("DOCNO");
                docNum = new String(bytes, "Windows-1251");
                newPayment = rs.getDouble("SP3589");
                tempInvoice = new RecordInvoice(idDoc, docNum, idOrder, time22, newPayment);

                if (! setIdOrders.contains(idOrder)) {
                    continue;
                }
                if (listInvoice.isEmpty() ) { // TODO: 28.05.2019
                    needInsert = true;
                }
                else if (listInvoice.get(idDoc) != null){
                    if ( ! listInvoice.get(idDoc).compareToInvoice(tempInvoice)){
                        needChange = true;
                    }
                    else continue;
                }
                else if (listInvoice.get(idDoc) == null){
                    needInsert = true;
                }

                if (needChange) {

                    ps2 = conn2.prepareStatement("UPDATE kiyv.test.invoice_test SET  docno = ?, id_order = ?, time_invoice = ?, time22 = ?, price = ? WHERE iddoc = ?;");
                    ps2.setString(1, docNum);
                    ps2.setString(2, idOrder);
                    ps2.setTimestamp(3, new Timestamp(time22));
                    ps2.setLong(4, time22);
                    ps2.setDouble(5, newPayment);
                    ps2.setString(6, idDoc);

                    ps2.addBatch();
                    ps2.executeBatch();

                    setOrderChange.add(idOrder);
                }

                if (needInsert) {
                    ps2 = conn2.prepareStatement("INSERT INTO kiyv.test.invoice_test (iddoc, docno, id_order, time_invoice, time22, price) VALUES (?, ?, ?, ?, ?, ?);");
                    ps2.setString(1, idDoc);
                    ps2.setString(2, docNum);
                    ps2.setString(3, idOrder);
                    ps2.setTimestamp(4, new Timestamp(time22));
                    ps2.setLong(5, time22);
                    ps2.setDouble(6, newPayment);

                    ps2.addBatch();
                    ps2.executeBatch();

                    setOrderChange.add(idOrder);
                }

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

        if ( ! setOrderChange.isEmpty()) {
            changeOrderPayment(setOrderChange);
        }
        doUpdate();
        long end = System.currentTimeMillis();
        System.out.println("t = " + (double)(end-start)/1000 + " c" );
    }

    public static void readOldInvoice(Map<String, RecordInvoice> listInvoce) {
        String idDoc, docNum, idOrder;
        Long time22;
        double price;

        RecordInvoice tempInvoice;

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select * from kiyv.test.invoice_test;");

            while (rs.next()) {
                idDoc = rs.getString("iddoc");
                docNum = rs.getString("docno");
                idOrder = rs.getString("id_order");
                time22 = rs.getLong("time22");
                price = rs.getDouble("price");

                tempInvoice = new RecordInvoice(idDoc, docNum, idOrder, time22, price);
                listInvoce.put(idDoc, tempInvoice);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void changeOrderPayment(Set<String> numOrder) {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");
//            System.out.println("start changeOrderPayment");
            PreparedStatement ps1 = null;
            PreparedStatement ps2 = null;

            double payment = 0.0;
            long time22 = 0L;
            String docNumInvoice;

            for (String idOrder : numOrder) {
                ps1 = conn.prepareStatement("SELECT SUM(price), MAX(time22), MAX(docno) FROM kiyv.test.invoice_test WHERE id_order = ?;");
                ps1.setString(1, idOrder);
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    payment = rs1.getDouble(1);
                    time22 = rs1.getLong(2);
                    docNumInvoice = rs1.getString(3);
//                    System.out.println("idDoc = " + idOrder +  ",  payment = " + payment);

                    ps2 = conn.prepareStatement("UPDATE kiyv.test.orders_test SET  payment = ?, docno_invoice = ? WHERE iddoc = ?;");
                    ps2.setDouble(1, payment);
                    ps2.setString(2, docNumInvoice);
                    ps2.setString(3, idOrder);
                    ps2.execute();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void doUpdate() {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");
//            System.out.println("start changeOrderPayment");

            st.execute("UPDATE kiyv.test.orders_test SET time22 = invoice_test.time22 FROM kiyv.test.invoice_test WHERE orders_test.iddoc = invoice_test.id_order AND orders_test.payment >= orders_test.price - 0.9 AND orders_test.time22 ISNULL;");
            st.execute("UPDATE kiyv.test.orders_test SET time_invoice = invoice_test.time_invoice FROM kiyv.test.invoice_test WHERE orders_test.iddoc = invoice_test.id_order AND orders_test.time22 NOTNULL AND orders_test.time_invoice ISNULL;");
            st.execute("UPDATE kiyv.test.statuses_test SET time_22 = time22 FROM kiyv.test.orders_test " +
                    "WHERE orders_test.iddoc = statuses_test.iddoc and orders_test.time22 NOTNULL;");
            st.execute("UPDATE kiyv.test.statuses_test SET status_index = 22 WHERE time_22 NOTNULL and status_index < 22 ;");
            st.execute("UPDATE kiyv.test.orders_test SET docno_invoice = invoice_test.docno FROM kiyv.test.invoice_test WHERE orders_test.iddoc = invoice_test.id_order AND orders_test.docno_invoice ISNULL;");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}