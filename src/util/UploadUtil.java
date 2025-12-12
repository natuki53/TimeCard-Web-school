package util;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.util.UUID;

/**
 * アップロード保存先やファイル名生成の共通処理
 */
public class UploadUtil {
    public static final long MAX_FILE_BYTES = 50L * 1024L * 1024L; // 50MB

    /**
     * 保存先ディレクトリを返す（存在しなければ作る）
     * 優先順位:
     * 1) catalina.base（Tomcat）配下
     * 2) webapp の /uploads 実体パス
     * 3) java.io.tmpdir
     */
    public static File getUploadDir(ServletContext ctx) {
        // Tomcatがある場合
        String catalinaBase = System.getProperty("catalina.base");
        File dir;
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            dir = new File(catalinaBase, "timecard_uploads");
        } else {
            String real = ctx != null ? ctx.getRealPath("/uploads") : null;
            if (real != null) {
                dir = new File(real);
            } else {
                dir = new File(System.getProperty("java.io.tmpdir"), "timecard_uploads");
            }
        }
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static boolean isAllowedMime(String mime) {
        if (mime == null) return false;
        String m = mime.toLowerCase();
        return m.startsWith("image/")
            || m.startsWith("video/")
            || m.startsWith("audio/")
            || "application/pdf".equals(m)
            || "application/zip".equals(m)
            || "application/x-zip-compressed".equals(m);
    }

    public static boolean isAllowedByExtension(String filename) {
        String ext = getExtension(filename);
        if (ext.isEmpty()) return false;
        // 画像
        if (".png".equals(ext) || ".jpg".equals(ext) || ".jpeg".equals(ext) || ".gif".equals(ext) || ".webp".equals(ext) || ".bmp".equals(ext)) return true;
        // 動画
        if (".mp4".equals(ext) || ".webm".equals(ext) || ".ogg".equals(ext) || ".mov".equals(ext)) return true;
        // 音声
        if (".mp3".equals(ext) || ".wav".equals(ext) || ".m4a".equals(ext) || ".aac".equals(ext) || ".ogg".equals(ext)) return true;
        // PDF / zip
        if (".pdf".equals(ext) || ".zip".equals(ext)) return true;
        return false;
    }

    /**
     * Content-Type が空/不明な時の補完用（プレビュー判定にも使う）
     */
    public static String guessMimeFromFileName(String filename) {
        String ext = getExtension(filename);
        if (ext.isEmpty()) return null;
        // 画像
        if (".png".equals(ext)) return "image/png";
        if (".jpg".equals(ext) || ".jpeg".equals(ext)) return "image/jpeg";
        if (".gif".equals(ext)) return "image/gif";
        if (".webp".equals(ext)) return "image/webp";
        if (".bmp".equals(ext)) return "image/bmp";
        // 動画
        if (".mp4".equals(ext)) return "video/mp4";
        if (".webm".equals(ext)) return "video/webm";
        if (".ogg".equals(ext)) return "video/ogg";
        if (".mov".equals(ext)) return "video/quicktime";
        // 音声
        if (".mp3".equals(ext)) return "audio/mpeg";
        if (".wav".equals(ext)) return "audio/wav";
        if (".m4a".equals(ext)) return "audio/mp4";
        if (".aac".equals(ext)) return "audio/aac";
        // PDF / zip
        if (".pdf".equals(ext)) return "application/pdf";
        if (".zip".equals(ext)) return "application/zip";
        return null;
    }

    public static String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "";
        String ext = filename.substring(idx).toLowerCase();
        // 拡張子が変すぎるのを抑制
        if (ext.length() > 10) return "";
        return ext;
    }

    public static boolean isZipFileName(String filename) {
        return ".zip".equalsIgnoreCase(getExtension(filename));
    }

    public static String newStoredFileName(String originalFilename) {
        String ext = getExtension(originalFilename);
        return UUID.randomUUID().toString().replace("-", "") + ext;
    }

    public static String safeDisplayName(String filename) {
        if (filename == null) return "";
        // パスっぽいものを落とす
        String s = filename.replace("\\", "/");
        int idx = s.lastIndexOf('/');
        if (idx >= 0) s = s.substring(idx + 1);
        return s;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1fKB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1fMB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1fGB", gb);
    }
}


