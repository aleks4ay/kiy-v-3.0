package reader;

import tools.MyConst;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

public class CopyClientBigData {
    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();
    private static String dbfPath = MyConst.getDbfPath();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        doCopyNewRecord(0);
//        doCopyNewRecord(52000);

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)((end-start)/1000) + " c." );
    }

    public static void doCopyNewRecord(int limit) throws ClassNotFoundException, SQLException, IOException {
        System.out.println("start write  'C L I E N T S'");

        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);

        String id, idParent, descr1, descr2, phone, inn, numCertif, addrFis, addrUrid;
        int isFolder;

        try {
//            conn2.createStatement().execute("DELETE FROM clients;");

//            conn2.setAutoCommit(false);
            PreparedStatement ps = null;

            Statement st = conn1.createStatement();
            ResultSet rs1 = st.executeQuery("select * from SC172;");
//            Statement st2 = conn2.createStatement();


            Map<String, String> listClient = new TreeMap<>();
            readOldClient(listClient);

            while (rs1.next()) {
                id = rs1.getString("ID");
                idParent = rs1.getString("PARENTID");
                isFolder = rs1.getInt("ISFOLDER");

                descr1 = rs1.getString("DESCR");
                if (descr1 != null) {
                    byte[] bytes = rs1.getBytes("DESCR");
                    descr1 = new String(bytes, "Windows-1251");
                } else {
                    descr1 = "-";
                }

                descr2 = rs1.getString("SP162");
                if (descr2 != null) {
                    byte[] bytes = rs1.getBytes("SP162");
                    descr2 = new String(bytes, "Windows-1251");
                } else {
                    descr2 = "-";
                }

                phone = rs1.getString("SP165");
                if (phone != null) {
                    byte[] bytes = rs1.getBytes("SP165");
                    phone = new String(bytes, "Windows-1251");
                } else {
                    phone = "-";
                }

                inn = rs1.getString("SP156");
                numCertif = rs1.getString("SP161");
                addrFis = rs1.getString("SP166");
                if (addrFis != null) {
                    byte[] bytes = rs1.getBytes("SP166");
                    addrFis = new String(bytes, "Windows-1251");
                } else {
                    addrFis = "-";
                }

                addrUrid = rs1.getString("SP167");
                if (addrUrid != null) {
                    byte[] bytes = rs1.getBytes("SP167");
                    addrUrid = new String(bytes, "Windows-1251");
                } else {
                    addrUrid = "-";
                }

                if (listClient.containsKey(id)) {
                    continue;
                }
                else {
                    ps = conn2.prepareStatement(
                            "INSERT INTO clients VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
                    ps.setString(1, id);
                    ps.setString(2, idParent);
                    ps.setInt(3, isFolder);
                    ps.setString(4, descr1);
                    ps.setString(5, descr2);
                    ps.setString(6, phone);
                    ps.setString(7, inn);
                    ps.setString(8, numCertif);
                    ps.setString(9, addrFis);
                    ps.setString(10, addrUrid);
//                ps.addBatch();
//                ps.executeBatch();
                    ps.execute();
                }
            }
//            conn2.commit();
            rs1.close();
            st.close();
        } finally {
            conn1.close();
            conn2.close();
        }
    }

    public static void readOldClient (Map<String, String> listClients) {

        String id, client;

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select id, descr1 from clients;");

            while (rs.next()) {
                id = rs.getString(1);
                client = rs.getString(2);
                listClients.put(id, client);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
