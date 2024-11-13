create table if not exists brand
(
    id   bigint       not null generated always as identity,
    name varchar(255) not null,

    primary key (id)
);

create table if not exists category
(
    id   bigint       not null,
    name varchar(255) not null,

    primary key (id)
);

create table if not exists product
(
    id          bigint not null generated always as identity,
    brand_id    bigint not null,
    category_id bigint not null,
    price       bigint not null,

    primary key (id)
);

create unique index if not exists idx__product__brand_id__category_id on product (brand_id, category_id);