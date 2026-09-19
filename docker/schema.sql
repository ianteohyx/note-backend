-- Schema for the note app (structure only, no data).
--
-- Mounted into the MySQL container at /docker-entrypoint-initdb.d/ by docker-compose.yml.
-- It runs once, against the database named by MYSQL_DATABASE (DB_NAME in .env),
-- the first time the container starts with an empty data volume.
-- To re-run it: `docker compose down -v && docker compose up`.

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text,
  `authorId` int NOT NULL,
  `dateCreated` datetime DEFAULT CURRENT_TIMESTAMP,
  `dateModified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_note_author` (`authorId`),
  CONSTRAINT `fk_note_author` FOREIGN KEY (`authorId`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `refreshtokens` (
  `token` varchar(255) NOT NULL,
  `userId` int NOT NULL,
  `expiryDate` datetime(6) NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT '0',
  `createdAt` datetime(6) NOT NULL,
  PRIMARY KEY (`token`),
  KEY `user_id` (`userId`),
  CONSTRAINT `refreshtokens_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `sharednotes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `note_id` int NOT NULL,
  `shared_to_user_id` int NOT NULL,
  `permission` enum('READ','WRITE') DEFAULT 'READ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sharednotes_note_user` (`note_id`,`shared_to_user_id`),
  KEY `fk_sharednote_user` (`shared_to_user_id`),
  CONSTRAINT `fk_sharednote_note` FOREIGN KEY (`note_id`) REFERENCES `notes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sharednote_user` FOREIGN KEY (`shared_to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
