package reader;

import box.RecordManuf;
import tools.MyConst;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class CopyManufactureFrom1S {

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        readManufactureFrom1S();
//        doUpdate();

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)(end-start)/1000 + " c." );
    }

    private static String dbfPath = MyConst.getDbfPath();

    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();


    public static void readManufactureFrom1S() throws ClassNotFoundException, SQLException, IOException {
        long start = System.currentTimeMillis();
        System.out.print("write  'M A N U F A C T U R E', ");

        Map<String, RecordManuf> listManuf = new TreeMap<>();

        readOldManufacture (listManuf);

        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);
//        conn2.setAutoCommit(false);
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;

        String idDoc, docNumber, idOrder, key;
        int pos;
        long time21;

        String idTmc, descrSecond, embodiment;
        int amount, sizeA, sizeB, sizeC, typeIndex;

        RecordManuf tempManuf;

        try {
            Statement st = conn1.createStatement();
            Statement st2 = conn2.createStatement();
            Set<String> setIdOrders = new TreeSet<>();
            ResultSet resultSet = st2.executeQuery("SELECT iddoc FROM orders;");
            while (resultSet.next()) {
                setIdOrders.add(resultSet.getString("iddoc"));
            }
//            System.out.println("setIdOrders.size() = " + setIdOrders.size());

            ResultSet rs = st.executeQuery("SELECT j.IDDOC, j.DOCNO, j.DATE, m.SP2722, m.LINENO, m.SP2725, m.SP2721, m.SP14726, m.SP14722, m.SP14723," +
                    " m.SP14724, m.SP14725 from 1SJOURN j, DT2728 m WHERE m.IDDOC = j.IDDOC AND YEAR (j.DATE) > 2018 AND m.SP2722 <> '     0' AND j.CLOSED <> 4;");

            while (rs.next()) {
                idDoc = rs.getString("IDDOC");
                byte[] bytes1 = rs.getBytes("DOCNO");
                docNumber = new String(bytes1, "Windows-1251");

                time21 = rs.getDate("DATE").getTime();
                idOrder = rs.getString("SP2722");
                pos = rs.getInt("LINENO");
                amount = rs.getInt("SP2725");

                idTmc = rs.getString("SP2721");
                descrSecond = rs.getString("SP14726");
                sizeA = rs.getInt("SP14722");
                sizeB = rs.getInt("SP14723");
                sizeC = rs.getInt("SP14724");
                embodiment = rs.getString("SP14725");

                if (descrSecond != null) {
                    byte[] bytes2 = rs.getBytes("SP14726");
                    descrSecond = new String(bytes2, "Windows-1251");
                } else {
                    descrSecond = "";
                }

                if (! setIdOrders.contains(idOrder)) {
                    continue;
                }

                tempManuf = new RecordManuf(0, idDoc, pos, docNumber, idOrder, time21, amount, idTmc,
                        descrSecond, sizeA, sizeB, sizeC, embodiment);
                key = idDoc + "-" + pos;

                if (listManuf.containsKey(key)) {
                    if ( ! listManuf.get(key).compareTo(tempManuf)) {
                        int id = listManuf.get(key).id;

                        ps2 = conn2.prepareStatement("UPDATE manufacture SET iddoc = ?, position = ?, docno = ?, " +
                                "id_order = ?, time_manuf = ?, time21 = ?, amount = ?, id_tmc = ?, descr_second = ?, " +
                                "size_a = ?, size_b = ?, size_c = ?, embodiment = ? WHERE id = ?;");
                        ps2.setString(1, idDoc);
                        ps2.setInt(2, pos);
                        ps2.setString(3, docNumber);
                        ps2.setString(4, idOrder);
                        ps2.setTimestamp(5, new Timestamp(time21));
                        ps2.setLong(6, time21);
                        ps2.setInt(7, amount);
                        ps2.setString(8, idTmc);
                        ps2.setString(9, descrSecond);
                        ps2.setInt(10, sizeA);
                        ps2.setInt(11, sizeB);
                        ps2.setInt(12, sizeC);
                        ps2.setString(13, embodiment);
                        ps2.setInt(14, id);
                        ps2.execute();
//                        ps2.addBatch();
//                        ps2.executeBatch();
                    }
                }
                else {
                    ps2 = conn2.prepareStatement("INSERT INTO manufacture (iddoc, position, docno, id_order, time_manuf, time21, amount, id_tmc, descr_second, " +
                            "size_a, size_b, size_c, embodiment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setString(1, idDoc);
                    ps2.setInt(2, pos);
                    ps2.setString(3, docNumber);
                    ps2.setString(4, idOrder);
                    ps2.setTimestamp(5, new Timestamp(time21));
                    ps2.setLong(6, time21);
                    ps2.setInt(7, amount);
                    ps2.setString(8, idTmc);
                    ps2.setString(9, descrSecond);
                    ps2.setInt(10, sizeA);
                    ps2.setInt(11, sizeB);
                    ps2.setInt(12, sizeC);
                    ps2.setString(13, embodiment);
                    ps2.execute();
//                    ps2.addBatch();
//                    ps2.executeBatch();
//                    conn2.commit();

                   /* ps3 = conn2.prepareStatement("SELECT kod FROM manufacture_view WHERE iddoc = ? AND pos = ?;");
                    ps3.setString(1, idDoc);
                    ps3.setInt(2, pos);
                    ResultSet rs3 = ps3.executeQuery();*/
//                    conn2.commit();
/*                    while (rs3.next()) {
                        int kod = rs3.getInt("kod");
                        st2.execute("UPDATE statuses SET status_index = 21 WHERE kod = '" + kod + "' AND status_index < 21;");
                        st2.execute("UPDATE statuses SET time_21 = " + time21 + " WHERE kod = '" + kod + "' ;");
                        conn2.commit();
                    }*/

                }


            }
//            conn2.commit();
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            conn1.close();
            conn2.close();
        }

        doUpdate();

        long end = System.currentTimeMillis();
        System.out.println("t = " + (double)(end-start)/1000 + " c" );
    }

    private static void doUpdate() {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st2 = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");
//            System.out.println("start changeOrderPayment");

//            st2.execute("UPDATE orders SET time21 = time21 FROM invoice WHERE orders.iddoc = invoice.id_order AND orders.payment >= orders.price - 0.9 AND orders.time22 ISNULL;");
            st2.execute("UPDATE orders SET time_manuf = manufacture.time_manuf FROM manufacture WHERE orders.iddoc = manufacture.id_order AND orders.time_manuf ISNULL;");  //AND manufacture.time_manuf NOTNULL
            st2.execute("UPDATE statuses SET time_21 = time21 FROM manufacture " +
                    "WHERE manufacture.id_order = statuses.iddoc and statuses.time_21 ISNULL;");
            st2.execute("UPDATE statuses SET status_index = 21 WHERE time_21 NOTNULL and status_index < 21 ;");

            st2.execute("UPDATE orders SET docno_manuf = manufacture.docno FROM manufacture WHERE orders.iddoc = manufacture.id_order AND orders.docno_manuf ISNULL;");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readOldManufacture (Map<String, RecordManuf> listManuf) {

        String idDoc, docNumber, idOrder, key, idTmc,  descrSecond, embodiment;
        int id, pos, amount, sizeA, sizeB, sizeC;
        long time21;

        RecordManuf tempManuf;

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select * from manufacture ORDER BY iddoc, position;");

            while (rs.next()) {
                id = rs.getInt("id");
                pos = rs.getInt("position");
                idDoc = rs.getString("iddoc");
                docNumber = rs.getString("docno");

                idOrder = rs.getString("id_order");
                time21 = rs.getLong("time21");

                amount = rs.getInt("amount");
                sizeA = rs.getInt("size_a");
                sizeB = rs.getInt("size_b");
                sizeC = rs.getInt("size_c");

                idTmc = rs.getString("id_tmc");
                descrSecond = rs.getString("descr_second");
                embodiment = rs.getString("embodiment");

                tempManuf = new RecordManuf(id, idDoc, pos, docNumber, idOrder, time21, amount, idTmc, descrSecond, sizeA, sizeB, sizeC, embodiment);
                key = idDoc + "-" + pos;
                listManuf.put(key, tempManuf);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
