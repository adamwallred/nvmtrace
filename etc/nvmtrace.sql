CREATE TABLE sample(
       md5 char(32) UNIQUE NOT NULL,
       submit_time int NOT NULL,
       process_date DATE,
       PRIMARY KEY (md5)
);
