# 勤怠管理サイト（Servlet/JSP）

学生やバイトなどの「出勤時間・退勤時間」を、Web上で簡単に記録・閲覧できる勤怠管理サイトです。

## 機能

- ユーザー登録
- ログイン / ログアウト
- 出勤打刻
- 退勤打刻
- 勤怠一覧表示（月単位）

## 技術スタック

- Java 11
- Jakarta Servlet / JSP
- MySQL 8.0
- Maven

## プロジェクト構成

```
src/
 ├─ servlet/        # サーブレットクラス（ここに作成）
 ├─ model/          # モデルクラス（ここに作成）
 ├─ dao/            # DAOクラス（ここに作成）
 ├─ util/           # ユーティリティクラス
 │   └─ DBUtil.java
 └─ resources/      # リソースファイル
     ├─ database.properties  # DB接続設定
     └─ create_tables.sql    # DBスキーマ

WebContent/
 ├─ WEB-INF/
 │   ├─ web.xml     # デプロイメント記述子
 │   ├─ index.jsp   # トップページ
 │   └─ jsp/        # JSPファイル（ここに作成）
 │       ├─ login.jsp
 │       ├─ register.jsp
 │       ├─ attendance.jsp
 │       └─ attendance_list.jsp
 └─ css/            # CSSファイル
     └─ style.css
```

## セットアップ

### 1. データベースの準備

MySQLにデータベースを作成し、スキーマを実行してください。

```bash
mysql -u root -p < src/resources/create_tables.sql
```

または、MySQLに接続して以下を実行：

```sql
CREATE DATABASE IF NOT EXISTS timecard_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE timecard_db;
-- src/resources/create_tables.sql の内容を実行
```

### 2. データベース接続設定

`src/resources/database.properties` を編集して、データベース接続情報を設定してください。

```properties
db.url=jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8
db.username=your_username
db.password=your_password
```

### 3. ビルド

```bash
mvn clean package
```

### 4. デプロイ

生成された `target/timecard-web-school.war` をTomcatなどのサーブレットコンテナにデプロイしてください。

または、IDE（Eclipse、IntelliJ IDEAなど）から直接実行することもできます。

## URL一覧

| URL | 説明 |
|-----|------|
| `/` | トップページ（ログイン済みなら `/attendance` へリダイレクト） |
| `/login` | ログイン画面 |
| `/register` | 新規登録画面 |
| `/attendance` | 勤怠打刻画面 |
| `/attendance/list` | 勤怠一覧画面 |
| `/logout` | ログアウト |

## テストユーザー

スキーマ実行後、以下のテストユーザーが作成されます：

- ログインID: `test_user`
- パスワード: `test123`
- 名前: `テストユーザー`

## 開発環境

- Java 11以上
- Maven 3.6以上
- MySQL 8.0以上
- Apache Tomcat 10.0以上（またはJakarta EE対応のサーブレットコンテナ）

## ライセンス

このプロジェクトは教育目的で作成されています。
