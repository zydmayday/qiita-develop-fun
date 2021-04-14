create table user
(
    id        bigint unsigned primary key auto_increment,
    name      varchar(255) not null,
    password  varchar(255) not null,
    create_at datetime default current_timestamp
)