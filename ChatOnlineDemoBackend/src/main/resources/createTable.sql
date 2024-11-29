create table db_account
(
    id            int auto_increment
        primary key,
    username      varchar(255) null,
    password      varchar(255) null,
    email         varchar(255) null,
    role          varchar(255) null,
    register_time datetime     null,
    constraint unique_email
        unique (email),
    constraint unique_username
        unique (username)
);

create table db_message
(
    id        int auto_increment
        primary key,
    content   varchar(255) not null,
    sid       int          not null,
    rid       int          not null,
    send_time datetime     null,
    status    tinyint      null comment '0. 成功发送 1.发送失败'
);
