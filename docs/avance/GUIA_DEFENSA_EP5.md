# Guía de la defensa EP5 — Lince / SouthTour (Equipo 10)

Presentación: **lunes 23-11-2026, 10:40 – 11:00** (Teams). 10 min de demo + 10 min de preguntas y modificación en vivo.

## Antes de entrar (15 min antes)

1. **Backend:** en `backend/` → `java -jar build\libs\lince-backend-1.0.0.jar`. Abrir http://localhost:8080/swagger-ui.html.
   - ¿Quieren partir de cero (sin check-in de los ensayos)? Detener el backend, borrar la carpeta `backend/data/` y volver a iniciarlo.
2. **Teléfono / emulador:** instalar `SouthTour/app/build/outputs/apk/release/app-release.apk`.
   - Teléfono físico: en `SouthTour/local.properties` poner `lince.backend.url=http://IP_DEL_PC:8080/` (misma WiFi) y volver a generar la APK (`gradlew.bat :app:assembleRelease`).
3. Android Studio abierto con `SouthTour/` y con `backend/` (para mostrar código y hacer la modificación).
4. Reporte de cobertura abierto: `SouthTour/app/build/reports/kover/htmlDebug/index.html`.

## Datos para la demo (servicios REALES de la Lince API del lunes 23)

| Usuario (clave 1234) | Servicio del día | Hotel (geocerca) |
|---|---|---|
| Transportista `12.345.678-5` (Nicolás Escobar, recurso 2) | **SRV 3129 · TRANSFER 18:15** Hotel Las Condes → Aeropuerto SCL | Hotel Las Condes: `-33.41450, -70.59400` |
| Guía `15.482.910-K` (Gonzalo Orellana, recurso 37) | **SRV 3114 · GUIDED_SERVICE 12:00** (Punta Arenas) · SRV 3119 está **cancelado (XX)** | Hotel Costanera Punta Arenas: `-53.15600, -70.90000` |
| Coordinación `coordinacion@southbound.cl` | Panel: hitos del día, posiciones, incidencias, radios | — |

**Emulador:** Extended controls (⋯) → *Location* → pegar la coordenada del hotel → *Set location*.
**Teléfono físico:** como el hotel es ficticio, coordinación mueve la geocerca a la sala: en Swagger `PUT /api/geocercas/4` con la latitud/longitud del lugar (la app muestra la ubicación al tocar *Medir distancia*).

## Guion de 10 minutos (sigue la guía del profesor)

| Min | Qué mostrar | Dónde está en el código |
|---|---|---|
| 0–1 | Problema del cliente y equipo. Cambiar idioma ES ↔ EN en *Mi perfil* (queda guardado) | `data/Sesion.kt` (DataStore) · `MainActivity.attachBaseContext` · `res/values-en` |
| 1–4 | Login transportista → **Hoy** (servicio real 3129) → **Calendario** (cuadrícula, filtros estado/tipo, búsqueda) → **Detalle**: radio 150 m, *Medir distancia* (distancia + precisión). Primero con la ubicación LEJOS del hotel → check-in **FUERA_DE_ZONA** + incidencia automática (el servicio sigue pendiente). Luego la ubicación EN el hotel → check-in **VERIFICADA** → servicio en curso → check-out. Login coordinación → panel con ambos hitos | `DetalleServicioViewModel.avanzarEstado()` · `util/Geocerca.kt` · `backend/.../HitoServicio.java` |
| 4–5:30 | Swagger: login → *Authorize* → `POST /api/hitos` o `PUT /api/confirmaciones/3129` → refrescar en la app. Explicar que el servidor revisa distancia, horario y asignación con JWT | `backend/web/*Controlador.java`, `servicio/HitoServicio.java`, `seguridad/` |
| 5:30–6:30 | **Lince API** desde la app: Perfil → *Catálogo Southbound*: servicios paginados (page/pageSize, data/meta), guías, transportistas, catálogos (CF/RQ/WL/XX, StatusPL). X-API-Key desde `local.properties` | `data/remoto/LinceApi.kt` · `Red.kt` (interceptor y requestId) |
| 6:30–7:30 | **Offline:** modo avión → check-in → *Pendiente de sincronizar* (Room) → quitar modo avión → WorkManager lo envía (sin duplicar: misma `idempotencia`) | `RepositorioLince.registrarHito()` · `sync/SincronizarHitosWorker.kt` · `data/local/Entidades.kt` |
| 7:30–8:30 | Pruebas: en Android Studio, clic derecho en `GeocercaTest` → *Run*. Mostrar el reporte Kover (99 %) y `LoginFormularioTest` (Compose UI Test) | `app/src/test/...` · `app/src/androidTest/...` |
| 8:30–9:15 | APK firmada: `signingConfigs` en `app/build.gradle.kts` lee el keystore desde `local.properties`; versión 2.0.0 (code 2) en Perfil | `app/build.gradle.kts` |
| 9:15–10 | GitHub (commits/Insights de cada uno) y Trello; cierre | — |

## Modificaciones en vivo probables (40 %) — recorrido del dato

**Cambiar el radio de 150 a 100 m**
- Para todas las geocercas nuevas: `backend/src/main/resources/application.properties` → `lince.geocerca.radio-defecto=100` (se usa al crear geocercas).
- Para un hotel existente: app de coordinación (botón −50) o Swagger `PUT /api/geocercas/{id}` con `radioMetros: 100` → la app lo muestra en el Detalle y el servidor lo usa al validar.

**Tolerancia de puntualidad configurable (ej. 20 min)**
- `application.properties` → `lince.puntualidad.minutos-antes=20` → reiniciar el backend. La app lo lee de `GET /api/reglas` y lo muestra en *Regla*.

**Motivo obligatorio al rechazar una asignación**
1. Backend: en `Dtos.ConfirmacionSolicitud` cambiar `motivo` a `@NotBlank String motivo`.
2. App: en `DetalleServicioViewModel.responderAsignacion()` pasar el motivo (y un campo de texto en `DetalleServicioScreen`).
3. Probar en Swagger (sin motivo → 400 VALIDATION_ERROR) y desde el teléfono.

**Campo «precisión del GPS» / validar estado CX o XX**
- La precisión ya viaja: `HitoSolicitud.precisionMetros` (app) → `Hito.precisionMetros` (entidad) → `ReglasGeocerca.evaluarCheckIn()`; el máximo está en `lince.geocerca.precision-maxima`.
- Los estados cancelados se validan en `HitoServicio.validarAsignacion()` (XX, CX, DL).

Después de cada cambio en la app: agregar o ajustar una prueba en `app/src/test/` y ejecutarla.
