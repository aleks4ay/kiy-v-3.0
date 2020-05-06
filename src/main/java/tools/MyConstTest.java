package tools;

public class MyConstTest {
    private static String currentProfile = "";
    private static final String URL = "jdbc:postgresql://192.168.0.11:5432/kiyv?useSSL=false";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root8254";
//    private static String fileName = "\\\\SERVER-KIY-V\\ptc\\KiyVtime.txt";
//    private static String fileUserName = "\\\\SERVER-KIY-V\\ptc\\KiyVuser.txt";

//    private static final String serverPath = "F:\\1C Base\\Copy250106";
    private static final String dbfPath = "F:\\KiyV management\\DB_copy";

    public static String getCurrentProfile() {
        return currentProfile;
    }

    public static String getURL() {
        return URL;
    }

    public static String getUSER() {
        return USER;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

//    public static String getFileName() {
//        return fileName;
//    }

//    public static String getFileUserName() {
//        return fileUserName;
//    }

//    public static String getServerPath() {
//        return serverPath;
//    }

    public static String getDbfPath() {
        return dbfPath;
    }
}
