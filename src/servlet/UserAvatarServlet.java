package servlet;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.AuthUtil;
import util.UploadUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * ユーザーアイコン取得
 */
@WebServlet("/user/avatar")
public class UserAvatarServlet extends HttpServlet {

    private static Integer toIntOrNull(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer id = toIntOrNull(request.getParameter("id"));
        if (id == null || id <= 0) id = loginUser.getId();

        UserDAO userDAO = new UserDAO();
        User u = userDAO.findPublicProfileById(id);
        if (u == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (u.getIconFilename() == null || u.getIconFilename().isBlank()) {
            response.sendRedirect(request.getContextPath() + "/img/icon.png");
            return;
        }

        File dir = UploadUtil.getUploadDir(getServletContext());
        File f = new File(dir, u.getIconFilename());
        if (!f.exists() || !f.isFile()) {
            response.sendRedirect(request.getContextPath() + "/img/icon.png");
            return;
        }

        String mime = Files.probeContentType(f.toPath());
        if (mime == null) mime = "application/octet-stream";

        response.setContentType(mime);
        response.setHeader("Content-Length", String.valueOf(f.length()));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");

        try (OutputStream os = response.getOutputStream()) {
            Files.copy(f.toPath(), os);
        }
    }
}


