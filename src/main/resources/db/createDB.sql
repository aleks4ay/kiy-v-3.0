/*CREATE TABLE worker
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  name VARCHAR
);

CREATE TABLE client
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  name VARCHAR
);*/


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

CREATE TABLE techno_item
(
  id VARCHAR(9) PRIMARY KEY NOT NULL,
  id_parent VARCHAR(9),
--   size_a INTEGER,
  --   size_b INTEGER,
  --   size_c INTEGER,
  descr VARCHAR
);

CREATE TABLE orders
(
  iddoc VARCHAR(9) PRIMARY KEY NOT NULL,
  big_number INTEGER,
  id_client VARCHAR(9),
  id_manager VARCHAR(9),
  duration INTEGER,
  docno VARCHAR(10),
  docno_manuf VARCHAR(10),
  docno_invoice VARCHAR(10),
  pos_count INTEGER,
  client_name VARCHAR,
  manager_name VARCHAR,
  t_create TIMESTAMP WITHOUT TIME ZONE,
  t_factory TIMESTAMP WITHOUT TIME ZONE,
  t_end TIMESTAMP WITHOUT TIME ZONE,
  time22 BIGINT,
  price NUMERIC(14,3),
  payment NUMERIC(14,3),
  time_manuf TIMESTAMP WITHOUT TIME ZONE,
  time_invoice TIMESTAMP WITHOUT TIME ZONE,
  is_parsing INTEGER DEFAULT 0
);


CREATE TABLE descriptions
(
  id VARCHAR(13) PRIMARY KEY NOT NULL,
  iddoc VARCHAR(9),
  position INTEGER,
  id_tmc VARCHAR(9),
  quantity INTEGER,
  descr_second VARCHAR,
  size_a INTEGER,
  size_b INTEGER,
  size_c INTEGER,
  embodiment VARCHAR,
  FOREIGN KEY (iddoc) REFERENCES orders (iddoc) ON DELETE CASCADE
);

CREATE TABLE statuses
(
  id VARCHAR(13) PRIMARY KEY NOT NULL,
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
  descr_first VARCHAR,
  is_parsing INTEGER,
  FOREIGN KEY (id) REFERENCES descriptions (id) ON DELETE CASCADE,
  FOREIGN KEY (iddoc) REFERENCES orders (iddoc) ON DELETE CASCADE
);


CREATE TABLE invoice
(
  iddoc VARCHAR(9) PRIMARY KEY NOT NULL,
  docno VARCHAR(10),
  id_order VARCHAR(9),
  time_invoice TIMESTAMP WITHOUT TIME ZONE,
  time22 BIGINT,
  price NUMERIC(14,3)
);

CREATE TABLE manufacture
(
  id VARCHAR(13) PRIMARY KEY NOT NULL,
  iddoc VARCHAR(9),
  position INTEGER,
  docno VARCHAR(10),
  id_order VARCHAR(9),
  time_manuf TIMESTAMP WITHOUT TIME ZONE,
  time21 BIGINT,
  quantity INTEGER,
  id_tmc VARCHAR(9),
  descr_second VARCHAR,
  size_a INTEGER,
  size_b INTEGER,
  size_c INTEGER,
  embodiment VARCHAR(9),
  FOREIGN KEY (id_order) REFERENCES orders (iddoc) ON DELETE CASCADE
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
     d.id, o.big_number, d.position, d.quantity, d.descr_second, d.size_a, d.size_b, d.size_c,
     o.iddoc, o.duration, o.docno, o.docno_manuf, o.docno_invoice,
     o.pos_count, o.t_create, o.t_factory, o.t_end, o.time22, o.manager_name, o.client_name,
     t.type_index, t.status_index, t.is_technologichka, t.designer_name, t.descr_first,
     t.time_0, t.time_1, t.time_2, t.time_3, t.time_4, t.time_5, t.time_6, t.time_7, t.time_8, t.time_9,
     t.time_10, t.time_11, t.time_12, t.time_13, t.time_14, t.time_15, t.time_16, t.time_17, t.time_18, t.time_19,
     t.time_20, t.time_21, t.time_22, t.time_23, t.time_24, o.price, o.payment, t.is_parsing
   from orders o, descriptions d, statuses t
   WHERE o.IDDOC = d.IDDOC and d.id = t.id
   ORDER BY d.id;

CREATE VIEW manufacture_view (
    id, iddoc, docno, pos, kod, id_tmc, amount, id_order, time_manuf, time21, descr_second, size_a, size_b, size_c, embodiment
)
AS SELECT
     m.id, m.iddoc, m.docno, m.position, d.id, m.id_tmc, m.quantity, m.id_order, m.time_manuf, m.time21,
     m.descr_second, m.size_a, m.size_b, m.size_c, m.embodiment
   from descriptions d, manufacture m
   WHERE m.id_order = d.iddoc AND m.id_tmc = d.id_tmc AND m.quantity = d.quantity AND m.size_a = d.size_a AND
         m.size_b = d.size_b AND m.size_c = d.size_c
   ORDER BY m.id, m.iddoc;





