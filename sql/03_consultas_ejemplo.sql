/* =========================================================
   Script 03: consultas de ejemplo para la sustentacion.
   Son las mismas que usa el sistema desde Java (JDBC).
   Ejecutar en phpMyAdmin (pestana SQL) una por una.
   ========================================================= */
USE colores_creaciones;

-- 1. Usuarios con su rol y permisos (JOIN de 3 tablas)
SELECT u.id_usuario, CONCAT(u.nombre, ' ', u.apellido) AS usuario, r.nombre_rol,
       GROUP_CONCAT(p.permiso ORDER BY p.permiso SEPARATOR ', ') AS permisos
FROM usuarios u
JOIN roles r        ON r.nombre_rol = u.nombre_rol
JOIN rol_permisos p ON p.nombre_rol = r.nombre_rol
GROUP BY u.id_usuario, u.nombre, u.apellido, r.nombre_rol;

-- 2. Productos con stock bajo (filtro que usa ProductoDao.stockBajo)
SELECT id_producto, nombre, stock, stock_minimo
FROM productos
WHERE stock <= stock_minimo
ORDER BY stock;

-- 3. Ventas con su detalle (JOIN que usa VentaDao)
SELECT v.id_venta, v.fecha, CONCAT(v.cliente_nombre, ' ', v.cliente_apellido) AS cliente,
       d.linea, d.nombre AS producto, d.cantidad, d.precio_unitario,
       d.cantidad * d.precio_unitario AS subtotal
FROM ventas v
JOIN detalle_venta d ON d.id_venta = v.id_venta
ORDER BY v.fecha, d.linea;

-- 4. Productos mas vendidos del mes actual
SELECT d.id_producto, d.nombre, SUM(d.cantidad) AS unidades,
       SUM(d.cantidad * d.precio_unitario) AS total
FROM detalle_venta d
JOIN ventas v ON v.id_venta = d.id_venta
WHERE YEAR(v.fecha) = YEAR(CURDATE()) AND MONTH(v.fecha) = MONTH(CURDATE())
GROUP BY d.id_producto, d.nombre
ORDER BY unidades DESC;

-- 5. Ventas por cliente
SELECT c.id_cliente, CONCAT(c.nombre, ' ', c.apellido) AS cliente,
       COUNT(v.id_venta) AS compras, IFNULL(SUM(v.monto_total), 0) AS total
FROM clientes c
LEFT JOIN ventas v ON v.id_cliente = c.id_cliente
GROUP BY c.id_cliente, c.nombre, c.apellido
ORDER BY total DESC;

-- 6. Kardex de un producto
SELECT fecha, id_usuario, tipo, cantidad, saldo
FROM movimientos
WHERE id_producto = 'P001'
ORDER BY id_movimiento;
