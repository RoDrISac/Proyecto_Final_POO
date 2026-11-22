/* =========================================================
   Colores y Creaciones - Sistema de Ventas e Inventario
   Script 01: crea la base de datos y sus tablas (MySQL / MariaDB de XAMPP)
   Ejecutar en phpMyAdmin (pestana SQL o Importar) o con:
       C:\xampp\mysql\bin\mysql -u root < sql\01_crear_bd.sql
   ========================================================= */
CREATE DATABASE IF NOT EXISTS colores_creaciones
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colores_creaciones;

/* ---------- Seguridad: roles, permisos y usuarios ---------- */
CREATE TABLE IF NOT EXISTS roles (
    nombre_rol      VARCHAR(40)  NOT NULL PRIMARY KEY,
    contrasena_rol  VARCHAR(40)  NOT NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS rol_permisos (
    nombre_rol  VARCHAR(40) NOT NULL,
    permiso     VARCHAR(20) NOT NULL
        CHECK (permiso IN ('INVENTARIO','VENTAS','REPORTES','USUARIOS')),
    PRIMARY KEY (nombre_rol, permiso),
    FOREIGN KEY (nombre_rol) REFERENCES roles(nombre_rol) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario  VARCHAR(24)  NOT NULL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    apellido    VARCHAR(120) NOT NULL,
    dni         CHAR(8)      NOT NULL UNIQUE CHECK (dni REGEXP '^[0-9]{8}$'),
    contrasena  VARCHAR(120) NOT NULL,
    nombre_rol  VARCHAR(40)  NOT NULL,
    FOREIGN KEY (nombre_rol) REFERENCES roles(nombre_rol)
) ENGINE = InnoDB;

/* ---------- Clientes ---------- */
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente  VARCHAR(24)  NOT NULL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    apellido    VARCHAR(120) NOT NULL,
    dni         CHAR(8)      NOT NULL CHECK (dni REGEXP '^[0-9]{8}$'),
    direccion   VARCHAR(200) NULL,
    telefono    VARCHAR(20)  NULL,
    correo      VARCHAR(120) NULL,
    categoria   VARCHAR(40)  NOT NULL   -- Publico general, Coleccionista, Decorador de interiores o Turista (se valida en Cliente.java)
) ENGINE = InnoDB;

/* ---------- Productos en inventario (Producto + Inventario) ---------- */
CREATE TABLE IF NOT EXISTS productos (
    id_producto    VARCHAR(24)   NOT NULL PRIMARY KEY,
    nombre         VARCHAR(120)  NOT NULL,
    modelo         VARCHAR(120)  NULL,
    categoria      VARCHAR(120)  NULL,
    precio         DECIMAL(12,2) NOT NULL CHECK (precio >= 0),
    descripcion    VARCHAR(400)  NULL,
    unidad         VARCHAR(30)   NOT NULL DEFAULT 'Unidad',
    stock          INT           NOT NULL CHECK (stock >= 0),
    stock_minimo   INT           NOT NULL DEFAULT 0,
    costo          DECIMAL(12,2) NOT NULL DEFAULT 0,
    fecha_ingreso  DATETIME(3)   NULL
) ENGINE = InnoDB;

/* ---------- Ventas (cabecera) y detalle (composicion) ---------- */
CREATE TABLE IF NOT EXISTS ventas (
    id_venta          VARCHAR(30)   NOT NULL PRIMARY KEY,
    fecha             DATETIME(3)   NOT NULL,
    id_cliente        VARCHAR(24)   NOT NULL,
    cliente_nombre    VARCHAR(120)  NOT NULL,   -- instantanea del cliente al vender
    cliente_apellido  VARCHAR(120)  NOT NULL,
    cliente_dni       CHAR(8)       NOT NULL,
    cliente_direccion VARCHAR(200)  NULL,
    cliente_telefono  VARCHAR(20)   NULL,
    cliente_correo    VARCHAR(120)  NULL,
    id_vendedor       VARCHAR(24)   NOT NULL,
    vendedor_nombre   VARCHAR(120)  NOT NULL,   -- instantanea del vendedor
    vendedor_apellido VARCHAR(120)  NOT NULL,
    vendedor_dni      CHAR(8)       NOT NULL,
    vendedor_rol      VARCHAR(40)   NOT NULL,
    metodo_pago       VARCHAR(30)   NOT NULL,
    tipo_comprobante  VARCHAR(20)   NOT NULL,
    estado            VARCHAR(20)   NOT NULL,
    descuento_pct     DECIMAL(5,2)  NOT NULL DEFAULT 0 CHECK (descuento_pct BETWEEN 0 AND 30),
    monto_total       DECIMAL(12,2) NOT NULL,
    INDEX ix_ventas_fecha (fecha),
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_vendedor) REFERENCES usuarios(id_usuario)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS detalle_venta (
    id_venta         VARCHAR(30)   NOT NULL,
    linea            INT           NOT NULL,
    id_producto      VARCHAR(24)   NOT NULL,   -- sin FK: el producto puede darse de baja y la venta se conserva
    nombre           VARCHAR(120)  NOT NULL,
    modelo           VARCHAR(120)  NULL,
    categoria        VARCHAR(120)  NULL,
    descripcion      VARCHAR(400)  NULL,
    cantidad         INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario  DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    PRIMARY KEY (id_venta, linea),
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE
) ENGINE = InnoDB;

/* ---------- Kardex: movimientos de inventario ---------- */
CREATE TABLE IF NOT EXISTS movimientos (
    id_movimiento  INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha          DATETIME(3)  NOT NULL,
    id_usuario     VARCHAR(24)  NOT NULL,
    id_producto    VARCHAR(24)  NOT NULL,
    tipo           VARCHAR(20)  NOT NULL CHECK (tipo IN ('INICIAL','ENTRADA','VENTA','BAJA','AJUSTE','PRODUCCION')),
    cantidad       INT          NOT NULL,
    saldo          INT          NOT NULL,
    INDEX ix_movimientos_producto (id_producto, fecha)
) ENGINE = InnoDB;

/* ---------- Produccion: materias primas, recetas, produccion y fallas ---------- */
CREATE TABLE IF NOT EXISTS materias_primas (
    id_materia      VARCHAR(24)    NOT NULL PRIMARY KEY,
    nombre          VARCHAR(120)   NOT NULL,
    unidad          VARCHAR(10)    NOT NULL,
    stock           DECIMAL(14,3)  NOT NULL CHECK (stock >= 0),
    stock_minimo    DECIMAL(14,3)  NOT NULL DEFAULT 0,
    costo_unitario  DECIMAL(12,4)  NOT NULL CHECK (costo_unitario >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS recetas (
    id_producto          VARCHAR(24)   NOT NULL,
    id_materia           VARCHAR(24)   NOT NULL,
    cantidad_por_unidad  DECIMAL(14,3) NOT NULL CHECK (cantidad_por_unidad > 0),
    PRIMARY KEY (id_producto, id_materia),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias_primas(id_materia)
) ENGINE = InnoDB;

-- Herencia en la BD: una sola tabla para Produccion y FallaProduccion, diferenciadas por "tipo"
CREATE TABLE IF NOT EXISTS operaciones_produccion (
    id_operacion     VARCHAR(10)   NOT NULL PRIMARY KEY,
    tipo             VARCHAR(12)   NOT NULL CHECK (tipo IN ('PRODUCCION','FALLA')),
    fecha            DATETIME(3)   NOT NULL,
    id_usuario       VARCHAR(24)   NOT NULL,
    id_producto      VARCHAR(24)   NULL,
    nombre_producto  VARCHAR(120)  NULL,
    cantidad         INT           NULL,
    aprobadas        INT           NULL,
    obs_calidad      VARCHAR(300)  NULL,
    costo_mano_obra  DECIMAL(12,2) NULL,
    motivo           VARCHAR(300)  NULL,
    CHECK (aprobadas IS NULL OR aprobadas BETWEEN 0 AND cantidad)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS consumos_materia (
    id_operacion    VARCHAR(10)   NOT NULL,
    linea           INT           NOT NULL,
    id_materia      VARCHAR(24)   NOT NULL,
    nombre          VARCHAR(120)  NOT NULL,
    unidad          VARCHAR(10)   NOT NULL,
    cantidad        DECIMAL(14,3) NOT NULL,
    costo_unitario  DECIMAL(12,4) NOT NULL,
    PRIMARY KEY (id_operacion, linea),
    FOREIGN KEY (id_operacion) REFERENCES operaciones_produccion(id_operacion) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS movimientos_materia (
    id_movimiento  INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha          DATETIME(3)   NOT NULL,
    id_usuario     VARCHAR(24)   NOT NULL,
    id_materia     VARCHAR(24)   NOT NULL,
    tipo           VARCHAR(12)   NOT NULL CHECK (tipo IN ('INICIAL','INGRESO','CONSUMO','FALLA')),
    cantidad       DECIMAL(14,3) NOT NULL,
    saldo          DECIMAL(14,3) NOT NULL,
    referencia     VARCHAR(30)   NULL
) ENGINE = InnoDB;

/* ---------- Pedidos personalizados ---------- */
CREATE TABLE IF NOT EXISTS pedidos (
    id_pedido       VARCHAR(10)   NOT NULL PRIMARY KEY,
    fecha           DATETIME(3)   NOT NULL,
    fecha_entrega   DATE          NOT NULL,
    id_cliente      VARCHAR(24)   NOT NULL,
    nombre_cliente  VARCHAR(250)  NOT NULL,
    prioridad       VARCHAR(10)   NOT NULL CHECK (prioridad IN ('Normal','Alta','Urgente')),
    estado          VARCHAR(12)   NOT NULL CHECK (estado IN ('PENDIENTE','APROBADO','ENTREGADO','CANCELADO')),
    obs_calidad     VARCHAR(300)  NULL,
    id_venta        VARCHAR(30)   NULL,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_pedido        VARCHAR(10)   NOT NULL,
    linea            INT           NOT NULL,
    id_producto      VARCHAR(24)   NOT NULL,
    nombre           VARCHAR(120)  NOT NULL,
    cantidad         INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario  DECIMAL(12,2) NOT NULL,
    personalizacion  VARCHAR(300)  NULL,
    PRIMARY KEY (id_pedido, linea),
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido) ON DELETE CASCADE
) ENGINE = InnoDB;

/* ---------- Auditoria y cierre contable ---------- */
CREATE TABLE IF NOT EXISTS auditoria (
    id_registro  INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fecha        DATETIME(3)   NOT NULL,
    id_usuario   VARCHAR(24)   NULL,
    accion       VARCHAR(40)   NOT NULL,
    detalle      VARCHAR(500)  NULL,
    INDEX ix_auditoria_fecha (fecha)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cierres_mes (
    periodo  CHAR(7) NOT NULL PRIMARY KEY CHECK (periodo REGEXP '^[0-9]{4}-[0-1][0-9]$')
) ENGINE = InnoDB;

SELECT 'Base de datos colores_creaciones lista.' AS resultado;
