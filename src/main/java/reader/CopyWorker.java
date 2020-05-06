package reader;

import tools.MyConst;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

public class CopyWorker {
    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        long start = System.currentTimeMillis();

        doCopyNewRecord();

        long end = System.currentTimeMillis();
        System.out.println("time = " + (double)(end-start) + " mc." );
    }

    public static void doCopyNewRecord() throws ClassNotFoundException, SQLException, IOException {
        System.out.println("start write  'W O R K E R S'");

        Map<String, String> listWorker = new TreeMap<>();

        readOldWorker(listWorker);

//        System.out.println(listWorker.size());

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
            ResultSet rs1 = st.executeQuery("select ID, DESCR from SC1670;");
//            ---------------------------
            while (rs1.next()) {
                id = rs1.getString(1);
                name = rs1.getString(2);
                if (name != null) {
                    byte[] bytes = rs1.getBytes(2);
                    name = new String(bytes, "Windows-1251");
                } else {
                    name = "-";
                }

                if (listWorker.containsKey(id)) {
                    if (listWorker.get(id).equalsIgnoreCase(name)) {
                        continue;
                    }
                    else {
//                        System.out.println("id = " + id + " [" + listWorker.get(id) + " -->  " + name + "]");
                        ps = conn2.prepareStatement("UPDATE workers SET name = ? WHERE id = ?;");
                        ps.setString(1, name);
                        ps.setString(2, id);
                        ps.addBatch();
                        ps.executeBatch();
//                        conn2.commit();
                    }
                }
                else {
                    ps = conn2.prepareStatement(
                            "INSERT INTO workers (id, name) VALUES (?, ?);");
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

    public static void readOldWorker (Map<String, String> listWorker) {

        String id, worker;


        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            ResultSet rs = st.executeQuery("select id, name from workers;");

            while (rs.next()) {
                id = rs.getString(1);
                worker = rs.getString(2);
                listWorker.put(id, worker);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
