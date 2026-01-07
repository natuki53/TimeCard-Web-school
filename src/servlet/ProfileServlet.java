package servlet;

import dao.UserDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.User;
import util.AuthUtil;
import util.UploadUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * プロフィール編集
 * - 表示名/自己紹介/アイコン変更（アイコンはトリミング済みPNGも受付）
 */
@WebServlet("/profile")
@MultipartConfig(
        maxFileSize = 5L * 1024L * 1024L,       // 5MB
        maxRequestSize = 6L * 1024L * 1024L     // 6MB
)
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 最新情報を反映
        UserDAO userDAO = new UserDAO();
        User fresh = userDAO.findById(loginUser.getId());
        if (fresh == null) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        session.setAttribute("loginUser", fresh);

        // メッセージ
        String successMessage = (String) session.getAttribute("successMessage");
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/profile.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String name = request.getParameter("name");
        if (name == null) name = "";
        name = name.trim();
        if (name.isEmpty() || name.length() > 100) {
            session.setAttribute("errorMessage", "表示名が不正です。");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        String bio = request.getParameter("bio");
        if (bio == null) bio = "";
        // 改行は保持
        if (bio.length() > 1000) {
            session.setAttribute("errorMessage", "自己紹介は1000文字以内で入力してください。");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        boolean dmAllowed = "1".equals(request.getParameter("dmAllowed"));

        // アイコン（任意）
        String storedIconName = null;

        // 1) ブラウザでトリミングしたPNG（data URL）を優先
        String cropped = request.getParameter("croppedIconData");
        if (cropped != null) cropped = cropped.trim();
        if (cropped != null && !cropped.isEmpty()) {
            try {
                String prefix = "data:image/png;base64,";
                if (!cropped.startsWith(prefix)) {
                    session.setAttribute("errorMessage", "トリミング画像の形式が不正です。");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }
                String b64 = cropped.substring(prefix.length());
                byte[] bytes = Base64.getDecoder().decode(b64);
                // 5MB制限（PNGにしては十分）
                if (bytes.length <= 0 || bytes.length > (5L * 1024L * 1024L)) {
                    session.setAttribute("errorMessage", "トリミング画像のサイズが大きすぎます。");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }
                // PNGシグネチャ確認
                if (bytes.length < 8
                        || (bytes[0] & 0xFF) != 0x89
                        || bytes[1] != 0x50
                        || bytes[2] != 0x4E
                        || bytes[3] != 0x47) {
                    session.setAttribute("errorMessage", "トリミング画像がPNGではありません。");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }

                storedIconName = UploadUtil.newStoredFileName("icon.png");
                File dir = UploadUtil.getUploadDir(getServletContext());
                File out = new File(dir, storedIconName);
                Files.copy(new ByteArrayInputStream(bytes), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IllegalArgumentException e) {
                session.setAttribute("errorMessage", "トリミング画像の読み取りに失敗しました。");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }
        }

        // 2) 通常アップロード（トリミングが無い場合のみ）
        Part iconPart = null;
        try {
            iconPart = request.getPart("icon");
        } catch (Exception ignored) {
            iconPart = null;
        }

        if (storedIconName == null && iconPart != null && iconPart.getSize() > 0) {
            String original = iconPart.getSubmittedFileName();
            String mime = iconPart.getContentType();

            boolean okExt = UploadUtil.isAllowedByExtension(original);
            boolean okMime = mime != null && mime.toLowerCase().startsWith("image/");
            if (!okExt || !okMime) {
                session.setAttribute("errorMessage", "アイコン画像の形式が不正です。");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            storedIconName = UploadUtil.newStoredFileName(original);
            File dir = UploadUtil.getUploadDir(getServletContext());
            File out = new File(dir, storedIconName);
            Files.copy(iconPart.getInputStream(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        UserDAO userDAO = new UserDAO();
        boolean updated = userDAO.updateProfile(loginUser.getId(), name, bio, dmAllowed, storedIconName);
        if (!updated) {
            session.setAttribute("errorMessage", "プロフィールの更新に失敗しました。");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        // セッション更新
        User refreshed = userDAO.findById(loginUser.getId());
        if (refreshed != null) {
            session.setAttribute("loginUser", refreshed);
        }

        session.setAttribute("successMessage", "プロフィールを更新しました。");
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}


