
        create table emotion_record_sub_emotions
        (
        emotion_record_id int          not null,
        sub_emotion_id    varchar(255) not null
        );

        create index fk_emotion_record_sub_emotions_emotion_record_id
        on emotion_record_sub_emotions (emotion_record_id);

        create index fk_emotion_record_sub_emotions_sub_emotion_id
        on emotion_record_sub_emotions (sub_emotion_id);

        create table emotion_record_triggers
        (
        emotion_record_id int not null,
        trigger_id        int not null
        );

        create index fk_emotion_record_triggers_emotion_record_id
        on emotion_record_triggers (emotion_record_id);

        create index fk_emotion_record_triggers_trigger_id
        on emotion_record_triggers (trigger_id);

        create table emotions
        (
        id           varchar(255)                        not null
        primary key,
        emotion_name varchar(255)                        not null,
        emotion_type varchar(255)                        not null,
        created      timestamp default CURRENT_TIMESTAMP not null,
        constraint emotion_name
        unique (emotion_name)
        );

        create index emotions_emotion_name_idx
        on emotions (emotion_name);

        create table sub_emotions
        (
        id               varchar(255)                        not null
        primary key,
        sub_emotion_name varchar(255)                        not null,
        emotion_id       varchar(255)                        null,
        created          timestamp default CURRENT_TIMESTAMP not null,
        constraint sub_emotion_name
        unique (sub_emotion_name)
        );

        create index fk_sub_emotions_emotion_id
        on sub_emotions (emotion_id);

        create table suggested_actions
        (
        id                 varchar(255) not null
        primary key,
        action_description varchar(255) not null,
        constraint action_description
        unique (action_description)
        );

        create table sub_emotion_suggested_action
        (
        sub_emotion_id      varchar(255) not null,
        suggested_action_id varchar(255) not null,
        primary key (sub_emotion_id, suggested_action_id),
        constraint fk_sub_emotion_suggested_action_sub_emotion_id
        foreign key (sub_emotion_id) references sub_emotions (id),
        constraint fk_sub_emotion_suggested_action_suggested_action_id
        foreign key (suggested_action_id) references suggested_actions (id)
        );

        create table suggested_action_links
        (
        id                  int auto_increment
        primary key,
        suggested_action_id varchar(255) not null,
        url                 varchar(255) not null
        );

        create table tags
        (
        id       int auto_increment
        primary key,
        user_id  int                                 null,
        tag_name varchar(255)                        not null,
        created  timestamp default CURRENT_TIMESTAMP not null
        );

        create table users
        (
        id       int auto_increment
        primary key,
        username varchar(255)                        not null,
        email    varchar(255)                        not null,
        password varchar(255)                        not null,
        created  timestamp default CURRENT_TIMESTAMP not null,
        constraint email
        unique (email),
        constraint username
        unique (username)
        );

        create table emotion_records
        (
        id         int auto_increment
        primary key,
        user_id    int                                 not null,
        emotion_id varchar(255)                        not null,
        created    timestamp default CURRENT_TIMESTAMP not null,
        intensity  decimal(10, 2)                      not null
        );

        create table emotion_record_tags
        (
        emotion_record_id int                                 not null,
        tag_id            int                                 not null,
        created           timestamp default CURRENT_TIMESTAMP not null
        );

        create table notes
        (
        id          int auto_increment
        primary key,
        title       varchar(255)                        not null,
        content     varchar(255)                        not null,
        user_id     int                                 null,
        created     timestamp default CURRENT_TIMESTAMP not null,
        lastUpdated timestamp default CURRENT_TIMESTAMP not null
        );

        create table note_tags
        (
        note_id int                                 not null,
        tag_id  int                                 not null,
        created timestamp default CURRENT_TIMESTAMP not null,
        primary key (note_id, tag_id)
        );

        create table triggers
        (
        id              int auto_increment
        primary key,
        trigger_name    varchar(255)                        not null,
        parent_id       int                                 null,
        created_by_user int                                 null,
        description     varchar(255)                        null,
        created         timestamp default CURRENT_TIMESTAMP not null
        );

        create table trigger_examples
        (
        id         int auto_increment
        primary key,
        trigger_id int          not null,
        example    varchar(255) not null
        );

        create index users_email_idx
        on users (email);

        create index users_username_idx
        on users (username);

