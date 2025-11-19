# Tomcatでの起動方法

このプロジェクトをTomcatで起動する方法を説明します。

## 方法1: MavenでビルドしてWARファイルをデプロイ（推奨）

### 1. プロジェクトをビルド

```bash
mvn clean package
```

これで `target/timecard-web-school.war` が作成されます。

### 2. TomcatにWARファイルをデプロイ

#### 方法A: Tomcatの管理画面からデプロイ

1. Tomcatを起動
2. ブラウザで `http://localhost:8080/manager/html` にアクセス
3. ユーザー名・パスワードを入力（Tomcatの設定が必要）
4. 「WAR file to deploy」セクションで `target/timecard-web-school.war` を選択
5. 「Deploy」ボタンをクリック

#### 方法B: ファイルを直接コピー

1. `target/timecard-web-school.war` をコピー
2. Tomcatの `webapps/` ディレクトリに貼り付け
3. Tomcatが自動的に展開・デプロイします

### 3. アクセス

ブラウザで以下のURLにアクセス：
```
http://localhost:8080/timecard-web-school/
```

---

## 方法2: IDE（Eclipse/IntelliJ IDEA）から直接起動

### Eclipseの場合

1. **プロジェクトをインポート**
   - File → Import → Existing Maven Projects
   - プロジェクトフォルダを選択

2. **Tomcatサーバーを追加**
   - Window → Show View → Servers
   - 「New Server」をクリック
   - Apache → Tomcat v10.0 Server を選択
   - Tomcatのインストールディレクトリを指定

3. **プロジェクトをサーバーに追加**
   - プロジェクトを右クリック → Run As → Run on Server
   - または、Serversビューでプロジェクトをドラッグ&ドロップ

4. **起動**
   - Serversビューでサーバーを右クリック → Start

### IntelliJ IDEAの場合

1. **プロジェクトを開く**
   - File → Open → pom.xml を選択

2. **Tomcat設定**
   - Run → Edit Configurations
   - 「+」→ Tomcat Server → Local
   - Application server: Tomcatのパスを指定
   - Deployment タブで「+」→ Artifact → timecard-web-school:war exploded
   - Application context: `/` または `/timecard-web-school`

3. **起動**
   - Run → Run 'Tomcat Server'

---

## 方法3: Maven Tomcat Pluginで起動（開発用）

pom.xmlにTomcatプラグインを追加すれば、コマンドラインから直接起動できます。

### pom.xmlに追加（オプション）

```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <port>8080</port>
        <path>/</path>
    </configuration>
</plugin>
```

### 起動コマンド

```bash
mvn tomcat7:run
```

---

## 起動前の確認事項

### 1. データベースの準備

```bash
# MySQLにデータベースを作成
mysql -u root -p < src/resources/create_tables.sql
```

### 2. データベース接続設定

`src/resources/database.properties` を編集：

```properties
db.url=jdbc:mysql://localhost:3306/timecard_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8
db.username=root
db.password=your_password
```

### 3. Tomcatのバージョン

- **Tomcat 10.0以上**が必要です（Jakarta EE対応）
- Tomcat 9以下は動作しません（Java EEを使用しているため）

---

## トラブルシューティング

### ポート8080が使用中の場合

Tomcatの `conf/server.xml` でポート番号を変更：

```xml
<Connector port="8081" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443" />
```

### データベース接続エラーの場合

1. MySQLが起動しているか確認
2. `database.properties` の設定を確認
3. データベース `timecard_db` が作成されているか確認

### クラスが見つからないエラーの場合

```bash
mvn clean compile
```

で再コンパイルしてください。

---

## アクセスURL一覧

起動後、以下のURLにアクセスできます：

- トップページ: `http://localhost:8080/timecard-web-school/`
- ログイン: `http://localhost:8080/timecard-web-school/login`
- 新規登録: `http://localhost:8080/timecard-web-school/register`
- 勤怠打刻: `http://localhost:8080/timecard-web-school/attendance`
- 勤怠一覧: `http://localhost:8080/timecard-web-school/attendance/list`

