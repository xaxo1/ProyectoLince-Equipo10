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

## Recorrido para la demo
1. Login con una clave incorrecta (aviso rojo) → Reintentar → `15.482.910-K` / `1234`.
2. Hoy: revisar las tarjetas → "Iniciar check-in" en "Santiago de Noche" (19:00).
3. En el Detalle: iniciar servicio (a tiempo) → finalizar servicio.
4. Ver reserva completa.
5. Calendario → filtros.
6. Disponibilidad: bloquear un día de esta semana (error) y uno a más de 7 días (éxito).
7. Resumen: total con cancelados pagados y puntualidad; mes anterior.
8. Avatar → Mi perfil → Cerrar sesión.
