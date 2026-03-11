CREATE DATABASE  IF NOT EXISTS `booking_tour` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `booking_tour`;
-- MySQL dump 10.13  Distrib 8.0.43, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: booking_tour
-- ------------------------------------------------------
-- Server version	9.0.1

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
-- Table structure for table `cho_xac_nhan`
--

DROP TABLE IF EXISTS `cho_xac_nhan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cho_xac_nhan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_dat_cho` int DEFAULT NULL,
  `trang_thai` tinytext,
  `ngay_cap_nhat` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_dat_cho` (`id_dat_cho`),
  CONSTRAINT `cho_xac_nhan_ibfk_1` FOREIGN KEY (`id_dat_cho`) REFERENCES `dat_cho` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cho_xac_nhan`
--

LOCK TABLES `cho_xac_nhan` WRITE;
/*!40000 ALTER TABLE `cho_xac_nhan` DISABLE KEYS */;
INSERT INTO `cho_xac_nhan` VALUES (1,2,'Pending','2025-10-13 15:42:35'),(2,8,'Pending','2025-10-13 15:42:35'),(3,4,'Rejected','2025-10-13 15:42:35'),(4,11,'Approved','2025-10-13 15:42:35'),(5,12,'Approved','2025-10-13 15:42:35');
/*!40000 ALTER TABLE `cho_xac_nhan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chuyen_di`
--

DROP TABLE IF EXISTS `chuyen_di`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chuyen_di` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tieu_de` varchar(255) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `gia` decimal(10,2) DEFAULT NULL,
  `ngay_khoi_hanh` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  `id_phuong_tien` int DEFAULT NULL,
  `id_noi_luu_tru` int DEFAULT NULL,
  `noi_bat` tinyint(1) DEFAULT '0',
  `hinh_anh` varchar(255) DEFAULT NULL,
  `highlight` text,
  `id_diem_don` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  KEY `id_phuong_tien` (`id_phuong_tien`),
  KEY `id_noi_luu_tru` (`id_noi_luu_tru`),
  KEY `FKf7y4cdxg2tmd2m7p0s4jwspxo` (`id_diem_don`),
  CONSTRAINT `chuyen_di_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`),
  CONSTRAINT `chuyen_di_ibfk_2` FOREIGN KEY (`id_phuong_tien`) REFERENCES `phuong_tien` (`id`),
  CONSTRAINT `chuyen_di_ibfk_3` FOREIGN KEY (`id_noi_luu_tru`) REFERENCES `noi_luu_tru` (`id`),
  CONSTRAINT `FKf7y4cdxg2tmd2m7p0s4jwspxo` FOREIGN KEY (`id_diem_don`) REFERENCES `diem_don` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chuyen_di`
--

LOCK TABLES `chuyen_di` WRITE;
/*!40000 ALTER TABLE `chuyen_di` DISABLE KEYS */;
INSERT INTO `chuyen_di` VALUES (1,'Tour Hà Nội 3N2Đ','Tham quan Hồ Gươm, Văn Miếu, Lăng Bác',2500000.00,'2026-03-18','2026-03-25',1,3,1,1,'/anh/chuyendi/hanoi.jpg','Khám phá chiều sâu văn hóa của \"Thủ đô ngàn năm văn hiến\". Quý khách sẽ có những phút giây lắng đọng tại Lăng Bác, tìm hiểu lịch sử tại Văn Miếu - Quốc Tử Giám. Đặc biệt, trải nghiệm Xích lô dạo quanh 36 phố phường vào buổi xế chiều và thưởng thức ẩm thực tinh tế như Phở Thìn, Chả cá Lã Vọng sẽ là ký ức khó quên.',1),(2,'Tour 4N3Đ Đà Nẵng - Hội An','Bà Nà Hill, Hội An cổ',4200000.00,'2026-03-09','2026-03-15',2,2,3,0,'/anh/diemden/danang.jpg','Hành trình xuyên qua những vùng đất di sản. Chạm tay vào \"mây ngàn\" tại Bà Nà Hills với cây Cầu Vàng biểu tượng. Khi màn đêm buông xuống, quý khách sẽ được thả mình vào không gian lung linh của Phố cổ Hội An, tự tay thả hoa đăng trên sông Hoài và thưởng thức show diễn \"Ký ức Hội An\" đẳng cấp thế giới.',NULL),(3,'Khám phá Huế 2N1Đ','Ăn uống cung đình, sông Hương',1800000.00,'2026-03-09','2026-03-15',3,3,5,0,'/anh/diemden/hue.jpg','Trở về không gian triều đình phong kiến với hệ thống Đại Nội và các lăng tẩm uy nghi (Lăng Tự Đức, Khải Định). Điểm khác biệt chính là trải nghiệm Nghe ca Huế trên sông Hương và thưởng thức \"Cơm Vua\" - nơi quý khách được hóa thân thành hoàng thân quốc thích trong bộ lễ phục triều đình.',NULL),(4,'Hạ Long Bay 2N1Đ','Du thuyền Vịnh Hạ Long',3000000.00,'2026-03-03','2026-03-04',4,4,6,0,'/anh/diemden/halong.jpg','Tận hưởng kỳ nghỉ dưỡng thượng lưu trên Du thuyền 5 sao. Quý khách sẽ được chèo thuyền Kayak xuyên qua các hang động đá vôi nghìn năm tuổi, đón bình minh với bài tập Tai-chi trên boong tàu và thưởng thức tiệc hải sản tươi sống giữa lòng di sản thiên nhiên thế giới.',NULL),(5,'Nha Trang Beach Relax 3N','Tắm biển, lặn ngắm san hô',3500000.00,'2026-03-03','2026-03-05',5,3,7,0,'/anh/diemden/nhatrang.jpg','Sự kết hợp hoàn hảo giữa nhịp sống phố thị và sự tĩnh lặng của biển khơi. Tour tập trung vào việc tối ưu thời gian để quý khách vừa có thể check-in những biểu tượng của Thủ đô, vừa kịp lúc bắt trọn khoảnh khắc hoàng hôn buông trên vịnh biển từ những góc nhìn đẹp nhất.',NULL),(6,'Phú Quốc Resort 4N','Resort 5*, câu cá, lặn',7000000.00,'2026-03-03','2026-03-06',6,9,8,0,'/anh/diemden/hue.jpg','Nghỉ dưỡng tại những bãi cát trắng mịn nhất Việt Nam. Chương trình tập trung vào trải nghiệm cá nhân hóa tại VinWonders, tắm bùn khoáng nóng thư giãn phục hồi sức khỏe và tham gia các trò chơi thể thao nước cảm giác mạnh như dù lượn, lặn ngắm san hô tại Vịnh San Hô.',NULL),(7,'Sa Pa Trek 2N','Trekking bản làng, cáp treo',2000000.00,'2025-05-30','2025-06-01',7,7,9,0,'/anh/diemden/sapa.jpg','Một hành trình nghỉ dưỡng thực thụ tại các resort cao cấp. Điểm nhấn là chuyến Cáp treo vượt biển dài nhất thế giới đi hòn Thơm, khám phá \"Thành phố không ngủ\" Grand World và thưởng thức đặc sản bún quậy trứ danh cùng rượu sim nồng nàn.',NULL),(8,'Cần Thơ Miền Tây 2N1Đ','Chợ nổi, miệt vườn',1600000.00,'2026-03-04','2026-03-06',8,1,10,0,'/anh/diemden/cantho.jpg','Sự giao thoa giữa hai vùng biển đẹp nhất Việt Nam. Quý khách sẽ thấy được sự khác biệt giữa vẻ đẹp hiện đại, năng động của Đà Nẵng và nét hoang sơ, thơ mộng của Phú Quốc. Đây là tour dành cho những tín đồ \"cuồng biển\" muốn chinh phục mọi cung đường xanh.',1),(9,'Beijing City Tour 4N','Tường thành, Tử Cấm Thành',1650000.00,'2025-10-01','2025-10-05',9,5,11,0,'/anh/diemden/thuonghai.jpg','Chạm tay vào kỳ quan Vạn Lý Trường Thành, khám phá sự huyền bí của Tử Cấm Thành và thưởng thức món Vịt quay Bắc Kinh nguyên bản trong không gian cung đình xưa.',NULL),(10,'Shanghai Highlights 3N','Tham quan Thượng Hải, Bến Thượng Hải',1300000.00,'2026-03-11','2026-03-13',10,5,12,1,'/anh/chuyendi/shanghai.jpg','Tập trung vào nhịp sống sôi động bậc nhất Trung Hoa. Dạo bước trên phố đi bộ Nam Kinh, chiêm bái Dự Viên và tận hưởng sự lộng lẫy của các tòa nhà chọc trời về đêm.',NULL),(11,'Zhangjiajie Nature 3N','Thiên nhiên Trương Gia Giới',1800000.00,'2025-11-01','2025-11-03',11,5,13,0,'/anh/diemden/hue.jpg','Lạc vào hành tinh Pandora ngoài đời thực với các cột đá cao vút tại Viên Gia Giới. Trải nghiệm cảm giác mạnh trên Cầu kính Đại Hiệp Cốc và đi thang máy Bách Lộ dài nhất thế giới.',NULL),(12,'Tokyo Family 5N','Tokyo Disneyland, Asakusa',2000000.00,'2026-03-03','2026-03-07',12,6,14,0,'/anh/diemden/tokyo.jpg','Tour lý tưởng cho gia đình. Khám phá Disneyland thần tiên, giao lộ Shibuya sầm uất và trải nghiệm văn hóa Anime tại Akihabara cùng với hoạt động làm Sushi thủ công.',NULL),(13,'Kyoto Culture 3N','Đền chùa, trà đạo',1800000.00,'2026-03-03','2026-03-05',13,6,14,0,'/anh/diemden/kyoto.jpg','Tìm về sự an yên tại các ngôi đền nghìn năm tuổi như Thanh Thủy Tự (Kiyomizu-dera). Điểm nhấn là đi bộ dưới hàng ngàn cổng Torii đỏ rực tại Fushimi Inari và gặp gỡ các nàng Geisha tại phố Gion.',NULL),(14,'Osaka Food Tour 2N','Món ăn đường phố, Dotonbori',1700000.00,'2025-12-11','2025-12-12',14,6,14,0,'/anh/diemden/osaka.jpg','Thiên đường cho những tín đồ ẩm thực. Càn quét khu phố Dotonbori với Takoyaki, Okonomiyaki. Tham quan lâu đài Osaka uy nghiêm và khu vui chơi Universal Studios.',NULL),(15,'Seoul City 4N','Mua sắm Myeongdong, palaces',2800000.00,'2025-09-15','2025-09-19',15,8,15,0,'/anh/diemden/seoul.jpg','Khám phá \"Làn sóng Hallyu\" tại thủ đô Hàn Quốc. Mặc Hanbok check-in cung điện Gyeongbokgung, mua sắm tại Myeongdong và ngắm toàn cảnh thành phố từ tháp Namsan lãng mạn.',NULL),(16,'Hà Nội - Hạ Long 2N','Combo Hà Nội + Hạ Long',3200000.00,'2026-03-03','2026-03-05',4,4,6,0,'/anh/diemden/hanoi.jpg','Sự giao thoa giữa hai vùng biển đẹp nhất Việt Nam. Quý khách sẽ thấy được sự khác biệt giữa vẻ đẹp hiện đại, năng động của Đà Nẵng và nét hoang sơ, thơ mộng của Phú Quốc. Đây là tour dành cho những tín đồ \"cuồng biển\" muốn chinh phục mọi cung đường xanh.',NULL),(17,'Đà Nẵng - Phú Quốc 5N','Đà Nẵng + Phú Quốc nghỉ dưỡng',9000000.00,'2025-09-10','2025-09-14',6,2,8,0,'/anh/diemden/phuquoc.jpg','Sự giao thoa giữa hai vùng biển đẹp nhất Việt Nam. Quý khách sẽ thấy được sự khác biệt giữa vẻ đẹp hiện đại, năng động của Đà Nẵng và nét hoang sơ, thơ mộng của Phú Quốc. Đây là tour dành cho những tín đồ \"cuồng biển\" muốn chinh phục mọi cung đường xanh.',NULL),(18,'Tour Bắc Kinh - Thượng Hải 7N','Kết hợp Bắc Kinh và Thượng Hải',2200000.00,'2026-03-03','2026-03-05',9,5,12,1,'/anh/chuyendi/backinh.jpg','Hành trình kết nối giữa quá khứ và tương lai. Từ vẻ cổ kính của cung điện Bắc Kinh đến sự xa hoa, hiện đại của Bến Thượng Hải và tháp truyền hình Minh Châu Phương Đông.',NULL);
/*!40000 ALTER TABLE `chuyen_di` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `content` text,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `guest_number` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tittle` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contacts`
--

LOCK TABLES `contacts` WRITE;
/*!40000 ALTER TABLE `contacts` DISABLE KEYS */;
INSERT INTO `contacts` VALUES (1,'Hải dương','thắc mắc cách di chuyển đến điểm đón cần giải thích\r\n','2026-03-11 11:54:15.915214','minhd4360@gmail.com',2,'Nguyễn minh đức','0866147595','NEW','Trợ giúp cách di chuyển','SUPPORT');
/*!40000 ALTER TABLE `contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_gia`
--

DROP TABLE IF EXISTS `danh_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_gia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_chuyen_di` int NOT NULL,
  `id_nguoi_dung` int NOT NULL,
  `diem` int DEFAULT NULL,
  `binh_luan` varchar(255) DEFAULT NULL,
  `ngay_danh_gia` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  CONSTRAINT `danh_gia_ibfk_1` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`),
  CONSTRAINT `danh_gia_ibfk_2` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `danh_gia_chk_1` CHECK ((`diem` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_gia`
--

LOCK TABLES `danh_gia` WRITE;
/*!40000 ALTER TABLE `danh_gia` DISABLE KEYS */;
INSERT INTO `danh_gia` VALUES (1,1,1,5,'Chuyến đi rất tuyệt vời, hướng dẫn viên nhiệt tình.','2025-01-03 03:15:00'),(2,1,2,4,'Khá hài lòng, nhưng khách sạn hơi xa trung tâm.','2025-01-05 07:20:00'),(3,2,3,5,'Dịch vụ tốt, phong cảnh đẹp.','2025-01-10 02:45:00'),(4,2,4,3,'Ổn nhưng lịch trình hơi dày.','2025-01-11 09:30:00'),(5,3,1,4,'Giá hợp lý, đi vui.','2025-01-15 01:00:00'),(6,3,5,2,'Không đúng mô tả, hơi thất vọng.','2025-01-16 12:10:00'),(7,4,2,5,'Quá tuyệt! Mọi thứ hoàn hảo.','2025-01-18 06:25:00'),(8,4,3,4,'Tour ổn, thời tiết đẹp.','2025-01-19 04:55:00'),(9,5,4,5,'Hướng dẫn viên thân thiện, ăn uống ngon.','2025-01-20 10:40:00'),(10,5,5,3,'Cũng được, hơi đông người.','2025-01-21 13:15:00'),(11,1,9,2,'hay','2026-03-03 08:29:12'),(12,2,9,5,'tốt','2026-03-07 22:17:20');
/*!40000 ALTER TABLE `danh_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dat_cho`
--

DROP TABLE IF EXISTS `dat_cho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dat_cho` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_nguoi_dung` int DEFAULT NULL,
  `id_chuyen_di` int DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `ngay_dat` date DEFAULT NULL,
  `trang_thai` varchar(50) DEFAULT NULL,
  `id_ma_giam_gia` int DEFAULT NULL,
  `dia_chi` varchar(500) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ghi_chu` text,
  `ho_ten` varchar(255) DEFAULT NULL,
  `so_dien_thoai` varchar(20) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `tong_gia` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  KEY `id_ma_giam_gia` (`id_ma_giam_gia`),
  CONSTRAINT `dat_cho_ibfk_1` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `dat_cho_ibfk_2` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`),
  CONSTRAINT `dat_cho_ibfk_3` FOREIGN KEY (`id_ma_giam_gia`) REFERENCES `ma_giam_gia` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dat_cho`
--

LOCK TABLES `dat_cho` WRITE;
/*!40000 ALTER TABLE `dat_cho` DISABLE KEYS */;
INSERT INTO `dat_cho` VALUES (1,1,1,2,'2025-05-01','CONFIRMED',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,2,2,4,'2025-05-02','Pending',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,3,6,2,'2025-06-10','CONFIRMED',3,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,4,4,3,'2025-05-20','CONFIRMED',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,5,5,1,'2025-06-21','CONFIRMED',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,6,9,2,'2025-09-01','CONFIRMED',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,7,12,2,'2025-11-15','CONFIRMED',4,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,8,3,1,'2025-05-18','PENDING',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,1,7,2,'2025-05-25','CONFIRMED',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,2,14,2,'2025-09-20','CONFIRMED',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(11,3,10,3,'2025-09-10','CONFIRMED',2,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(12,5,16,4,'2025-05-10','CONFIRMED',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(25,9,1,2,'2026-03-05','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-05 22:47:37.905667',5600000),(26,9,1,2,'2026-03-05','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-05 23:06:08.798173',5600000),(27,9,1,1,'2026-03-05','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-05 23:09:39.958195',2800000),(28,4,18,1,'2026-03-05','PAID',NULL,'an đông','lla@example.com','','Lưu Thị Lan Anh','0866147595','2026-03-05 23:43:21.104281',5108000),(29,4,1,2,'2026-03-05','FAILED',NULL,'an đoài','lla@example.com','','Lưu Thị Lan Anh','0916928559','2026-03-05 23:55:13.171559',5600000),(30,9,2,2,'2026-03-05','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-05 23:58:04.781565',18000000),(31,9,2,1,'2026-03-06','PENDING',NULL,'aa','minhd4360@gmail.com','','Nguyễn Minh Đức','0123111111','2026-03-06 21:10:19.628142',7000000),(32,9,2,1,'2026-03-06','PENDING',NULL,'aa','minhd4360@gmail.com','','Nguyễn Minh Đức','0123111111','2026-03-06 23:02:25.091998',7000000),(33,9,2,1,'2026-03-08','PENDING',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-08 12:18:54.753182',9150000),(34,9,1,1,'2026-03-11','PENDING',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-11 10:41:48.690300',2800000),(35,9,1,1,'2026-03-11','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-11 10:43:58.032799',2800000),(36,9,3,1,'2026-03-11','PAID',NULL,'an đông','minhd4360@gmail.com','','Nguyễn Minh Đức','0866147595','2026-03-11 11:21:08.213231',2100000);
/*!40000 ALTER TABLE `dat_cho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diem_den`
--

DROP TABLE IF EXISTS `diem_den`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diem_den` (
  `id` int NOT NULL AUTO_INCREMENT,
  `thanh_pho` varchar(255) DEFAULT NULL,
  `quoc_gia` varchar(255) DEFAULT NULL,
  `chau_luc` varchar(255) DEFAULT NULL,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `noi_bat` tinyint(1) DEFAULT '0',
  `mo_ta` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diem_den`
--

LOCK TABLES `diem_den` WRITE;
/*!40000 ALTER TABLE `diem_den` DISABLE KEYS */;
INSERT INTO `diem_den` VALUES (1,'Hà Nội','Việt Nam','Á Đông','/anh/diemden/hanoi.jpg',1,NULL),(2,'Đà Nẵng','Việt Nam','Á Đông','/anh/diemden/danang.jpg',1,NULL),(3,'Huế','Việt Nam','Á Đông','/anh/diemden/hue.jpg',0,NULL),(4,'Hạ Long','Việt Nam','Á Đông','/anh/diemden/halong.jpg',1,NULL),(5,'Nha Trang','Việt Nam','Á Đông','/anh/diemden/nhatrang.jpg',0,NULL),(6,'Phú Quốc','Việt Nam','Á Đông','/anh/diemden/phuquoc.jpg',0,NULL),(7,'Sa Pa','Việt Nam','Á Đông','/anh/diemden/sapa.jpg',1,NULL),(8,'Cần Thơ','Việt Nam','Á Đông','/anh/diemden/cantho.jpg',0,NULL),(9,'Bắc Kinh','Trung Quốc','Á Đông','/anh/diemden/backinh.jpg',0,NULL),(10,'Thượng Hải','Trung Quốc','Á Đông','/anh/diemden/thuonghai.jpg',1,NULL),(11,'Trương Gia Giới','Trung Quốc','Á Đông','/anh/diemden/truonggiagioi.jpg',0,NULL),(12,'Tokyo','Nhật Bản','Á Đông','/anh/diemden/tokyo.jpg',1,NULL),(13,'Kyoto','Nhật Bản','Á Đông','/anh/diemden/kyoto.jpg',0,NULL),(14,'Osaka','Nhật Bản','Á Đông','/anh/diemden/osaka.jpg',0,NULL),(15,'Seoul','Hàn Quốc','Á Đông','/anh/diemden/seoul.jpg',0,NULL);
/*!40000 ALTER TABLE `diem_den` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diem_don`
--

DROP TABLE IF EXISTS `diem_don`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diem_don` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diem_don`
--

LOCK TABLES `diem_don` WRITE;
/*!40000 ALTER TABLE `diem_don` DISABLE KEYS */;
INSERT INTO `diem_don` VALUES (1,'Hà Nội'),(2,'Hồ Chí Minh'),(3,'Đà Nẵng');
/*!40000 ALTER TABLE `diem_don` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ma_giam_gia`
--

DROP TABLE IF EXISTS `ma_giam_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ma_giam_gia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ma` varchar(50) DEFAULT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `phan_tram_giam` int DEFAULT NULL,
  `ngay_bat_dau` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ma_giam_gia`
--

LOCK TABLES `ma_giam_gia` WRITE;
/*!40000 ALTER TABLE `ma_giam_gia` DISABLE KEYS */;
INSERT INTO `ma_giam_gia` VALUES (1,'SUMMER25','Giảm hè 25%',25,'2025-06-01','2025-08-31'),(2,'WINTER10','Giảm mùa đông 10%',10,'2025-12-01','2026-02-28'),(3,'SPRING15','Giảm xuân 15%',15,'2025-03-01','2025-05-31'),(4,'VIP50','Ưu đãi cho khách VIP',50,'2025-01-01','2025-12-31'),(5,'NEWYEAR20','Khuyến mãi Tết',20,'2026-01-01','2026-01-15');
/*!40000 ALTER TABLE `ma_giam_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ngay_khoi_hanh`
--

DROP TABLE IF EXISTS `ngay_khoi_hanh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ngay_khoi_hanh` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nam` int DEFAULT NULL,
  `ngay` date NOT NULL,
  `thang` int DEFAULT NULL,
  `id_chuyen_di` int NOT NULL,
  `gia_ve` double DEFAULT NULL,
  `gia_ve_di` double DEFAULT NULL,
  `gia_ve_ve` double DEFAULT NULL,
  `gio_bay_di` varchar(255) DEFAULT NULL,
  `gio_bay_ve` varchar(255) DEFAULT NULL,
  `gio_den_di` varchar(255) DEFAULT NULL,
  `gio_den_ve` varchar(255) DEFAULT NULL,
  `ma_chuyen_bay_di` varchar(255) DEFAULT NULL,
  `ma_chuyen_bay_ve` varchar(255) DEFAULT NULL,
  `ngay_ve` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ag0i3l2vuvf5mc52pw3nk1mo` (`id_chuyen_di`,`ngay`),
  CONSTRAINT `FKde2b6vwklfoia4jx3pmb9r2m6` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ngay_khoi_hanh`
--

LOCK TABLES `ngay_khoi_hanh` WRITE;
/*!40000 ALTER TABLE `ngay_khoi_hanh` DISABLE KEYS */;
INSERT INTO `ngay_khoi_hanh` VALUES (10,2026,'2026-03-09',3,2,NULL,600000,2200000,'06:45','15:45','08:15','17:42','QH460','VN116','2026-03-12'),(11,2026,'2026-03-10',3,2,NULL,2200000,2750000,'10:15','16:30','11:48','18:12','QH638','VN847','2026-03-13'),(12,2026,'2026-03-26',3,2,NULL,2800000,2000000,'09:00','18:45','10:40','20:39','VN441','VN816','2026-03-29'),(13,2026,'2026-05-02',5,1,NULL,300000,0,'14:25','22:20','16:30','00:20','VJ135','VJ162','2026-05-05'),(17,2026,'2026-03-03',3,3,NULL,1454000,1454000,'17:10','05:00','19:20','07:10','VJ1149','VJ194','2026-03-04'),(19,2026,'2026-03-03',3,12,NULL,1454000,1454000,'17:45','05:00','19:55','07:10','VJ1151','VJ194','2026-03-07'),(20,2026,'2026-03-11',3,12,NULL,1454000,1454000,'05:30','05:00','07:40','07:10','VJ197','VJ194','2026-03-14'),(21,2026,'2026-03-03',3,5,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-05'),(22,2026,'2026-03-06',3,5,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-09'),(23,2026,'2026-03-11',3,1,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-13'),(26,2026,'2026-03-04',3,8,NULL,1454000,1454000,'05:30','05:00','07:40','07:10','VJ197','VJ194','2026-03-11'),(27,2026,'2026-03-06',3,18,NULL,1454000,1454000,'05:30','05:00','07:40','07:10','VJ197','VJ194','2026-03-12'),(28,2026,'2026-03-18',3,1,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-20'),(29,2026,'2026-03-20',3,1,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-22'),(33,2026,'2026-03-11',3,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-03-14'),(34,2026,'2026-03-19',3,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-03-22'),(38,2026,'2026-03-18',3,3,NULL,300000,0,'08:00','14:00','12:00','18:00','BUS','BUS','2026-03-20'),(42,2026,'2026-03-12',3,10,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-03-14');
/*!40000 ALTER TABLE `ngay_khoi_hanh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nguoi_dung`
--

DROP TABLE IF EXISTS `nguoi_dung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nguoi_dung` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  `vai_tro` tinytext,
  `ngay_tao` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `ho_ten` varchar(255) DEFAULT NULL,
  `number` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `anh_dai_dien` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ten_dang_nhap` (`ten_dang_nhap`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguoi_dung`
--

LOCK TABLES `nguoi_dung` WRITE;
/*!40000 ALTER TABLE `nguoi_dung` DISABLE KEYS */;
INSERT INTO `nguoi_dung` VALUES (1,'hongnhung','hongnhung@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Nguyễn Thị Hồng Nhung',NULL,NULL,NULL),(2,'nguyenvana','nvana@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Nguyễn Văn An ',NULL,NULL,NULL),(3,'tranthuan','tranthuan@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Trần Thanh Tuấn',NULL,NULL,NULL),(4,'llan_4','lla@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Lưu Thị Lan Anh','0916928559',NULL,NULL),(5,'lethuy','lethuy@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Lê Thị Thủy',NULL,NULL,NULL),(6,'phamanh','phamanh@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Phạm Ngọc Anh',NULL,NULL,NULL),(7,'phamthuy','phamthuy@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Phạm Thanh Thúy',NULL,NULL,NULL),(8,'khanhleo','khanhleo@example.com','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','USER','2025-10-13 15:42:35','Khánh Lê Messi',NULL,NULL,NULL),(9,'duczaki','','$2a$10$WEi.lNJlIL9YRq/XG2whiO0G.2pd9PhEzIeNpr6iKrFMLmJ.T5A9q','ADMIN','2025-10-13 15:42:35','Nguyễn Minh Đức','',NULL,'/anh/user/50349fc3-78be-413b-b250-28d2af408092_c7e90d43-28a7-45a7-a0a4-f5b42b3d2798.png'),(11,'nvhuwng','nvh234205@gmail.com','123456','USER','2026-03-09 07:28:28','Ngô Văn Hưng','0352674198',NULL,NULL),(12,'minhduc190625@gmail.com','minhduc190625@gmail.com',NULL,'USER','2026-03-10 19:16:53','Đức Nguyễn Minh',NULL,'GOOGLE',NULL),(13,'facebook_1646175743402967',NULL,NULL,'USER','2026-03-10 19:49:00','Minh Đức',NULL,'FACEBOOK',NULL);
/*!40000 ALTER TABLE `nguoi_dung` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `noi_luu_tru`
--

DROP TABLE IF EXISTS `noi_luu_tru`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `noi_luu_tru` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten` varchar(255) DEFAULT NULL,
  `loai` varchar(100) DEFAULT NULL,
  `dia_chi` varchar(255) DEFAULT NULL,
  `gia` decimal(10,2) DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  CONSTRAINT `noi_luu_tru_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `noi_luu_tru`
--

LOCK TABLES `noi_luu_tru` WRITE;
/*!40000 ALTER TABLE `noi_luu_tru` DISABLE KEYS */;
INSERT INTO `noi_luu_tru` VALUES (1,'Khách sạn Hà Nội 1','Hotel','Hoàn Kiếm, Hà Nội',1200000.00,1),(2,'Nhà nghỉ Hà Nội giá rẻ','Homestay','Tây Hồ, Hà Nội',350000.00,1),(3,'Sunrise Đà Nẵng Hotel','Hotel','Mỹ Khê, Đà Nẵng',900000.00,2),(4,'Apartment Đà Nẵng Center','Apartment','Hải Châu, Đà Nẵng',650000.00,2),(5,'Khách sạn Huế River','Hotel','Huế',700000.00,3),(6,'Resort Hạ Long Bay','Resort','Hạ Long',1500000.00,4),(7,'Hotel Nha Trang Beach','Hotel','Nha Trang',1100000.00,5),(8,'Resort Phú Quốc 5*','Resort','Phú Quốc',2200000.00,6),(9,'Homestay Sa Pa View','Homestay','Sa Pa',400000.00,7),(10,'Khách sạn Cần Thơ Riverside','Hotel','Cần Thơ',550000.00,8),(11,'Beijing Grand Hotel','Hotel','Bắc Kinh',1300.00,9),(12,'Shanghai River Hotel','Hotel','Thượng Hải',1400.00,10),(13,'Zhangjiajie Inn','Inn','Trương Gia Giới',500.00,11),(14,'Tokyo Central Hotel','Hotel','Tokyo',15000.00,12),(15,'Seoul Cozy Stay','Hotel','Seoul',1200000.00,15);
/*!40000 ALTER TABLE `noi_luu_tru` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `phuong_tien`
--

DROP TABLE IF EXISTS `phuong_tien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `phuong_tien` (
  `id` int NOT NULL AUTO_INCREMENT,
  `loai` tinytext,
  `hang` varchar(255) DEFAULT NULL,
  `id_diem_den` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_diem_den` (`id_diem_den`),
  CONSTRAINT `phuong_tien_ibfk_1` FOREIGN KEY (`id_diem_den`) REFERENCES `diem_den` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `phuong_tien`
--

LOCK TABLES `phuong_tien` WRITE;
/*!40000 ALTER TABLE `phuong_tien` DISABLE KEYS */;
INSERT INTO `phuong_tien` VALUES (1,'Plane','Vietnam Airlines',1),(2,'Plane','Vietjet',2),(3,'Bus','SinhCafe Bus',2),(4,'Plane','Vietjet',4),(5,'Plane','China Eastern',9),(6,'Plane','ANA',12),(7,'Bus','Local Bus',7),(8,'Plane','Korean Air',15),(9,'Plane','Ferry Phu Quoc',6);
/*!40000 ALTER TABLE `phuong_tien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quan_ly_cho`
--

DROP TABLE IF EXISTS `quan_ly_cho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quan_ly_cho` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_chuyen_di` int DEFAULT NULL,
  `tong_so_cho` int DEFAULT NULL,
  `da_dat` int DEFAULT NULL,
  `con_lai` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  CONSTRAINT `quan_ly_cho_ibfk_1` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quan_ly_cho`
--

LOCK TABLES `quan_ly_cho` WRITE;
/*!40000 ALTER TABLE `quan_ly_cho` DISABLE KEYS */;
INSERT INTO `quan_ly_cho` VALUES (1,1,30,5,25),(2,2,40,10,30),(3,3,20,3,17),(4,4,25,8,17),(5,5,30,12,18),(6,6,20,5,15),(7,7,18,6,12),(8,8,15,2,13),(9,9,50,20,30),(10,10,45,22,23),(11,11,25,5,20),(12,12,35,10,25);
/*!40000 ALTER TABLE `quan_ly_cho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `ten_dang_nhap` varchar(255) DEFAULT NULL,
  `ngay_tao` datetime(6) DEFAULT NULL,
  `vai_tro` varchar(20) NOT NULL,
  `ho_ten` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yeu_thich`
--

DROP TABLE IF EXISTS `yeu_thich`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `yeu_thich` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_nguoi_dung` int DEFAULT NULL,
  `id_chuyen_di` int DEFAULT NULL,
  `ngay_them` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_nguoi_dung` (`id_nguoi_dung`),
  KEY `id_chuyen_di` (`id_chuyen_di`),
  CONSTRAINT `yeu_thich_ibfk_1` FOREIGN KEY (`id_nguoi_dung`) REFERENCES `nguoi_dung` (`id`),
  CONSTRAINT `yeu_thich_ibfk_2` FOREIGN KEY (`id_chuyen_di`) REFERENCES `chuyen_di` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeu_thich`
--

LOCK TABLES `yeu_thich` WRITE;
/*!40000 ALTER TABLE `yeu_thich` DISABLE KEYS */;
INSERT INTO `yeu_thich` VALUES (1,1,2,'2025-10-13 15:42:35'),(2,1,6,'2025-10-13 15:42:35'),(3,2,1,'2025-10-13 15:42:35'),(4,3,9,'2025-10-13 15:42:35'),(5,5,6,'2025-10-13 15:42:35'),(6,7,5,'2025-10-13 15:42:35'),(7,8,12,'2025-10-13 15:42:35'),(8,2,14,'2025-10-13 15:42:35'),(11,9,2,'2026-01-16 00:58:36'),(14,1,4,'2026-01-28 03:22:08'),(16,9,1,'2026-03-03 03:25:59'),(17,4,1,'2026-03-05 09:47:31');
/*!40000 ALTER TABLE `yeu_thich` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-11 12:17:01
