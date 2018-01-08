CREATE TABLE virtual_account (
  id             VARCHAR(36),
  account_number VARCHAR(255)   NOT NULL,
  invoice_number VARCHAR(255)   NOT NULL,
  name           VARCHAR(255)   NOT NULL,
  account_type   VARCHAR(255)   NOT NULL,
  amount         NUMERIC(19, 2) NOT NULL,
  description    VARCHAR(255),
  email          VARCHAR(255),
  phone          VARCHAR(255),
  create_time    TIMESTAMP      NOT NULL,
  expire_date    DATE           NOT NULL,
  account_status VARCHAR(255)   NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (invoice_number)
);

CREATE TABLE payment (
  id                    VARCHAR(36),
  id_virtual_account    VARCHAR(36)    NOT NULL,
  amount                NUMERIC(19, 2) NOT NULL,
  transaction_time      TIMESTAMP      NOT NULL,
  transaction_reference VARCHAR(36)    NOT NULL,
  client_reference      VARCHAR(36)    NOT NULL,
  payment_status        VARCHAR(255)   NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (id_virtual_account) REFERENCES virtual_account (id),
  UNIQUE (id_virtual_account,client_reference)
);

CREATE TABLE payment_reversal (
  id                    VARCHAR(36),
  id_payment            VARCHAR(36)  NOT NULL,
  transaction_time      TIMESTAMP    NOT NULL,
  transaction_reference VARCHAR(255) NOT NULL,
  client_reference      VARCHAR(36)  NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (id_payment) REFERENCES payment (id),
  UNIQUE (id_payment)
);

CREATE TABLE inquiry_request (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE inquiry_response (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE payment_request (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE payment_response (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE reversal_request (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE reversal_response (
  id               VARCHAR(36),
  transaction_time TIMESTAMP    NOT NULL,
  account_number   VARCHAR(255) NOT NULL,
  message          TEXT         NOT NULL,
  PRIMARY KEY (id)
);
