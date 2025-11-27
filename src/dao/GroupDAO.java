package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Group;
import model.GroupMember;
import util.DBUtil;

/**
 * グループ関連のデータアクセスオブジェクト
 */
public class GroupDAO {
    
    /**
     * グループを作成する
     * @param group 作成するグループ情報
     * @return 作成されたグループ（IDが設定される）
     */
    public Group createGroup(Group group) {
        String sql = "INSERT INTO groups (name, description, admin_user_id) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setInt(3, group.getAdminUserId());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                // 生成されたIDを取得
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        group.setId(rs.getInt(1));
                        
                        // 作成者を自動的にメンバーに追加
                        addGroupMember(group.getId(), group.getAdminUserId());
                        
                        return group;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * ユーザーが管理者のグループ一覧を取得
     * @param adminUserId 管理者のユーザーID
     * @return グループ一覧
     */
    public List<Group> findGroupsByAdmin(int adminUserId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups WHERE admin_user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminUserId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Group group = new Group();
                    group.setId(rs.getInt("id"));
                    group.setName(rs.getString("name"));
                    group.setDescription(rs.getString("description"));
                    group.setAdminUserId(rs.getInt("admin_user_id"));
                    group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    groups.add(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * ユーザーが参加しているグループ一覧を取得
     * @param userId ユーザーID
     * @return グループ一覧
     */
    public List<Group> findGroupsByMember(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g " +
                    "INNER JOIN group_members gm ON g.id = gm.group_id " +
                    "WHERE gm.user_id = ? ORDER BY g.created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Group group = new Group();
                    group.setId(rs.getInt("id"));
                    group.setName(rs.getString("name"));
                    group.setDescription(rs.getString("description"));
                    group.setAdminUserId(rs.getInt("admin_user_id"));
                    group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    groups.add(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * グループIDでグループを取得
     * @param groupId グループID
     * @return グループ情報
     */
    public Group findGroupById(int groupId) {
        String sql = "SELECT * FROM groups WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Group group = new Group();
                    group.setId(rs.getInt("id"));
                    group.setName(rs.getString("name"));
                    group.setDescription(rs.getString("description"));
                    group.setAdminUserId(rs.getInt("admin_user_id"));
                    group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return group;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * グループメンバーを追加
     * @param groupId グループID
     * @param userId ユーザーID
     * @return 成功した場合true
     */
    public boolean addGroupMember(int groupId, int userId) {
        String sql = "INSERT INTO group_members (group_id, user_id) VALUES (?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            // 重複エラーの場合は既に参加済み
            if (e.getErrorCode() == 1062) { // MySQL duplicate entry error
                return false;
            }
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * グループメンバー一覧を取得（ユーザー情報付き）
     * @param groupId グループID
     * @return グループメンバー一覧
     */
    public List<GroupMember> findGroupMembers(int groupId) {
        List<GroupMember> members = new ArrayList<>();
        String sql = "SELECT gm.*, u.name, u.login_id FROM group_members gm " +
                    "INNER JOIN users u ON gm.user_id = u.id " +
                    "WHERE gm.group_id = ? ORDER BY gm.joined_at ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GroupMember member = new GroupMember();
                    member.setId(rs.getInt("id"));
                    member.setGroupId(rs.getInt("group_id"));
                    member.setUserId(rs.getInt("user_id"));
                    member.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
                    member.setUserName(rs.getString("name"));
                    member.setUserLoginId(rs.getString("login_id"));
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
    
    /**
     * グループメンバーを削除
     * @param groupId グループID
     * @param userId ユーザーID
     * @return 成功した場合true
     */
    public boolean removeGroupMember(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * ユーザーがグループの管理者かチェック
     * @param groupId グループID
     * @param userId ユーザーID
     * @return 管理者の場合true
     */
    public boolean isGroupAdmin(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM groups WHERE id = ? AND admin_user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * ユーザーがグループのメンバーかチェック
     * @param groupId グループID
     * @param userId ユーザーID
     * @return メンバーの場合true
     */
    public boolean isGroupMember(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
