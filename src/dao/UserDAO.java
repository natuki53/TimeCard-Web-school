package dao;

import model.User;

/**
 * ユーザーDAOクラス（データベースアクセス）
 */
public class UserDAO {
    
    /**
     * ログインIDとパスワードでユーザーを検索
     * @param loginId ログインID
     * @param password パスワード
     * @return 見つかったユーザー、見つからなければnull
     */
    public User findByLoginIdAndPassword(String loginId, String password) {
        // TODO: 実装
        return null;
    }
    
    /**
     * 新規ユーザーを登録
     * @param user 登録するユーザー
     * @return 登録成功したらtrue
     */
    public boolean insert(User user) {
        // TODO: 実装
        return false;
    }
}

