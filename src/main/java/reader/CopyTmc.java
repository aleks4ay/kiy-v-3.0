package reader;

import tools.MyConst;

import java.io.IOException;
import java.sql.*;

public class CopyTmc {
    private static String url = MyConst.getURL();
    private static String user = MyConst.getUSER();
    private static String password = MyConst.getPASSWORD();

    public static void main(String[] args) {
        try {
            readAndWriteTmc();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readAndWriteTmc() throws ClassNotFoundException, SQLException, IOException {
        String dbfPath = MyConst.getDbfPath();
        Class.forName("com.hxtt.sql.dbf.DBFDriver");
        Connection conn1 = DriverManager.getConnection("jdbc:dbf:/" + dbfPath);

        Connection conn2 = DriverManager.getConnection(url, user, password);
        Class.forName("org.postgresql.Driver");
        conn2.setAutoCommit(false);
        PreparedStatement ps = null;

        try {
            String qry1 = "select ID, PARENTID, CODE, DESCR, ISFOLDER, SP276, SP277 from SC302;";

            Statement st = conn1.createStatement();
            ResultSet rs = st.executeQuery(qry1);
            while (rs.next()) {
                String id = rs.getString(1);
                String parentId = rs.getString(2);
                String code = rs.getString(3);
                String descr = rs.getString(4);
                int isFolder = rs.getInt(5);
                String descrAll = rs.getString(6);
                String type = rs.getString(7);


                if (descr != null) {
                    byte[] bytes = rs.getBytes(4);
                    descr = new String(bytes, "Windows-1251");
                }
                else {
                    descr = "-";
                }

                if (descrAll != null) {
                    byte[] bytes = rs.getBytes(6);
                    descrAll = new String(bytes, "Windows-1251");
                }
                else {
                    descrAll = "-";
                }

                ps = conn2.prepareStatement(
                            "INSERT INTO tmc (id, id_parent, code, descr, is_folder, descr_all, type) VALUES (?, ?, ?, ?, ?, ?, ?);");
                ps.setString(1, id);
                ps.setString(2, parentId);
                ps.setString(3, code);
                ps.setString(4, descr);
                ps.setInt(5, isFolder);
                ps.setString(6, descrAll);
                ps.setString(7, type);

                ps.addBatch();
                ps.executeBatch();

            }
            conn2.commit();
            rs.close();
            st.close();
        } finally {
            conn1.close();
            conn2.close();
        }

    }
}
