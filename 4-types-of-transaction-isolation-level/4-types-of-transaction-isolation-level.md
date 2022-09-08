最近はトランザクション分離レベルについて勉強しました。  
これはそのメモです。

今回は MySQL で実験してみました。

## 準備

### mysql docker

参考：<https://hub.docker.com/_/mysql>

```shell
docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:tag
```

### 環境起動後

Docker desktop から CLI を起動する。

起動後：

```shell
$ mysql -u root -p
mysql> create database test;
mysql> use test;
```

### 検証用のテーブルを作成

```sql
create table users (
 ID int not null,
 Age int,
 primary key (ID)
);
```

```sql
mysql> select * from users;
Empty set (0.00 sec)
```

これで準備ができました。

## read uncommitted

文字通りに、トランザクションAがトランザクションBのコミットしていないレコードを読むことができる分離レベルです。



## read committed

## repeatable read

## serializable
