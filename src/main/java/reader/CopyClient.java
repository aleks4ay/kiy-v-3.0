package reader;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

import tools.MyConst;

public class CopyClient {
    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

//        MyConst m = new MyConst();
        doCopyNewRecord();

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)(end-start) + " mc." );
    }

    public static void doCopyNewRecord() throws ClassNotFoundException, SQLException, IOException {
        System.out.println("start write  'C L I E N T S'");

        Map<String, String> mapClient = new TreeMap<>();

        readOldClient(mapClient);
        String dbfPath = MyConst.getDbfPath();
        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Connection conn2 = DriverManager.getConnection(url, user, password);
        Class.forName("org.postgresql.Driver");
        conn2.setAutoCommit(false);
        PreparedStatement ps = null;
        String id, name;
        try {
            Statement st = conn1.createStatement();
            ResultSet rs1 = st.executeQuery("select ID, DESCR from SC172;");

            while (rs1.next()) {
                id = rs1.getString(1);
                name = rs1.getString(2);
                if (name != null) {
                    byte[] bytes = rs1.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                } else {
                    name = "-";
                }

                if (mapClient.containsKey(id)) {
                    if (mapClient.get(id).equalsIgnoreCase(name)) {
                        continue;
                    }
                    else {
                        ps = conn2.prepareStatement(
                                "UPDATE clients SET descr = ? WHERE id = ?;");
                        ps.setString(1, name);
                        ps.setString(2, id);
                        ps.addBatch();
                        ps.executeBatch();
                    }
                }
                else {
                    ps = conn2.prepareStatement(
                            "INSERT INTO clients (id, descr) VALUES (?, ?);");
                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.addBatch();
                    ps.executeBatch();
                }
            }
            conn2.commit();
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

            ResultSet rs = st.executeQuery("select id, descr from clients;");

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
