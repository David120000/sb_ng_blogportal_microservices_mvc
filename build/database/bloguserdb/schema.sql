CREATE DATABASE IF NOT EXISTS bloguserdb;

CREATE TABLE IF NOT EXISTS bloguserdb.users (
  email varchar(100) NOT NULL,
  password varchar(255) NOT NULL,
  role varchar(20) NOT NULL,
  account_enabled bit(1) NOT NULL,
  first_name varchar(100) NOT NULL,
  last_name varchar(100) NOT NULL,
  about varchar(255) DEFAULT NULL,
  PRIMARY KEY (email)
);

LOCK TABLES bloguserdb.users WRITE;
INSERT IGNORE INTO bloguserdb.users (email, password, role, account_enabled, first_name, last_name, about)
VALUES 
('tait.glass@yahoo.com','$2a$04$NxJd3ehNszVAoOn5mQYwqe5YIdyb0dJIQ58D58fMr2THi..moNFoq', 'USER', 1, 'Tait', 'Glass', NULL),
('miguel.david@outlook.com','$2a$04$atVgEy.PiRdQskoC.Mx.0eewTZ5yn/JEdDlmbu0aNkRb8c45i2NHa', 'USER', 1, 'Miguel', 'David', 'I am new here.'),
('rebecca.sandile@outlook.com','$2a$04$QY3FD6zD7OH5U5oHgmZXQ.5uZHh9LO/E79sPRWnYpRZrQSmO1yioy', 'ADMIN', 1, 'Rebecca', 'Sandile', NULL),
('leopoldo.hannes@aol.com','$2a$04$ZRpNxQiOxEoaSxKg.pmZbe85c.6Fb3OiEILuHXDEg6npRCfQtuMwi', 'USER', 1, 'Leopoldo', 'Hannes', NULL),
('ryley.york@msn.com','$2a$04$uYtM2QpfyD9F0E.Pn4WYkeJZ1N/ELzn6An9tZ7bi.jmo1fZyjuYXu', 'ADMIN', 1, 'Riley', 'York', NULL);
UNLOCK TABLES;

CREATE USER 'mysqlroot'@'%' IDENTIFIED BY 'root';
GRANT UPDATE, DELETE, SELECT, INSERT ON `bloguserdb`.* TO 'mysqlroot'@'%';

FLUSH PRIVILEGES;