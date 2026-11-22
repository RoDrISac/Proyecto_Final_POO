/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.19-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: colores_creaciones
-- ------------------------------------------------------
-- Server version	10.11.19-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `colores_creaciones`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `colores_creaciones` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `colores_creaciones`;

--
-- Table structure for table `auditoria`
--

DROP TABLE IF EXISTS `auditoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria` (
  `id_registro` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime(3) NOT NULL,
  `id_usuario` varchar(24) DEFAULT NULL,
  `accion` varchar(40) NOT NULL,
  `detalle` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id_registro`),
  KEY `ix_auditoria_fecha` (`fecha`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditoria`
--

LOCK TABLES `auditoria` WRITE;
/*!40000 ALTER TABLE `auditoria` DISABLE KEYS */;
INSERT INTO `auditoria` VALUES
(1,'2026-11-23 20:07:41.000','SISTEMA','CARGA_INICIAL','Datos de demostración del taller de cerámica');
/*!40000 ALTER TABLE `auditoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cierres_mes`
--

DROP TABLE IF EXISTS `cierres_mes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cierres_mes` (
  `periodo` char(7) NOT NULL CHECK (`periodo` regexp '^[0-9]{4}-[0-1][0-9]$'),
  PRIMARY KEY (`periodo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cierres_mes`
--

LOCK TABLES `cierres_mes` WRITE;
/*!40000 ALTER TABLE `cierres_mes` DISABLE KEYS */;
/*!40000 ALTER TABLE `cierres_mes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id_cliente` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `apellido` varchar(120) NOT NULL,
  `dni` char(8) NOT NULL CHECK (`dni` regexp '^[0-9]{8}$'),
  `direccion` varchar(200) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(120) DEFAULT NULL,
  `categoria` varchar(40) NOT NULL,
  PRIMARY KEY (`id_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES
('C000','Clientes','Varios','00000000','','','','Público general'),
('C001','Ana','Quispe','12345678','Jr. Junín 198, Cajamarca','987654321','ana.quispe@example.com','Coleccionista'),
('C002','Pedro','Sandoval','87654321','Av. Atahualpa 540, Cajamarca','912345678','pedro.sandoval@example.com','Decorador de interiores'),
('C003','Lucía','Manzilla','34251678','Jr. Del Comercio 310, Cajamarca','946372645','lucia.manzilla@example.com','Turista'),
('C004','Ricardo','Salcedo','45612378','Av. Vía de Evitamiento 1200, Cajamarca','976543210','ricardo.salcedo@example.com','Decorador de interiores'),
('C005','Mariela','Chávez','70112233','Lima','955112233','mariela.chavez@example.com','Turista'),
('C006','Jorge','Tello','41239876','Jr. Amalia Puga 455, Cajamarca','944556677','','Público general');
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consumos_materia`
--

DROP TABLE IF EXISTS `consumos_materia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `consumos_materia` (
  `id_operacion` varchar(10) NOT NULL,
  `linea` int(11) NOT NULL,
  `id_materia` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `unidad` varchar(10) NOT NULL,
  `cantidad` decimal(14,3) NOT NULL,
  `costo_unitario` decimal(12,4) NOT NULL,
  PRIMARY KEY (`id_operacion`,`linea`),
  CONSTRAINT `consumos_materia_ibfk_1` FOREIGN KEY (`id_operacion`) REFERENCES `operaciones_produccion` (`id_operacion`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consumos_materia`
--

LOCK TABLES `consumos_materia` WRITE;
/*!40000 ALTER TABLE `consumos_materia` DISABLE KEYS */;
INSERT INTO `consumos_materia` VALUES
('FA0001',1,'MP01','Arcilla roja de Cajamarca','kg',3.600,1.2000),
('FA0001',2,'MP04','Engobe blanco','litro',0.160,22.0000),
('FA0001',3,'MP05','Pigmento óxido de hierro','kg',0.040,35.0000),
('FA0001',4,'MP07','Gas GLP para horno','kg',1.400,4.3308),
('FA0001',5,'MP08','Caja y embalaje','unidad',2.000,0.8000),
('PR0001',1,'MP01','Arcilla roja de Cajamarca','kg',24.000,1.2000),
('PR0001',2,'MP04','Engobe blanco','litro',1.000,22.0000),
('PR0001',3,'MP03','Esmalte transparente','litro',1.600,28.0000),
('PR0001',4,'MP05','Pigmento óxido de hierro','kg',0.200,35.0000),
('PR0001',5,'MP07','Gas GLP para horno','kg',8.000,4.2000),
('PR0001',6,'MP08','Caja y embalaje','unidad',20.000,0.8000),
('PR0002',1,'MP01','Arcilla roja de Cajamarca','kg',10.000,1.2000),
('PR0002',2,'MP05','Pigmento óxido de hierro','kg',0.120,35.0000),
('PR0002',3,'MP07','Gas GLP para horno','kg',3.200,4.2000),
('PR0002',4,'MP08','Caja y embalaje','unidad',40.000,0.8000),
('PR0003',1,'MP02','Caolín (arcilla blanca)','kg',10.500,2.5000),
('PR0003',2,'MP03','Esmalte transparente','litro',1.500,28.0000),
('PR0003',3,'MP06','Pigmento óxido de cobalto','kg',0.060,120.0000),
('PR0003',4,'MP07','Gas GLP para horno','kg',4.500,4.3308),
('PR0003',5,'MP08','Caja y embalaje','unidad',30.000,0.8000),
('PR0004',1,'MP01','Arcilla roja de Cajamarca','kg',10.000,1.2000),
('PR0004',2,'MP03','Esmalte transparente','litro',0.600,28.0000),
('PR0004',3,'MP05','Pigmento óxido de hierro','kg',0.080,35.0000),
('PR0004',4,'MP06','Pigmento óxido de cobalto','kg',0.020,120.0000),
('PR0004',5,'MP07','Gas GLP para horno','kg',3.600,4.3308),
('PR0004',6,'MP08','Caja y embalaje','unidad',4.000,0.8000);
/*!40000 ALTER TABLE `consumos_materia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_pedido`
--

DROP TABLE IF EXISTS `detalle_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_pedido` (
  `id_pedido` varchar(10) NOT NULL,
  `linea` int(11) NOT NULL,
  `id_producto` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `cantidad` int(11) NOT NULL CHECK (`cantidad` > 0),
  `precio_unitario` decimal(12,2) NOT NULL,
  `personalizacion` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_pedido`,`linea`),
  CONSTRAINT `detalle_pedido_ibfk_1` FOREIGN KEY (`id_pedido`) REFERENCES `pedidos` (`id_pedido`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_pedido`
--

LOCK TABLES `detalle_pedido` WRITE;
/*!40000 ALTER TABLE `detalle_pedido` DISABLE KEYS */;
INSERT INTO `detalle_pedido` VALUES
('PE0001',1,'P001','Plato decorativo diseño Cajamarca',6,65.00,'Logo del hotel pintado al centro, tonos tierra'),
('PE0002',1,'P002','Jarrón ceremonial',1,120.00,'Iconografía Cajamarca en azul cobalto'),
('PE0003',1,'P003','Vasija tradicional cajamarquina',10,85.00,'Nombre de la familia grabado en la base');
/*!40000 ALTER TABLE `detalle_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_venta`
--

DROP TABLE IF EXISTS `detalle_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_venta` (
  `id_venta` varchar(30) NOT NULL,
  `linea` int(11) NOT NULL,
  `id_producto` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `modelo` varchar(120) DEFAULT NULL,
  `categoria` varchar(120) DEFAULT NULL,
  `descripcion` varchar(400) DEFAULT NULL,
  `cantidad` int(11) NOT NULL CHECK (`cantidad` > 0),
  `precio_unitario` decimal(12,2) NOT NULL CHECK (`precio_unitario` >= 0),
  PRIMARY KEY (`id_venta`,`linea`),
  CONSTRAINT `detalle_venta_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
INSERT INTO `detalle_venta` VALUES
('V0001',1,'P002','Jarrón ceremonial','JC-30 (30 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',1,120.00),
('V0001',2,'P001','Plato decorativo diseño Cajamarca','PD-25 (25 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',2,65.00),
('V0002',1,'P006','Cántaro miniatura de recuerdo','CM-10 (10 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',6,18.00),
('V0002',2,'P004','Taza artesanal con asa','TA-01 (350 ml)','Cerámica decorativa','Cerámica artesanal hecha a mano',2,28.00),
('V0003',1,'P001','Plato decorativo diseño Cajamarca','PD-25 (25 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',6,65.00),
('V0003',2,'P005','Set de cuencos (3 unidades)','SC-03','Cerámica decorativa','Cerámica artesanal hecha a mano',2,75.00),
('V0004',1,'P006','Cántaro miniatura de recuerdo','CM-10 (10 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',4,18.00),
('V0005',1,'P003','Vasija tradicional cajamarquina','VT-20 (20 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',2,85.00),
('V0005',2,'P006','Cántaro miniatura de recuerdo','CM-10 (10 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',3,18.00),
('V0006',1,'P002','Jarrón ceremonial','JC-30 (30 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',2,120.00),
('V0006',2,'P001','Plato decorativo diseño Cajamarca','PD-25 (25 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',4,65.00),
('V0007',1,'P004','Taza artesanal con asa','TA-01 (350 ml)','Cerámica decorativa','Cerámica artesanal hecha a mano',6,28.00),
('V0008',1,'P005','Set de cuencos (3 unidades)','SC-03','Cerámica decorativa','Cerámica artesanal hecha a mano',1,75.00),
('V0008',2,'P003','Vasija tradicional cajamarquina','VT-20 (20 cm)','Cerámica decorativa','Cerámica artesanal hecha a mano',1,85.00);
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `materias_primas`
--

DROP TABLE IF EXISTS `materias_primas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `materias_primas` (
  `id_materia` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `unidad` varchar(10) NOT NULL,
  `stock` decimal(14,3) NOT NULL CHECK (`stock` >= 0),
  `stock_minimo` decimal(14,3) NOT NULL DEFAULT 0.000,
  `costo_unitario` decimal(12,4) NOT NULL CHECK (`costo_unitario` >= 0),
  PRIMARY KEY (`id_materia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `materias_primas`
--

LOCK TABLES `materias_primas` WRITE;
/*!40000 ALTER TABLE `materias_primas` DISABLE KEYS */;
INSERT INTO `materias_primas` VALUES
('MP01','Arcilla roja de Cajamarca','kg',152.400,40.000,1.2000),
('MP02','Caolín (arcilla blanca)','kg',39.500,15.000,2.5000),
('MP03','Esmalte transparente','litro',6.300,3.000,28.0000),
('MP04','Engobe blanco','litro',4.840,2.000,22.0000),
('MP05','Pigmento óxido de hierro','kg',1.560,0.500,35.0000),
('MP06','Pigmento óxido de cobalto','kg',0.420,0.500,120.0000),
('MP07','Gas GLP para horno','kg',59.300,20.000,4.3308),
('MP08','Caja y embalaje','unidad',54.000,30.000,0.8000);
/*!40000 ALTER TABLE `materias_primas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos`
--

DROP TABLE IF EXISTS `movimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime(3) NOT NULL,
  `id_usuario` varchar(24) NOT NULL,
  `id_producto` varchar(24) NOT NULL,
  `tipo` varchar(20) NOT NULL CHECK (`tipo` in ('INICIAL','ENTRADA','VENTA','BAJA','AJUSTE','PRODUCCION')),
  `cantidad` int(11) NOT NULL,
  `saldo` int(11) NOT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `ix_movimientos_producto` (`id_producto`,`fecha`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos`
--

LOCK TABLES `movimientos` WRITE;
/*!40000 ALTER TABLE `movimientos` DISABLE KEYS */;
INSERT INTO `movimientos` VALUES
(1,'2026-10-01 08:00:00.000','U003','P001','INICIAL',8,8),
(2,'2026-10-01 08:00:00.000','U003','P002','INICIAL',5,5),
(3,'2026-10-01 08:00:00.000','U003','P003','INICIAL',6,6),
(4,'2026-10-01 08:00:00.000','U003','P004','INICIAL',20,20),
(5,'2026-10-01 08:00:00.000','U003','P005','INICIAL',8,8),
(6,'2026-10-01 08:00:00.000','U003','P006','INICIAL',15,15),
(7,'2026-10-03 16:00:00.000','U003','P001','PRODUCCION',19,27),
(8,'2026-10-08 11:00:00.000','U002','P002','VENTA',-1,4),
(9,'2026-10-08 11:00:00.000','U002','P001','VENTA',-2,25),
(10,'2026-10-12 15:00:00.000','U003','P006','PRODUCCION',40,55),
(11,'2026-10-15 12:00:00.000','U002','P006','VENTA',-6,49),
(12,'2026-10-15 12:00:00.000','U002','P004','VENTA',-2,18),
(13,'2026-10-22 17:00:00.000','U001','P001','VENTA',-6,19),
(14,'2026-10-22 17:00:00.000','U001','P005','VENTA',-2,6),
(15,'2026-10-29 10:00:00.000','U002','P006','VENTA',-4,45),
(16,'2026-11-03 13:00:00.000','U002','P003','VENTA',-2,4),
(17,'2026-11-03 13:00:00.000','U002','P006','VENTA',-3,42),
(18,'2026-11-05 16:00:00.000','U003','P004','PRODUCCION',28,46),
(19,'2026-11-10 16:00:00.000','U001','P002','VENTA',-2,2),
(20,'2026-11-10 16:00:00.000','U001','P001','VENTA',-4,15),
(21,'2026-11-14 15:00:00.000','U003','P002','PRODUCCION',4,6),
(22,'2026-11-17 11:00:00.000','U002','P004','VENTA',-6,40),
(23,'2026-11-20 12:00:00.000','U002','P005','VENTA',-1,5),
(24,'2026-11-20 12:00:00.000','U002','P003','VENTA',-1,3);
/*!40000 ALTER TABLE `movimientos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_materia`
--

DROP TABLE IF EXISTS `movimientos_materia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_materia` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime(3) NOT NULL,
  `id_usuario` varchar(24) NOT NULL,
  `id_materia` varchar(24) NOT NULL,
  `tipo` varchar(12) NOT NULL CHECK (`tipo` in ('INICIAL','INGRESO','CONSUMO','FALLA')),
  `cantidad` decimal(14,3) NOT NULL,
  `saldo` decimal(14,3) NOT NULL,
  `referencia` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_materia`
--

LOCK TABLES `movimientos_materia` WRITE;
/*!40000 ALTER TABLE `movimientos_materia` DISABLE KEYS */;
INSERT INTO `movimientos_materia` VALUES
(1,'2026-10-01 08:00:00.000','U003','MP01','INICIAL',200.000,200.000,''),
(2,'2026-10-01 08:00:00.000','U003','MP02','INICIAL',50.000,50.000,''),
(3,'2026-10-01 08:00:00.000','U003','MP03','INICIAL',10.000,10.000,''),
(4,'2026-10-01 08:00:00.000','U003','MP04','INICIAL',6.000,6.000,''),
(5,'2026-10-01 08:00:00.000','U003','MP05','INICIAL',2.000,2.000,''),
(6,'2026-10-01 08:00:00.000','U003','MP06','INICIAL',0.500,0.500,''),
(7,'2026-10-01 08:00:00.000','U003','MP07','INICIAL',50.000,50.000,''),
(8,'2026-10-01 08:00:00.000','U003','MP08','INICIAL',150.000,150.000,''),
(9,'2026-10-03 16:00:00.000','U003','MP01','CONSUMO',-24.000,176.000,'PR0001'),
(10,'2026-10-03 16:00:00.000','U003','MP04','CONSUMO',-1.000,5.000,'PR0001'),
(11,'2026-10-03 16:00:00.000','U003','MP03','CONSUMO',-1.600,8.400,'PR0001'),
(12,'2026-10-03 16:00:00.000','U003','MP05','CONSUMO',-0.200,1.800,'PR0001'),
(13,'2026-10-03 16:00:00.000','U003','MP07','CONSUMO',-8.000,42.000,'PR0001'),
(14,'2026-10-03 16:00:00.000','U003','MP08','CONSUMO',-20.000,130.000,'PR0001'),
(15,'2026-10-12 15:00:00.000','U003','MP01','CONSUMO',-10.000,166.000,'PR0002'),
(16,'2026-10-12 15:00:00.000','U003','MP05','CONSUMO',-0.120,1.680,'PR0002'),
(17,'2026-10-12 15:00:00.000','U003','MP07','CONSUMO',-3.200,38.800,'PR0002'),
(18,'2026-10-12 15:00:00.000','U003','MP08','CONSUMO',-40.000,90.000,'PR0002'),
(19,'2026-11-01 09:00:00.000','U003','MP07','INGRESO',30.000,68.800,'Compra'),
(20,'2026-11-05 16:00:00.000','U003','MP02','CONSUMO',-10.500,39.500,'PR0003'),
(21,'2026-11-05 16:00:00.000','U003','MP03','CONSUMO',-1.500,6.900,'PR0003'),
(22,'2026-11-05 16:00:00.000','U003','MP06','CONSUMO',-0.060,0.440,'PR0003'),
(23,'2026-11-05 16:00:00.000','U003','MP07','CONSUMO',-4.500,64.300,'PR0003'),
(24,'2026-11-05 16:00:00.000','U003','MP08','CONSUMO',-30.000,60.000,'PR0003'),
(25,'2026-11-08 18:00:00.000','U003','MP01','FALLA',-3.600,162.400,'FA0001'),
(26,'2026-11-08 18:00:00.000','U003','MP04','FALLA',-0.160,4.840,'FA0001'),
(27,'2026-11-08 18:00:00.000','U003','MP05','FALLA',-0.040,1.640,'FA0001'),
(28,'2026-11-08 18:00:00.000','U003','MP07','FALLA',-1.400,62.900,'FA0001'),
(29,'2026-11-08 18:00:00.000','U003','MP08','FALLA',-2.000,58.000,'FA0001'),
(30,'2026-11-14 15:00:00.000','U003','MP01','CONSUMO',-10.000,152.400,'PR0004'),
(31,'2026-11-14 15:00:00.000','U003','MP03','CONSUMO',-0.600,6.300,'PR0004'),
(32,'2026-11-14 15:00:00.000','U003','MP05','CONSUMO',-0.080,1.560,'PR0004'),
(33,'2026-11-14 15:00:00.000','U003','MP06','CONSUMO',-0.020,0.420,'PR0004'),
(34,'2026-11-14 15:00:00.000','U003','MP07','CONSUMO',-3.600,59.300,'PR0004'),
(35,'2026-11-14 15:00:00.000','U003','MP08','CONSUMO',-4.000,54.000,'PR0004');
/*!40000 ALTER TABLE `movimientos_materia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operaciones_produccion`
--

DROP TABLE IF EXISTS `operaciones_produccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `operaciones_produccion` (
  `id_operacion` varchar(10) NOT NULL,
  `tipo` varchar(12) NOT NULL CHECK (`tipo` in ('PRODUCCION','FALLA')),
  `fecha` datetime(3) NOT NULL,
  `id_usuario` varchar(24) NOT NULL,
  `id_producto` varchar(24) DEFAULT NULL,
  `nombre_producto` varchar(120) DEFAULT NULL,
  `cantidad` int(11) DEFAULT NULL,
  `aprobadas` int(11) DEFAULT NULL,
  `obs_calidad` varchar(300) DEFAULT NULL,
  `costo_mano_obra` decimal(12,2) DEFAULT NULL,
  `motivo` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id_operacion`),
  CONSTRAINT `CONSTRAINT_1` CHECK (`aprobadas` is null or `aprobadas` between 0 and `cantidad`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operaciones_produccion`
--

LOCK TABLES `operaciones_produccion` WRITE;
/*!40000 ALTER TABLE `operaciones_produccion` DISABLE KEYS */;
INSERT INTO `operaciones_produccion` VALUES
('FA0001','FALLA','2026-11-08 18:00:00.000','U003',NULL,NULL,NULL,NULL,NULL,NULL,'Vasija tradicional cajamarquina x2: se rajaron en el horno por exceso de temperatura'),
('PR0001','PRODUCCION','2026-10-03 16:00:00.000','U003','P001','Plato decorativo diseño Cajamarca',20,19,'1 plato con grieta en el borde',180.00,NULL),
('PR0002','PRODUCCION','2026-10-12 15:00:00.000','U003','P006','Cántaro miniatura de recuerdo',40,40,'',120.00,NULL),
('PR0003','PRODUCCION','2026-11-05 16:00:00.000','U003','P004','Taza artesanal con asa',30,28,'2 tazas con burbujas en el esmalte',150.00,NULL),
('PR0004','PRODUCCION','2026-11-14 15:00:00.000','U003','P002','Jarrón ceremonial',4,4,'',140.00,NULL);
/*!40000 ALTER TABLE `operaciones_produccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id_pedido` varchar(10) NOT NULL,
  `fecha` datetime(3) NOT NULL,
  `fecha_entrega` date NOT NULL,
  `id_cliente` varchar(24) NOT NULL,
  `nombre_cliente` varchar(250) NOT NULL,
  `prioridad` varchar(10) NOT NULL CHECK (`prioridad` in ('Normal','Alta','Urgente')),
  `estado` varchar(12) NOT NULL CHECK (`estado` in ('PENDIENTE','APROBADO','ENTREGADO','CANCELADO')),
  `obs_calidad` varchar(300) DEFAULT NULL,
  `id_venta` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id_pedido`),
  KEY `id_cliente` (`id_cliente`),
  KEY `id_venta` (`id_venta`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  CONSTRAINT `pedidos_ibfk_2` FOREIGN KEY (`id_venta`) REFERENCES `ventas` (`id_venta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES
('PE0001','2026-11-18 10:00:00.000','2026-12-03','C002','Pedro Sandoval','Alta','PENDIENTE','',NULL),
('PE0002','2026-11-19 15:00:00.000','2026-11-26','C001','Ana Quispe','Urgente','APROBADO','Diseño y acabado conformes',NULL),
('PE0003','2026-11-21 09:00:00.000','2026-12-08','C005','Mariela Chávez','Normal','PENDIENTE','',NULL);
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id_producto` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `modelo` varchar(120) DEFAULT NULL,
  `categoria` varchar(120) DEFAULT NULL,
  `precio` decimal(12,2) NOT NULL CHECK (`precio` >= 0),
  `descripcion` varchar(400) DEFAULT NULL,
  `unidad` varchar(30) NOT NULL DEFAULT 'Unidad',
  `stock` int(11) NOT NULL CHECK (`stock` >= 0),
  `stock_minimo` int(11) NOT NULL DEFAULT 0,
  `costo` decimal(12,2) NOT NULL DEFAULT 0.00,
  `fecha_ingreso` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES
('P001','Plato decorativo diseño Cajamarca','PD-25 (25 cm)','Cerámica decorativa',65.00,'Cerámica artesanal hecha a mano','Unidad',15,6,17.48,'2026-10-01 08:00:00.000'),
('P002','Jarrón ceremonial','JC-30 (30 cm)','Cerámica decorativa',120.00,'Cerámica artesanal hecha a mano','Unidad',6,4,48.20,'2026-10-01 08:00:00.000'),
('P003','Vasija tradicional cajamarquina','VT-20 (20 cm)','Cerámica decorativa',85.00,'Cerámica artesanal hecha a mano','Unidad',3,4,30.00,'2026-10-01 08:00:00.000'),
('P004','Taza artesanal con asa','TA-01 (350 ml)','Cerámica decorativa',28.00,'Cerámica artesanal hecha a mano','Unidad',40,12,9.61,'2026-10-01 08:00:00.000'),
('P005','Set de cuencos (3 unidades)','SC-03','Cerámica decorativa',75.00,'Cerámica artesanal hecha a mano','Unidad',5,5,26.00,'2026-10-01 08:00:00.000'),
('P006','Cántaro miniatura de recuerdo','CM-10 (10 cm)','Cerámica decorativa',18.00,'Cerámica artesanal hecha a mano','Unidad',42,15,4.54,'2026-10-01 08:00:00.000');
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recetas`
--

DROP TABLE IF EXISTS `recetas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `recetas` (
  `id_producto` varchar(24) NOT NULL,
  `id_materia` varchar(24) NOT NULL,
  `cantidad_por_unidad` decimal(14,3) NOT NULL CHECK (`cantidad_por_unidad` > 0),
  PRIMARY KEY (`id_producto`,`id_materia`),
  KEY `id_materia` (`id_materia`),
  CONSTRAINT `recetas_ibfk_1` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`) ON DELETE CASCADE,
  CONSTRAINT `recetas_ibfk_2` FOREIGN KEY (`id_materia`) REFERENCES `materias_primas` (`id_materia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recetas`
--

LOCK TABLES `recetas` WRITE;
/*!40000 ALTER TABLE `recetas` DISABLE KEYS */;
INSERT INTO `recetas` VALUES
('P001','MP01',1.200),
('P001','MP03',0.080),
('P001','MP04',0.050),
('P001','MP05',0.010),
('P001','MP07',0.400),
('P001','MP08',1.000),
('P002','MP01',2.500),
('P002','MP03',0.150),
('P002','MP05',0.020),
('P002','MP06',0.005),
('P002','MP07',0.900),
('P002','MP08',1.000),
('P003','MP01',1.800),
('P003','MP04',0.080),
('P003','MP05',0.020),
('P003','MP07',0.700),
('P003','MP08',1.000),
('P004','MP02',0.350),
('P004','MP03',0.050),
('P004','MP06',0.002),
('P004','MP07',0.150),
('P004','MP08',1.000),
('P005','MP02',0.900),
('P005','MP03',0.120),
('P005','MP07',0.400),
('P005','MP08',1.000),
('P006','MP01',0.250),
('P006','MP05',0.003),
('P006','MP07',0.080),
('P006','MP08',1.000);
/*!40000 ALTER TABLE `recetas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permisos`
--

DROP TABLE IF EXISTS `rol_permisos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permisos` (
  `nombre_rol` varchar(40) NOT NULL,
  `permiso` varchar(20) NOT NULL CHECK (`permiso` in ('INVENTARIO','VENTAS','REPORTES','USUARIOS')),
  PRIMARY KEY (`nombre_rol`,`permiso`),
  CONSTRAINT `rol_permisos_ibfk_1` FOREIGN KEY (`nombre_rol`) REFERENCES `roles` (`nombre_rol`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permisos`
--

LOCK TABLES `rol_permisos` WRITE;
/*!40000 ALTER TABLE `rol_permisos` DISABLE KEYS */;
INSERT INTO `rol_permisos` VALUES
('Administrador','INVENTARIO'),
('Administrador','REPORTES'),
('Administrador','USUARIOS'),
('Administrador','VENTAS'),
('Supervisor de almacen','INVENTARIO'),
('Supervisor de almacen','REPORTES'),
('Vendedor','VENTAS');
/*!40000 ALTER TABLE `rol_permisos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `nombre_rol` varchar(40) NOT NULL,
  `contrasena_rol` varchar(40) NOT NULL,
  PRIMARY KEY (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES
('Administrador','ADM-2026'),
('Supervisor de almacen','SUP-2026'),
('Vendedor','VEN-2026');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id_usuario` varchar(24) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `apellido` varchar(120) NOT NULL,
  `dni` char(8) NOT NULL CHECK (`dni` regexp '^[0-9]{8}$'),
  `contrasena` varchar(120) NOT NULL,
  `nombre_rol` varchar(40) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `dni` (`dni`),
  KEY `nombre_rol` (`nombre_rol`),
  CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`nombre_rol`) REFERENCES `roles` (`nombre_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES
('U001','Admin','Principal','00000001','1234','Administrador'),
('U002','Vendedor','Tienda','00000002','1234','Vendedor'),
('U003','Supervisor','Almacen','00000003','1234','Supervisor de almacen');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ventas`
--

DROP TABLE IF EXISTS `ventas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `ventas` (
  `id_venta` varchar(30) NOT NULL,
  `fecha` datetime(3) NOT NULL,
  `id_cliente` varchar(24) NOT NULL,
  `cliente_nombre` varchar(120) NOT NULL,
  `cliente_apellido` varchar(120) NOT NULL,
  `cliente_dni` char(8) NOT NULL,
  `cliente_direccion` varchar(200) DEFAULT NULL,
  `cliente_telefono` varchar(20) DEFAULT NULL,
  `cliente_correo` varchar(120) DEFAULT NULL,
  `id_vendedor` varchar(24) NOT NULL,
  `vendedor_nombre` varchar(120) NOT NULL,
  `vendedor_apellido` varchar(120) NOT NULL,
  `vendedor_dni` char(8) NOT NULL,
  `vendedor_rol` varchar(40) NOT NULL,
  `metodo_pago` varchar(30) NOT NULL,
  `tipo_comprobante` varchar(20) NOT NULL,
  `estado` varchar(20) NOT NULL,
  `descuento_pct` decimal(5,2) NOT NULL DEFAULT 0.00 CHECK (`descuento_pct` between 0 and 30),
  `monto_total` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id_venta`),
  KEY `ix_ventas_fecha` (`fecha`),
  KEY `id_cliente` (`id_cliente`),
  KEY `id_vendedor` (`id_vendedor`),
  CONSTRAINT `ventas_ibfk_1` FOREIGN KEY (`id_cliente`) REFERENCES `clientes` (`id_cliente`),
  CONSTRAINT `ventas_ibfk_2` FOREIGN KEY (`id_vendedor`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ventas`
--

LOCK TABLES `ventas` WRITE;
/*!40000 ALTER TABLE `ventas` DISABLE KEYS */;
INSERT INTO `ventas` VALUES
('V0001','2026-10-08 11:00:00.000','C001','Ana','Quispe','12345678','Jr. Junín 198, Cajamarca','987654321','ana.quispe@example.com','U002','Vendedor','Tienda','00000002','Vendedor','Yape','Boleta','Concluido',0.00,250.00),
('V0002','2026-10-15 12:00:00.000','C003','Lucía','Manzilla','34251678','Jr. Del Comercio 310, Cajamarca','946372645','lucia.manzilla@example.com','U002','Vendedor','Tienda','00000002','Vendedor','Efectivo','Boleta','Concluido',0.00,164.00),
('V0003','2026-10-22 17:00:00.000','C002','Pedro','Sandoval','87654321','Av. Atahualpa 540, Cajamarca','912345678','pedro.sandoval@example.com','U001','Admin','Principal','00000001','Administrador','Transferencia','Recibo','Concluido',10.00,486.00),
('V0004','2026-10-29 10:00:00.000','C000','Clientes','Varios','00000000','','','','U002','Vendedor','Tienda','00000002','Vendedor','Efectivo','Boleta','Concluido',0.00,72.00),
('V0005','2026-11-03 13:00:00.000','C005','Mariela','Chávez','70112233','Lima','955112233','mariela.chavez@example.com','U002','Vendedor','Tienda','00000002','Vendedor','Tarjeta','Boleta','Concluido',0.00,224.00),
('V0006','2026-11-10 16:00:00.000','C004','Ricardo','Salcedo','45612378','Av. Vía de Evitamiento 1200, Cajamarca','976543210','ricardo.salcedo@example.com','U001','Admin','Principal','00000001','Administrador','Transferencia','Recibo','Concluido',5.00,475.00),
('V0007','2026-11-17 11:00:00.000','C006','Jorge','Tello','41239876','Jr. Amalia Puga 455, Cajamarca','944556677','','U002','Vendedor','Tienda','00000002','Vendedor','Plin','Boleta','Concluido',0.00,168.00),
('V0008','2026-11-20 12:00:00.000','C001','Ana','Quispe','12345678','Jr. Junín 198, Cajamarca','987654321','ana.quispe@example.com','U002','Vendedor','Tienda','00000002','Vendedor','Yape','Boleta','Concluido',0.00,160.00);
/*!40000 ALTER TABLE `ventas` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
