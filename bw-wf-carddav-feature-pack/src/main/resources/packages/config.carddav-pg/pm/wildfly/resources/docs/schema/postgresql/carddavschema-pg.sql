alter table bwcd_card_properties
    drop constraint bwcd_card_prop_fk;

alter table bwcd_card_properties
    drop constraint bwcd_prprp_pid_fk;

alter table bwcd_prop_params
    drop constraint bwcd_prop_param_fk;

alter table bwcd_prop_params
    drop constraint bwcd_prp_param_fk;

drop table if exists bwcd_card_properties cascade;

drop table if exists bwcd_cards cascade;

drop table if exists bwcd_collections cascade;

drop table if exists bwcd_prop_params cascade;

drop table if exists bwcd_propparams cascade;

drop table if exists bwcd_props cascade;

drop sequence if exists hibernate_sequence;
create sequence hibernate_sequence start 1 increment 1;

create table bwcd_card_properties (
    bwcd_cardid int8,
    bwcd_propid int8 not null
);

create table bwcd_cards (
    bwcd_id int8 not null,
    bwcd_seq int4 not null,
    bwcd_bytesize int4,
    bwcd_ownerhref varchar(255) not null,
    bwcd_creatorhref varchar(255) not null,
    bwcd_acl varchar(3900),
    bwcd_parent_path varchar(3000) not null,
    bwcd_path varchar(3000) not null,
    bwcd_created varchar(16) not null,
    bwcd_name varchar(500),
    bwcd_lastmod varchar(16) not null,
    bwcd_fn varchar(1000),
    bwcd_uid varchar(1000),
    bwcd_kind varchar(100),
    bwcd_strForm text,
    primary key (bwcd_id)
);

create table bwcd_collections (
    bwcd_id int8 not null,
    bwcd_seq int4 not null,
    bwcd_bytesize int4,
    bwcd_ownerhref varchar(255) not null,
    bwcd_creatorhref varchar(255) not null,
    bwcd_acl varchar(3900),
    bwcd_parent_path varchar(3000),
    bwcd_path varchar(3000) not null,
    bwcd_created varchar(16) not null,
    bwcd_name varchar(500),
    bwcd_lastmod varchar(16) not null,
    bwcd_desc varchar(1000),
    bwcd_addrbook boolean not null,
    primary key (bwcd_id)
);

create table bwcd_prop_params (
    bwcd_propid int8,
    bwcd_paramid int8 not null
);

create table bwcd_propparams (
    bwcd_id int8 not null,
    bwcd_name varchar(500),
    bwcd_value text,
    primary key (bwcd_id)
);

create table bwcd_props (
    bwcd_id int8 not null,
    bwcd_name varchar(500),
    bwcd_value text,
    primary key (bwcd_id)
);

alter table bwcd_card_properties
    add constraint UK_jocupb79od30fysar4u8domhp unique (bwcd_propid);
create index bwcdidx_card_owner on bwcd_cards (bwcd_ownerhref);
create index bwcdidx_card_parentpath on bwcd_cards (bwcd_parent_path);
create index bwcdidx_card_path on bwcd_cards (bwcd_path);
create index bwcdidx_card_name on bwcd_cards (bwcd_name);
create index bwcdidx_card_fn on bwcd_cards (bwcd_fn);
create index bwcdidx_card_uid on bwcd_cards (bwcd_uid);
create index bwcdidx_card_kind on bwcd_cards (bwcd_kind);

alter table bwcd_cards
    add constraint UK_dv2qib4ofbsnklxyluwwtqgmw unique (bwcd_path);
create index bwcdidx_col_owner on bwcd_collections (bwcd_ownerhref);
create index bwcdidx_col_parentpath on bwcd_collections (bwcd_parent_path);
create index bwcdidx_col_path on bwcd_collections (bwcd_path);
create index bwcdidx_col_name on bwcd_collections (bwcd_name);
create index bwcdidx_col_desc on bwcd_collections (bwcd_desc);

alter table bwcd_prop_params
    add constraint UK_akess656qlkr13obnml4enaad unique (bwcd_paramid);
create index bwcdidx_prop_name on bwcd_props (bwcd_name);

alter table bwcd_card_properties
    add constraint bwcd_card_prop_fk
    foreign key (bwcd_propid)
    references bwcd_props;

alter table bwcd_card_properties
    add constraint bwcd_prprp_pid_fk
    foreign key (bwcd_cardid)
    references bwcd_cards;

alter table bwcd_prop_params
    add constraint bwcd_prop_param_fk
    foreign key (bwcd_paramid)
    references bwcd_propparams;

alter table bwcd_prop_params
    add constraint bwcd_prp_param_fk
    foreign key (bwcd_propid)
    references bwcd_props;
