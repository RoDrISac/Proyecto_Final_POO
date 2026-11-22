# Sistema de Ventas, Producción e Inventario - Colores y Creaciones

Proyecto del curso Técnicas de Programación Orientada a Objetos (UPN, 2026-2).
**Colores y Creaciones** elabora y vende cerámica artesanal inspirada en la cultura Cajamarca (S06).
Aplicación de escritorio en **Java Swing** (NetBeans) con base de datos **MySQL (XAMPP)** accedida por **JDBC**,
implementada según los informes del grupo: S06 (problema), S07 (objetivos y alcance), S08 (requerimientos),
S09 (diagrama de clases), S10 (criterios de aceptación) y S12–S13 (control de implementación).
Versión de la Evaluación Final (EF).

## Requisitos
- JDK 17 o superior (probado con JDK 24) y Apache NetBeans 25.
- XAMPP 8.5 con MySQL (MariaDB 10.11) y phpMyAdmin.
- El driver JDBC (MySQL Connector/J 8.4) ya viene incluido en `lib/`.

## Cómo ejecutar
Instrucciones completas en **[docs/MANUAL_INSTALACION.md](docs/MANUAL_INSTALACION.md)**. Resumen:
1. XAMPP con **MySQL** iniciado (botón Start en el XAMPP Control Panel, puerto 3306).
2. Importar en phpMyAdmin `sql/01_crear_bd.sql` y `sql/02_usuario_app.sql` (o el backup `bd/colores_creaciones.sql` y luego el script 02).
3. NetBeans → **File → Open Project** → esta carpeta → **Run** (F6). También: doble clic en `ejecutar.cmd`.
4. Iniciar sesión en la ventana de acceso.

| Código | Contraseña | Rol | Ve en el menú |
|---|---|---|---|
| U001 | 1234 | Administrador | Todo |
| U002 | 1234 | Vendedor | Resumen, Inventario, Pedidos, Clientes, Nueva venta, Historial |
| U003 | 1234 | Supervisor de almacén | Resumen, Inventario, Materias primas, Producción, Pedidos (calidad), Reportes |

Para asignar un rol se pide la contraseña del rol: Administrador `ADM-2026`, Vendedor `VEN-2026`, Supervisor de almacén `SUP-2026`.

## Funciones (requerimientos S08)
| Módulo | Qué hace | S08 |
|---|---|---|
| Inventario | Registrar, editar y eliminar productos; ingreso de stock; búsqueda por código o lector de barras; stock comprometido y disponible; alertas | 1, 6, 7, 8, 15, 32–37 |
| Materias primas | Registrar, editar, eliminar; compras con costo promedio; kardex; alertas de stock bajo | 2 |
| Producción | Receta por producto; registrar producción que descuenta materia prima; revisión de calidad (solo las aprobadas ingresan); fallas; costo por lote y por pieza | 3, 4, 5, 11 |
| Pedidos | Pedidos personalizados con prioridad; stock comprometido; revisión de calidad antes del envío; entrega que genera la venta; cancelación | 12, 13, 14 (HU 16) |
| Clientes | Registrar, editar, eliminar; categoría (público general, coleccionista, decorador, turista) para segmentar campañas; historial | S06, 41 |
| Ventas | Carrito, boleta o recibo, cliente opcional en boleta ("clientes varios"), métodos de pago, descuento hasta 30 %, verificación de stock, confirmación | 9, 16, 38–44 |
| Comprobantes | PDF con datos de la empresa, IGV, descuento y numeración B001-/R001-; reimpresión; impresión; envío por correo | 17, 21, 23, 25–29 |
| Reportes | Inventario, stock bajo, ventas, más vendidos, diarios y mensuales (automáticos), movimientos, producción y costos, materias primas, pedidos, clientes por categoría | 10, 18–20 |
| Usuarios | Registro y cambio de rol con contraseña del rol; control de acceso por permisos | 30 |
| Auditoría y cierre | Quién hizo qué y cuándo; cierre contable mensual que bloquea operaciones en meses cerrados | 22, 24, 31 |

## Clases del dominio (diagrama S09 + EF)
| Clase | Descripción |
|---|---|
| `Persona` | Clase padre abstracta: nombre, apellido, DNI, `mostrarPersona()` |
| `Usuario` | Hereda de Persona. `validarAcceso()`, `cambiarRol()` |
| `Cliente` | Hereda de Persona. Categoría, `registrarCliente()`, `obtenerHistorialVentas()`, `esClienteVarios()` |
| `Rol` | Nombre, permisos y contraseña del rol. `tenerPermiso()`, `validarContraseñaRol()` |
| `Producto` | Código, nombre, modelo, categoría, precio, descripción. `mostrarInfo()` |
| `Inventario` | Hereda de Producto. Stock, mínimo, costo, fecha de ingreso. `actualizarStock()`, `tieneStockBajo()` |
| `Venta` | Se compone de `DetalleVenta`. `agregarProducto()`, `aplicarDescuento()`, `calcularMontoTotal()` |
| `DetalleVenta` | Línea de la venta. `calcularSubtotal()` |
| `Comprobante` | Boleta o recibo con IGV. `emitirPDF()`, `enviarPorCorreo()` |
| `Reporte` | `generar()`, `exportar()` (PDF o TXT) |
| `MateriaPrima` | Stock, mínimo y costo promedio. `ingresar()`, `descontar()`, `tieneStockBajo()` |
| `Receta` | Se compone de `Insumo` (materia prima por pieza). `calcularNecesidades()` |
| `OperacionProduccion` | Clase padre abstracta con sus consumos de materia prima. `getTipo()`, `calcularCostoTotal()`, `describir()` |
| `Produccion` | Hereda de OperacionProduccion. Piezas, aprobadas en calidad, mano de obra. `calcularCostoUnitario()` |
| `FallaProduccion` | Hereda de OperacionProduccion. Motivo; el costo es pérdida |
| `Pedido` | Se compone de `DetallePedido` (con personalización). Prioridad, estado, `revisarCalidad()`, `marcarEntregado()`, `cancelar()` |
| `RegistroAuditoria` | Fecha, usuario, acción y detalle (inmutable) |
| `Empresa`, `CorreoSMTP` | Datos del comprobante y envío de correo SMTP con adjunto |

## Arquitectura por capas
| Capa | Paquete | Clases |
|---|---|---|
| Presentación (Swing) | `proyectofina.ui` | `Aplicacion` (acceso, menú y resumen), `Gui` (tema y componentes) y 10 paneles (inventario, materias primas, producción, pedidos, clientes, venta, historial, reportes, usuarios, auditoría) |
| Servicios (reglas de negocio) | `proyectofina.servicios` | `Sistema` (sesión, permisos, transacción y auditoría), `InventarioServicio`, `ProduccionServicio`, `PedidosServicio`, `ClientesServicio`, `VentasServicio`, `UsuariosServicio`, `ReportesServicio`, `AdministracionServicio` |
| Dominio | `proyectofina` | Tabla anterior |
| Persistencia | `proyectofina.persistencia` | Interfaz `Repositorio`, `Estado`, `RepositorioArchivo`, `DatosDemo` |
| Acceso a datos SQL (JDBC) | `proyectofina.persistencia.sql` | `ConexionBD`, interfaz `Dao<T>`, clase abstracta `DaoBase<T>`, `RolDao`, `UsuarioDao`, `ClienteDao`, `ProductoDao`, `VentaDao`, `MateriaPrimaDao`, `RecetaDao`, `PedidoDao`, `OperacionProduccionDao`, `MovimientoDao`, `HistorialDao`, `RepositorioSQL` |

`RepositorioSQL` implementa la misma interfaz `Repositorio` que la versión con archivos (y que el repositorio en
memoria de las pruebas), por eso las ventanas y servicios no dependen de dónde se guardan los datos (polimorfismo).
Cada guardado se hace en **una transacción**: si algo falla se hace `rollback`. Detalle en `docs/ARQUITECTURA.md`.

## Base de datos (MySQL de XAMPP)
17 tablas: `roles`, `rol_permisos`, `usuarios`, `clientes`, `productos`, `ventas`, `detalle_venta`, `movimientos`,
`materias_primas`, `recetas`, `operaciones_produccion`, `consumos_materia`, `movimientos_materia`, `pedidos`,
`detalle_pedido`, `auditoria`, `cierres_mes`.

| Archivo | Uso |
|---|---|
| `sql/01_crear_bd.sql` | Crea la BD `colores_creaciones`, tablas, claves primarias y foráneas, restricciones e índices |
| `sql/02_usuario_app.sql` | Crea el login `colores_app` con permisos de lectura/escritura |
| `sql/03_consultas_ejemplo.sql` | Consultas con JOIN, filtros y agrupaciones |
| `bd/colores_creaciones.sql` | Backup de la BD con los datos de demostración (se importa en phpMyAdmin) |
| `config/bd.properties` | URL, usuario y clave de la conexión JDBC |
| `config/empresa.properties`, `config/correo.properties` | Datos del comprobante y cuenta de correo |
| `lib/mysql-connector-j-8.4.0.jar` | Driver JDBC oficial de MySQL (Connector/J) |

## Estructura del proyecto
| Carpeta o archivo | Contenido |
|---|---|
| `src/proyectofina/` | Código fuente: clases del dominio y los paquetes `servicios`, `persistencia` (con `sql` para JDBC) y `ui` (Swing) |
| `test/proyectofina/` | Pruebas automáticas |
| `sql/` | Scripts para crear la base de datos, el usuario y consultas de ejemplo |
| `bd/` | Backup de la base de datos |
| `lib/` | Driver JDBC de MySQL |
| `config/` | Conexión a la BD, datos de la empresa y cuenta de correo |
| `docs/` | Manuales, arquitectura, matriz de aceptación, pruebas manuales y evidencias |
| `nbproject/`, `build.xml` | Proyecto de NetBeans |
| `compilar.cmd`, `ejecutar.cmd`, `verificar.cmd` | Compilar, ejecutar y probar sin NetBeans |

## Documentación
| Documento | Contenido |
|---|---|
| [docs/MANUAL_INSTALACION.md](docs/MANUAL_INSTALACION.md) | Requisitos, XAMPP, scripts, configuración, ejecución y problemas frecuentes |
| [docs/MANUAL_USUARIO.md](docs/MANUAL_USUARIO.md) | Manual breve de uso, módulo por módulo |
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Capas, clases, relaciones OO justificadas y flujo de una venta |
| [docs/MATRIZ_ACEPTACION.md](docs/MATRIZ_ACEPTACION.md) | Trazabilidad de los 44 RF del S08 a clases, pantallas y pruebas |
| [docs/PRUEBAS_MANUALES.docx](docs/PRUEBAS_MANUALES.docx) | 16 pruebas manuales ejecutadas: resultado esperado, obtenido y captura de cada una (16 de 16 conformes) |
| `docs/evidencias/` | Resultados de las pruebas, PDF de ejemplo y capturas de las pantallas |

## Pruebas
- **Automáticas** (`verificar.cmd`): 62 verificaciones de aceptación (PA), 41 de producción, pedidos y administración (EF),
  15 contra MySQL, 18 de eventos de la interfaz y la captura de las 14 pantallas Swing.
  Resultados en `docs/evidencias/` (capturas en `docs/evidencias/ef/`).
- **Manuales**: 16 casos ejecutados, con su captura, en [docs/PRUEBAS_MANUALES.docx](docs/PRUEBAS_MANUALES.docx): 16 de 16 conformes.

## Versiones
El proyecto usa Git Flow (`master`, `develop`, `feature/*`, `release/*`, `hotfix/*`). Etiquetas: `v0.1`, `v0.2`,
`v1.0`, `v1.1`, `v1.2` y **`v2.0`** (Evaluación Final), publicada como
[Release v2.0](https://github.com/RoDrISac/Proyecto_Final_POO/releases/tag/v2.0) en GitHub con el ZIP del software.

## Archivos que genera (carpeta `datos/`)
Los datos se guardan en MySQL (XAMPP). En `datos/` quedan los comprobantes (`comprobantes/*.pdf`) y los reportes (`reportes/*`).

Autores: Alvaro Agustín Silva Barrantes, Rodrigo Aldair Sánchez Carrasco.
