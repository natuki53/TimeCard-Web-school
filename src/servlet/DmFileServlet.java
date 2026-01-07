package servlet;

import dao.DmDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.DmAttachment;
import model.User;
import util.AuthUtil;
import util.UploadUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * DM添付ファイルのダウンロード/表示
 */
@WebServlet("/dm/chat/file")
public class DmFileServlet extends HttpServlet {
    private static Integer toIntOrNull(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
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
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        DmDAO dmDAO = new DmDAO();
        DmAttachment att = dmDAO.findAttachmentById(id);
        if (att == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!dmDAO.isUserInThread(att.getThreadId(), loginUser.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File dir = UploadUtil.getUploadDir(getServletContext());
        File f = new File(dir, att.getStoredFileName());
        if (!f.exists() || !f.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mime = att.getMimeType() != null ? att.getMimeType() : Files.probeContentType(f.toPath());
        if (mime == null) mime = "application/octet-stream";

        boolean inline = mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/") || "application/pdf".equals(mime);
        String disp = inline ? "inline" : "attachment";

        String safeName = UploadUtil.safeDisplayName(att.getOriginalFileName());
        response.setContentType(mime);
        response.setHeader("Content-Length", String.valueOf(f.length()));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Content-Disposition", UploadUtil.buildContentDisposition(disp, safeName));

        try (OutputStream os = response.getOutputStream()) {
            Files.copy(f.toPath(), os);
        }
    }
}


