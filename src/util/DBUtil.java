package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * データベース接続ユーティリティクラス
 */
public class DBUtil {
    private static final String PROPERTIES_FILE = "database.properties";
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;

    static {
        try {
            // プロパティファイルを読み込む
            Properties props = new Properties();
            InputStream is = DBUtil.class.getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILE);
            
            if (is == null) {
                throw new RuntimeException("データベース設定ファイルが見つかりません: " + PROPERTIES_FILE);
            }
            
            props.load(is);
            dbDriver = props.getProperty("db.driver");
            dbUrl = props.getProperty("db.url");
            dbUsername = props.getProperty("db.username");
            dbPassword = props.getProperty("db.password");
            
            // JDBCドライバをロード
            Class.forName(dbDriver);
            
            is.close();
        } catch (Exception e) {
            throw new RuntimeException("データベース初期化エラー", e);
        }
    }

    /**
     * データベース接続を取得
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    /**
     * 接続を閉じる
     * @param conn Connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

