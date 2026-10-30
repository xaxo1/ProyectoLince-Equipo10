# Guía para la presentación del avance

## Qué se integró del diseño de Google Stitch
| Pantalla de Stitch | En la app |
|---|---|
| iniciar_sesion | `ui/screens/LoginScreen.kt` (logo moái, RUT o correo, aviso de fallo de autenticación) |
| servicios_del_dia | `ui/screens/InicioScreen.kt` (hora de recogida, estados, check-in, cancelado pagado, fin de jornada) |
| resumen_mensual | `ui/screens/ResumenScreen.kt` (total del mes, completados, cancelados pagados, detalle) + puntualidad |
| NavigationBar (todas) | `ui/navigation/AppNavigation.kt`: Hoy · Calendario · Disponibilidad · Resumen; perfil desde el avatar |
| DESIGN.md "Atacama Fieldwork" | `ui/theme/` y `ui/components/ComponentesComunes.kt` (tarjetas con borde arena, píldoras de estado sólidas, botones redondeados) |

## Requisitos del cliente (reunión 11/09) y dónde se cumplen
| Requisito | Dónde |
|---|---|
| Ver lo asignado sin llamadas | Hoy y Calendario |
| Requisitos de la agencia | Detalle del servicio |
| Controlar atrasos (15 min antes del pickup) | `Formatos.llegaConAtraso()` + `RepositorioLince.avanzarEstado()` |
| Premiar puntuales | Resumen: % de puntualidad |
| Bloqueo con una semana de anticipación | `RepositorioLince.bloquearDia()` |
| Cancelados igual se pagan | `RepositorioLince.obtenerResumen()` |
| Check-in no modificable (trazabilidad) | Sin botón "Reiniciar" en el Detalle |

## Contenidos del ramo
| Contenido | Ejemplo |
|---|---|
| Clase 6: UiState + MutableStateFlow privado + StateFlow público + update{copy} | `model/*UiState.kt` y `viewmodel/` |
| collectAsStateWithLifecycle() | todas las pantallas |
| Navigation Compose con argumentos | `detalle/{servicioId}`, `reserva/{numeroReserva}` |
| POO, herencia, polimorfismo | `Usuario` → `Guia` / `Transportista`, `informar()` |
| Colecciones (filter, map, groupBy, count, sumOf, fold, find) | `RepositorioLince`, `InicioUiState` |
| Corrutinas (suspend, launch, async) | `RepositorioLince`, `InicioViewModel` |
| try-catch-finally y Result | `PerfilViewModel`, `RepositorioLince.login()` |
| Room: Entity, DAO, Database | `data/local/*Entity.kt`, `data/local/*Dao.kt`, `data/local/LinceDatabase.kt` |
| Room: Repository | `RepositorioLince` mezcla los datos ficticios con lo guardado en Room (`conRegistro()`) |
| Room + Flow | `IncidenciaDao.observarDelServicio()` → `DetalleServicioViewModel.observarIncidencias()` |
| Permisos en tiempo de ejecución | `DetalleServicioScreen` (`RequestMultiplePermissions`) + `AndroidManifest.xml` |
| Recurso nativo: GPS | `data/UbicacionDispositivo.kt` (`LocationManagerCompat.getCurrentLocation`) |
| Recurso nativo: cámara | `IncidenciaScreen` (`TakePicturePreview`) → `RepositorioLince.guardarFoto()` |
| Validación de formularios | `IncidenciaViewModel.validarDescripcion()` / `validarPasajeros()`, `LoginViewModel.validarIdentificador()` |
| Búsqueda + filtro | `ServiciosViewModel.aplicarFiltro()` (combina estado + texto) |

## Recorrido para la demo
1. Login: "Ingresar" con los campos vacíos (error bajo cada campo) → RUT `123-4` (error de formato) → clave incorrecta (aviso rojo) → `15.482.910-K` / `1234`.
2. Hoy: "Iniciar check-in" en un servicio de hoy.
3. Detalle: "Iniciar servicio" → **permitir ubicación** → aparece la hora y las coordenadas GPS del check-in.
4. "Reportar incidencia": guardar vacío (errores por campo) → "Pasajero ausente" con todos los pasajeros (regla de negocio) → completar bien → **tomar foto** → guardar → confirmación → la incidencia aparece en el Detalle.
5. **Persistencia**: cerrar la app por completo (deslizarla desde recientes), volver a entrar → el check-in, la ubicación y la incidencia siguen ahí (Room).
6. "Finalizar servicio" (check-out con su propia ubicación). Ver reserva completa.
7. Calendario: buscar "maipo" o "SB-24290" + cambiar filtros.
8. Disponibilidad: bloquear un día de esta semana (error) y uno a más de 7 días (éxito); se conserva al reiniciar.
9. Resumen: total con cancelados pagados y puntualidad (incluye el servicio recién finalizado).
10. Avatar → Mi perfil → Cerrar sesión.

> En el emulador, la ubicación se cambia en *Extended controls (⋯) → Location*. La cámara del emulador muestra una escena virtual.
