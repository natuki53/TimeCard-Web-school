package servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import dao.GroupChatDAO;
import dao.GroupDAO;
import model.Group;
import model.GroupMessage;
import model.User;
import util.AuthUtil;
import util.UploadUtil;

/**
 * グループチャット
 * GET: チャット表示
 * POST: 投稿（テキスト＋添付）
 */
@WebServlet("/group/chat")
@MultipartConfig(
        maxFileSize = UploadUtil.MAX_FILE_BYTES,
        maxRequestSize = UploadUtil.MAX_FILE_BYTES * 6, // 複数添付想定
        fileSizeThreshold = 1024 * 1024
)
public class GroupChatServlet extends HttpServlet {

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

    private boolean canAccess(GroupDAO groupDAO, int groupId, int userId) {
        return groupDAO.isGroupAdmin(groupId, userId) || groupDAO.isGroupMember(groupId, userId);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer groupId = toIntOrNull(request.getParameter("id"));
        if (groupId == null || groupId <= 0) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        Group group = groupDAO.findGroupById(groupId);
        if (group == null) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }

        if (!canAccess(groupDAO, groupId, loginUser.getId())) {
            request.setAttribute("errorMessage", "このグループの閲覧権限がありません。");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/error.jsp");
            dispatcher.forward(request, response);
            return;
        }

        GroupChatDAO chatDAO = new GroupChatDAO();
        List<GroupMessage> messages = chatDAO.findRecentMessages(groupId, 200);

        request.setAttribute("group", group);
        request.setAttribute("messages", messages);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_chat.jsp");
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

        Integer groupId = toIntOrNull(request.getParameter("groupId"));
        if (groupId == null || groupId <= 0) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        if (!canAccess(groupDAO, groupId, loginUser.getId())) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }

        String content = request.getParameter("content");
        if (content != null) content = content.trim();
        if (content != null && content.length() > 5000) {
            content = content.substring(0, 5000);
        }

        List<Part> fileParts = new ArrayList<>();
        try {
            for (Part p : request.getParts()) {
                if ("files".equals(p.getName())) {
                    if (p.getSize() > 0) fileParts.add(p);
                }
            }
        } catch (IllegalStateException | ServletException e) {
            // MultipartConfig の上限超過などで getParts() 自体が失敗するケース
            log("group chat upload failed at getParts()", e);
            request.setAttribute("postError", "添付に失敗しました（アップロード容量が上限を超えました）。");
            doGetWithError(request, response, groupId);
            return;
        }

        // 空投稿は禁止（内容も添付もなし）
        if ((content == null || content.isEmpty()) && fileParts.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/group/chat?id=" + groupId);
            return;
        }

        // 添付検証（50MB/種類）
        for (Part p : fileParts) {
            String original = UploadUtil.safeDisplayName(p.getSubmittedFileName());
            if (p.getSize() > UploadUtil.MAX_FILE_BYTES) {
                request.setAttribute("postError",
                        "容量オーバー: " + original + " は1ファイルあたり50MBまでです（" + UploadUtil.formatBytes(p.getSize()) + "）。");
                doGetWithError(request, response, groupId);
                return;
            }
            String mime = p.getContentType();
            boolean allowed = UploadUtil.isAllowedMime(mime) || UploadUtil.isAllowedByExtension(original);
            if (!allowed) {
                String shownMime = (mime == null || mime.trim().isEmpty()) ? "不明" : mime;
                request.setAttribute("postError",
                        "形式未対応: " + original + "（" + shownMime + "）は添付できません。対応形式は画像/動画/音楽/PDF/zipです。");
                doGetWithError(request, response, groupId);
                return;
            }
        }

        GroupChatDAO chatDAO = new GroupChatDAO();
        Integer messageId = chatDAO.insertMessage(groupId, loginUser.getId(), content);
        if (messageId == null) {
            request.setAttribute("postError", "投稿に失敗しました。");
            doGetWithError(request, response, groupId);
            return;
        }

        File uploadDir = UploadUtil.getUploadDir(getServletContext());

        // 添付保存
        for (Part p : fileParts) {
            String original = UploadUtil.safeDisplayName(p.getSubmittedFileName());
            String stored = UploadUtil.newStoredFileName(original);
            File dest = new File(uploadDir, stored);

            // 既に存在したら作り直す（衝突はほぼないが念のため）
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
            chatDAO.insertAttachment(messageId, original, stored, mime, p.getSize());
        }

        response.sendRedirect(request.getContextPath() + "/group/chat?id=" + groupId);
    }

    private void doGetWithError(HttpServletRequest request, HttpServletResponse response, int groupId)
            throws ServletException, IOException {
        // doGetを呼ぶと request.getParameter("id") が取れないので、ここで必要な情報を詰めてJSPへforwardする
        User loginUser = AuthUtil.getLoginUser(request);
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        GroupDAO groupDAO = new GroupDAO();
        Group group = groupDAO.findGroupById(groupId);
        if (group == null) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }
        if (!canAccess(groupDAO, groupId, loginUser.getId())) {
            response.sendRedirect(request.getContextPath() + "/groups");
            return;
        }

        GroupChatDAO chatDAO = new GroupChatDAO();
        List<GroupMessage> messages = chatDAO.findRecentMessages(groupId, 200);
        request.setAttribute("group", group);
        request.setAttribute("messages", messages);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/group_chat.jsp");
        dispatcher.forward(request, response);
    }
}


