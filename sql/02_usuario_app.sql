/* =========================================================
   Script 02: usuario de MySQL que usa la aplicacion (JDBC).
   Solo permisos de lectura/escritura sobre colores_creaciones.
   La misma clave esta en config/bd.properties (si la cambias, cambia ambos).
   Se crea para 'localhost' y '127.0.0.1' porque Java puede conectarse por cualquiera de los dos.
   Ejecutar en phpMyAdmin (pestana SQL) o con:
       C:\xampp\mysql\bin\mysql -u root < sql\02_usuario_app.sql
   ========================================================= */
CREATE USER IF NOT EXISTS 'colores_app'@'localhost' IDENTIFIED BY 'Cc-aoP6eGgSkEu639#26';
CREATE USER IF NOT EXISTS 'colores_app'@'127.0.0.1' IDENTIFIED BY 'Cc-aoP6eGgSkEu639#26';
-- Si el usuario ya existia, se le vuelve a asignar la clave de config/bd.properties
ALTER USER 'colores_app'@'localhost' IDENTIFIED BY 'Cc-aoP6eGgSkEu639#26';
ALTER USER 'colores_app'@'127.0.0.1' IDENTIFIED BY 'Cc-aoP6eGgSkEu639#26';
GRANT SELECT, INSERT, UPDATE, DELETE ON colores_creaciones.* TO 'colores_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON colores_creaciones.* TO 'colores_app'@'127.0.0.1';
FLUSH PRIVILEGES;
SELECT 'Usuario colores_app listo.' AS resultado;
