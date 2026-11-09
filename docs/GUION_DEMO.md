# Demostración de T3 — 8 a 10 minutos

## Preparación

Extraer el ZIP completo. Abrir `ejecutar.cmd` o el proyecto en NetBeans. Mostrar `docs/INFORME_T3.html` en otra ventana. No trabajar dentro del ZIP. Los registros DEMO se pueden crear con otro código si ya existen por una demostración previa.

1. **Acceso y navegación (1 min):** ingresar U001 / 1234. Mostrar Resumen, Inventario, Clientes, Nueva venta, Historial, Reportes y Usuarios. Explicar que son vistas Swing con eventos reales.
2. **RF06/RF07 (1 min):** Inventario → Nuevo. Código DEMO01, nombre Bufanda, modelo Andino, categoría Textil, unidad Unidad, precio 25.50, costo 12, stock 5, mínimo 2. Guardar. Intentar repetir el código: debe rechazarse. Seleccionar y editar el nombre; no se permite modificar el stock por este formulario.
3. **RF35/RF19 (1 min):** buscar DEMO01 y pulsar Enter. Ingresar 2 unidades. Ver saldo 7. En Reportes → MOVIMIENTOS, usar el rango de hoy; mostrar entrada y saldo.
4. **Cliente (30 s):** Clientes → Nuevo cliente; usar código DEMOC, nombre Cliente, apellido Demo, DNI 87654321, correo demo@example.com. El DNI debe ser único. Una dirección de correo es opcional, pero si se escribe debe ser válida.
5. **RF16/RF40 (1 min):** Nueva venta → seleccionar cliente, pago Yape y Boleta. Escribir DEMO01. Intentar cantidad 8: mensaje de stock insuficiente. Ingresar cantidad 2 y Agregar. El total esperado es S/51.00. El stock aún debe ser 7 antes de confirmar.
6. **RF09/RF25/RF27/RF38/RF39/RF42/RF43/RF44 (1 min):** Confirmar venta y aceptar. Mostrar ID y PDF. Volver a Inventario: stock 5. Historial → seleccionar venta → Ver detalle y Generar y abrir PDF. Mostrar desglose de IGV incluido.
7. **RF15/RF18/RF20/RF29 (1 min):** mostrar stock bajo (el producto P002 del ejemplo), reporte de inventario y más vendidos del mes. Exportar PDF/TXT. La reimpresión conserva ID, fecha y datos históricos; la prueba automatizada además comprueba igualdad del PDF después de editar/eliminar un producto.
8. **RF30 (1 min):** cerrar sesión; ingresar U002 / 1234. Mostrar que Usuarios y Reportes no están en el menú y que Inventario es solo consulta. Las pruebas PA20 demuestran que tampoco es posible eludirlo invocando servicios.
9. **Calidad y evidencias (1 min):** abrir ARQUITECTURA.md y resultados-pruebas.txt. Señalar composición Venta/DetalleVenta, interfaz Repositorio, PA29 de fallo de disco y la matriz con números de S08. Adjuntar aparte los dos informes de campo.

## Comprobaciones manuales con equipo

- Lector físico: seleccionar campo Código / lector; escanear un código existente configurado para terminar en Enter. Debe añadir el producto correcto. No afirmar que se probó un equipo si solo se escribió el código.
- Impresora: Historial → Imprimir. Revisar la impresora y la vista previa; confirmar una sola copia únicamente si se dispone del equipo. La entrega no ejecutó una impresión física.
- No presentar el envío por correo, facturación tributaria, cierre contable ni materias primas como completados.
