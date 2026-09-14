# ProyectoLince-Equipo10
Evaluacion n°1 Desarrollo de aplicaciones moviles


# Proyecto Lince: Lince Driver

**Propósito:** Aplicación móvil diseñada para mejorar la trazabilidad y rapidez del proceso de asignación y confirmación de servicios turísticos de Southbound. Permite a los drivers y guías revisar sus asignaciones, confirmar o rechazar servicios, registrar disponibilidad y marcar hitos de check-in/check-out directamente en terreno.

## 🎨 Identidad Visual
* **Logotipo:** Ubicado en `docs/diseno/logo.png`
* **Paleta de Colores:**
  * **Principal:** Verde Oscuro (`#2E7D32`) - Para acciones afirmativas (Confirmar, Check-in).
  * **Secundario:** Ámbar (`#FF8F00`) - Para alertas o bloqueos de disponibilidad.
  * **Fondo:** Gris Oscuro (`#121212`) - Para reducir el cansancio visual del usuario en ruta.
  * **Texto:** Blanco (`#FFFFFF`) - Alto contraste para legibilidad en exteriores.

## 🔄 Flujo de Usuario (UML)

```mermaid
flowchart TD
    %% Nodos de inicio y fin
    Start([Inicio])
    End([Fin])

    %% Flujo de Autenticación
    Start --> Login[Pantalla: Login]
    Login --> Validar{¿Credenciales válidas?}
    Validar -- No --> Login
    Validar -- Sí --> Home[Pantalla: Inicio]

    %% Navegación Principal
    Home --> Menu{Seleccionar Opción}
    
    %% Flujos Secundarios
    Menu -- Gestionar Fechas --> Disp[Pantalla: Disponibilidad]
    Disp --> End
    
    %% Flujo Principal de Asignaciones
    Menu -- Ver Asignaciones --> Listado[Pantalla: Listado de Servicios]
    Listado --> Detalle[Pantalla: Detalle del Servicio]
    
    %% Toma de Decisiones en el Servicio
    Detalle --> Decision{¿Qué acción realizar?}
    
    %% Camino 1: Confirmar
    Decision -- Confirmar --> Confirmacion[Registrar Confirmación]
    Confirmacion --> EstConf[Estado: Confirmada]
    EstConf --> Listado
    
    %% Camino 2: Rechazar
    Decision -- Rechazar --> Rechazo[Ingresar Motivo]
    Rechazo --> EstRech[Estado: Rechazada]
    EstRech --> Listado
    
    %% Camino 3: Ejecución en terreno
    Decision -- Iniciar Ruta --> CheckIn[Registrar Check-in]
    CheckIn --> GPS1[Capturar Hora y Ubicación]
    GPS1 --> EstCurso[Estado: En Curso]
    
    EstCurso --> CheckOut[Registrar Check-out]
    CheckOut --> GPS2[Capturar Hora y Ubicación]
    GPS2 --> EstFin[Estado: Finalizada]
    
    EstFin --> End