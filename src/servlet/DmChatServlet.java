package servlet;

import dao.DmDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.DmMessage;
import model.User;
import util.AuthUtil;
import util.UploadUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DMチャット
 * GET: 表示 + 既読更新
 * POST: 送信
 */
@WebServlet("/dm/chat")
@MultipartConfig(
        maxFileSize = UploadUtil.MAX_FILE_BYTES,
        maxRequestSize = UploadUtil.MAX_FILE_BYTES * 6,
        fileSizeThreshold = 1024 * 1024
)
public class DmChatServlet extends HttpServlet {
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

        Integer threadId = toIntOrNull(request.getParameter("id"));
        if (threadId == null || threadId <= 0) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        DmDAO dmDAO = new DmDAO();
        if (!dmDAO.isUserInThread(threadId, loginUser.getId())) {
            request.setAttribute("errorMessage", "このDMの閲覧権限がありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // 既読更新
        dmDAO.upsertLastRead(loginUser.getId(), threadId);

        List<DmMessage> messages = dmDAO.findRecentMessages(threadId, 200);
        request.setAttribute("threadId", threadId);
        request.setAttribute("messages", messages);
        request.setAttribute("otherName", dmDAO.getOtherName(threadId, loginUser.getId()));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dm_chat.jsp");
        dispatcher.forward(request, response);
    }

    private void doGetWithError(HttpServletRequest request, HttpServletResponse response, int threadId, String postError)
            throws ServletException, IOException {
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        DmDAO dmDAO = new DmDAO();
        if (!dmDAO.isUserInThread(threadId, loginUser.getId())) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        request.setAttribute("postError", postError);
        request.setAttribute("threadId", threadId);
        request.setAttribute("messages", dmDAO.findRecentMessages(threadId, 200));
        request.setAttribute("otherName", dmDAO.getOtherName(threadId, loginUser.getId()));

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dm_chat.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer threadId = toIntOrNull(request.getParameter("threadId"));
        if (threadId == null || threadId <= 0) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        DmDAO dmDAO = new DmDAO();
        if (!dmDAO.isUserInThread(threadId, loginUser.getId())) {
            response.sendRedirect(request.getContextPath() + "/dm");
            return;
        }

        String content = request.getParameter("content");
        if (content == null) content = "";
        content = content.trim();
        if (content.length() > 5000) content = content.substring(0, 5000);

        // 添付
        List<Part> fileParts = new ArrayList<>();
        try {
            for (Part p : request.getParts()) {
                if ("files".equals(p.getName()) && p.getSize() > 0) {
                    fileParts.add(p);
                }
            }
        } catch (IllegalStateException | ServletException e) {
            log("dm upload failed at getParts()", e);
            doGetWithError(request, response, threadId, "添付に失敗しました（アップロード容量が上限を超えました）。");
            return;
        }

        if (content.isEmpty() && fileParts.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dm/chat?id=" + threadId);
            return;
        }

        // 添付検証
        for (Part p : fileParts) {
            String original = UploadUtil.safeDisplayName(p.getSubmittedFileName());
            if (p.getSize() > UploadUtil.MAX_FILE_BYTES) {
                doGetWithError(request, response, threadId,
                        "容量オーバー: 1ファイルあたり50MBまでです。" + original + "（" + UploadUtil.formatBytes(p.getSize()) + "）。");
                return;
            }
            String mime = p.getContentType();
            boolean allowed = UploadUtil.isAllowedMime(mime) || UploadUtil.isAllowedByExtension(original);
            if (!allowed) {
                String shownMime = (mime == null || mime.trim().isEmpty()) ? "不明" : mime;
                doGetWithError(request, response, threadId,
                        "形式未対応: 対応形式は画像/動画/音楽/PDF/zipです。" + original + "（" + shownMime + "）。");
                return;
            }
        }

        // contentは空でもOK（添付のみの場合は空文字で保存）
        Integer messageId = dmDAO.insertMessage(threadId, loginUser.getId(), content);
        if (messageId == null) {
            doGetWithError(request, response, threadId, "送信に失敗しました。");
            return;
        }
        if (messageId != null && !fileParts.isEmpty()) {
            File uploadDir = UploadUtil.getUploadDir(getServletContext());
            for (Part p : fileParts) {
                String original = UploadUtil.safeDisplayName(p.getSubmittedFileName());
                String stored = UploadUtil.newStoredFileName(original);
                File dest = new File(uploadDir, stored);

                int tries = 0;
                while (dest.exists() && tries < 3) {
                    stored = UploadUtil.newStoredFileName(original);
                    dest = new File(uploadDir, stored);
                    tries++;
                }

                Files.copy(p.getInputStream(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                String mime = p.getContentType();
                if (mime == null || mime.trim().isEmpty() || "application/octet-stream".equalsIgnoreCase(mime)) {
                    String guessed = UploadUtil.guessMimeFromFileName(original);
                    if (guessed != null) mime = guessed;
                }
                dmDAO.insertAttachment(messageId, original, stored, mime, p.getSize());
            }
        }

        // 送信者側は既読扱い
        dmDAO.upsertLastRead(loginUser.getId(), threadId);

        response.sendRedirect(request.getContextPath() + "/dm/chat?id=" + threadId);
    }
}


