package reader;

import tools.MyConst;

import java.io.IOException;
import java.sql.*;
import java.util.Set;
import java.util.TreeSet;

public class ReaderTechnoProduct {

    //        String dbfPath = "d:\\_Backup Project\\1S_Copy250106";
    private static String dbfPath = MyConst.getDbfPath(); //"\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy\\test";
//    private static String dbfPath = "\\\\SERVER-KIY-V\\User\\Konstruktor_Sergienko\\_KIY-V_1.3_\\DB_copy";

    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        doCopyNewRecord();
    }

    public static void doCopyNewRecord() {
        long start = System.currentTimeMillis();
        System.out.print("write  'T M C', ");

        Set<String> folders = new TreeSet<>();
        Set<String> items = new TreeSet<>();
        folders.add("    19");
        try {
            readListTmc(folders, items);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writeDescrTmcToDB(items);
//            deleteSomePosition();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("t = " + (double)(end-start)/1000 + " c" );
    }

    public static void writeDescrTmcToDB(Set<String> set) throws ClassNotFoundException, SQLException, IOException {
        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Class.forName("org.postgresql.Driver");
        Connection conn2 = DriverManager.getConnection(url, user, password);
        conn2.setAutoCommit(false);
        PreparedStatement ps2 = null;

        try {

            Statement st = conn1.createStatement();
            if (set.size() > 0) {
                Statement st2 = conn2.createStatement();
                st2.execute("DELETE FROM set_technologichka;");
                for (String s : set) {
                    ResultSet rs1 = st.executeQuery("select ID, DESCR, PARENTID from SC302 WHERE ID = '" + s + "';");
                    while (rs1.next()) {
                        String id = rs1.getString(1);
                        String descr = rs1.getString(2);
                        String parentId = rs1.getString(3);

                        if (descr != null) {
                            byte[] bytes = rs1.getBytes(2);
                            descr = new String(bytes, "Windows-1251");
                        }
                        else {
                            descr = "";
                        }

                        ps2 = conn2.prepareStatement("INSERT INTO set_technologichka (id, parentid, descr) VALUES (?, ?, ?)");
                        ps2.setString(1, id);
                        ps2.setString(2, parentId);
                        ps2.setString(3, descr);
                        ps2.addBatch();
                        ps2.executeBatch();

                    }
                    rs1.close();
                }
                conn2.commit();

                st.close();
            }
        } finally {
            conn1.close();
            conn2.close();
        }

    }

    public static void deleteSomePosition() throws ClassNotFoundException, SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(url, user, password); Statement st = conn.createStatement()) {
            Class.forName("org.postgresql.Driver");

            st.execute("Delete FROM set_technologichka WHERE descr LIKE '%Стіл%' OR descr LIKE '%Стелаж%' OR descr LIKE '%Мийка%' " +
                    "OR descr LIKE '%Станція бармена%' OR descr LIKE '%Полка%';");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Set<String> readListTmc(Set<String> folders, Set<String> items) throws ClassNotFoundException, SQLException, IOException {
        Set<String> newFolders = new TreeSet<>();

        if (folders.size() == 0) {
            return items;
        }
        else {
            Class.forName("com.hxtt.sql.dbf.DBFDriver");
            Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

            try {

                Statement st = conn1.createStatement();

                for (String s : folders) {
                    ResultSet rs1 = st.executeQuery("select ID, PARENTID, ISFOLDER from SC302 WHERE PARENTID = '" + s + "';");
                    while (rs1.next()) {
                        int isfolder = rs1.getInt(3);

                        if (isfolder == 2) {
                            items.add(rs1.getString(1));
                        } else {
                            newFolders.add(rs1.getString(1));
                        }
                    }
                    rs1.close();
                }

                st.close();
            } finally {
                conn1.close();
            }
            return readListTmc(newFolders, items);
        }

    }
}
