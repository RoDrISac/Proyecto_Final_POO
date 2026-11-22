# Manual de instalación y ejecución — Colores y Creaciones

Sistema de ventas e inventario en Java Swing con base de datos MySQL de XAMPP (JDBC).

**Resumen.** Instalar JDK, NetBeans y XAMPP → iniciar MySQL en XAMPP → importar los scripts SQL en phpMyAdmin →
abrir el proyecto en NetBeans y pulsar F6 → ingresar con `U001` / `1234`. Cada paso se detalla abajo.

## 1. Requisitos

| Software | Versión probada | Notas |
|---|---|---|
| JDK | 17 o superior (probado con JDK 24) | `java -version` debe responder |
| Apache NetBeans | 25 | Opcional: también se ejecuta con `ejecutar.cmd` |
| XAMPP | 8.5 (MariaDB 10.11, compatible con MySQL) | Instalar en `C:\xampp` con los componentes MySQL y phpMyAdmin |
| Driver JDBC | `lib/mysql-connector-j-8.4.0.jar` | Ya incluido en el proyecto |

**Obtener el proyecto.** Descomprimir el ZIP del software (también se descarga del
[Release v2.0](https://github.com/RoDrISac/Proyecto_Final_POO/releases/tag/v2.0)) en una carpeta, por ejemplo
`C:\Proyectos\Proyecto_Final_POO`. Con Git: `git clone https://github.com/RoDrISac/Proyecto_Final_POO.git`.

## 2. Iniciar MySQL en XAMPP

1. Abrir el **XAMPP Control Panel**.
2. Pulsar **Start** en **MySQL** (puerto 3306) y en **Apache** (para usar phpMyAdmin). Ambos deben quedar en verde.
3. phpMyAdmin se abre en el navegador con `http://localhost/phpmyadmin`.

El usuario `root` de XAMPP no tiene contraseña; solo se usa para crear la base. El sistema se conecta con el usuario
`colores_app`, que tiene permisos de lectura y escritura únicamente sobre la base `colores_creaciones`.

## 3. Crear la base de datos

Opción A — base nueva (recomendada). En phpMyAdmin:

1. Pestaña **Importar** → **Seleccionar archivo** → `sql/01_crear_bd.sql` → botón **Importar** (al final de la página).
2. Repetir con `sql/02_usuario_app.sql`.
3. A la izquierda debe aparecer la base `colores_creaciones` con 17 tablas.

También se puede hacer en PowerShell, desde la carpeta del proyecto:

```powershell
cmd /c "C:\xampp\mysql\bin\mysql -u root < sql\01_crear_bd.sql"
cmd /c "C:\xampp\mysql\bin\mysql -u root < sql\02_usuario_app.sql"
```

La primera vez que se abre el sistema con la BD vacía se cargan automáticamente los datos de demostración
(ver paso 5).

Opción B — restaurar el backup con datos de demostración: en phpMyAdmin, pestaña **Importar**, seleccionar
`bd/colores_creaciones.sql` (crea la base con sus datos) y luego importar `sql/02_usuario_app.sql` (crea el usuario
de la aplicación). En PowerShell:

```powershell
cmd /c "C:\xampp\mysql\bin\mysql -u root < bd\colores_creaciones.sql"
cmd /c "C:\xampp\mysql\bin\mysql -u root < sql\02_usuario_app.sql"
```

## 4. Configurar la conexión

No hay que cambiar nada si se usaron los scripts tal como vienen: `config/bd.properties` ya tiene la dirección de
MySQL, el usuario y la clave que crea el script 02. Su contenido es:

```properties
url=jdbc:mysql://localhost:3306/colores_creaciones?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&connectTimeout=5000
usuario=colores_app
clave=(la misma clave del script 02_usuario_app.sql)
```

Si MySQL usa otro puerto (se ve en el XAMPP Control Panel), cambiar `3306` en la URL.

## 5. Ejecutar

**Con NetBeans:** *File → Open Project* → carpeta `Proyecto_Final_POO` → **Run (F6)**.
El driver ya está agregado en *Properties → Libraries*.

**Sin NetBeans:** doble clic en `ejecutar.cmd` (compila la primera vez con `compilar.cmd`).

Usuarios de demostración:

| Código | Contraseña | Rol |
|---|---|---|
| U001 | 1234 | Administrador |
| U002 | 1234 | Vendedor |
| U003 | 1234 | Supervisor de almacén |

La primera vez que el sistema abre una base vacía carga los **datos de demostración** del taller de cerámica:
6 productos con su receta, 8 materias primas, producciones, una falla, 8 ventas (octubre y noviembre) y 3 pedidos.

Para asignar un rol a un usuario se pide la contraseña del rol: Administrador `ADM-2026`, Vendedor `VEN-2026`,
Supervisor de almacén `SUP-2026`.

## 6. Configuración opcional

**Datos de la empresa en el comprobante** — `config/empresa.properties` (razón social, RUC, dirección, teléfono, lema).
Completar el RUC real de la empresa; si queda vacío no se imprime.

**Envío de comprobantes por correo** — `config/correo.properties`:

```properties
host=smtp.gmail.com
puerto=465
usuario=cuenta.de.la.empresa@gmail.com
clave=contraseña de aplicación de 16 letras
remitente=
```

En Gmail: *Cuenta de Google → Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones*.
Sin esta configuración, el botón **Enviar por correo** explica qué falta y el resto del sistema funciona igual.

## 7. Verificar

Con MySQL iniciado, doble clic en `verificar.cmd` o, en PowerShell desde la carpeta del proyecto:

```powershell
.\verificar.cmd
```

Ejecuta:

| Prueba | Qué comprueba | Resultado |
|---|---|---|
| `PruebasAceptacion` | 62 verificaciones de aceptación de inventario, clientes, ventas, comprobantes, reportes y roles (PA01–PA33) | `docs/evidencias/resultados-pruebas.txt` |
| `PruebaImportacion` | Importación de datos desde archivos CSV y recuperación tras reiniciar | `docs/evidencias/importacion.txt` |
| `PruebasEF` | 41 verificaciones de producción, calidad, fallas, pedidos, stock comprometido, descuentos, cierre de mes, auditoría, roles y correo | `docs/evidencias/resultados-pruebas-ef.txt` |
| `PruebasVisuales` | 18 verificaciones de eventos de la interfaz: acceso, formulario Guardar, búsqueda con Enter, carrito, confirmar y cancelar venta | `docs/evidencias/resultados-visuales.txt` |
| `PruebasVisualesEF` | Renderiza las 14 pantallas Swing y el menú por rol | `docs/evidencias/ef/` |
| `PruebaBD` | 15 verificaciones contra MySQL: conexión, INSERT/UPDATE/DELETE, consultas con filtro y JOIN, rollback y flujo completo | `docs/evidencias/resultados-bd.txt` |

`PruebaBD` deja en la base una producción, un pedido cancelado y una venta de prueba. Para volver a los datos
de demostración, importar `bd/colores_creaciones.sql` (paso 3, opción B).

## 8. Problemas frecuentes

| Mensaje | Causa | Solución |
|---|---|---|
| *Communications link failure* | MySQL no está iniciado en XAMPP | Paso 2 (botón Start de MySQL) |
| *Access denied for user 'colores_app'* | Falta el script 02 o la clave es distinta | Importar `sql/02_usuario_app.sql` y revisar `config/bd.properties` |
| *Unknown database 'colores_creaciones'* | No se importó el script 01 | Paso 3 |
| *El proyecto ya está abierto* | Hay otra ventana del sistema abierta | Cerrar la otra ventana |
| *Public Key Retrieval is not allowed* | Falta `allowPublicKeyRetrieval=true` en la URL | Revisar `config/bd.properties` |
| MySQL no inicia en XAMPP (puerto 3306 ocupado) | Otro MySQL instalado usa el puerto | Detener el otro servicio o cambiar el puerto en XAMPP y en la URL |
| *El envío por correo no está configurado* | `config/correo.properties` vacío | Paso 6 |
| *El servidor de correo respondió: 535…* | Usuario o contraseña de aplicación incorrectos | Generar otra contraseña de aplicación |
| *El mes AAAA-MM tiene la contabilidad cerrada* | Se cerró el mes actual en Auditoría y cierre | Es la validación esperada (S08 N.° 22 y 24) |

Si no hay conexión, el sistema lo explica y ofrece abrir en **modo archivo local** para no detener la demostración.

## 9. Respaldo

Desde phpMyAdmin: seleccionar la base `colores_creaciones` → pestaña **Exportar** → **Continuar** (descarga un archivo
`.sql`). O en PowerShell:

```powershell
cmd /c "C:\xampp\mysql\bin\mysqldump -u root --databases colores_creaciones --default-character-set=utf8mb4 --result-file=bd\colores_creaciones.sql"
```

Para restaurarlo, importar ese archivo como en el paso 3, opción B.
