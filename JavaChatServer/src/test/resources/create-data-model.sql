--
-- Файл сгенерирован с помощью SQLiteStudio v3.2.1 в Вт сен 24 10:41:43 2019
--
-- Использованная кодировка текста: System
--
PRAGMA foreign_keys = off;
BEGIN TRANSACTION;

-- Таблица: blacklist
DROP TABLE IF EXISTS blacklist;
CREATE TABLE blacklist (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_nick_owner INTEGER REFERENCES main (id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL ON CONFLICT FAIL, id_nick_blocked INTEGER REFERENCES main (id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL);

-- Таблица: history
DROP TABLE IF EXISTS history;
CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, date_msg DATETIME, sender_id INTEGER REFERENCES main (id) ON DELETE CASCADE ON UPDATE CASCADE NOT NULL, receiver_id REFERENCES main (id) ON UPDATE CASCADE, msg TEXT);

-- Таблица: main
DROP TABLE IF EXISTS main;
CREATE TABLE main (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT UNIQUE, password INTEGER NOT NULL, nickname TEXT UNIQUE);

-- Индекс: bl_unik
DROP INDEX IF EXISTS bl_unik;
CREATE UNIQUE INDEX bl_unik ON blacklist (id_nick_owner, id_nick_blocked);

COMMIT TRANSACTION;
PRAGMA foreign_keys = on;
