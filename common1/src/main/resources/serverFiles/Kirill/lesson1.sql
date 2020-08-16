-- MySQL dump 10.13  Distrib 8.0.20, for Win64 (x86_64)
--
-- Host: localhost    Database: lesson1_db
-- ------------------------------------------------------
-- Server version	8.0.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `city_tbl`
--

DROP TABLE IF EXISTS `city_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `city_tbl` (
  `city_id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `type_id` tinyint NOT NULL,
  `name_fld` varchar(255) NOT NULL,
  `region_id` smallint unsigned DEFAULT NULL,
  `disctict_id` smallint unsigned DEFAULT NULL,
  `country_id` smallint unsigned DEFAULT NULL,
  PRIMARY KEY (`city_id`),
  KEY `fk_city_tbl_1_idx` (`type_id`),
  KEY `fk_city_tbl_2_idx` (`disctict_id`),
  KEY `fk_city_tbl_3_idx` (`region_id`),
  KEY `fk_city_tbl_4_idx` (`country_id`),
  CONSTRAINT `fk_city_tbl_1` FOREIGN KEY (`type_id`) REFERENCES `type_city_tbl` (`type_city_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_city_tbl_2` FOREIGN KEY (`disctict_id`) REFERENCES `district_tbl` (`district_id`),
  CONSTRAINT `fk_city_tbl_3` FOREIGN KEY (`region_id`) REFERENCES `region_tbl` (`region_id`),
  CONSTRAINT `fk_city_tbl_4` FOREIGN KEY (`country_id`) REFERENCES `country_tbl` (`country_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `city_tbl`
--

LOCK TABLES `city_tbl` WRITE;
/*!40000 ALTER TABLE `city_tbl` DISABLE KEYS */;
INSERT INTO `city_tbl` VALUES (1,1,'Moscow',NULL,NULL,1),(2,2,'Orsk',NULL,3,NULL),(3,1,'Orenburg',1,NULL,NULL),(4,1,'Washington',NULL,NULL,2),(5,3,'Saltykovka',NULL,2,NULL);
/*!40000 ALTER TABLE `city_tbl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `country_tbl`
--

DROP TABLE IF EXISTS `country_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `country_tbl` (
  `country_id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `country_fld` varchar(255) NOT NULL,
  PRIMARY KEY (`country_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `country_tbl`
--

LOCK TABLES `country_tbl` WRITE;
/*!40000 ALTER TABLE `country_tbl` DISABLE KEYS */;
INSERT INTO `country_tbl` VALUES (1,'Russia'),(2,'USA'),(3,'China');
/*!40000 ALTER TABLE `country_tbl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `district_tbl`
--

DROP TABLE IF EXISTS `district_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `district_tbl` (
  `district_id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `district_fld` varchar(255) NOT NULL,
  `region_id` smallint unsigned NOT NULL,
  PRIMARY KEY (`district_id`),
  KEY `fk_disctrict_tbl_1_idx` (`region_id`),
  CONSTRAINT `fk_disctrict_tbl_1` FOREIGN KEY (`region_id`) REFERENCES `region_tbl` (`region_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `district_tbl`
--

LOCK TABLES `district_tbl` WRITE;
/*!40000 ALTER TABLE `district_tbl` DISABLE KEYS */;
INSERT INTO `district_tbl` VALUES (1,'Sergievsko-Posadskiy',2),(2,'Balashihinskiy',2),(3,'Orskiy',1);
/*!40000 ALTER TABLE `district_tbl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `region_tbl`
--

DROP TABLE IF EXISTS `region_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region_tbl` (
  `region_id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `region_fld` varchar(255) NOT NULL,
  `country_id` smallint unsigned NOT NULL,
  PRIMARY KEY (`region_id`),
  KEY `fk_region_tbl_1_idx` (`country_id`),
  CONSTRAINT `fk_region_tbl_1` FOREIGN KEY (`country_id`) REFERENCES `country_tbl` (`country_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region_tbl`
--

LOCK TABLES `region_tbl` WRITE;
/*!40000 ALTER TABLE `region_tbl` DISABLE KEYS */;
INSERT INTO `region_tbl` VALUES (1,'Orenburgskaya region',1),(2,'Moscow region',1);
/*!40000 ALTER TABLE `region_tbl` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `type_city_tbl`
--

DROP TABLE IF EXISTS `type_city_tbl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `type_city_tbl` (
  `type_city_id` tinyint NOT NULL AUTO_INCREMENT,
  `type_city_fld` varchar(45) NOT NULL,
  PRIMARY KEY (`type_city_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `type_city_tbl`
--

LOCK TABLES `type_city_tbl` WRITE;
/*!40000 ALTER TABLE `type_city_tbl` DISABLE KEYS */;
INSERT INTO `type_city_tbl` VALUES (1,'city'),(2,'town'),(3,'village');
/*!40000 ALTER TABLE `type_city_tbl` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-22 23:36:38
