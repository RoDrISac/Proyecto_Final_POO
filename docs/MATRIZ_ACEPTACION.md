# Matriz de aceptación y trazabilidad — Evaluación Final

Proyecto: **Sistema de ventas, producción e inventario — Colores y Creaciones** (cerámica artesanal de Cajamarca).
Base: requerimientos del trabajo de campo S08 (numeración original), criterios de aceptación del S10 e
historias de usuario del Excel `Plantilla Historias Usuario_.xlsx` (31 HU con 3 criterios cada una).

Columnas de prueba: **PA** = `PruebasAceptacion` (inventario, clientes, ventas, comprobantes, reportes y roles), **EF** = `PruebasEF` (producción, pedidos y administración),
**BD** = `PruebaBD` (contra MySQL), **PM** = caso de `docs/PRUEBAS_MANUALES.docx`.
Resultados en `docs/evidencias/`.

## Requerimientos funcionales (S08)

| RF | Requerimiento | Clase / método principal | Pantalla | Pruebas | Estado |
|---|---|---|---|---|---|
| RF01 | Registro de productos | `InventarioServicio.guardar()` | Inventario | PA02, PM-03 | Implementado |
| RF02 | Registro de materias primas | `ProduccionServicio.guardarMateria()`, `MateriaPrima` | Materias primas | EF01–EF04, PM-04 | Implementado |
| RF03 | Producción (ingresos) con descuento de materia prima | `ProduccionServicio.producir()`, `Receta` | Producción | EF05, EF10, EF11, PM-05 | Implementado |
| RF04 | Producción (egresos por falla) | `ProduccionServicio.registrarFalla()`, `FallaProduccion` | Producción | EF12, PM-06 | Implementado |
| RF05 | Costo de producción por pieza y por lote | `Produccion.calcularCostoTotal()`, `calcularCostoUnitario()` | Producción | EF07, EF08 | Implementado |
| RF06 | Datos del producto (código, categoría, unidad, precios, stock) | `Producto`, `Inventario` | Inventario | PA02, PA03 | Implementado |
| RF07 | Editar producto | `InventarioServicio.guardar()` | Inventario | PA11 | Implementado |
| RF08 | Eliminar producto conservando ventas | `InventarioServicio.eliminar()` | Inventario | PA13 | Implementado |
| RF09 | Registro de venta con descuento de stock | `VentasServicio.confirmar()` | Nueva venta | PA07, PM-09 | Implementado |
| RF10 | Reportes de inventario y movimientos | `ReportesServicio`, `Reporte` | Reportes | PA16, PM-14 | Implementado |
| RF11 | Revisión de calidad antes de almacenar | `Produccion` (aprobadas) | Producción | EF06, EF09, PM-05 | Implementado |
| RF12 | Revisión de calidad al enviar un pedido | `Pedido.revisarCalidad()` | Pedidos | EF17, EF18, PM-07 | Implementado |
| RF13 | Pedidos personalizados | `PedidosServicio.registrar()`, `Pedido` | Pedidos | EF14, EF16, PM-07 | Implementado |
| RF14 | Stock comprometido | `Estado.stockComprometido()`, `stockDisponible()` | Inventario, Pedidos | EF14, EF15, EF21, PM-08 | Implementado |
| RF15 | Alertas por stock bajo | `Inventario.tieneStockBajo()`, `MateriaPrima.tieneStockBajo()` | Resumen | PA08, PM-02 | Implementado (la cerámica no caduca: la alerta es por stock) |
| RF16 | Formulario de venta | `VentasPanel` | Nueva venta | PA07, PM-09 | Implementado |
| RF17 | Diseño del comprobante | `Comprobante`, `Empresa` | PDF | PA09 | Implementado (RUC en `config/empresa.properties`) |
| RF18 | Inventario general | `Reporte.generar("INVENTARIO")` | Reportes | PA16, PA31 | Implementado |
| RF19 | Movimientos por período | `ReportesServicio.movimientos()` | Reportes | PA17–PA19 | Implementado |
| RF20 | Productos más vendidos | `Reporte.generar("MAS_VENDIDOS")` | Reportes | PA30 | Implementado |
| RF21 | Orden cronológico | `HistorialPanel` | Historial | PA12 | Implementado |
| RF22 | Validaciones legales de fecha | `Sistema.validarFechaOperacion()` | Ventas, Producción | EF30, EF31 | Implementado |
| RF23 | Fecha y hora de emisión | `Venta`, `Comprobante` | PDF | PA09 | Implementado |
| RF24 | No operar en meses con contabilidad cerrada | `AdministracionServicio.cerrarMes()` | Auditoría y cierre | EF29–EF32, PM-15 | Implementado |
| RF25 | Comprobante PDF | `Comprobante.emitirPDF()`, `ArchivoPDF` | Nueva venta | PA09, PM-09 | Implementado |
| RF26 | Impresión desde el sistema | `HistorialPanel.imprimir()` | Historial | PM-12 | Implementado |
| RF27 | Guardado con numeración única | `RepositorioSQL` (transacción) | — | PA14, BD | Implementado |
| RF28 | Envío del comprobante por correo | `Comprobante.enviarPorCorreo()`, `CorreoSMTP` | Historial | EF39, EF40, PM-13 | Implementado (requiere cuenta SMTP) |
| RF29 | Consulta y reimpresión | `VentasServicio.comprobante()` | Historial | PA15, PM-12 | Implementado |
| RF30 | Control de acceso por rol | `Sistema.exigir()`, `Rol.tenerPermiso()` | Menú | PA20, EF36–EF38, PM-16 | Implementado |
| RF31 | Auditoría | `Sistema.cambiar()`, `RegistroAuditoria` | Auditoría y cierre | EF33, BD, PM-15 | Implementado |
| RF32–RF37 | Lector de código de barras | `VentasPanel`, `InventarioPanel` (Enter) | Nueva venta, Inventario | PA04, PM-09 | Implementado (lector tipo teclado) |
| RF38 | Subtotal y total | `Venta.calcularSubtotal()`, `calcularMontoTotal()` | Nueva venta | PA07, PA27 | Implementado |
| RF39 | IGV y total | `Comprobante.generarLineas()` | PDF | PA26 | Implementado |
| RF40 | Verificar stock disponible | `VentasServicio.registrar()` | Nueva venta | PA06, PA28, EF15 | Implementado |
| RF41 | Cliente opcional en boleta | `VentasServicio.buscarCliente()`, `Cliente.esClienteVarios()` | Nueva venta | EF24, EF25, PM-10 | Implementado |
| RF42 | Métodos de pago | `VentasServicio.PAGOS` | Nueva venta | PA07 | Implementado |
| RF43 | Confirmar y registrar la venta | `VentasServicio.confirmar()` | Nueva venta | PA07, PA28b | Implementado |
| RF44 | Actualizar el inventario | `VentasServicio.registrar()` | Nueva venta | PA07, PA29 | Implementado |

## Alcance del S07 y problema del S06

| Origen | Requerimiento | Clase | Pruebas | Estado |
|---|---|---|---|---|
| S06 | Categorizar clientes para campañas | `Cliente.setCategoria()`, `ReportesServicio.clientes()` | EF27, PM-11 | Implementado |
| S06 | Pedidos personalizados que no se pierdan | `Pedido`, `PedidosServicio` | EF14–EF21 | Implementado |
| S07 | Interfaz gráfica intuitiva | `Aplicacion`, `Gui` y 12 paneles Swing | PruebasVisuales, PruebasVisualesEF | Implementado |
| S07 | Descuento en la venta | `Venta.aplicarDescuento()` | EF22, EF23, BD, PM-09 | Implementado |
| S07 | Materias primas y procesos de producción | `MateriaPrima`, `Receta`, `Produccion` | EF01–EF13 | Implementado |

## Requerimientos no funcionales (S08)

| Tipo | Cómo se cumple | Evidencia |
|---|---|---|
| Autenticación | Código y contraseña, 3 intentos | PM-01 |
| Control de accesos | Permisos verificados en servicios y menú | PA20, EF36–EF38, PM-16 |
| Persistencia | MySQL (XAMPP) con claves foráneas y transacciones | `resultados-bd.txt` |
| Mantenibilidad | Capas, interfaces `Repositorio` y `Dao<T>`, pruebas automáticas | `ARQUITECTURA.md` |
| Usabilidad | Mensajes claros, confirmaciones, formularios que conservan los datos | PM-03, PM-09 |
| Portabilidad | Java 17+; configuración externa; manual de instalación | `MANUAL_INSTALACION.md` |

## Cómo verificar

1. `verificar.cmd` ejecuta todas las pruebas automáticas (ver `MANUAL_INSTALACION.md`, sección 7).
2. Las pruebas manuales ejecutadas, con sus capturas, están en `PRUEBAS_MANUALES.docx`.
3. Las capturas de cada pantalla están en `docs/evidencias/ef/`.
