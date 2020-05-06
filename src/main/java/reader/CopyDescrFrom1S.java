package reader;

import box.RecordDescr;
import tools.DateConverter;
import tools.MyConst;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class CopyDescrFrom1S {

    private static String dbfPath = MyConst.getDbfPath(); //"\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy";

    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        readDescriptionFrom1S();

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)(end-start)/1000 + " c." );
    }


    public static void readDescrTmc(Map<String, String> tmc, Map<String, String> embodiment)
                                    throws ClassNotFoundException, SQLException, IOException {


        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);
        try {

            Statement st = conn1.createStatement();
            ResultSet rs1 = st.executeQuery("select ID, DESCR from SC302;");
            while (rs1.next()) {
                String id = rs1.getString(1);
                String descr = rs1.getString(2);

                if (descr != null) {
                    byte[] bytes = rs1.getBytes(2);
                    descr = new String(bytes, "Windows-1251");
                }
                else {
                    descr = "";
                }
                tmc.put(id, descr);
            }
            ResultSet rs2 = st.executeQuery("select ID, DESCR from SC14716;");
            while (rs2.next()) {
                String id = rs2.getString(1);
                String descr = rs2.getString(2);

                if (descr != null) {
                    byte[] bytes = rs2.getBytes(2);
                    descr = new String(bytes, "Windows-1251");
                }
                else {
                    descr = "";
                }
                embodiment.put(id, descr);
            }
            rs1.close();
            rs2.close();
            st.close();
        } finally {
            conn1.close();
        }
    }





    public static void readDescriptionFrom1S() throws ClassNotFoundException, SQLException {

        long start = System.currentTimeMillis();

        System.out.print("write  'D E S C R I P T I O N S', ");

        List<Integer> listStatus = new ArrayList<>();
        Map<Integer, RecordDescr> listDescr = new TreeMap<>();
        readOldDescr(listDescr);
        readOldStatusNumber(listStatus);
//        System.out.println(listDescr.size());
        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);
//        conn2.setAutoCommit(false);
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        String idDoc, idTmc, descrFirst, descrSecond, embodiment;
        int position, bigNumber, kod, amount, sizeA, sizeB, sizeC, typeIndex;
//        long timeToFactory;
        boolean needChangeDescr;
        boolean needInsertDescr;
        boolean needChangeStatuses;
        boolean needInsertStatuses;

        try {
            Statement st1 = conn1.createStatement();
            Statement st2 = conn2.createStatement();

            ResultSet rs = st2.executeQuery("SELECT iddoc, big_number, t_factory from orders;");
            Map<String, Integer> listBigNumbers = new TreeMap<>();
            Map<String, Long> listDateToFactory = new TreeMap<>();
            while (rs.next()) {
                listBigNumbers.put(rs.getString("iddoc"), rs.getInt("big_number"));
                listDateToFactory.put(rs.getString("iddoc"), rs.getTimestamp("t_factory").getTime());
            }

            rs = st2.executeQuery("SELECT id FROM set_technologichka");
            List<String> listTechn = new ArrayList<>();
            while (rs.next()) {
                listTechn.add(rs.getString(1));
            }

            Map<String, String> tmcDescr = new TreeMap<>();
            Map<String, String> embodimentDescr = new TreeMap<>();

            readDescrTmc(tmcDescr, embodimentDescr);

            rs = st1.executeQuery("select IDDOC, LINENO, SP1902, SP1905, SP14676, SP14686, SP14687, SP14688, SP14717 from DT1898;");

            while (rs.next()) {
                needChangeDescr = false;
                needInsertDescr = false;
                needChangeStatuses = false;
                needInsertStatuses = false;

                idDoc = rs.getString("IDDOC");
                if (! listBigNumbers.containsKey(idDoc)) {
                    continue;
                }

                position = rs.getInt("LINENO");
                bigNumber = listBigNumbers.get(idDoc);
                kod = bigNumber * 100 + position;
                idTmc = rs.getString("SP1902");
                if (idTmc.equalsIgnoreCase("   CBN")) {  //Go designer to size measurement
                    continue;
                }
                if (idTmc.equalsIgnoreCase("   7LH")) { //Shipment
                    continue;
                }
                if (idTmc.equalsIgnoreCase("   9VQ")) { //Fixing
                    continue;
                }
                amount = rs.getInt("SP1905");
                descrSecond = rs.getString("SP14676");
                sizeA = rs.getInt("SP14686");
                sizeB = rs.getInt("SP14687");
                sizeC = rs.getInt("SP14688");
                embodiment = rs.getString("SP14717");
                typeIndex = 0;
//                    timeToFactory = listDateToFactory.get(idDoc);

                if (descrSecond != null) {
                    byte[] bytes = rs.getBytes("SP14676");
                    descrSecond = new String(bytes, "Windows-1251");
                } else {
                    descrSecond = "";
                }

                descrFirst = tmcDescr.get(idTmc) + " ";
                if ( ! embodiment.trim().equals("0")) {
                    descrFirst += embodimentDescr.get(embodiment) + " ";
                }
                int statusIndex = 0;
                int isTechnologichka = 0;
                for (String tmcItem : listTechn) {
                    if ( tmcItem.equalsIgnoreCase(idTmc)/* && descrSecond == null*/) {
                        isTechnologichka = 1;
                        typeIndex = 3;
//                        statusIndex = 7;
                        break;
                    }
                }
                RecordDescr newDescription = new RecordDescr(kod, bigNumber, idDoc, position, idTmc, amount, descrSecond,
                        sizeA, sizeB, sizeC, embodiment);

                if (listDescr.isEmpty()) {
                    needInsertDescr = true;
                }
                else if (! listDescr.containsKey(kod)) {
                    needInsertDescr = true;
                }
                else if (! listDescr.get(kod).compareTo(newDescription)) {
                    needChangeDescr = true;
                }

                if (listStatus.isEmpty()) {
                    needInsertStatuses = true;
                }
                else if (! listStatus.contains(kod)) {
                    needInsertStatuses = true;
                }
                else if (listStatus.contains(kod) & (needChangeDescr | needInsertDescr) ) {
                    needChangeStatuses = true;
                }



                if (needInsertDescr) {
//                        System.out.println("Number " + kod + " don't found.");
                    ps1 = conn2.prepareStatement("INSERT INTO descriptions (kod, big_number, iddoc, pos, id_tmc, amount, " +
                            "descr_second, size_a, size_b, size_c, embodiment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    ps1.setInt(1, kod);
                    ps1.setInt(2, bigNumber);
                    ps1.setString(3, idDoc);
                    ps1.setInt(4, position);
                    ps1.setString(5, idTmc);
                    ps1.setInt(6, amount);
                    ps1.setString(7, descrSecond);
                    ps1.setInt(8, sizeA);
                    ps1.setInt(9, sizeB);
                    ps1.setInt(10, sizeC);
                    ps1.setString(11, embodiment);
//                        ps1.addBatch();
                    ps1.execute();
                }

                if (needInsertStatuses) {
                    ps2 = conn2.prepareStatement("INSERT INTO statuses (kod, iddoc, time_0, time_1, type_index, status_index, is_technologichka, " +
                            "descr_first)  VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                    ps2.setInt(1, kod);
                    ps2.setString(2, idDoc);
                    ps2.setLong(3, listDateToFactory.get(idDoc));
                    ps2.setLong(4, DateConverter.getNowDate());
                    ps2.setInt(5, typeIndex);
                    ps2.setInt(6, statusIndex);
                    ps2.setInt(7, isTechnologichka);
                    ps2.setString(8, descrFirst);
//                        ps2.addBatch();
                    ps2.execute();
                }

                if (needChangeDescr) {
//                        System.out.println("don't match: " + kod);
                    ps1 = conn2.prepareStatement("UPDATE descriptions SET big_number = ?, iddoc = ?, pos = ?, id_tmc = ?, amount = ?, " +
                            "descr_second = ?, size_a = ?, size_b = ?, size_c = ?, embodiment = ? WHERE kod = ?;");
                    ps1.setInt(1, bigNumber);
                    ps1.setString(2, idDoc);
                    ps1.setInt(3, position);
                    ps1.setString(4, idTmc);
                    ps1.setInt(5, amount);
                    ps1.setString(6, descrSecond);
                    ps1.setInt(7, sizeA);
                    ps1.setInt(8, sizeB);
                    ps1.setInt(9, sizeC);
                    ps1.setString(10, embodiment);
                    ps1.setInt(11, kod);

//                        ps1.addBatch();
                    ps1.execute();
//                        conn2.commit();
                }
                if (needChangeStatuses) {
                    ps2 = conn2.prepareStatement("UPDATE statuses SET iddoc = ?, time_0 = ?, time_1 = ?, type_index = ?, status_index = ?, " +
                            "is_technologichka = ?, descr_first = ? WHERE kod = ?;");
                    ps2.setString(1, idDoc);
                    ps2.setLong(2, listDateToFactory.get(idDoc));
                    ps2.setLong(3, DateConverter.getNowDate());
                    ps2.setInt(4, typeIndex);
                    ps2.setInt(5, statusIndex);
                    ps2.setInt(6, isTechnologichka);
                    ps2.setString(7, descrFirst);
                    ps2.setInt(8, kod);
//                        ps2.addBatch();
                    ps2.execute();
                }


            }
//            conn2.commit();
            rs.close();
            st1.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            conn1.close();
            conn2.close();
        }

        long end = System.currentTimeMillis();
        System.out.println("t = " + (double)(end-start)/1000 + " c" );

    }

    public static void readOldDescr (Map<Integer, RecordDescr> listDescr) {

        int kod, bigNumber, position, amount, sizeA, sizeB, sizeC;
        String idDoc, idTmc,  descrSecond, embodyment;

        RecordDescr tempDescr;

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select * from descriptions;");

            while (rs.next()) {
                kod = rs.getInt("kod");
                bigNumber = rs.getInt("big_number");
                position = rs.getInt("pos");
                amount = rs.getInt("amount");
                sizeA = rs.getInt("size_a");
                sizeB = rs.getInt("size_b");
                sizeC = rs.getInt("size_c");

                idDoc = rs.getString("iddoc");
                idTmc = rs.getString("id_tmc");
                descrSecond = rs.getString("descr_second");
                embodyment = rs.getString("embodiment");

                tempDescr = new RecordDescr(kod, bigNumber, idDoc, position, idTmc, amount, /*descrFirst,*/
                        descrSecond, sizeA, sizeB, sizeC, embodyment);
                listDescr.put(kod, tempDescr);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readOldStatusNumber (List<Integer> listStatus) {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select kod from statuses;");

            while (rs.next()) {
                listStatus.add(rs.getInt("kod"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
