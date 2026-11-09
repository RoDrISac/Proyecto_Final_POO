# Colores y Creaciones — T3 mejorado

Versión gráfica de Java Swing / NetBeans. Conserva el historial del proyecto original y añade la entrega T3.

## Abrir y ejecutar

- **Más directo:** doble clic en `ejecutar.cmd` (primero extraer todo el ZIP). Si no hay JAR, se compila automáticamente. Se necesita JDK 17 o superior. En el equipo de preparación se comprobó con JDK 24.
- **NetBeans:** Archivo → Abrir proyecto → esta carpeta → Ejecutar proyecto (F6). El proyecto se llama **ColoresCreacionesT3**. Si pide plataforma, seleccionar el JDK instalado (17 o superior).
- El JAR ejecutable está en `dist/ColoresCreacionesT3.jar`. Si lo ejecutas manualmente, usa como directorio de trabajo la raíz del proyecto: `java -jar dist/ColoresCreacionesT3.jar`.
- `compilar.cmd` reconstruye el JAR sin librerías externas; `verificar.cmd` compila y ejecuta las pruebas.

## Acceso de demostración

| Código | Contraseña | Rol |
|---|---|---|
| U001 | 1234 | Administrador |
| U002 | 1234 | Vendedor |
| U003 | 1234 | Supervisor de almacén |

Son credenciales de demostración heredadas del proyecto original. Tras 3 intentos incorrectos hay que cerrar y volver a abrir la aplicación.

## Qué se completó

- Ocho vistas: acceso, resumen, inventario, clientes, nueva venta, historial, reportes y usuarios.
- Formularios con validación, confirmaciones, mensajes y navegación según rol.
- Servicios separados para inventario, clientes, ventas, usuarios y reportes; interfaz de repositorio y persistencia atómica.
- Composición Venta → DetalleVenta, instantáneas históricas, herencia de Persona y polimorfismo.
- Unidad de medida, movimientos con usuario/fecha/saldo, reporte por período, precisión monetaria y protección contra ventas parciales.
- Matriz trazable a S08, pruebas repetibles, pantallas y comprobantes reales de prueba.

## Datos

En una descarga de GitHub, `datos/` no se distribuye: la primera ejecución crea los tres usuarios de demostración y un catálogo vacío. Si colocas tus CSV en `datos/` antes del primer inicio, se importan y se crea `datos/estado-t3.dat`. A partir de ahí, este archivo es la fuente de datos; los CSV se conservan como entrada histórica y no se actualizan. Los PDF y reportes se guardan en `datos/comprobantes/` y `datos/reportes/`.

Para respaldar, cerrar la aplicación y copiar **toda la carpeta `datos`**. Solo se admite una instancia por carpeta, protegida por un bloqueo. Los datos de trabajo están excluidos de Git. `test/fixtures/` contiene únicamente datos ficticios para verificar la importación.

## Documentación de entrega

- `docs/INFORME_T3.html`: revisión visual con enlaces a todas las evidencias.
- `docs/MATRIZ_ACEPTACION.md`: criterios por número de S08 y alcance explícito.
- `docs/ARQUITECTURA.md`: responsabilidades y relaciones orientadas a objetos.
- `docs/GUION_DEMO.md`: demostración paso a paso.
- `docs/evidencias/`: logs, imágenes, PDF y movimiento de prueba.

Los dos informes de campo y responsabilidad social se adjuntan por separado. La calificación la determina el docente; esta entrega documenta lo implementado y lo efectivamente verificado.
