いくつかの脆弱性についてキャッチアップしました。

## SQL injection

APはユーザからの入力を受け付け、その後SQLを実行するとユーザの入力により意図しない結果になる恐れがある。

**例**

このようなSQLがあるとします。
```sql
select * from user where userid = '{{userid}}' and nm = '{{nm}}';
```

ユーザからの入力は：
- userid: 42' or 1=1--
- nm: nm

変換後のSQL
```sql
select * from user where userid = '42' or 1=1-- and nm = 'nm';
```
になる。

こうして、42というユーザだけではなく、全てのユーザ情報が抽出されてしまうだ。

## SQL injection (second order)

SQLiと違って、ユーザが入力した情報をその場で利用せず、一回APのほうで保管します。  
また未来のどこかのタイミングで仕込んだ情報が意図しない結果になる恐れがある。

**例**

まずユーザは新しいアカウントを作成します。
useridは`42';update user set passwd=xxx where userid = 'admin'--`だとします。

ユーザ42がログインするときに、下記のSQLが実行されると考えられます。
```sql
select * from user where userid = '42';update user set passwd=xxx where userid = 'admin'--';
```

これで、adminユーザのパスワードが変更され、攻撃者は次回にフル権限を持つadminユーザでログインできるようになる。

## Sensitive Information Saved Unencrypted

重要な情報がエンクリプトされずに外部に流出してしまった。

## Directory Traversal

ファイルの相対パスを利用して機密情報を取得するなり、破壊するなりのこと。

**例**

例えば画像ファイルの取得ロジックは以下のような場合：
```java
File file =  new File('/application/src/images/' + imgFileName); // imgFileNameは別途から来たパラメータ
return file.getCanonicalPath(); // 画像のフルパスを返す
```

- logPath：../../../../etc/passwd 

これで実際にロガーを作成する場所は
`/application/tomcat/webapp/../../../../etc/passwd` -> `/etc/passwd`になる。

これでシステムのパスワードを漏洩する恐れがある。

## Directory Traversal (second order)

特に調査できませんでした。

## Denial of Service (DoS) via Blocking Call

APのリソースを枯渇させることが目的である。

**例一**

```java
String rowNum = request.getParameter("rowNum");
int row = Integer.parseInt(rowNum);
TcsRowSet[] tcwRowSets = new TcsRowSet[row]; // これでrowNumの数によって一気にメモリを食ってしまう恐れがある
```

**例二**

```java
String[] params = request.getParams("info");
for(String param : params) {
  // paramsはすごく多く、かつ処理が遅い場合にAPの反応が遅くなり、ひどい場合にサービス自体が停止してしまう恐れがあります。
}
```

他にいろいろ例があります。

## Insufficient SSL Enforcement

1. SSLを利用していない
2. SSL認証の設定が不適切
3. APとDBが同じ筐体でない場合に、かつAPとDBの通信はセキュアじゃない場合

## Unrestricted File Upload

アップロードされたファイルの検査が足りない。
