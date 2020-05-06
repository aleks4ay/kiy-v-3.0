CREATE TABLE clients
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  id_parent VARCHAR(9),
  isfolder INTEGER,
  descr1 VARCHAR,
  descr2 VARCHAR,
  phone VARCHAR(48),
  inn VARCHAR(12),
  num_certificate VARCHAR(12),
  addr_fis VARCHAR,
  addr_urid VARCHAR
);

CREATE TABLE descriptions
(
  kod INTEGER PRIMARY KEY NOT NULL,
  big_number INTEGER,
  iddoc VARCHAR(9),
  pos INTEGER,
  id_tmc VARCHAR(9),
  amount INTEGER,
  descr_second VARCHAR,
  size_a INTEGER,
  size_b INTEGER,
  size_c INTEGER,
  embodiment VARCHAR(9)
);

CREATE TABLE invoice
(
  iddoc VARCHAR(9) PRIMARY KEY NOT NULL,
  docno VARCHAR(10),
  id_order VARCHAR(9),
  time_invoice TIMESTAMP,
  time22 BIGINT,
  price NUMERIC(14,3)
);

CREATE TABLE manufacture
(
  id SERIAL PRIMARY KEY NOT NULL,
  iddoc VARCHAR(9),
  position INTEGER,
  docno VARCHAR(10),
  id_order VARCHAR(9),
  time_manuf TIMESTAMP,
  time21 BIGINT,
  amount INTEGER,
  id_tmc VARCHAR(9),
  descr_second VARCHAR,
  size_a INTEGER,
  size_b INTEGER,
  size_c INTEGER,
  embodiment VARCHAR(9)
);

CREATE TABLE orders
(
  big_number INTEGER PRIMARY KEY NOT NULL,
  iddoc VARCHAR(9),
  idclient VARCHAR(9),
  idmanager VARCHAR(9),
  duration INTEGER,
  docno VARCHAR(10),
  docno_manuf VARCHAR(10),
  docno_invoice VARCHAR(10),
  pos_count INTEGER,
  client_name VARCHAR,
  manager_name VARCHAR,
  t_create TIMESTAMP,
  t_factory TIMESTAMP,
  t_end TIMESTAMP,
  time22 BIGINT,
  price NUMERIC(14,3),
  payment NUMERIC(14,3),
  time_manuf TIMESTAMP,
  time_invoice TIMESTAMP,
  is_parsing INTEGER DEFAULT 0
);

CREATE TABLE set_technologichka
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  parentid VARCHAR(9),
  descr VARCHAR,
  size_a INTEGER,
  size_b INTEGER,
  size_c INTEGER
);

CREATE TABLE statuses
(
  kod INTEGER PRIMARY KEY NOT NULL,
  iddoc VARCHAR(9),
  time_0 BIGINT,
  time_1 BIGINT,
  time_2 BIGINT,
  time_3 BIGINT,
  time_4 BIGINT,
  time_5 BIGINT,
  time_6 BIGINT,
  time_7 BIGINT,
  time_8 BIGINT,
  time_9 BIGINT,
  time_10 BIGINT,
  time_11 BIGINT,
  time_12 BIGINT,
  time_13 BIGINT,
  time_14 BIGINT,
  time_15 BIGINT,
  time_16 BIGINT,
  time_17 BIGINT,
  time_18 BIGINT,
  time_19 BIGINT,
  time_20 BIGINT,
  time_21 BIGINT,
  time_22 BIGINT,
  time_23 BIGINT,
  time_24 BIGINT,
  type_index INTEGER,
  status_index INTEGER,
  designer_name VARCHAR,
  is_technologichka INTEGER,
  descr_first VARCHAR
);

CREATE TABLE tmc
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  id_parent VARCHAR(9),
  code VARCHAR(5),
  descr VARCHAR(50),
  is_folder INTEGER,
  descr_all VARCHAR(100),
  type VARCHAR(9)
);


CREATE VIEW order_view (
    kod, big_number, pos, amount, descr_second, size_a, size_b, size_c,
    iddoc, duration, docno, docno2, docno3,
    pos_count, t_create, t_factory, t_end, t_invoice,  manager, client,
    type_index, status_index, is_technologichka, designer, descr_first,
    time_0, time_1, time_2, time_3, time_4, time_5, time_6, time_7, time_8, time_9,
    time_10, time_11, time_12, time_13, time_14, time_15, time_16, time_17, time_18, time_19,
    time_20, time_21, time_22, time_23, time_24, price, payment, is_parsing
)
AS SELECT
     d.kod, d.big_number, d.pos, d.amount, d.descr_second, d.size_a, d.size_b, d.size_c,
     o.iddoc, o.duration, o.docno, o.docno_manuf, o.docno_invoice,
     o.pos_count, o.t_create, o.t_factory, o.t_end, o.time22, o.manager_name, o.client_name,
     t.type_index, t.status_index, t.is_technologichka, t.designer_name, t.descr_first,
     t.time_0, t.time_1, t.time_2, t.time_3, t.time_4, t.time_5, t.time_6, t.time_7, t.time_8, t.time_9,
     t.time_10, t.time_11, t.time_12, t.time_13, t.time_14, t.time_15, t.time_16, t.time_17, t.time_18, t.time_19,
     t.time_20, t.time_21, t.time_22, t.time_23, t.time_24, o.price, o.payment, t.is_parsing
   from orders o, descriptions d, statuses t
   WHERE o.IDDOC = d.IDDOC and d.kod = t.kod
   ORDER BY d.kod;

CREATE VIEW manufacture_view (
    id, iddoc, docno, pos, kod, id_tmc, amount, id_order, time21, descr_second, size_a, size_b, size_c, embodiment
)
AS SELECT
     m.id, m.iddoc, m.docno, m.position, d.kod, m.id_tmc, m.amount, m.id_order, m.time21, m.descr_second, m.size_a, m.size_b, m.size_c, m.embodiment
   from descriptions d, manufacture m
   WHERE m.id_order = d.iddoc AND m.id_tmc = d.id_tmc AND m.amount = d.amount AND m.size_a = d.size_a AND m.size_b = d.size_b AND m.size_c = d.size_c
   ORDER BY m.id, m.iddoc;





