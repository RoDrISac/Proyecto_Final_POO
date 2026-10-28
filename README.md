# Sistema de Ventas e Inventario - Colores y Creaciones

Proyecto del curso Técnicas de Programación Orientada a Objetos (UPN, 2026-2).
Aplicación de consola en Java (NetBeans) implementada según los informes del grupo:
S07 (alcance), S08 (historias de usuario y requerimientos), S09 (diagrama de clases y manejo de archivos)
y S10 (requerimientos y criterios de aceptación).

## Cómo ejecutar
1. NetBeans → **File → Open Project** → seleccionar esta carpeta.
2. **Run** (F6) e iniciar sesión en la ventana Output.

| Código | Contraseña | Rol |
|---|---|---|
| U001 | 1234 | Administrador |
| U002 | 1234 | Vendedor |
| U003 | 1234 | Supervisor de almacén |

Para asignar un rol a un usuario nuevo se pide la contraseña del rol:
Administrador `ADM-2026`, Vendedor `VEN-2026`, Supervisor de almacén `SUP-2026`.

## Clases (diagrama de clases S09)
| Clase | Descripción |
|---|---|
| `Persona` | Clase padre abstracta: nombre, apellido, DNI, `mostrarPersona()` |
| `Usuario` | Hereda de Persona. `validarAcceso()`, `cambiarRol()` |
| `Cliente` | Hereda de Persona. `registrarCliente()` (valida datos), `obtenerHistorialVentas()` |
| `Rol` | Nombre, permisos y contraseña del rol. `tenerPermiso()` |
| `Producto` | Código, nombre, modelo, categoría, precio, descripción. `mostrarInfo()` |
| `Inventario` | Hereda de Producto. Stock, stock mínimo, costo, fecha de ingreso. `actualizarStock()`, `calcularGananciaUnitaria()`, `estaDisponible()` |
| `Venta` | Cliente, vendedor, productos/cantidades/precios, método de pago, tipo de comprobante. `agregarProducto()`, `calcularMontoTotal()`, `getResumenVenta()` |
| `DetalleVenta` | Línea del desglose de una venta: producto, cantidad, precio unitario. `calcularSubtotal()` |
| `Comprobante` | Boleta o recibo con IGV. `emitirPDF()`, `enviarPorCorreo()` |
| `Reporte` | Inventario, stock bajo, ventas y más vendidos del mes. `generar()`, `exportar()` (PDF o TXT) |
| `GestorArchivos`, `ArchivoPDF` | Manejo de archivos : CSV de datos y archivos PDF |
| `ProyectoFina` | Menú principal |

Atributos agregados al diagrama: `Producto.modelo` (código único por modelo),
`Inventario.stockMinimo` y `tieneStockBajo()` (alertas de stock), `Cliente.historialVentas` y `Comprobante.venta`.

## Funciones
- Inicio de sesión con código único y contraseña (3 intentos) y permisos por rol.
- Productos: registrar, editar, eliminar, ingreso de stock (producción o compra), ver inventario con alertas de stock bajo.
- Búsqueda por código: funciona con lector de código de barras (el lector escribe el código y presiona Enter).
- Clientes: registro con validación (DNI de 8 dígitos, correo), listado e historial de compras.
- Ventas: varios productos, verificación de stock, método de pago (efectivo, Yape, Plin, transferencia, tarjeta),
  confirmación, descuento automático del stock y comprobante PDF con numeración única (B001-/R001-).
- Reimpresión de comprobantes. El PDF del comprobante se abre automáticamente al emitirlo.
- Reportes: inventario, stock bajo, todas las ventas y productos más vendidos del mes, exportables a PDF o TXT
  (el archivo exportado se abre automáticamente).
- Usuarios: listado, registro y cambio de rol.

## Archivos que genera (carpeta `datos/`)
`usuarios.csv`, `clientes.csv`, `inventario.csv`, `ventas.csv`, `comprobantes/*.pdf` y `reportes/*`.
Se cargan al iniciar el programa, así que la información se conserva al cerrarlo.

Autores: Alvaro Agustín Silva Barrantes, Rodrigo Aldair Sánchez Carrasco.
