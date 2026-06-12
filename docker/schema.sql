-- MySQL dump 10.13  Distrib 5.7.24, for osx10.9 (x86_64)
--
-- Host: note-app-dev.cf28gk2igkwn.ap-southeast-2.rds.amazonaws.com    Database: note_app
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `notes`
--

DROP TABLE IF EXISTS `notes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notes`
--

LOCK TABLES `notes` WRITE;
/*!40000 ALTER TABLE `notes` DISABLE KEYS */;
INSERT INTO `notes` VALUES (2,'Vacation','taiwan, japan, bali',1,'2025-10-27 17:57:23','2026-02-03 15:45:32'),(6,'Taiwan Expenses','Taipei Air bnb 930, Taichung Air bnb 440, RainCoat: 11.80, DayTrip: 500',2,'2025-11-04 00:39:13','2025-11-08 17:36:26'),(8,'Interview Questions','1. How to set up a Trie, 2. Design a restraunt system to take oders, 3. Explain REST controllers',1,'2025-11-05 16:15:25','2025-11-05 09:56:29'),(9,'People Who Owe Me Money','1. Jack, 2. Joy, 3.Yteo (I did not owe u okay)',1,'2025-11-05 17:59:44','2025-11-07 16:36:29'),(10,'This week food','1. Origini, 2. Sandwitch',1,'2025-11-08 00:38:53','2025-11-08 00:38:53'),(11,'testing update by Ian','contented updated by Ian',5,'2026-01-24 18:27:49','2026-01-24 10:48:21');
/*!40000 ALTER TABLE `notes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refreshtokens`
--

DROP TABLE IF EXISTS `refreshtokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refreshtokens`
--

LOCK TABLES `refreshtokens` WRITE;
/*!40000 ALTER TABLE `refreshtokens` DISABLE KEYS */;
INSERT INTO `refreshtokens` VALUES ('073ddae3-5047-4e6f-9dfc-f38a34d4ff97',1,'2026-03-29 11:16:50.633164',0,'2026-03-22 11:16:50.925594'),('07747bcb-db10-4e5a-ad7d-910621ea875c',1,'2026-03-29 11:09:10.257358',0,'2026-03-22 11:09:10.576568'),('33f36a68-c913-431c-98a1-83d3bd2850e6',1,'2026-04-03 17:19:29.293671',0,'2026-03-27 17:19:29.561498'),('ea49b4c0-e116-47ef-afae-d4c1bcbc9b53',1,'2026-04-03 15:18:34.275773',0,'2026-03-27 15:18:34.604512'),('f60a18b8-45e9-4127-99e4-3a5b9ea3645d',1,'2026-03-29 06:42:28.458222',0,'2026-03-22 06:42:28.816239');
/*!40000 ALTER TABLE `refreshtokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sharednotes`
--

DROP TABLE IF EXISTS `sharednotes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sharednotes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `note_id` int NOT NULL,
  `shared_to_user_id` int NOT NULL,
  `permission` enum('READ','WRITE') DEFAULT 'READ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_sharednotes_note_user` (`note_id`,`shared_to_user_id`),
  UNIQUE KEY `UK3q7uklojr18hgkexrx1m1vmei` (`note_id`,`shared_to_user_id`),
  KEY `fk_sharednote_user` (`shared_to_user_id`),
  CONSTRAINT `fk_sharednote_note` FOREIGN KEY (`note_id`) REFERENCES `notes` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sharednote_user` FOREIGN KEY (`shared_to_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sharednotes`
--

LOCK TABLES `sharednotes` WRITE;
/*!40000 ALTER TABLE `sharednotes` DISABLE KEYS */;
INSERT INTO `sharednotes` VALUES (2,8,2,'READ'),(8,6,1,'READ'),(9,9,2,'WRITE'),(11,2,2,'WRITE');
/*!40000 ALTER TABLE `sharednotes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'ianteohyx','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW'),(2,'yteo0019','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW'),(3,'teohyx','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW'),(4,'ryantyc','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW'),(5,'user1','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW'),(6,'usre2','$2a$10$0AnuAOs0KJ3BGRe9vgjxN.2feZcVbgrxRIVtrBaMPeBn.rsz0EQcW');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'note_app'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-01 20:31:00
