# Matriz de aceptación y evidencias — T3

Proyecto: **Colores y Creaciones · versión gráfica**. Base: documento `2026-2 Trabajo_de_Campo___S08.docx`, sección 3. Los identificadores RF conservan la numeración original de S08; no se han inventado requerimientos de la empresa.

La rúbrica pide al menos 10 requerimientos implementados y demostrables. Se seleccionan 21 números de S08 en 20 filas (RF43/RF44 se verifican juntos). Esta selección no significa que los 44 requerimientos del proyecto completo estén terminados. S10 no fue proporcionado.

Los criterios siguientes se formalizaron para esta entrega técnica; no se atribuyen a una validación realizada por la empresa. Las imágenes son renders de componentes Swing reales y las pruebas usan datos ficticios aislados. Los logs conservan la fecha y el resultado real de ejecución.

| S08 | Requerimiento | Criterio de aceptación | Prueba ejecutada / apoyo | Evidencia |
|---|---|---|---|---|
| RF06 | Registro de productos | Con código nuevo y nombre, modelo, categoría, unidad, precio, costo, stock y mínimo válidos, guardar crea exactamente un producto. Rechaza duplicados, negativos y más de dos decimales. | PA02, PA03a–e; evento GUI Nuevo/Guardar | 03-inventario.png; 11-formulario-producto.png |
| RF07 | Editar producto | Modificar nombre y precio conserva el código y el stock. Una venta previa mantiene nombre y precio originales. | PA11 | 03-inventario.png |
| RF08 | Eliminar producto | La confirmación elimina el producto del catálogo y conserva las ventas y comprobantes históricos. | PA13, PA15 | 03-inventario.png |
| RF09 | Venta y salida de inventario | Confirmar 12 unidades de un stock de 14 deja 2 y registra una venta de S/150.00. Un rechazo no cambia stock ni ventas. | PA06a–b, PA07, PA28a–b | 05-venta.png; 12-venta-confirmada.png |
| RF15 | Alertas por stock bajo | Si stock <= mínimo, Inventario y Resumen muestran la alerta. El criterio aplicado es stock; no caducidad de textiles. | PA08 | 02-resumen.png; 03-inventario.png |
| RF16 | Formulario de venta | La venta reúne cliente, productos, cantidades, precios, fecha/hora actual, pago y tipo; permite revisar antes de confirmar. | PA07 y evento GUI Confirmar venta | 05-venta.png; 06-historial.png |
| RF18 | Inventario general | El reporte lista los productos y sus existencias actuales, e indica cantidad de productos y stock bajo. | PA16-INVENTARIO, PA31 | 07-reportes.png |
| RF19 | Movimientos por período | El rango incluye ambas fechas; registra entrada, venta y baja con usuario, cantidad y saldo. Otro período excluye esos movimientos. Fechas invertidas se rechazan. | PA17, PA18, PA19 | movimientos-prueba.txt |
| RF20 | Más vendidos del mes | El ranking suma unidades del mes actual y excluye ventas de meses anteriores. El producto de prueba muestra 2 y no 102 unidades. | PA30 | resultados-pruebas.txt |
| RF25 | Comprobante PDF | Tras confirmar se crea PDF con identificador, cliente, detalle, total e IGV; si falla el PDF la venta no se duplica y puede regenerarse desde Historial. | PA09; evento GUI Confirmar venta | comprobante-prueba.pdf; 12-venta-confirmada.png |
| RF27 | Guardado y numeración | Venta, stock y movimiento se guardan en un único estado atómico. Las nuevas ventas tienen IDs distintos; se recuperan al reiniciar. Un fallo de disco no confirma cambios parciales. | PA12, PA14, PA23, PA29 | resultados-pruebas.txt; importacion.txt |
| RF29 | Consulta y regeneración | Historial permite consultar y regenerar el comprobante de una venta; el PDF permanece idéntico después de editar/eliminar el producto y reiniciar. La impresión física se verifica aparte. | PA11, PA13, PA15 | 06-historial.png; comprobante-prueba.pdf |
| RF30 | Control de acceso | Vendedor no modifica inventario ni administra usuarios/reportes; supervisor no registra ventas. Los servicios rechazan la operación aunque se invoquen sin la pantalla. | PA20a–d; navegación por rol GUI | 10-rol-vendedor.png |
| RF33 | Búsqueda por código | Código escrito y Enter filtra inventario. En venta, código y Enter identifica el producto y añade cantidad válida. El lector debe emular teclado y Enter. | Ingreso por código PA04; evento GUI Agregar | 03-inventario.png; 05-venta.png |
| RF35 | Ingreso de stock | Ingresar 4 unidades sobre 10 deja 14 y genera movimiento con usuario, fecha y saldo; cantidades no positivas son rechazadas por el servicio. | PA04, PA17 | 03-inventario.png; movimientos-prueba.txt |
| RF38 | Subtotal y total | Calcula suma de cantidad por precio; 12 × 12.50 = 150.00 y 3 × 0.10 = 0.30 con redondeo monetario de dos decimales. | PA07, PA27 | 05-venta.png; comprobante-prueba.pdf |
| RF39 | IGV y total | Los precios incluyen 18% de IGV: para S/150.00, base S/127.12 e IGV S/22.88. Se muestran en comprobante sin duplicar el impuesto. | PA26 | comprobante-prueba.pdf |
| RF40 | Verificar stock | Se rechaza carrito vacío o cantidad mayor al stock. La suma de adiciones del mismo producto no supera existencias; se revalida al confirmar. | PA06a–c, PA28a–b | resultados-pruebas.txt |
| RF42 | Método de pago | Permite Efectivo, Yape, Plin, Transferencia y Tarjeta. El valor elegido se conserva con la venta y aparece en el comprobante. | PA07, PA27 | 05-venta.png; comprobante-prueba.pdf |
| RF43 / RF44 | Confirmación y actualización | Cancelar conserva el carrito sin persistir una venta; confirmar registra detalle, descuenta stock y vacía el carrito. La GUI muestra el ID y el PDF creado. | PA07, PA28b, PA29; evento GUI Confirmar venta | 12-venta-confirmada.png |

## Cómo verificar

1. Ejecutar `verificar.cmd` desde la carpeta raíz (JDK 17 o superior). La suite no modifica los CSV ni los datos de trabajo: usa carpetas temporales.
2. Abrir `docs/evidencias/resultados-pruebas.txt`, `resultados-visuales.txt` e `importacion.txt`.
3. Consultar el código de pruebas en `test/proyectofina/` y repetir la demostración de `GUION_DEMO.md`.
4. Abrir `INFORME_T3.html` para revisar las pantallas y documentos de ejemplo.

## Límites explícitos

- RF02, RF04, RF05, RF11–14 y RF28: fuera de la selección T3; materias primas, costeo de producción completo, calidad, pedidos y envío automático de correo no se implementan.
- RF03: implementado el ingreso de productos terminados; no se descuenta materia prima.
- RF17: existe plantilla académica con cliente, detalle, fecha, IGV y total. No se acredita cumplimiento tributario ni se inventa RUC o logo de la empresa.
- RF21/23: el historial se ordena por fecha y registra fecha/hora; RF22/24 (cierres contables y reglas de fechas) no se presentan como completados.
- RF26: botón de impresión con diálogo del sistema implementado; no se envió una impresión física porque no se verificó impresora. No se usa como evidencia principal de los 10 requisitos.
- RF31: los movimientos registran autor y fecha, pero no existe auditoría integral de todos los cambios de usuarios/clientes. No se declara completo.
- RF32–37: soporte para lectores que emulan teclado; se prueba la entrada de código por software. Falta comprobar el dispositivo físico. No se genera ni decodifica una imagen de código de barras.
- RF41: se selecciona un cliente registrado; se exige cliente en boletas/recibos y no se implementan facturas. No se declara completa su opción de cliente anónimo.
- Reportes automáticos: se actualizan al entrar y salir de sesión autorizada, no hay servicio programado con la aplicación cerrada.
- El historial de movimientos comienza en la nueva versión; no se reconstruyen movimientos antiguos que no estaban registrados en el ZIP.
- Disponibilidad 99%, pruebas de carga y otras plataformas: no certificadas por esta revisión local en Windows.
- Los informes de práctica de campo y responsabilidad social se adjuntan por separado por el estudiante.
