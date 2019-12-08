CREATE TABLE scheduled_message
( id BIGINT NOT NULL AUTO_INCREMENT
, schedule_time TIME NOT NULL
, week_days TINYINT NOT NULL
, next_datetime DATETIME NOT NULL
, request_id BIGINT NOT NULL
, request_sender VARCHAR(120) NOT NULL
, request_receiver VARCHAR(120) NOT NULL
, sender_address VARCHAR(120) NOT NULL
, message_text VARCHAR(2000) NOT NULL
, CONSTRAINT scheduled_message_pk PRIMARY KEY (id)
);

CREATE TABLE ns_train_station
( eva_code BIGINT NOT NULL
, code VARCHAR(12) NOT NULL
, name VARCHAR(60) NOT NULL
, CONSTRAINT ns_train_station_pk PRIMARY KEY (eva_code, code)
);

