# TASKS.md — Reparto de trabajo SIVIDO

> Documento del equipo. Fuentes: código del repo (`main`), el canónico de Visual Paradigm
> `SIVIDO_CETS_U4.vpp` (diagramas de secuencia, clases, actividad y *Diagrama de navegación SIVIDO*)
> y el script `SIVIDO_CETS_mysql.sql`.
> Los marcadores `[DAVID]`, `[Galilea]`, `[Alex Torres]` y `[Diego Roque]` identifican a cada integrante.

---

## Índice

1. [Resumen](#1-resumen)
2. [Orden y dependencias](#2-orden-y-dependencias)
3. [Reglas obligatorias](#3-reglas-obligatorias)
4. [Patrón a copiar (HU-1.1, HU-1.2, HU-6.1)](#4-patrón-a-copiar-hu-11-hu-12-hu-61)
5. [Tareas por integrante](#5-tareas-por-integrante)
   - [DAVID — Panel CETS + cuentas](#51-david--panel-cets--cuentas)
   - [Galilea — Solicitudes + donación](#52-galilea--solicitudes--donación)
   - [Alex Torres — Notificaciones y cuestionario](#53-alex-torres--notificaciones-y-cuestionario)
   - [Diego Roque — Mi historial](#54-diego-roque--mi-historial)
6. [Preparar el entorno](#6-preparar-el-entorno)
7. [Flujo de git](#7-flujo-de-git)
8. [Pruebas de regresión antes de merge](#8-pruebas-de-regresión-antes-de-merge)
9. [Decisiones tomadas](#9-decisiones-tomadas)
10. [Inconsistencias a revisar](#10-inconsistencias-a-revisar)

---

## 1. Resumen

**SIVIDO** (Sistema de Vinculación de Donadores) conecta a donadores voluntarios con solicitudes
urgentes de sangre de reposición del **CETS Oaxaca**:

- Un familiar captura una solicitud con el **folio CETS**.
- Se genera una publicación pública con QR.
- Se notifica a donadores **compatibles y cercanos**, sin exponer su ubicación exacta.
- El donador responde un cuestionario orientativo y confirma su interés.
- El personal del CETS consulta solicitudes, cambia estatus, registra donaciones y administra
  los criterios de exclusión.

### Arquitectura (proyecto Enterprise Application de NetBeans: EAR + `-ejb` + `-war`)

```
vista .xhtml (PrimeFaces)                       -war/web/vista/<módulo>/X.xhtml
   │  #{xController.entidad.campo}
   ▼
controller @Named (@RequestScoped/@ViewScoped)  -war/src/java/controller/
   │  @EJB  (pasa la ENTIDAD completa)
   ▼
Ln @Stateless @LocalBean                        -ejb/src/java/ln/        (= Control del diagrama)
   │  @EJB
   ▼
Facade (AbstractFacade<T>)                      -ejb/src/java/acceso_datos/
   │  EntityManager (PU mx.edu.itoaxaca.sividocets-ejbPU)
   ▼
Entity JPA                                      -ejb/src/java/modelo/
   │  jdbc/sivido  →  pool sividoPool
   ▼
MySQL 8, base `sivido`
```

Stack:

| Pieza | Versión |
|---|---|
| Servidor | Payara 7 |
| Plataforma | Jakarta EE 11 |
| JDK | 21 |
| PrimeFaces | 16.0.1 |
| Base de datos | MySQL 8 |

### Ya terminado (es el ejemplo a seguir)

| HU | Vista | Controller → Ln |
|---|---|---|
| HU-1.1 Alta inicial | `cuentas/crearCuenta` | `CuentasController.registrarUsuario()` → `LnCuentas.registrarUsuario(Usuario)` |
| HU-1.2 Autenticación | `cuentas/iniciarSesion` | `CuentasController.iniciarSesion()` → `LnCuentas.iniciarSesion(Usuario)` |
| HU-6.1 Login institucional | `cets/loginInstitucional` | `PanelCetsController.iniciarSesionInstitucional()` → `LnPanelCETS.iniciarSesionInstitucional(PersonalCETS)` |

`SesionController` (`@SessionScoped`) guarda el `Usuario` y el `rol` (`"usuario"` o `"CETS"`). Las plantillas
`plantillaUsuario` y `plantillaInstitucional` llaman `verificar()` y `verificarCETS()` en cada vista.

### Tabla de reparto

| Integrante | Módulo | HU | Vistas | Controller → Ln |
|---|---|---|---|---|
| **[DAVID]** | Panel CETS + cuentas | HU-6.2, 6.3, 6.4, 1.3 (incluye el checkbox de contacto de HU-1.4), 3.4 (+ T0 compartidas y T-final: diagrama) | `cets/solicitudesActivas`, `cets/detalleSolicitud`, `cuentas/miPerfil` | `PanelCetsController` → `LnPanelCETS`; `CuentasController` → `LnCuentas` |
| **[Galilea]** | Solicitudes + donación | HU-2.1, 2.2, 2.3, 2.4, 1.4, 5.1, 5.4, 7.3 | `solicitudes/capturarSolicitud`, `solicitudes/misSolicitudes`, `publico/publicacion`, `cets/capturarDonacion`, `cets/donadores` | `SolicitudController` → `LnSolicitud`; `DonacionController` → `LnDonacion` |
| **[Alex Torres]** | Notificaciones y cuestionario | HU-3.1, 3.2, 3.3, 4.1, 4.2, 4.3 | `notificaciones/notificaciones`, `notificaciones/cuestionario`, `notificaciones/resultado`, `notificaciones/confirmarInteres`, `cets/criteriosExclusion` | `NotificacionController` → `LnNotificacion`; `CuestionarioController` → `LnCuestionario` |
| **[Diego Roque]** | Mi historial | HU-5.2, 5.3 (+ `esElegible` para HU-3.3) | `historial/miHistorial` | `HistorialController` (o métodos en `DonacionController` si lo acuerda con [Galilea]) → `LnDonacion` (solo métodos de consulta) |

Total: **21 HU** por implementar.

---

## 2. Orden y dependencias

```
Día 1  [DAVID] T0 (metadata en plantillaBase + datos de prueba comunes) ─┐
                                                                         ├─► todos pueden empezar
Semana 1  [Galilea] HU-2.4 + HU-2.1 (solicitudes reales en la BD)  ◄─────┘   (mientras tanto: INSERT de prueba)
          [Diego]   HU-5.2, HU-5.3 y esElegible() ───► lo usan [Alex] (HU-3.3) y [Galilea] (HU-5.4)
          [DAVID]   HU-6.2 (listarSolicitudesActivas) ─► lo usa [Galilea] en capturarDonacion (HU-5.1)
Semana 2  [Alex]    HU-3.1/3.2 buscarDonadoresCercanos() ─► [Galilea] lo llama al final de registrarSolicitud
          integración + regresión
```

| Quién necesita | Qué | De quién | Mientras tanto |
|---|---|---|---|
| Todos | Solicitudes en la BD | [Galilea] HU-2.1 | Correr `sql/datos_prueba.sql` (solicitudes 901–903, sección 2.2) |
| Todos los que usan `f:viewParam` | `<ui:insert name="metadata"/>` en `plantillaBase.xhtml` | [DAVID] T0.1 | — (son 5 minutos; DAVID lo sube a `main` el día 1) |
| [Alex] HU-3.3 | `LnDonacion.esElegible(Usuario)` | [Diego] | Un *stub* local que regrese `true` (no subirlo) |
| [Galilea] HU-5.4 | `LnDonacion.obtenerHistorial(Usuario)`, `obtenerElegibilidad(Usuario)`, `verificarExclusion(Usuario)` | [Diego] | Mostrar solo la lista de donadores |
| [Galilea] HU-5.1 | `LnPanelCETS.listarSolicitudesActivas()` | [DAVID] HU-6.2 | `INSERT` de prueba + combo vacío |
| [Galilea] HU-2.1 (paso final) | `LnNotificacion.buscarDonadoresCercanos(SolicitudReposicion)` | [Alex] HU-3.1 | Dejar `// TODO HU-3.1` en `LnSolicitud.registrarSolicitud` |
| [Galilea] HU-2.2 / HU-1.4 | `LnSolicitud.obtenerPublicacion(...)` | [Galilea] (ambas HU, D8) | — |
| [Alex] HU-3.1 | Donadores con `latitud/longitud` (botón "Usar mi ubicación", D1) | [DAVID] HU-1.3 | `sql/datos_prueba.sql` (903 y 904 ya traen coordenadas, sección 2.1) |

### Datos de prueba por módulo

Los datos de prueba están en el repo:

| Archivo | Qué hace |
|---|---|
| `sql/datos_prueba.sql` | Carga los datos de 2.1 a 2.4 en una BD que **ya tiene las 17 tablas**, en orden de llaves foráneas y dentro de una transacción |
| `sql/limpiar_datos_prueba.sql` | Borra **solo** esos datos y lo que se haya generado en la app con las cuentas de prueba |
| `sql/SIVIDO_CETS_mysql.sql` | Script de tablas (solo para quien todavía no las tiene; ver 6.2) |

Cómo están hechos:

- Todos los registros usan **IDs fijos 901+**. La limpieza borra por esos IDs exactos, nunca por rangos, así que no
  toca datos reales.
- Los hospitales solo se insertan si no existe ya uno con el mismo nombre, y las solicitudes buscan el hospital por
  nombre.
- Para recargar desde cero: `limpiar_datos_prueba.sql` y luego `datos_prueba.sql`.
- Si un registro real apunta a un dato de prueba en una columna que admite NULL, se desliga en vez de borrarse.
  Ejemplos: una solicitud real atendida por el personal 901, o una donación real ligada a la solicitud 901.
- Ninguno de los dos scripts usa `EXIT`, así que corren en el editor SQL de NetBeans.
- **Con la app desplegada, redespliega (Run en NetBeans) después de correr cualquiera de los dos scripts.** La aplicación
  guarda en caché lo que ya leyó y no se entera de cambios hechos directo en MySQL (Inconsistencias #34).
- Efecto secundario: después de cargarlos, los IDs `AUTO_INCREMENT` de esas tablas siguen desde el 902–906. Es normal.

**Contraseñas de prueba**, en SHA-256 sin sal, igual que `LnCuentas.hash()`:

| Cuenta | Correo de login | Contraseña | Hash SHA-256 |
|---|---|---|---|
| Personal CETS (rol `Administrador`) | `admin@cets.oaxaca.gob.mx` (login institucional) | `Cets2026!` | `b4eb84f0c1d0ee7d6a1cd590e626a405a50e2a005dc68683543bc93762cd8525` |
| Familiar | `familiar@prueba.mx` | `Donador2026!` | `555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4` |
| Donador cercano O− | `donador1@prueba.mx` | `Donador2026!` | (mismo) |
| Donador lejano A+ | `donador2@prueba.mx` | `Donador2026!` | (mismo) |

#### 2.1 Comunes (todos los necesitan)

| Tabla | IDs | Contenido |
|---|---|---|
| `Hospital` | 901–905 | Hospital Civil, HRAEO, HGZ 1 IMSS, Presidente Juárez ISSSTE y Hospital de la Niñez, con coordenadas aproximadas de Oaxaca |
| `Usuario` + `PersonalCETS` | 901 | Personal CETS `admin@cets.oaxaca.gob.mx`, rol `Administrador`, activo |
| `Usuario` | 902, 903, 904 | Familiar, donador cercano y donador lejano |
| `PerfilClinico` | 903, 904 | 903: O−, radio 5, a ~1 km del Hospital Civil. 904: A+, radio 3, en el Istmo (~190 km) |

#### 2.2 Solicitudes (mientras [Galilea] termina HU-2.1)

| ID | Folio | Tipo | Estatus | Hospital |
|---|---|---|---|---|
| 901 | `CETS-OAX-2026-0901` | O− | Activa | Hospital Civil |
| 902 | `CETS-OAX-2026-0902` | A+ | Activa | HRAEO |
| 903 | `CETS-OAX-2026-0903` | B+ | Atendida | HGZ 1 IMSS |

Todas las creó el familiar 902 y tienen un `uuidPublico` aleatorio.

#### 2.3 Notificaciones y cuestionario ([Alex])

| Tabla | IDs | Contenido |
|---|---|---|
| `CriterioExclusion` | 901–904 | Tatuaje (Temporal, 365), hepatitis (Permanente), antibióticos (Temporal, 7) y peso (Temporal, 0, **inactivo**) |
| `Notificacion` | 901 | Para el donador 903 sobre la solicitud 901, a 1.00 km, `Enviada` |

#### 2.4 Donaciones e historial ([Diego] y [Galilea])

| Tabla | IDs | Contenido |
|---|---|---|
| `RegistroDonacion` | 901, 902 | 903 donó hace 200 días (ya elegible). 904 donó hace 20 días, ligado a la 903 (próxima fecha en 36 días) |
| `HistorialClinico` | 901 | 904: Hepatitis C con el criterio 902 (Permanente) |
| `Usuario.exclusionPermanente` | 904 | `TRUE` (D10, fuente única) |
| `InstitucionExterna` + `HistorialExterno` | 901 | Donación externa de 903 el 2025-03-10 |

---

## 3. Reglas obligatorias

Estas reglas las marcó el profe y aplican a todo el código nuevo.

1. **La entidad viaja completa.**
   - La vista llena `#{xController.entidad.campo}`.
   - El controller pasa **la entidad** al Ln.
   - El Ln la recibe como parámetro, por ejemplo `registrarSolicitud(SolicitudReposicion s)`.
   - No se permiten campos sueltos (`String folio`, `int hospitalId`) ni atributos que dupliquen los de la entidad.
   - Solo se aceptan campos propios de la vista que **no** existen en la entidad, como
     `confirmacion` y `aceptaAviso` en `CuentasController`.
   - Para relaciones, el controller crea el objeto vacío en su constructor, por ejemplo
     `solicitud.setHospitalid(new Hospital())`. La vista llena el id
     (`#{solicitudController.solicitud.hospitalid.id}`) y el Ln hace `find()` de la relación.
2. **Ln delgado** (`@Stateless @LocalBean`, con `@EJB` a las facades):
   - **Sí** va en el Ln: la lógica de negocio (validar, calcular, decidir, poner fechas, UUID, hash).
   - **Sí** se permite un método **público** del Ln que sea una sola línea a la facade cuando es un mensaje del
     diagrama. Es el estilo de la clase `Tutores` del profe: `getTutores()` → `tutorF.findAll()`.
   - **No** se crean métodos *privados* que solo envuelvan una llamada a la facade, como
     `guardarUsuario(u) { usuarioF.create(u); }`. Llama la facade directo. `LnCuentas` ya está así.
   - Las reglas que el diagrama pone **en la entidad** (`Usuario.autenticarse`,
     `SolicitudReposicion.validarFormatoFolio`, `PerfilClinico.validarCompatibilidad`, `RegistroDonacion.calcularProximaFecha`…)
     se implementan **en el Ln**, no en la entidad.
3. **Consultas nuevas = métodos NUEVOS en la facade.**
   - No modifiques los métodos que ya existen (`create`, `edit`, `find`, `findAll`, `existeCorreo`, `buscarPorCorreo`…).
   - Usa el estilo de `UsuarioFacade`: `getEntityManager().createQuery(...)` o `createNamedQuery(...)`.
   - Los valores por defecto ya están en los constructores vacíos de las entidades:
     `PerfilClinico.radioNotificacion=5`, `SolicitudReposicion.estatus="Activa"`,
     `CriterioExclusion.tipoRespuesta="SiNo"`, `activo=true`, `PersonalCETS.activo=true` e `InstitucionExterna.activa=true`.
     No los vuelvas a poner en el Ln.
4. **Nombres del Ln = mensajes del Control en la secuencia.**
   - Si el diagrama trae parámetros sueltos, se **conserva el nombre** y se cambia la firma a la entidad.
     Ejemplo: `ControlSolicitud.registrarSolicitud(String folioCETS, …, int hospitalId, …)` →
     `LnSolicitud.registrarSolicitud(SolicitudReposicion s)`.
   - El id del usuario en sesión **no** se pasa como parámetro suelto. El controller lo toma de
     `SesionController` y lo pone en la entidad (`s.setUsuarioid(sesion.getUsuario())`).
5. **La navegación sigue el *Diagrama de navegación SIVIDO*.**
   - Los outcomes son `/vista/<módulo>/X` (con `?faces-redirect=true` después de una acción).
   - Los parámetros entre vistas van por query string y se reciben con `f:viewParam` (ver sección 4).
6. **Vistas:**
   - Conserva el diseño, las clases CSS y la estructura de la maqueta.
   - **Quita los datos de ejemplo fijos** (nombres, folios, contadores "(24)", filas repetidas) y cámbialos por EL
     y `ui:repeat`.
   - No borres secciones de la maqueta. Si algo no tiene respaldo en la BD, déjalo y anótalo en
     [Inconsistencias](#10-inconsistencias-a-revisar).
7. **Archivos COMPARTIDOS.** Solo los cambia [DAVID]; si alguien más necesita tocarlos, avisa antes en el grupo:
   - `-ejb/src/java/modelo/*.java` (entidades)
   - `-war/src/java/controller/SesionController.java`
   - `-war/web/plantillas/*.xhtml` y `-war/web/resources/css/**`
     ([Galilea] sí puede agregar la librería de QR en `-war/web/resources/js/`, decisión D5)
   - `-war/web/WEB-INF/web.xml`, `-ejb/src/conf/persistence.xml`
   - `*/nbproject/project.properties`, `*/nbproject/project.xml`
   - Las **facades** sí las puede editar cualquiera, pero **solo agregando métodos nuevos al final**.
     Si dos personas agregan métodos al mismo archivo, git normalmente lo une sin conflicto.
8. **Los controllers solo llaman al Ln, nunca a los facades.** Si el Ln solo lee una entidad, igual lleva el método
   (es el mensaje del diagrama de secuencia). Ejemplo: `LnPanelCETS.obtenerDetalle(s)` solo hace un `find`, pero existe
   porque el controller no puede inyectar `SolicitudReposicionFacade`.

---

## 4. Patrón a copiar (HU-1.1, HU-1.2, HU-6.1)

### Controller

Ejemplo: `-war/src/java/controller/CuentasController.java`.

```java
@Named
@RequestScoped               // o @ViewScoped (+ implements Serializable) si la vista hace postback sobre listas
public class SolicitudController {

    @EJB
    private LnSolicitud lnSolicitud;

    @Inject
    private SesionController sesion;          // usuario en sesión: sesion.getUsuario()

    private SolicitudReposicion solicitud;    // entidad que llena la vista

    public SolicitudController() {            // NO usar @PostConstruct: jakarta.annotation no está en el classpath del war
        solicitud = new SolicitudReposicion();
        solicitud.setHospitalid(new Hospital());
    }

    // HU-2.1
    public String registrarSolicitud() {
        solicitud.setUsuarioid(sesion.getUsuario());
        boolean valido = lnSolicitud.registrarSolicitud(solicitud);
        if (valido) {
            return "/vista/solicitudes/misSolicitudes?faces-redirect=true";
        }
        error("El folio no tiene el formato correcto o ya tiene una solicitud activa");
        return null;
    }
    // private void error(String) igual que en CuentasController; getters/setters de solicitud
}
```

### Ln

Ejemplo: `-ejb/src/java/ln/LnCuentas.java`.

```java
@Stateless
@LocalBean
public class LnSolicitud {
    @EJB
    private SolicitudReposicionFacade solicitudF;
    @EJB
    private HospitalFacade hospitalF;

    public boolean registrarSolicitud(SolicitudReposicion s) {
        boolean valido = validarFolio(s);                       // HU-2.4
        if (!valido) {
            return false;
        }
        s.setHospitalid(hospitalF.find(s.getHospitalid().getId()));
        s.setFechaHoraRegistro(new Date());
        s.setUuidPublico(UUID.randomUUID().toString());         // generarUuidPublico() del diagrama
        solicitudF.create(s);
        return true;
    }
}
```

### Facade: método nuevo al final del archivo

```java
public boolean existeFolioActivo(String folio) {
    return getEntityManager()
            .createQuery("SELECT COUNT(s) FROM SolicitudReposicion s "
                       + "WHERE s.folioCETS = :folio AND s.estatus = 'Activa'", Long.class)
            .setParameter("folio", folio)
            .getSingleResult() > 0;
}
```

> En JPQL las relaciones se navegan por el **atributo Java**, no por la columna. Por ejemplo:
> `s.usuarioid = :usuario`, `s.hospitalid.id = :id`, `n.solicitudReposicionid = :s` e `i.solicitudReposicion = :s`
> (en `Interes` el atributo no lleva `id`).

### Vista con parámetro de entrada

Ejemplo: `cets/detalleSolicitud.xhtml?id=901`. Requiere la tarea **T0.1** de [DAVID].

```xml
<ui:composition template="/plantillas/plantillaInstitucional.xhtml" ...>
    <ui:define name="metadata">
        <f:metadata>
            <f:viewParam name="id" value="#{panelCetsController.solicitud.id}"/>
            <f:viewAction action="#{panelCetsController.obtenerDetalle}"/>
        </f:metadata>
    </ui:define>
    ...
```

Enlace hacia esa vista:
`<p:button outcome="/vista/cets/detalleSolicitud" value="Ver solicitud"><f:param name="id" value="#{s.id}"/></p:button>`

### Cuidados que ya nos costaron tiempo

- **Los `f:viewAction` corren antes que el `preRenderView` de las plantillas**, que es donde se verifica la sesión. Si el
  controller consulta la BD en un `f:viewAction` (o en una acción), llama primero a `sesion.verificar()` (vistas de usuario)
  o `sesion.verificarCETS()` (vistas CETS) y regresa si `FacesContext...getResponseComplete()` es `true`, como
  `PanelCetsController`, `PerfilController` y `DonacionController`.
- `@PostConstruct` **no compila** en el war porque falta `jakarta.annotation`. Inicializa en el constructor
  o usa `f:viewAction`.
- Las entidades con **PK compartida** (`PerfilClinico`, `PersonalCETS`, `Interes`) no tienen `@GeneratedValue`.
  Antes de `create()` hay que poner a mano `p.setUsuarioid(usuario.getId())` o, en el caso de Interes,
  `i.setInteresPK(new InteresPK(uid, sid))`.
- Dentro del Ln, **no** "ocultes" datos con `setX(null)` sobre una entidad que vino de `find()`.
  Está administrada por JPA y el cambio **se guarda** en la BD. Para ocultar, la vista simplemente no
  muestra ese campo.
- Las fechas `DATE` (`fechaDonacion`, `fechaRegistro`) usan `p:datePicker`; no uses `datetime-local`.
- Después de cada cambio, compila con JDK 21:
  `ant -Duser.properties.file=$HOME/.netbeans/25/build.properties clean dist`
  (o Clean and Build en NetBeans).

---

## 5. Tareas por integrante

Formato de cada HU:

1. Objetivo.
2. Secuencia del `.vpp`: número, mensaje y su equivalente en código.
3. Firma corregida.
4. Archivos.
5. Facade.
6. Navegación.
7. Criterios de terminado.
8. Checklist.

Notación de guardas, como en el `.vpp`: `alt[...]`, `opt[...]`, `loop[...]`, `neg[...]`
(`neg` = **prohibido**: lo que el sistema nunca debe hacer) y `critical`.

---

### 5.1 [DAVID] — Panel CETS + cuentas

Rama: `feature/panel-cets`

#### T0 — Tareas compartidas (día 1, antes que los demás)

- [x] **T0.1** Agregar `<ui:insert name="metadata"/>` en `-war/web/plantillas/plantillaBase.xhtml`, justo antes de
      `<h:head>`. Así cualquier vista puede definir `<ui:define name="metadata"><f:metadata>…`, aun con las
      plantillas anidadas (`plantillaUsuario`, `plantillaInstitucional` y `plantillaPublica` heredan de `plantillaBase`).
- [x] **T0.2** Subir al repo `sql/SIVIDO_CETS_mysql.sql`, `sql/datos_prueba.sql` y `sql/limpiar_datos_prueba.sql`
      (sección 2).
- [x] **T0.3** Correr `sql/datos_prueba.sql` y comprobar el login CETS con `admin@cets.oaxaca.gob.mx` / `Cets2026!`.
- [x] **T0.4** Merge a `main` y avisar al equipo.

---

#### HU-6.2 Visualizar solicitudes activas

**Objetivo:** el personal CETS ve la lista de solicitudes activas y la puede filtrar por estatus, tipo sanguíneo y Rh.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `UIPanelCETS → ControlPanelCETS.listarSolicitudesActivas(): List<SolicitudReposicion>` | `f:viewAction="#{panelCetsController.listarSolicitudesActivas}"` → `lnPanelCETS.listarSolicitudesActivas()` |
| 1.1 | `ControlPanelCETS → DBMS: consultarSolicitudesActivas()` → `return solicitudes` | `solicitudF.buscarFiltradas(filtro)` con el filtro por defecto (la entidad nueva ya trae `estatus = "Activa"`) |
| 2 | `PersonalInstitucional → UIPanelCETS.filtrar(estatus, tipoSanguineo, factorRh)` | Botón "Refrescar" → `#{panelCetsController.filtrarSolicitudes}` |
| 2.1 | `UIPanelCETS → ControlPanelCETS.filtrarSolicitudes(String estatus, String tipoSanguineo, String factorRh)` | `lnPanelCETS.filtrarSolicitudes(filtro)` |
| 2.1.1 | `ControlPanelCETS → DBMS: consultarSolicitudesFiltradas(estatus, tipoSanguineo, factorRh)` → `return solicitudes` | `solicitudF.buscarFiltradas(filtro)` |

**Firmas corregidas (LnPanelCETS)**

```java
public List<SolicitudReposicion> listarSolicitudesActivas()                       // = filtrarSolicitudes(new SolicitudReposicion()): el constructor ya trae "Activa"
public List<SolicitudReposicion> filtrarSolicitudes(SolicitudReposicion filtro)   // normaliza: "" o null = todos; llama a la facade con un filtro limpio
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnPanelCETS.java` (`@EJB SolicitudReposicionFacade solicitudF`) |
| Modificar | `-ejb/src/java/acceso_datos/SolicitudReposicionFacade.java` (métodos nuevos) |
| Modificar | `-war/src/java/controller/PanelCetsController.java` (`List<SolicitudReposicion> solicitudes`, `SolicitudReposicion filtro`) |
| Modificar | `-war/web/vista/cets/solicitudesActivas.xhtml` |

`PanelCetsController` hoy es `@RequestScoped` por el login. Si el filtro hace postback, conviene pasarlo a
`@ViewScoped` con `implements Serializable` y comprobar que el login sigue funcionando (regresión HU-6.1).

**Facade nueva (`SolicitudReposicionFacade`)**

```java
// Hecho. Un solo método cubre los mensajes 1.1 y 2.1.1: arma el JPQL solo con las condiciones que no son null
// (así no se usa "? IS NULL"), ordena por fechaHoraRegistro DESC.
public List<SolicitudReposicion> buscarFiltradas(SolicitudReposicion filtro)
```

**Vista:**

- Hecho: las 6 filas fijas pasaron a un `p:dataTable` (`styleClass="tabla"`, paginador de 10 filas) sobre
  `#{panelCetsController.solicitudes}`. Se agregó un bloque de CSS `body .ui-datatable.tabla …` en `estilos.css`
  para conservar el aspecto de `.tabla`; **falta revisarlo a ojo en el navegador**.
- `PanelCetsController` pasó a `@ViewScoped` (la lista debe sobrevivir al paginador); la contraseña del login se borra
  del bean después de cada intento.
- Las tarjetas de resumen se calculan sobre las solicitudes mostradas (ver [Inconsistencias](#10-inconsistencias-a-revisar) #20).
- Quita los contadores fijos "(24)", "(15)"… del combo de estatus.
- Los values del combo de estatus deben coincidir con la BD: `Activa`, `Atendida`, `Cerrada`, `Cancelada`, y vacío para "Todas".
- El combo "grupo" (`O+`, `A-`…) mezcla tipo y Rh. Sepáralo en dos combos (`filtro.tipoSanguineo` y `filtro.factorRh`).
- El botón "Ver solicitud" debe llevar `<f:param name="id" value="#{s.id}"/>`.

**Navegación**

- Entrada: `loginInstitucional → solicitudesActivas` (clic "Ingresar al portal institucional", `[autenticado == true && rol == "CETS"]`).
  También desde el menú del Panel CETS, "Solicitudes activas".
- Salida: `solicitudesActivas → detalleSolicitud` (clic "Ver solicitud", con `?id=`).

**Terminado cuando**

- Con los `INSERT` de 2.2, al entrar como CETS se ven las solicitudes 901 y 902 (activas) y **no** la 903.
- Filtrar por estatus "Atendida" muestra solo la 903. Filtrar por O / − muestra solo la 901.
- Sin sesión CETS, abrir `/faces/vista/cets/solicitudesActivas.xhtml` te regresa a `cets/loginInstitucional`.
- SQL de verificación:
  `SELECT id, folioCETS, tipoSanguineo, factorRh, estatus FROM SolicitudReposicion WHERE estatus = 'Activa' ORDER BY fechaHoraRegistro DESC;`
  debe coincidir con la tabla.

**Checklist**

- [x] `buscarFiltradas(...)` en `SolicitudReposicionFacade` (no hizo falta `buscarActivas`)
- [x] `listarSolicitudesActivas()` y `filtrarSolicitudes(SolicitudReposicion)` en `LnPanelCETS`
- [x] Controller: lista, filtro y método para el `f:viewAction`
- [x] Vista: `p:dataTable`, combos separados, `f:param id`, sin datos fijos
- [x] Pruebas HTTP automáticas (login, listado, 4 filtros, sin sesión y rol equivocado): todas pasan
- [x] Revisión visual en el navegador (tabla, paginador, tarjetas)
- [x] Regresión (sección 8)

---

#### HU-6.3 Ver detalle de donadores vinculados a una solicitud

**Objetivo:** al elegir una solicitud, el CETS ve su detalle y cuántos donadores confirmaron interés,
**sin** ver la ubicación de ningún donador.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIPanelCETS.seleccionarSolicitud(int solicitudId)` | Clic "Ver solicitud" (`?id=`) |
| 1.1 | `UIPanelCETS → UIDetalleSolicitud.mostrar(int solicitudId)` | Navega a `cets/detalleSolicitud` |
| 2 | `UIDetalleSolicitud → ControlPanelCETS.obtenerDetalle(int solicitudId): SolicitudReposicion` | `f:viewParam id` → `solicitud.id`; `f:viewAction` → `lnPanelCETS.obtenerDetalle(solicitud)` |
| 2.1 | `ControlPanelCETS → DBMS: consultarSolicitudPorId(solicitudId)` → `return solicitud` | `solicitudF.find(s.getId())` (devuelve null si no hay id) |
| 3 | `UIDetalleSolicitud → ControlPanelCETS.obtenerCantidadInteresados(int solicitudId): int` | `lnPanelCETS.obtenerCantidadInteresados(solicitud)` |
| 3.1 | `ControlPanelCETS → DBMS: contarInteresados(solicitudId)` → `return cantidadInteresados` | `interesF.contarPorSolicitud(s)` |
| neg | `ControlPanelCETS → UIDetalleSolicitud: enviarUbicacionDonador()` `neg[mostrarUbicacion == true]` | **Prohibido:** la vista nunca muestra `PerfilClinico.latitud/longitud` |

**Firmas corregidas (LnPanelCETS)**

```java
public SolicitudReposicion obtenerDetalle(SolicitudReposicion s)        // regresa la solicitud completa (o null)
public int obtenerCantidadInteresados(SolicitudReposicion s)
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnPanelCETS.java` (`@EJB InteresFacade interesF`) |
| Modificar | `-ejb/src/java/acceso_datos/InteresFacade.java` (método nuevo; avisa a [Alex], que también usa esta facade) |
| Modificar | `-war/src/java/controller/PanelCetsController.java` (`SolicitudReposicion solicitud` y `int cantidadInteresados`; inicializa `solicitud` en el constructor) |
| Modificar | `-war/web/vista/cets/detalleSolicitud.xhtml` (`f:metadata`, EL en lugar de datos fijos) |

**Facade nueva (`InteresFacade`)**

```java
public int contarPorSolicitud(SolicitudReposicion s) {
    // JPQL: SELECT COUNT(i) FROM Interes i WHERE i.solicitudReposicion = :s   → ((Long) …).intValue()
}
```

**Navegación**

- Entrada: `solicitudesActivas → detalleSolicitud` (clic "Ver solicitud").
- Salida: `detalleSolicitud → solicitudesActivas` (clic "Volver a solicitudes activas").

**Terminado cuando**

- `detalleSolicitud.xhtml?id=901` muestra folio `CETS-OAX-2026-0901`, el hospital y O−.
- Después de `INSERT INTO Interes VALUES (903, 901, NOW());` muestra **1 interesado**.
- En el HTML (Ctrl+U) no aparece ninguna latitud ni longitud de donador.
- Un `?id=` inexistente muestra un mensaje en lugar de una excepción.
- SQL de verificación: `SELECT COUNT(*) FROM Interes WHERE SolicitudReposicionid = 901;`
- (2026-11-24, #17) Con ese interés, la tabla "Donador / Tipo de sangre" muestra solo el nombre y `O−` del 903; sin intereses no hay tabla.

**Checklist**

- [x] `contarPorSolicitud` en `InteresFacade`
- [x] `obtenerDetalle` y `obtenerCantidadInteresados` en `LnPanelCETS`
- [x] `f:viewParam` y `f:viewAction` en la vista (requiere T0.1)
- [x] Vista sin datos fijos y sin ubicación (se retiró la tabla de donadores de ejemplo y el historial de cambios de
      ejemplo; el bloque "Actualizar estatus" lo activa la HU-6.4)
- [x] Pruebas HTTP: id válido, 0 y 1 interesados, id inexistente, sin id, id no numérico, sin sesión y rol equivocado

---

#### HU-6.4 Actualizar estatus de una solicitud

**Objetivo:** el CETS cambia el estatus de una solicitud y queda bitácora de quién lo cambió y cuándo.

**Regla de transición (la única que definen los diagramas).** La nota del atributo `SolicitudReposicion.estatus` en el
diagrama de clases dice: *"Activa al crearse; el personal del CETS la pasa a Atendida, Cerrada o Cancelada"*. Los
valores válidos son los 4 del CHECK de la BD. Por eso el Ln acepta el cambio solo si:

1. el nuevo estatus es **Atendida, Cerrada o Cancelada** (nunca "Activa": solo es el estado al crearse), y
2. es **distinto del estatus actual**.

Los diagramas **no** definen transiciones entre los otros estatus (¿Atendida → Cerrada?, ¿reabrir una Cerrada?). No se
inventó ninguna restricción más: ver [Inconsistencias](#10-inconsistencias-a-revisar) #18.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIDetalleSolicitud.cambiarEstatus(int solicitudId, String nuevoEstatus)` | Combo "Nuevo estatus" → `#{panelCetsController.solicitud.estatus}` + botón "Guardar cambio de estatus" |
| 1.1 | `UIDetalleSolicitud → ControlPanelCETS.actualizarEstatus(int solicitudId, String nuevoEstatus): boolean` | `panelCetsController.actualizarEstatus()` → `lnPanelCETS.actualizarEstatus(solicitud)` |
| 1.1.1 | `ControlPanelCETS → ControlPanelCETS.validarEstatus(String nuevoEstatus): boolean` → `return valido` | `validarEstatus(actual, nuevo)` privado en el Ln (la regla de arriba) |
| 1.1.2 | `alt[valido == true]` `DBMS: consultarSolicitudPorId(solicitudId)` → `return solicitud` | `solicitudF.find(s.getId())` |
| 1.1.3 | `SolicitudReposicion.actualizarEstatus(String nuevoEstatus)` | En el Ln: `solicitud.setEstatus(...)` y `solicitud.setPersonalCETSUsuarioid(personal)` |
| 1.1.4 | `DBMS: actualizarSolicitud(solicitud)` → `return true` | `solicitudF.edit(solicitud)` |
| 1.1.5 | `«create» HistorialEstatusSolicitud(solicitudId, personalId, estatusAnterior, estatusNuevo)` | En el Ln: `new HistorialEstatusSolicitud()` con solicitud, personal, estatus anterior, nuevo y `fechaCambio` |
| 1.1.6 | `DBMS: guardarHistorialEstatus(historial)` → `return true` | `historialEstatusF.create(historial)` (misma transacción que 1.1.4) |
| — | `alt[valido == false]` `return false` | `p:messages` con el error; la vista vuelve a leer el estatus real de la BD |

**Firma (hecha).** La entidad completa es la `SolicitudReposicion`: la vista llena `estatus`; el controller agrega el
personal CETS de la sesión; el Ln lo guarda todo en una transacción.

```java
// s.id, s.estatus (el nuevo) y s.personalCETSUsuarioid (el controller lo toma de sesion.getUsuario().getPersonalCETS())
public boolean actualizarEstatus(SolicitudReposicion s)
```

**Archivos (hechos)**

| Acción | Archivo |
|---|---|
| Modificado | `-ejb/src/java/ln/LnPanelCETS.java`: `actualizarEstatus(SolicitudReposicion)`, `validarEstatus` privado y `@EJB HistorialEstatusSolicitudFacade` |
| Modificado | `-war/src/java/controller/PanelCetsController.java`: acción `actualizarEstatus()` y `info(...)` |
| Modificado | `-war/web/vista/cets/detalleSolicitud.xhtml`: bloque "Actualizar estatus" activo (`p:commandButton`, `ajax="false"`) |

**Facade:** no necesita métodos nuevos (`find`, `edit`, `create`).

**Navegación:** se queda en `detalleSolicitud?id=…` con mensaje de éxito o error. Ni el *Diagrama de navegación* ni
*AD - Panel CETS* (`DetalleSolicitud → ActualizarEstatus [cambiarEstatus == true] → MergeNode`) definen otra pantalla.

**Terminado cuando**

- Activa → Atendida muestra "El estatus de la solicitud cambió a Atendida", saca la solicitud del listado de activas
  (HU-6.2) y crea un renglón de bitácora.
- Atendida → Cerrada también funciona (queda otro renglón).
- El detalle muestra el bloque "Historial de cambios" con cada renglón de la bitácora (estatus anterior → nuevo, quién y cuándo).
- Elegir el mismo estatus, o volver a "Activa", muestra un error, no cambia nada y no inserta bitácora.
- Una solicitud inexistente no muestra el formulario de estatus.
- SQL de verificación:
  ```sql
  SELECT estatus, PersonalCETSUsuarioid FROM SolicitudReposicion WHERE id = 901;          -- Atendida, 901
  SELECT estatusAnterior, estatusNuevo, PersonalCETSUsuarioid, fechaCambio
    FROM HistorialEstatusSolicitud WHERE SolicitudReposicionid = 901 ORDER BY id DESC;    -- Activa → Atendida
  ```
  Para volver a probar, restaura la solicitud:
  ```sql
  UPDATE SolicitudReposicion SET estatus = 'Activa', PersonalCETSUsuarioid = NULL WHERE id = 901;
  DELETE FROM HistorialEstatusSolicitud WHERE SolicitudReposicionid = 901;
  ```

**Checklist**

- [x] `actualizarEstatus(SolicitudReposicion)` + `validarEstatus` privado en `LnPanelCETS`
- [x] Controller con la acción (el personal viene de la sesión; la protección CETS ya existía, no se duplicó)
- [x] Vista con `p:commandButton` y `p:messages`
- [x] Compila (`clean dist`, 0 errores y 0 warnings)
- [x] Pruebas HTTP (23 comprobaciones, todas OK): transición válida (2), inválida (mismo estatus, reabrir y valor
      inventado), id inexistente, sin sesión y rol equivocado; la BD se restauró al terminar
- [x] Revisión visual en el navegador

---

#### HU-1.3 Perfil clínico del donador

**Objetivo:**

- El donador captura su grupo sanguíneo, su Rh y su **ubicación** (D1). Sin perfil y sin coordenadas no le llegan
  notificaciones (HU-3.1).
- En la misma vista elige si se revela su contacto al confirmar interés (`consentimientoContacto`, D8).

**Estado:** implementada en `cuentas/miPerfil` (junto con HU-3.4). Controller **`PerfilController`**
(`@ViewScoped`), no `CuentasController`: ver [Inconsistencias](#10-inconsistencias-a-revisar) #27.

**Secuencia (`.vpp`).** Todo va dentro de `opt[perfilClinico == null]`.

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → Dashboard.seleccionarPerfilClinico()` | Menú "Mi perfil" |
| 1.1 | `Dashboard → UIPerfilClinico.mostrar()` | `cuentas/miPerfil.xhtml` (`f:viewAction` → `perfilController.cargarPerfil`) |
| 2 | `Donador → UIPerfilClinico.capturar(String tipoSanguineo, String factorRh)` | Combos `grupo` y `rh` → `#{perfilController.perfil.tipoSanguineo}` y `.factorRh`; botón "Usar mi ubicación" → `perfil.latitud` y `perfil.longitud` (D1); toggle → `perfil.usuario.consentimientoContacto` (D8) |
| 2.1 | `UIPerfilClinico → ControlCuentas.guardarPerfilClinico(String tipoSanguineo, String factorRh): boolean` | `perfilController.guardarPerfil()` → `lnCuentas.guardarPerfilClinico(perfil)` |
| 2.1.1 | `ControlCuentas → Usuario.completarPerfilClinico(tipoSanguineo, factorRh)` | **En el Ln** (no en la entidad) |
| 2.1.1.1 | `Usuario → «create» PerfilClinico(tipoSanguineo, factorRh)` | El Ln crea el `PerfilClinico` (PK compartida: `usuarioid`, `usuario` y `fechaRegistroPerfil`); el radio 5 viene del constructor |
| 2.1.1.2 | `PerfilClinico.marcarElegibilidadPreliminar(): boolean` → `return elegible` | **No implementado**: no hay columna ni regla (Inconsistencias #4) |
| 2.1.2 | `ControlCuentas → DBMS: insertarPerfilClinico(usuarioId, tipoSanguineo, factorRh)` → `return usuarioId` | `perfilF.create(perfil)`, o `perfilF.edit(perfil)` si ya existía; más `usuarioF.edit(usuario)` por el consentimiento |
| — | `return true` → `UIPerfilClinico → Dashboard` | Mensaje "Tu perfil se guardó" (`p:messages`) |

**Firmas (hechas, en `LnCuentas`)**

```java
public boolean guardarPerfilClinico(PerfilClinico p)        // p.usuario = Usuario de la sesión, con consentimientoContacto
public PerfilClinico obtenerPerfilClinico(Usuario u)         // el perfil guardado, o uno nuevo ligado al usuario (no está en el diagrama)
public List<String> receptoresCompatibles(PerfilClinico p)   // "puedes donar a..." (no está en el diagrama)
```

**Reglas que valida el Ln.** Todo se valida **antes** de modificar algo; si falla, no se guarda nada:

1. `tipoSanguineo` ∈ {A, B, AB, O} y `factorRh` ∈ {+, −} (el CHECK de la BD). Vacío o nulo es inválido.
2. Ubicación: latitud y longitud van **juntas** (o ninguna), con |latitud| ≤ 90 y |longitud| ≤ 180.
   Se redondean a 6 decimales (`DECIMAL(9,6)`).
3. Si se envían sin coordenadas (nulas o vacías) **no se borra** la ubicación ya guardada.
4. Si el perfil ya existe se actualiza (grupo, Rh y ubicación) **sin tocar** el radio ni `fechaRegistroPerfil`.
   El radio se cambia solo con `configurarRadio` (HU-3.4).
5. `Usuario.consentimientoContacto` se guarda en cada "Guardar cambios" (con el perfil nuevo o ya existente).

**Archivos (hechos)**

| Acción | Archivo |
|---|---|
| Modificado | `-ejb/src/java/ln/LnCuentas.java` (`@EJB PerfilClinicoFacade`) |
| Creado | `-war/src/java/controller/PerfilController.java` (`@Named @ViewScoped`; carga con `f:viewAction`, sin `@PostConstruct`) |
| Modificado | `-war/web/vista/cuentas/miPerfil.xhtml` |

**Facade:** sin métodos nuevos (`find`, `create`, `edit`).

**Vista miPerfil (HU-1.3)**

- **Datos de contacto:** nombre, correo y teléfono salen del `Usuario` real y son de **solo lectura** (ninguna HU los
  edita; el servidor ignora un intento de modificarlos). Se quitaron "Rosalba Méndez", "Verificado" y "Registro activo".
- **Región (D1):** el combo se eliminó. No existía columna.
- **Ubicación:** botón **"Usar mi ubicación"** con la Geolocation API (JS inline, sin librerías externas), que llena dos
  `h:inputHidden` ligados a `perfil.latitud` y `perfil.longitud`. Los guarda "Guardar cambios". Maneja permiso denegado,
  posición no disponible, tiempo agotado y navegador sin soporte con un mensaje en la misma vista. Nota visible: solo
  funciona en `localhost` o con HTTPS. Las coordenadas **no se muestran** como texto.
- **Grupo y Rh:** combos con opción "Selecciona". El badge ("O+ DECLARADO") y el texto "puedes donar a..." se calculan con
  el perfil guardado (`receptoresCompatibles` en el Ln); sin perfil muestran "?" y una indicación.
- **Privacidad (D8, decisión D13):**
  - El toggle **"Permitir que se revele mi contacto al confirmar interés"** está ligado a
    `perfil.usuario.consentimientoContacto` y **apagado por defecto** (opt-in; manda el `DEFAULT FALSE` de la BD).
  - "Compartir mi contacto solo cuando yo confirme interés" y "Recibir avisos de solicitudes compatibles" no tienen columna:
    quedan **deshabilitados** con una nota (D14).
- Tras cada guardado (con éxito o con error) la vista vuelve a leer de la BD, así siempre muestra lo realmente guardado.
  Un rechazo descarta lo que el usuario había elegido.
- La protección de sesión es la de `plantillaUsuario`; `PerfilController` la repite antes de consultar la BD (como en
  `PanelCetsController`).

**Navegación**

- Entrada: menú de usuario → "Mi perfil" (`Usuario → miPerfil`).
- Salida: se queda en miPerfil.
- Activity *AD - Acceso al sistema*: `IniciarSesion (HU-1.2) → PerfilClinico (HU-1.3)`.

**Terminado cuando**

- Un usuario nuevo ve "Perfil clínico pendiente", sin ubicación y consentimiento apagado.
- Elegir O−, pulsar "Usar mi ubicación" (aceptar el permiso en `localhost`), activar el consentimiento y guardar: al
  recargar conserva todo, y la BD tiene el perfil, las coordenadas y `consentimientoContacto = 1`.
- Guardar de nuevo con A+ actualiza el mismo registro (no lo duplica) y conserva la fecha de registro y el radio.
- Grupo o Rh vacíos, o una ubicación inválida, muestran un error y no guardan nada.
- SQL de verificación:
  ```sql
  SELECT p.Usuarioid, p.tipoSanguineo, p.factorRh, p.radioNotificacion, p.latitud, p.longitud, p.fechaRegistroPerfil,
         u.consentimientoContacto
    FROM PerfilClinico p JOIN Usuario u ON u.id = p.Usuarioid
   WHERE u.correo = '<tu correo>';
  ```

**Checklist**

- [x] `guardarPerfilClinico`, `obtenerPerfilClinico` y `receptoresCompatibles` en `LnCuentas`
- [x] `PerfilController` (`perfil` inicializado en el constructor; carga con `f:viewAction`)
- [x] Vista sin datos fijos; combo "Región" reemplazado por "Usar mi ubicación" + `h:inputHidden` (D1)
- [x] Toggle `consentimientoContacto` apagado por defecto (D8, D13); toggles sin columna deshabilitados (D14)
- [x] `sesion.getUsuario().setPerfilClinico(...)` después de guardar (otras vistas leen el perfil de la sesión)
- [x] Compila (`clean dist` en una copia del proyecto: `BUILD SUCCESSFUL`, 0 warnings)
- [x] Pruebas HTTP (41 comprobaciones, todas OK; el script crea y borra su propio usuario): perfil nuevo y actualizado, tipo o
      Rh vacíos, ubicación inválida o incompleta, redondeo a 6 decimales, consentimiento, datos de solo lectura y sin sesión
      o con rol equivocado
- [ ] Revisión visual en el navegador, incluido el botón "Usar mi ubicación" con permiso aceptado y denegado

---

#### HU-3.4 Configurar radio de notificación

**Objetivo:** el donador elige el radio (3, 5 o 10 km) en que quiere recibir avisos.

**Estado:** implementada en la misma vista `cuentas/miPerfil` (D7: `UIAjustes` = miPerfil), como una tarjeta con su propio
formulario `formRadio` (no se pueden anidar formularios).

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → Dashboard.seleccionarAjustes()` | Menú "Mi perfil" |
| 1.1 | `Dashboard → UIAjustes.mostrar()` | `cuentas/miPerfil.xhtml`, tarjeta "Radio de notificación" |
| 2 | `UIAjustes → ControlCuentas.obtenerRadio(): int` | `lnCuentas.obtenerRadio(usuario)` al cargar; la vista muestra "Radio guardado: N km" |
| 2.1 | `ControlCuentas → DBMS: consultarRadio(usuarioId)` → `return radioNotificacion` | `perfilF.find(u.getId())`; si no hay perfil regresa 5 (el default de la entidad) |
| 3 | `Donador → UIAjustes.seleccionarRadio(int radioNotificacion)` | `p:selectOneButton` 3 / 5 / 10 → `#{perfilController.perfil.radioNotificacion}` |
| 3.1 | `UIAjustes → ControlCuentas.configurarRadio(int radioNotificacion): boolean` | `perfilController.guardarRadio()` → `lnCuentas.configurarRadio(perfil)` |
| 3.1.1 | `ControlCuentas → ControlCuentas.validarRadio(int radioNotificacion): boolean` → `return valido` | `validarRadio` privado en el Ln: radio ∈ {3, 5, 10} (CHECK de la BD) |
| 3.1.2 | `alt[valido == true]` `DBMS: actualizarRadio(usuarioId, radioNotificacion)` → `return true` | `perfilF.edit(perfil)` |
| — | `alt[valido == false]` `return false` | Mensaje de error |

**Firmas (hechas, en `LnCuentas`)**

```java
public int obtenerRadio(Usuario u)                 // 5 si no hay perfil
public boolean configurarRadio(PerfilClinico p)    // false si el radio no es 3, 5 o 10, o si el usuario no tiene perfil
```

**Mensajes del controller:** si `configurarRadio` regresa `false` y el perfil aún no existe → "Primero completa tu perfil
clínico"; si existe → "El radio debe ser de 3, 5 o 10 km". Si va bien: "Tu radio de notificación ahora es de N km".
Cambiar el radio **no** modifica grupo, Rh ni ubicación.

**Archivos (hechos):** los mismos de HU-1.3 (`LnCuentas`, `PerfilController`, `miPerfil.xhtml`).

**Facade:** sin métodos nuevos.

**Navegación**

- Usuario → miPerfil.
- Activity *AD - Notificaciones*: `ConfigurarRadio (HU-3.4)`.
- **[Alex]:** el selector de radio de `notificaciones.xhtml` sigue estático. Se reemplaza por el radio actual
  (`#{sesionController.usuario.perfilClinico.radioNotificacion}`, que `PerfilController` mantiene actualizado en la sesión)
  y un enlace a miPerfil (D7).

**Terminado cuando**

- Elegir 10 km y guardar, luego recargar: queda seleccionado 10 y "Radio guardado: 10 km".
- Un usuario **sin** perfil clínico ve "Primero completa tu perfil clínico" y no se crea nada.
- Guardar el perfil clínico después no cambia el radio guardado.
- SQL de verificación: `SELECT radioNotificacion FROM PerfilClinico WHERE Usuarioid = <id>;`

**Checklist**

- [x] `obtenerRadio(Usuario)`, `configurarRadio(PerfilClinico)` y `validarRadio` privado
- [x] Selector 3 / 5 / 10 en miPerfil, ligado a la entidad
- [x] Mensaje "Primero completa tu perfil clínico"
- [x] Compila (`BUILD SUCCESSFUL`, 0 warnings)
- [x] Pruebas HTTP (parte de las 41 de HU-1.3): radio 3, 5 y 10, radio sin perfil, radio inválido y que no altere el resto del perfil.
      El radio inválido (7) lo rechaza la validación de JSF (no está en la lista del selector); la rama `validarRadio` falsa del
      Ln no se puede forzar por HTTP
- [x] Avisar a [Alex] cuando esté en `main` (reemplaza su selector, D7)
- [ ] Revisión visual en el navegador (el selector `p:selectOneButton` y los mensajes)

---

#### T-final — Actualizar el diagrama `.vpp` (decisiones D6 y D12)

Se hace al final, cuando el código de todos esté en `main`, para que el diagrama refleje lo implementado.

- [x] **Firmas con entidad completa** en todos los Controls del diagrama de clases y en los mensajes de las secuencias.
      Ejemplos: `ControlCuentas.registrarUsuario(Usuario u)`, `ControlSolicitud.registrarSolicitud(SolicitudReposicion s)`,
      `ControlPanelCETS.actualizarEstatus(HistorialEstatusSolicitud h)`. Usa las firmas de este documento.
- [x] **`iniciarSesion` devuelve `Usuario`**, no `boolean`. `iniciarSesionInstitucional` devuelve `PersonalCETS`.
      Ajustar los `return` y las guardas `alt[autenticado …]` de HU-1.2 y HU-6.1.
- [x] **Agregar la clase `Interes`** (con `fechaInteres` y la PK `Usuario`+`SolicitudReposicion`) al diagrama de
      clases, con sus asociaciones a `Usuario` y `SolicitudReposicion`.
- [x] **Agregar `SesionController`** (sesión: `usuario`, `rol`, `iniciar`, `cerrarSesion`, `verificar`, `verificarCETS`).
- [x] **Mover a los Controls** los métodos de negocio que hoy están en entidades: `Usuario.autenticarse`,
      `SolicitudReposicion.validarFormatoFolio`, `PerfilClinico.validarCompatibilidad`,
      `RegistroDonacion.calcularProximaFecha`, etc. En las secuencias, el mensaje sale del Control hacia sí mismo.
- [x] **Quitar los mensajes que ya no son métodos** (Ln delgado): `validarDatos` y `guardarUsuario` (HU-1.1),
      `verificarFolioUnico` (HU-2.1/2.4).
- [x] **D7:** en las secuencias, `UIAjustes` pasa a `miPerfil` (HU-3.4) y a `criteriosExclusion` (HU-4.3).
      `UIBuscarDonador`/`UIFichaDonador` pasan a `donadores` (HU-5.4, HU-7.3).
- [x] **D10:** en HU-5.3 y HU-5.4, `verificarExclusion` lee `Usuario.exclusionPermanente`; ya no se recorren los
      padecimientos.
- [ ] Revisar con el profe y subir el `.vpp` actualizado.

---

### 5.2 [Galilea] — Solicitudes + donación

Rama: `feature/solicitudes`

**Crear:**

- `-war/src/java/controller/SolicitudController.java` (`@Named @RequestScoped`, o `@ViewScoped` si hace falta)
- `-war/src/java/controller/DonacionController.java`

**Implementar:**

- `-ejb/src/java/ln/LnSolicitud.java`
- La parte de *registro y búsqueda* de `-ejb/src/java/ln/LnDonacion.java`.
  [Diego] escribe los métodos de consulta en el mismo archivo; pónganse de acuerdo para no editar las mismas líneas.

#### HU-2.4 Validación de formato de folio (hazla primero; HU-2.1 la usa)

**Objetivo:** el folio CETS tiene formato válido y no existe otra solicitud **Activa** con el mismo folio.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Familiar → CapturarSolicitud.ingresarFolio(String folioCETS)` | `p:inputText id="folio" value="#{solicitudController.solicitud.folioCETS}"` (opcional: `p:ajax event="blur"`) |
| 1.1 | `CapturarSolicitud → ControlSolicitud.validarFolio(String folioCETS): boolean` | `lnSolicitud.validarFolio(solicitud)` |
| 1.1.1 | `ControlSolicitud → SolicitudReposicion.validarFormatoFolio(String folioCETS): boolean` → `return formatoValido` | **En el Ln:** `s.getFolioCETS().matches(FORMATO_FOLIO)` |
| 1.1.2 | `ControlSolicitud → ControlSolicitud.verificarFolioUnico(String folioCETS): boolean` → `return unico` | Inline: `!solicitudF.existeFolioActivo(folio)` (Ln delgado, igual que `validarDatos` en `LnCuentas`) |
| 1.1.2.1 | `ControlSolicitud → DBMS: existeFolioActivo(folioCETS)` → `return existe` | `solicitudF.existeFolioActivo(...)` |
| — | `alt[formatoValido == false]` → false; `alt[unico == false]` → false; `alt[unico == true]` → true | |

**Firma corregida (LnSolicitud)**

```java
public boolean validarFolio(SolicitudReposicion s)
```

**Facade nueva (`SolicitudReposicionFacade`):** `existeFolioActivo(String folio)`, con el JPQL de la sección 4.

**Formato (decisión D4):** `^CETS-OAX-\d{4}-\d{4}$`. Es una **decisión del equipo (regla de negocio)**; deja la
regex en una sola constante del Ln para cambiarla fácil si el CETS pide otro formato.

**Terminado cuando**

- `ABC-123` → error de formato.
- `CETS-OAX-2026-0901` (ya activa por los `INSERT` de prueba) → error "ya existe una solicitud activa".
- `CETS-OAX-2026-0903` (existe pero está *Atendida*) → **válido**.
- SQL de verificación: `SELECT folioCETS, estatus FROM SolicitudReposicion WHERE folioCETS = 'CETS-OAX-2026-0901';`

**Checklist**

- [x] `existeFolioActivo` en `SolicitudReposicionFacade`
- [x] `validarFolio(SolicitudReposicion)` con la regex en una constante del Ln
- [x] Mensajes distintos para formato y duplicado
- [x] Pruebas + SQL

---

#### HU-2.1 Capturar solicitud con folio

**Objetivo:** el familiar registra una solicitud de reposición: folio, tipo, Rh, hospital y descripción.

**Secuencia (`.vpp`).** De 2.1.1 en adelante todo va en una región `critical`.

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Familiar → Dashboard.seleccionarNuevaSolicitud()` | Menú "Capturar solicitud" |
| 1.1 | `Dashboard → CapturarSolicitud.mostrar()` | `solicitudes/capturarSolicitud.xhtml` |
| 2 | `Familiar → CapturarSolicitud.llenarSolicitud(folioCETS, tipoSanguineo, factorRh, hospitalId, descripcion)` | Formulario → `#{solicitudController.solicitud.*}`; hospital → `solicitud.hospitalid.id` |
| 2.1 | `CapturarSolicitud → ControlSolicitud.registrarSolicitud(String folioCETS, String tipoSanguineo, String factorRh, int hospitalId, String descripcion): boolean` | `lnSolicitud.registrarSolicitud(solicitud)` |
| 2.1.1 | `ControlSolicitud.verificarFolioUnico(folioCETS)` → `return unico` | `validarFolio(s)` (HU-2.4) |
| 2.1.1.1 | `DBMS: existeFolioActivo(folioCETS)` → `return existe` | `solicitudF.existeFolioActivo` |
| 2.1.2 | `alt[unico == true]` `ControlSolicitud → Usuario.capturarSolicitud(...)` → `return solicitud` | **En el Ln.** El usuario creador lo pone el controller: `s.setUsuarioid(sesion.getUsuario())` |
| 2.1.2.1 | `Usuario → «create» SolicitudReposicion(folioCETS, tipoSanguineo, factorRh, hospitalId, descripcion) {estatus = Activa}` | La entidad ya viene del controller; `estatus="Activa"` ya está en el constructor |
| 2.1.2.1.1 | `SolicitudReposicion.generarUuidPublico(): String` → `return uuidPublico` | **En el Ln:** `s.setUuidPublico(UUID.randomUUID().toString())` |
| 2.1.3 | `ControlSolicitud → DBMS: guardarSolicitud(solicitud)` → `return solicitudId` | `s.setHospitalid(hospitalF.find(...))`, `s.setFechaHoraRegistro(new Date())`, `solicitudF.create(s)` |
| — | `return true` / `alt[unico == false]` `return false` | |
| (HU-3.1) | `ControlSolicitud → ControlNotificacion.buscarDonadoresCercanos(int solicitudId)` | Al final de `registrarSolicitud`: `lnNotificacion.buscarDonadoresCercanos(s)` (cuando [Alex] lo tenga) |

**Firmas corregidas (LnSolicitud)**

```java
public boolean registrarSolicitud(SolicitudReposicion s)
public List<Hospital> listarHospitales()     // NO está en el diagrama; lo necesita el combo (1 línea: hospitalF.findAll())
```

**Archivos**

| Acción | Archivo |
|---|---|
| Implementar | `-ejb/src/java/ln/LnSolicitud.java` (`@EJB SolicitudReposicionFacade`, `HospitalFacade`; luego `LnNotificacion`) |
| Crear | `-war/src/java/controller/SolicitudController.java` (constructor: `solicitud = new SolicitudReposicion(); solicitud.setHospitalid(new Hospital());`) |
| Modificar | `-war/web/vista/solicitudes/capturarSolicitud.xhtml` |

**Vista:**

- Quita `value="CETS-OAX-2024-0582"` del folio.
- El combo de hospitales usa `f:selectItems value="#{solicitudController.hospitales}" var="h" itemLabel="#{h.nombre}" itemValue="#{h.id}"`
  en lugar de los 5 `f:selectItem` fijos.
- El botón "Publicar solicitud" pasa a `p:commandButton action="#{solicitudController.registrarSolicitud}"`.

**Navegación**

- Entrada: menú de usuario → "Capturar solicitud"; desde misSolicitudes → "Capturar nueva solicitud".
- Salida: `capturarSolicitud → misSolicitudes` (clic "Publicar solicitud"). "Cancelar" → misSolicitudes.

**Terminado cuando**

- Con sesión de `familiar@prueba.mx`, capturar `CETS-OAX-2026-0950`, O+, Hospital Civil: aparece en misSolicitudes
  y en el panel CETS (HU-6.2).
- Repetir el mismo folio da error.
- SQL de verificación:
  `SELECT id, folioCETS, tipoSanguineo, factorRh, estatus, uuidPublico, Usuarioid, Hospitalid, fechaHoraRegistro FROM SolicitudReposicion ORDER BY id DESC LIMIT 3;`
  Esperado: `estatus = 'Activa'`, `uuidPublico` de 36 caracteres y `Usuarioid = 902`.

**Checklist**

- [x] `LnSolicitud.registrarSolicitud` + `listarHospitales` (valida en el Ln folio, grupo, Rh, y que el hospital y el usuario existan; si algo falla regresa `false`, sin excepción)
- [x] `SolicitudController` + vista
- [x] (Integración) llamar `lnNotificacion.buscarDonadoresCercanos(s)` cuando [Alex] lo suba
- [x] Pruebas + SQL
- [x] **Merge a `main` lo antes posible** (los demás dependen de solicitudes reales) (PR #4)

---

#### HU-2.2 Publicación digital con QR

**Objetivo:** cada solicitud tiene una página pública (por `uuidPublico`) y un QR que apunta a ella.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `CapturarSolicitud → ControlSolicitud.generarPublicacion(int solicitudId): String` → `return imagenQR` | `lnSolicitud.generarPublicacion(solicitud)`: regresa la URL pública |
| 1.1 | `ControlSolicitud → Usuario.obtenerUuidPublico(int solicitudId): String` | En el Ln: `solicitudF.find(id).getUuidPublico()` |
| 1.1.1 | `Usuario → SolicitudReposicion.obtenerUuidPublico(): String` → `return uuidPublico` | getter de la entidad |
| 1.2 | `ControlSolicitud → ControlSolicitud.generarQR(String uuidPublico): String` → `return imagenQR` | Ver la nota de QR abajo |
| 2 | `CapturarSolicitud → PublicacionSolicitud.mostrar(int solicitudId)` | Navega a `publico/publicacion?uuid=…` |
| (HU-1.4) | `PublicacionSolicitud → ControlSolicitud.obtenerPublicacion(String uuidPublico): SolicitudReposicion` | `f:viewParam uuid` + `f:viewAction` → `lnSolicitud.obtenerPublicacion(solicitud)` |

**Firmas corregidas (LnSolicitud)**

```java
public String generarPublicacion(SolicitudReposicion s)            // "…/faces/vista/publico/publicacion.xhtml?uuid=" + uuid
public SolicitudReposicion obtenerPublicacion(SolicitudReposicion s) // s.uuidPublico lleno; regresa la solicitud o null
```

**QR (decisión D5):**

- El QR se genera **en el navegador** con una librería JS **local**, sin CDN; por ejemplo `qrcode.min.js` de
  *qrcodejs* (licencia MIT).
  1. Descárgala una vez y súbela a `-war/web/resources/js/qrcode.min.js`.
  2. Cárgala con `<h:outputScript name="js/qrcode.min.js"/>`.
- El QR codifica **la URL pública con `uuidPublico`** que regresa `generarPublicacion`
  (`…/faces/vista/publico/publicacion.xhtml?uuid=…`), **nunca** el id de la solicitud.
- No se toca `project.properties`; no hay jar nuevo.

**Facade nueva (`SolicitudReposicionFacade`)**

```java
public SolicitudReposicion buscarPorUuid(String uuid)
// usa la NamedQuery existente "SolicitudReposicion.findByUuidPublico" (parámetro "uuidPublico"); null si no hay
```

**Vista publicacion.xhtml** (usa `plantillaPublica`; no requiere sesión):

- Quita los datos fijos "CETS-OAX-2024-1102"…
- **Nunca** muestres `solicitud.usuarioid.*`. Es el `neg` de HU-1.4, que también es tuya (D8).
- "Copiar enlace", "Descargar imagen" e "Imprimir ficha" pueden hacerse con JS del lado del cliente.

**Navegación**

- `misSolicitudes → publicacion` (clic "Ver QR", con `?uuid=#{s.uuidPublico}`).
- `publicacion → iniciarSesion` (clic "Quiero donar para esta solicitud").

**Terminado cuando**

- Abrir `/faces/vista/publico/publicacion.xhtml?uuid=<uuid de la 901>` **sin sesión** muestra la solicitud 901.
- Al escanear el QR con el celular se abre la misma URL (en la red local, usa la IP de tu máquina).
- Un uuid inventado muestra "Solicitud no encontrada".
- SQL de verificación: `SELECT uuidPublico FROM SolicitudReposicion WHERE id = 901;`

**Checklist**

- [x] `buscarPorUuid` en la facade
- [x] `generarPublicacion` y `obtenerPublicacion` en `LnSolicitud` (`generarPublicacion` regresa `null` si la solicitud no existe)
- [x] Librería QR local en `-war/web/resources/js/` + QR con la URL de `uuidPublico` (D5)
- [x] Vista sin datos fijos ni contacto del familiar
- [x] Pruebas + SQL

---

#### HU-1.4 Privacidad de contacto (decisión D8: se queda con [Galilea])

**Objetivo:** la publicación pública de una solicitud **nunca** muestra el contacto del familiar mientras no haya un
interés confirmado.

La parte de `Usuario.consentimientoContacto` (checkbox en miPerfil) la hace [DAVID] en HU-1.3.

**D19 (contacto del donador al familiar):** `LnSolicitud.obtenerSolicitudPropia` y `obtenerInteresados(SolicitudReposicion, Usuario)`,
`SolicitudController.cargarInteresados` y la vista `solicitudes/interesados?id=N` (también HU-3.3). **Pendiente del `.vpp`:**
agregar `ControlSolicitud.obtenerInteresados` / `LnSolicitud.obtenerInteresados`, la vista `InteresadosSolicitud` y reemplazar
`PublicacionSolicitud.mostrarContactoDonador(int)`.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Visitante → PublicacionSolicitud.abrirPublicacion(String uuidPublico)` | `publico/publicacion.xhtml?uuid=…` |
| 1.1 | `PublicacionSolicitud → ControlSolicitud.obtenerPublicacion(String uuidPublico): SolicitudReposicion` | `lnSolicitud.obtenerPublicacion(solicitud)` (con `solicitud.uuidPublico` lleno); es el mismo método de HU-2.2 |
| 1.1.1 | `ControlSolicitud → DBMS: consultarSolicitud(uuidPublico)` → `return solicitud` | `solicitudF.buscarPorUuid(uuid)` |
| 1.1.2 | `ControlSolicitud → SolicitudReposicion.ocultarDatosContacto()` | **No** se modifica la entidad (ver la sección 4, "Cuidados"). La vista solo muestra folio, tipo/Rh, hospital y descripción |
| neg | `ControlSolicitud → PublicacionSolicitud: enviarContactoFamiliar()` `neg[interesConfirmado == false]` | **Prohibido:** `publicacion.xhtml` nunca pinta `solicitud.usuarioid.telefono/correo/nombreCompleto` |

**Firma corregida (LnSolicitud):** la misma de HU-2.2:

```java
public SolicitudReposicion obtenerPublicacion(SolicitudReposicion s)   // s.uuidPublico lleno; regresa la solicitud o null
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnSolicitud.java` |
| Modificar | `-war/src/java/controller/SolicitudController.java` |
| Modificar | `-war/web/vista/publico/publicacion.xhtml` |

**Facade:** `buscarPorUuid` (la misma de HU-2.2).

**Navegación**

- `misSolicitudes → publicacion` (clic "Ver QR").
- `publicacion → iniciarSesion` (clic "Quiero donar para esta solicitud").
- Activity *AD - Solicitudes*: `PublicacionQR (HU-2.2) → PrivacidadContacto (HU-1.4)`.

**Terminado cuando**

- Abrir la publicación de la 901 **sin sesión** muestra los datos de la solicitud.
- El HTML (Ctrl+U) no contiene `familiar@prueba.mx`, `9510000002` ni "Familiar Prueba".
- La URL pública solo acepta `uuid`. No existe una forma de abrirla con `?id=901`.
- SQL de verificación: `SELECT u.correo, u.telefono FROM SolicitudReposicion s JOIN Usuario u ON u.id = s.Usuarioid WHERE s.id = 901;`
  Ninguno de esos valores debe aparecer en la página.

**Checklist**

- [x] `obtenerPublicacion` sin modificar la entidad
- [x] `publicacion.xhtml` sin datos de contacto del familiar (`neg`)
- [x] Pruebas + SQL

---

#### HU-2.3 Mis solicitudes

**Objetivo:** el familiar ve la lista de las solicitudes que él capturó.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Familiar → Dashboard.seleccionarMisSolicitudes()` | Menú "Mis solicitudes" |
| 1.1 | `Dashboard → MisSolicitudes.mostrar()` | `solicitudes/misSolicitudes.xhtml` |
| 2 | `MisSolicitudes → ControlSolicitud.listarMisSolicitudes(): List<SolicitudReposicion>` → `return lista` | `lnSolicitud.listarMisSolicitudes(sesion.getUsuario())` |
| 2.1 | `ControlSolicitud → DBMS: consultarSolicitudes(usuarioId)` → `return lista` | `solicitudF.buscarPorUsuario(u)` |

**Firma corregida (LnSolicitud)**

```java
public List<SolicitudReposicion> listarMisSolicitudes(Usuario u)
```

**Facade nueva (`SolicitudReposicionFacade`)**

```java
public List<SolicitudReposicion> buscarPorUsuario(Usuario u)
// JPQL: SELECT s FROM SolicitudReposicion s WHERE s.usuarioid = :u ORDER BY s.fechaHoraRegistro DESC
```

**Vista:**

- Las 4 tarjetas fijas pasan a un `ui:repeat`.
- Los links "Ver QR" y "Compartir" llevan `<f:param name="uuid" value="#{s.uuidPublico}"/>`.
- Si la lista está vacía, muestra un mensaje. En *AD - Solicitudes* es la decisión `[totalSolicitudes == 0]`.

**Navegación**

- Entrada: menú "Mis solicitudes"; `capturarSolicitud → misSolicitudes` (clic "Publicar solicitud").
- Salida: `misSolicitudes → publicacion` ("Ver QR"); "Capturar nueva solicitud" → capturarSolicitud.

**Terminado cuando**

- `familiar@prueba.mx` ve 901, 902 y 903 (más las que capture).
- `donador1@prueba.mx` ve la lista vacía con su mensaje.
- SQL de verificación: `SELECT folioCETS, estatus FROM SolicitudReposicion WHERE Usuarioid = 902 ORDER BY fechaHoraRegistro DESC;`

**Checklist**

- [x] `buscarPorUsuario` + `listarMisSolicitudes`
- [x] Vista con `ui:repeat`, estado vacío y `f:param uuid` (`misSolicitudes` rechaza sin sesión y también al rol CETS)
- [x] Pruebas + SQL

---

#### HU-5.1 Capturar una donación realizada

**Objetivo:** el personal CETS registra que un donador donó (opcionalmente ligado a una solicitud) y el sistema
calcula su próxima fecha elegible.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIRegistrarDonacion.capturarDonacion(int donadorId, Integer solicitudId)` | `cets/capturarDonacion.xhtml?donador=903`; combo de solicitud → `#{donacionController.registro.solicitudReposicionid.id}` |
| 1.1 | `UIRegistrarDonacion → ControlDonacion.registrarDonacion(int donadorId, Integer solicitudId): boolean` | `lnDonacion.registrarDonacion(registro)` |
| 1.1.1 | `ControlDonacion → PersonalCETS.registrarDonacion(donadorId, solicitudId): RegistroDonacion` → `return registro` | **En el Ln.** El personal lo pone el controller: `registro.setPersonalCETSUsuarioid(sesion.getUsuario().getPersonalCETS())` |
| 1.1.1.1 | `PersonalCETS → «create» RegistroDonacion(donadorId, solicitudId)` | La entidad viene del controller; el Ln hace `find` del donador y de la solicitud (si `id != null`) |
| 1.1.1.2 | `PersonalCETS → RegistroDonacion.calcularProximaFecha(): Date` → `return proximaFechaElegible` | **En el Ln** (privado): `fechaDonacion + 56 días` (regla del comentario del script; confirmar) |
| 1.1.2 | `ControlDonacion → DBMS: guardarRegistroDonacion(registro)` → `return registroId` | `registroF.create(r)` |
| — | `return true` | |

**Firma corregida (LnDonacion)**

```java
public boolean registrarDonacion(RegistroDonacion r)
// r.usuarioid (id), r.solicitudReposicionid (id o null), r.personalCETSUsuarioid, r.fechaDonacion
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnDonacion.java` (`@EJB RegistroDonacionFacade`, `UsuarioFacade`, `SolicitudReposicionFacade`) |
| Crear | `-war/src/java/controller/DonacionController.java` (constructor: `registro = new RegistroDonacion(); registro.setUsuarioid(new Usuario()); registro.setSolicitudReposicionid(new SolicitudReposicion());`) |
| Modificar | `-war/web/vista/cets/capturarDonacion.xhtml` |

**Vista:**

- **Donador:** quita el texto fijo "Ernesto Valdivieso…". Se recibe por `f:viewParam name="donador"` desde donadores (HU-5.4).
- **Fecha:** usa `p:datePicker` (la columna es `DATE`).
- **Combo de solicitudes:** usa `lnPanelCETS.listarSolicitudesActivas()` de [DAVID] (el controller puede inyectar `LnPanelCETS`).
  La opción "Sin vincular" deja `solicitudReposicionid` en `null`; el Ln la convierte a `null` si su `id` viene vacío.
- **"Registrar donación":** pasa a `p:commandButton`.

**Navegación**

- Entrada: menú CETS → "Capturar donación"; `donadores → capturarDonacion` ("Registrar donación").
- Salida: `capturarDonacion → donadores` (clic "Registrar donación").

**Terminado cuando**

- Con sesión CETS, registrar una donación de 903 ligada a la 901 con fecha de hoy: aparece en el historial
  de 903 (HU-5.2) y en su ficha (HU-5.4).
- SQL de verificación:
  `SELECT fechaDonacion, proximaFechaElegible, Usuarioid, PersonalCETSUsuarioid, SolicitudReposicionid FROM RegistroDonacion ORDER BY id DESC LIMIT 1;`
  Esperado: `proximaFechaElegible = fechaDonacion + 56` y `PersonalCETSUsuarioid = 901`.

**Checklist**

- [x] `registrarDonacion(RegistroDonacion)` + `calcularProximaFecha` privado (sin excepción con nulos; exige personal CETS activo y, si hay solicitud, que exista y esté Activa)
- [x] `DonacionController` + vista (las acciones y los `f:viewAction` verifican la sesión CETS antes de consultar)
- [x] Pruebas + SQL

---

#### HU-5.4 Consultar historial de un donador específico

**Objetivo:** el CETS busca un donador y ve su ficha: historial de donaciones, próxima fecha elegible y exclusión.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIBuscarDonador.buscar(String criterio)` | `cets/donadores.xhtml`, campo `termino` → `#{donacionController.criterio.nombreCompleto}` |
| 1.1 | `UIBuscarDonador → ControlDonacion.buscarDonador(String criterio): List<Usuario>` → `return donadores` | `lnDonacion.buscarDonador(criterio)` |
| 1.1.1 | `ControlDonacion → DBMS: buscarDonadores(criterio)` → `return donadores` | `usuarioF.buscarDonadores(texto)` |
| 2 | `PersonalInstitucional → UIBuscarDonador.seleccionarDonador(int donadorId)` | Clic en un renglón → `donadores.xhtml?id=903` |
| 2.1 | `UIBuscarDonador → UIFichaDonador.mostrar(int donadorId)` | Panel "ficha" dentro de `donadores.xhtml` (D7: `UIBuscarDonador`/`UIFichaDonador` = `cets/donadores`) |
| 3 / 3.1 | `ControlDonacion.obtenerHistorial(int donadorId): List<RegistroDonacion>` / `consultarDonaciones` | `lnDonacion.obtenerHistorial(donador)` (**lo escribe [Diego]**) |
| 4 / 4.1–4.4 | `ControlDonacion.obtenerElegibilidad(int donadorId): RegistroDonacion` (`consultarUltimaDonacion`, `consultarPadecimientos`, `Usuario.verificarExclusionPermanente` (D10: se lee `Usuario.exclusionPermanente`) + `loop[for (HistorialClinico h : padecimientos)]` `HistorialClinico.determinarExclusion` → `CriterioExclusion.obtenerTipoExclusion`, `RegistroDonacion.calcularProximaFecha`) | `lnDonacion.obtenerElegibilidad(donador)` + `lnDonacion.verificarExclusion(donador)` (**los escribe [Diego]**) |

**Firma corregida (LnDonacion, parte de [Galilea])**

```java
public List<Usuario> buscarDonador(Usuario criterio)   // criterio.nombreCompleto = texto a buscar (nombre o correo)
```

**Facade nueva (`UsuarioFacade`).** Es compartida con [DAVID]; solo se agrega el método.

```java
public List<Usuario> buscarDonadores(String texto)
// JPQL: SELECT u FROM Usuario u WHERE u.perfilClinico IS NOT NULL
//        AND (LOWER(u.nombreCompleto) LIKE :t OR LOWER(u.correo) LIKE :t)      (:t = "%" + texto.toLowerCase() + "%")
```

**Vista donadores:**

- La tabla de resultados pasa a `ui:repeat`.
- El combo de grupo es opcional, filtrando por `u.perfilClinico.tipoSanguineo/factorRh`.
- Quita "DON-OAX-2024-984", porque no existe un código de donador en la BD.
- La ficha (historial + elegibilidad) se pinta solo si viene `?id=`.

**Navegación**

- Entrada: menú CETS → "Donadores".
- Salida: "Registrar donación" → `capturarDonacion?donador=#{u.id}`.
- *AD - Panel CETS*: `ConsultarDonador (HU-5.4) → ConsultarHistorialExterno (HU-7.3) → CapturarDonacion (HU-5.1) [registrarDonacion == true]`.

**Terminado cuando**

- Buscar "donador" muestra 903 y 904, pero no 901 ni 902 (sin perfil clínico).
- La ficha de 903 muestra 1 donación y "elegible". La de 904 muestra 1 donación, la próxima fecha en el futuro y la
  **exclusión permanente**.
- SQL de verificación:
  `SELECT u.id, u.nombreCompleto, p.tipoSanguineo, p.factorRh FROM Usuario u JOIN PerfilClinico p ON p.Usuarioid = u.id WHERE u.nombreCompleto LIKE '%donador%';`

**Checklist**

- [x] `buscarDonadores` en `UsuarioFacade`
- [x] `buscarDonador(Usuario)` en `LnDonacion`
- [x] Ficha usando los métodos de [Diego]
- [x] Pruebas + SQL

---

#### HU-7.3 Consultar historial recibido de la institución externa

**Objetivo:** en la ficha del donador, el CETS ve las donaciones que reportó una institución externa, o un aviso si no
hay ninguna.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIFichaDonador.verHistorialExterno(int donadorId)` | Sección o pestaña "Historial externo" en la ficha de `donadores.xhtml?id=` |
| 1.1 | `UIFichaDonador → ControlDonacion.obtenerHistorialExterno(int donadorId): List<HistorialExterno>` → `return historial` | `lnDonacion.obtenerHistorialExterno(donador)` |
| 1.1.1 | `ControlDonacion → DBMS: consultarHistorialExterno(donadorId)` → `return historial` | `historialExternoF.buscarPorUsuario(u)` |
| 1.2 | `UIFichaDonador → ControlDonacion.contarRegistros(List<HistorialExterno> historial): int` → `return total` | En la vista: `#{empty donacionController.historialExterno}` (ver [Inconsistencias](#10-inconsistencias-a-revisar) #15) |
| 1.3 | `alt[total == 0]` `UIFichaDonador.mostrarAvisoSinInformacion()` | `<h:panelGroup rendered="#{empty …}">` |
| 1.4 / 1.5 | `alt[total > 0]` `loop[for (registro : historial)]` `HistorialExterno.obtenerDatos()` / `mostrarHistorialExterno(historial)` | `ui:repeat` mostrando `fechaDonacion`, `institucionExternaid.nombreInstitucion` y `observaciones` |

**Firma corregida (LnDonacion)**

```java
public List<HistorialExterno> obtenerHistorialExterno(Usuario donador)
```

**Facade nueva (`HistorialExternoFacade`)**

```java
public List<HistorialExterno> buscarPorUsuario(Usuario u)
// JPQL: SELECT h FROM HistorialExterno h WHERE h.usuarioid = :u ORDER BY h.fechaDonacion DESC
```

**Navegación:** dentro de la ficha (`donadores.xhtml?id=`).

**Terminado cuando**

- La ficha de 903 muestra 1 registro externo (2025-03-10, "Banco de Sangre externo (prueba)").
- La ficha de 904 muestra el aviso "Sin información de instituciones externas".
- SQL de verificación: `SELECT * FROM HistorialExterno WHERE Usuarioid = 903;`

**Checklist**

- [x] `buscarPorUsuario` en `HistorialExternoFacade`
- [x] `obtenerHistorialExterno(Usuario)`
- [x] Vista con los dos casos (`alt`)
- [x] Pruebas + SQL

---

### 5.3 [Alex Torres] — Notificaciones y cuestionario

Rama: `feature/notificaciones`

**Crear:**

- `-war/src/java/controller/NotificacionController.java`
- `-war/src/java/controller/CuestionarioController.java` (recomendado `@ViewScoped` + `Serializable`, porque el
  cuestionario hace postback sobre una lista)

**Implementar:**

- `-ejb/src/java/ln/LnNotificacion.java`
- `-ejb/src/java/ln/LnCuestionario.java`

#### HU-3.1 Recibir notificación de solicitud cercana

**Objetivo:** al registrarse una solicitud, se crea una notificación para cada donador **compatible** y **dentro de
su radio**. El donador ve sus notificaciones.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `ControlSolicitud → ControlNotificacion.buscarDonadoresCercanos(int solicitudId)` | `LnSolicitud.registrarSolicitud` ([Galilea]) llama `lnNotificacion.buscarDonadoresCercanos(s)` |
| 1.1 | `ControlNotificacion → DBMS: consultarDonadores(solicitudId)` → `return donadores` | `perfilF.buscarDonadoresActivos()` |
| 1.2 | `loop[for (Usuario d : donadores)]` `PerfilClinico.validarCompatibilidad(String tipoSanguineo, String factorRh): boolean` → `return compatible` | **En el Ln** (privado): tabla ABO/Rh donador → receptor |
| 1.3 | `opt[compatible == true]` `«create» Notificacion(donadorId, solicitudId)` | `new Notificacion()`, `setUsuarioid(p.getUsuario())`, `setSolicitudReposicionid(s)` |
| — | (HU-3.2) `calcularRadioCercania(...)` → `dentroRadio` | Ver HU-3.2 |
| 1.4 | `alt[dentroRadio == true]` `DBMS: guardarNotificacion(notificacion)` → `return notificacionId` | `n.setFechaEnvio(new Date())`, `n.setEstatus("Enviada")`, `notificacionF.create(n)` |
| 1.5 | `Notificacion.enviarAviso()` | El aviso **es** el renglón: aparece en `notificaciones.xhtml` (no hay correo ni push) |
| 1.6 | `ControlNotificacion → UINotificacion.mostrarAviso(int notificacionId)` | Lo pinta la vista notificaciones |
| 1.7 | `alt[dentroRadio == false]` `«destroy» Notificacion` | No se hace `create` |
| 2 | `Donador → UINotificacion.abrirNotificacion(int notificacionId)` | Clic en una tarjeta de notificación |
| 2.1 | `UINotificacion → ControlNotificacion.obtenerNotificacion(int notificacionId): Notificacion` | `lnNotificacion.obtenerNotificacion(n)` |
| 2.1.1 | `ControlNotificacion → DBMS: consultarNotificacion(notificacionId)` → `return notificacion` | `notificacionF.find(id)` |
| 2.2 | `UINotificacion → PublicacionSolicitud.mostrar(int solicitudId)` | D9, se sigue la navegación: `notificaciones → cuestionario` |

**Firmas corregidas (LnNotificacion)**

```java
public void buscarDonadoresCercanos(SolicitudReposicion s)
public Notificacion obtenerNotificacion(Notificacion n)          // n.id lleno
public List<Notificacion> listarMisNotificaciones(Usuario u)    // NO está en el diagrama; lo necesita la vista
```

**Facades nuevas**

```java
// PerfilClinicoFacade
public List<PerfilClinico> buscarDonadoresActivos()
// JPQL: SELECT p FROM PerfilClinico p WHERE p.tipoSanguineo IS NOT NULL AND p.factorRh IS NOT NULL
//        AND p.latitud IS NOT NULL AND p.longitud IS NOT NULL AND p.usuario.exclusionPermanente = false

// NotificacionFacade
public List<Notificacion> buscarPorUsuario(Usuario u)
// JPQL: SELECT n FROM Notificacion n WHERE n.usuarioid = :u ORDER BY n.fechaEnvio DESC
```

La BD tiene `UNIQUE(Usuarioid, SolicitudReposicionid)` en `Notificacion`: no crees dos avisos para el mismo par.

**Vista notificaciones:**

- Las 3 tarjetas fijas pasan a un `ui:repeat`. Muestra folio, tipo/Rh, hospital y la **distancia aproximada**
  (nunca coordenadas).
- El link "Ver solicitud y evaluar aptitud" lleva `?solicitud=#{n.solicitudReposicionid.id}`.
- **Selector de radio (D7):** el `p:selectOneButton id="radio"` (3/5/10 km) se cambia por el radio actual
  (`#{sesionController.usuario.perfilClinico.radioNotificacion} km`) y un enlace "Cambiar" a `/vista/cuentas/miPerfil`.
  La configuración del radio vive en miPerfil (HU-3.4, de [DAVID]). Conserva las clases CSS del bloque.
- **D1:** si el usuario no tiene `latitud/longitud` en su perfil, muestra el aviso "Comparte tu ubicación en Mi perfil
  para recibir solicitudes cercanas", con un enlace a miPerfil.

**Navegación**

- Entrada: `iniciarSesion → notificaciones` ("Entrar" `[autenticado == true && rol == "usuario"]`); menú "Notificaciones".
- Salida: `notificaciones → cuestionario` (clic "Ver solicitud y evaluar aptitud").

**Terminado cuando**

- Con los datos de prueba, crear una solicitud **O−** en el Hospital Civil (con HU-2.1 de [Galilea], o llamando
  el Ln desde un botón temporal): se crea una notificación para 903 (O−, a ~1 km, radio 5) y **no** para 904
  (A+ y lejos).
- 903 la ve en notificaciones.
- D1: con `UPDATE PerfilClinico SET latitud = NULL, longitud = NULL WHERE Usuarioid = 903;`, una nueva solicitud O−
  **no** lo notifica, y su vista de notificaciones muestra el aviso "Comparte tu ubicación".
- SQL de verificación:
  `SELECT n.Usuarioid, n.SolicitudReposicionid, n.distanciaAproximada, n.estatus FROM Notificacion n ORDER BY n.id DESC LIMIT 5;`

**Checklist**

- [x] Facades: `buscarDonadoresActivos` (sin coordenadas no se notifica, D1) y `buscarPorUsuario`
- [x] Vista: selector de radio → radio actual + enlace a miPerfil (D7); aviso si no hay ubicación (D1)
- [x] `buscarDonadoresCercanos`, `obtenerNotificacion`, `listarMisNotificaciones` + compatibilidad privada
- [x] Avisar a [Galilea] para que lo llame desde `registrarSolicitud` — conectado en `LnSolicitud.registrarSolicitud` por [David]
- [x] `NotificacionController` + vista
- [x] Pruebas + SQL

---

#### HU-3.2 Cálculo de radio de cercanía sin exponer ubicación exacta

**Objetivo:** decidir si el donador está dentro de su `radioNotificacion` respecto al hospital de la solicitud y guardar
solo una **distancia aproximada**.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `ControlNotificacion → Notificacion.calcularRadioCercania(String ubicacionDonador, String ubicacionSolicitud): boolean` | **En el Ln** (privado), con haversine entre `PerfilClinico.latitud/longitud` y `s.hospitalid.latitud/longitud` |
| — | `alt[distanciaAproximada <= radioNotificacion]` → `return true` | `n.setDistanciaAproximada(redondeo)`, regresa true |
| — | `alt[distanciaAproximada > radioNotificacion]` → `return false` | Regresa false |
| neg | `ControlNotificacion → UINotificacion: enviarUbicacionExacta()` `neg[mostrarUbicacion == true]` | **Prohibido:** ninguna vista ni controller expone `latitud/longitud` del donador |

**Firma corregida (LnNotificacion, privado)**

```java
private boolean calcularRadioCercania(Notificacion n, PerfilClinico donador, SolicitudReposicion s)
// usa BigDecimal.doubleValue(); redondea la distancia a 0.5 km antes de guardarla
```

**Archivos:** `-ejb/src/java/ln/LnNotificacion.java`. No necesita facades nuevas.

**Terminado cuando**

- 903 (17.065, −96.720) contra el Hospital Civil (17.0702, −96.7187) ≈ 0.6 km → notificado, con
  `distanciaAproximada` 0.50 o 1.00.
- 904 (Istmo, ~190 km) → no se notifica.
- En el HTML de notificaciones no aparecen coordenadas.
- SQL de verificación: `SELECT distanciaAproximada FROM Notificacion WHERE Usuarioid = 903;`

**Checklist**

- [x] `calcularRadioCercania` con haversine y redondeo
- [x] Revisar que ninguna vista pinte `perfilClinico.latitud/longitud`
- [x] Pruebas + SQL

---

#### HU-3.3 Confirmar interés en una solicitud notificada

**Objetivo:** un donador **apto** confirma que irá a donar; se registra el `Interes`.

**Secuencia (`.vpp`).** Todo va dentro de `opt[cuestionario.resultado == "Apto"]`.

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → UIConfirmacion.confirmarInteres(int solicitudId)` | `confirmarInteres.xhtml?solicitud=901`, botón "Confirmo que iré a donar" |
| 1.1 | `UIConfirmacion → ControlNotificacion.registrarInteres(int solicitudId): SolicitudReposicion` | `lnNotificacion.registrarInteres(interes)` |
| 1.1.1 | `ControlNotificacion → ControlDonacion.esElegible(int donadorId): boolean` → `return elegible` | `lnDonacion.esElegible(i.getUsuario())` (**lo escribe [Diego]**) |
| 1.1.2 | `alt[elegible == true]` `Usuario.confirmarInteres(int solicitudId)` | **En el Ln:** `i.setInteresPK(new InteresPK(uid, sid))`, `i.setFechaInteres(new Date())` |
| 1.1.3 | `DBMS: guardarInteres(solicitudId, usuarioId)` → `return true` | `interesF.create(i)` |
| 1.1.4 | `PublicacionSolicitud.mostrarContactoDonador(int solicitudId)` | Abierta: ver [Inconsistencias](#10-inconsistencias-a-revisar) #6 (HU-1.4 es de [Galilea], D8) |
| 1.2 | `alt[elegible == true]` `PublicacionSolicitud.mostrar(solicitudId)` / `alt[elegible == false]` `UIMiHistorial.mostrar()` + `return null` | D9, se sigue la navegación: true → `notificaciones`; false → mensaje "Aún no puedes donar" y se queda en la vista |

**Firma corregida (LnNotificacion)**

```java
public SolicitudReposicion registrarInteres(Interes i)   // i.usuario (sesión), i.solicitudReposicion (id); null si no es elegible
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnNotificacion.java` (`@EJB InteresFacade`, `SolicitudReposicionFacade`, `LnDonacion`) |
| Modificar | `-war/src/java/controller/NotificacionController.java` |
| Modificar | `-war/web/vista/notificaciones/confirmarInteres.xhtml` (datos de la solicitud por `f:viewParam`, `p:commandButton`) |

**Facade:** sin métodos nuevos (`create`, `find`). Si quieres evitar duplicados, agrega
`InteresFacade.existe(Usuario u, SolicitudReposicion s)`.

**Navegación**

- `resultado → confirmarInteres` ("Continuar para confirmar asistencia" `[elegible == true]`).
- `confirmarInteres → notificaciones` ("Confirmo que iré a donar").

**Terminado cuando**

- 903 confirma interés en la 901 → se crea el renglón y el CETS lo ve como 1 interesado (HU-6.3).
- 904 (no elegible) → no se crea nada y aparece el mensaje "Aún no puedes donar" (D9: la navegación no tiene
  transición a miHistorial).
- SQL de verificación: `SELECT * FROM Interes WHERE SolicitudReposicionid = 901;`

**Checklist**

- [x] `registrarInteres(Interes)` con PK compartida
- [x] Stub temporal de `esElegible` hasta que [Diego] suba el real (no subir el stub) (ya no hace falta: se usa el `esElegible` real de [Diego])
- [x] Vista + los dos casos (elegible → notificaciones; no elegible → mensaje)
- [x] Pruebas + SQL

---

#### HU-4.1 Responder cuestionario de elegibilidad

**Objetivo:** el donador contesta el cuestionario (criterios **activos** del catálogo) para una solicitud y se guarda con
sus respuestas.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PublicacionSolicitud → UICuestionario.mostrar(int solicitudId)` | `cuestionario.xhtml?solicitud=901` |
| 2 | `UICuestionario → ControlCuestionario.obtenerCriterios(): List<CriterioExclusion>` → `return criterios` | `f:viewAction` → `lnCuestionario.obtenerCriterios()` |
| 2.1 | `ControlCuestionario → DBMS: consultarCriterios()` → `return criterios` | `criterioF.buscarActivos()` |
| 3 | `Donador → UICuestionario.responder(String respuestas)` | `ui:repeat` de `RespuestaCriterio` con `p:selectOneRadio value="#{r.respuesta}"` (Si/No) |
| 3.1 | `UICuestionario → ControlCuestionario.registrarRespuestas(int solicitudId, String respuestas): int` → `return cuestionarioId` | `lnCuestionario.registrarRespuestas(cuestionario)` |
| 3.1.1 | `ControlCuestionario → Usuario.responderCuestionario(String respuestas)` → `return cuestionario` | **En el Ln** |
| 3.1.1.1 | `Usuario → «create» CuestionarioRespuesta(respuestas)` | El controller arma `CuestionarioRespuesta` con `respuestaCriterioList` (un `RespuestaCriterio` por criterio: `criterioExclusionid`, `preguntaTexto` = copia del texto y `respuesta`) |
| 3.1.2 | `ControlCuestionario → DBMS: guardarCuestionario(cuestionario, solicitudId, usuarioId)` → `return cuestionarioId` | En el Ln: `fechaRespuesta`, **evaluar (HU-4.2) antes de guardar** (`resultado` es NOT NULL) y `cuestionarioF.create(c)` (cascade a las respuestas) |
| 3.2 | `UICuestionario → UIResultado.mostrar(int cuestionarioId, int solicitudId)` | `return "/vista/notificaciones/resultado?faces-redirect=true&id=" + id + "&solicitud=" + sid` |

**Firmas corregidas (LnCuestionario)**

```java
public List<CriterioExclusion> obtenerCriterios()                   // solo activos
public int registrarRespuestas(CuestionarioRespuesta c)             // c.usuarioid (sesión), c.solicitudReposicionid, c.respuestaCriterioList
```

**Facade nueva (`CriterioExclusionFacade`)**

```java
public List<CriterioExclusion> buscarActivos()
// JPQL: SELECT c FROM CriterioExclusion c WHERE c.activo = true ORDER BY c.id
```

**Decisión D2:**

1. El **controller** arma la entidad completa `CuestionarioRespuesta` con su `List<RespuestaCriterio>`: una por
   criterio activo, con `criterioExclusionid`, `preguntaTexto` (copia del texto) y la `respuesta` elegida.
2. El **Ln** recibe esa entidad, pone `fechaRespuesta` y calcula `resultado` (`'Apto'`/`'NoApto'`, D3) y
   `tiempoEsperaDias` con los privados de HU-4.2. Eso pasa **antes** de `cuestionarioF.create(c)`, porque `resultado`
   es NOT NULL.

Para que `create(c)` también guarde las respuestas, cada `RespuestaCriterio` debe traer `setCuestionarioRespuestaid(c)`.

**Vista cuestionario:** las 5 preguntas fijas (p1…p5) pasan a un `ui:repeat`. Conserva el diseño de cada pregunta.

**Navegación**

- Entrada: `notificaciones → cuestionario` ("Ver solicitud y evaluar aptitud"); `resultado → cuestionario` ("Rehacer cuestionario").
- Salida: `cuestionario → resultado` ("Ver resultado").

**Terminado cuando**

- Se ven exactamente los 3 criterios activos de prueba; el 904 está inactivo y no aparece.
- Al enviar se crea 1 `CuestionarioRespuesta` con 3 `RespuestaCriterio`.
- SQL de verificación:
  ```sql
  SELECT id, resultado, tiempoEsperaDias, Usuarioid, SolicitudReposicionid FROM CuestionarioRespuesta ORDER BY id DESC LIMIT 1;
  SELECT CriterioExclusionid, preguntaTexto, respuesta, excluye FROM RespuestaCriterio WHERE CuestionarioRespuestaid = <id>;
  ```

**Checklist**

- [x] `buscarActivos` en la facade
- [x] `obtenerCriterios` y `registrarRespuestas`
- [x] `CuestionarioController` `@ViewScoped` + vista dinámica
- [x] Pruebas + SQL

---

#### HU-4.2 Ver resultado orientativo del cuestionario

**Objetivo:** mostrar "Apto" o "No apto" y, si no es apto, el tiempo de espera en días.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `UIResultado → ControlCuestionario.obtenerResultado(int cuestionarioId): CuestionarioRespuesta` → `return cuestionario` | `f:viewParam id` + `f:viewAction` → `lnCuestionario.obtenerResultado(c)` |
| 1.1 | `DBMS: consultarCuestionario(cuestionarioId)` | `cuestionarioF.find(id)` |
| 1.2 | `DBMS: consultarCriterios()` | `criterioF.buscarActivos()` |
| 1.3 | `CuestionarioRespuesta.evaluarRespuestas(String respuestas, List<CriterioExclusion> criterios): String` → `return resultado` | **En el Ln** (privado), llamado desde `registrarRespuestas` (HU-4.1) |
| 1.3.1 | `loop[for (CriterioExclusion c : criterios)]` `CriterioExclusion.evaluarCriterio(String respuesta): boolean` → `return excluye` | Privado: para `SiNo`, excluye si la respuesta es "Si"; pone `r.setExcluye(...)` |
| 1.4 | `opt[resultado == "No apto"]` `CuestionarioRespuesta.calcularTiempoEspera(criterios, respuestas): int` → `return tiempoEspera` | Privado: el mayor `duracionDias` de los criterios **Temporales** que excluyen |
| 1.4.1 / 1.4.2 | `evaluarCriterio` / `CriterioExclusion.obtenerDuracion(): int` → `return duracionDias` | getters |
| 1.5 | `DBMS: actualizarResultado(cuestionario)` → `return true` | Ya se guardó evaluado en HU-4.1; aquí solo se lee |
| 1.6 | `UIResultado.mostrarResultado(String resultado, int tiempoEspera)` | EL: `#{cuestionarioController.cuestionario.resultado}` y `.tiempoEsperaDias` |
| 2 | `alt[cuestionario.resultado == "Apto"]` `UIConfirmacion.mostrar(solicitudId)` / `alt["No apto"]` `Dashboard.mostrar()` | Botón "Continuar…" (`rendered` si Apto) → `confirmarInteres?solicitud=`; si No apto → notificaciones |

**Firma corregida (LnCuestionario)**

```java
public CuestionarioRespuesta obtenerResultado(CuestionarioRespuesta c)   // c.id lleno
```

**Valores (decisión D3):** en código se usan exactamente `'Apto'` y `'NoApto'`, sin espacio, como exige el CHECK de la
BD. La vista puede mostrar el texto "No apto".

**Vista resultado:**

- Quita el texto fijo.
- Muestra un bloque para Apto y otro para NoApto, con `rendered`.
- Conserva el link "Consultar requisitos completos".

**Navegación**

- `resultado → confirmarInteres` (`[elegible == true]`).
- `resultado → cuestionario` ("Rehacer cuestionario").
- `resultado → requisitos` ("Consultar requisitos completos").

**Terminado cuando**

- Responder "No" a todo → **Apto**, con el botón para continuar.
- Responder "Sí" al tatuaje → **No apto**, 365 días.
- Responder "Sí" a hepatitis → **No apto**. La exclusión es permanente; decidan qué días mostrar ([Inconsistencias](#10-inconsistencias-a-revisar) #10).
- SQL de verificación: `SELECT resultado, tiempoEsperaDias FROM CuestionarioRespuesta ORDER BY id DESC LIMIT 1;`

**Checklist**

- [x] `evaluarRespuestas`, `evaluarCriterio` y `calcularTiempoEspera` privados
- [x] `obtenerResultado`
- [x] Vista con los dos casos
- [x] Pruebas + SQL

---

#### HU-4.3 Configurar catálogo de criterios de exclusión

**Objetivo:** el personal CETS agrega, edita y activa o desactiva los criterios del cuestionario.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 / 1.1 | `EquipoDesarrollo → Dashboard.seleccionarAjustes()` → `UIAjustes.mostrar()` | Menú CETS → "Criterios de exclusión" (`cets/criteriosExclusion.xhtml`; D7: `UIAjustes` = criteriosExclusion) |
| 2 / 2.1 | `UIAjustes → ControlCuestionario.obtenerCriterios(): List<CriterioExclusion>` / `consultarCriterios()` | `lnCuestionario.obtenerCatalogo()` (**todos**, incluidos los inactivos; ver [Inconsistencias](#10-inconsistencias-a-revisar) #3) |
| 3 | `EquipoDesarrollo → UIAjustes.agregarCriterio(String criterio, String tipoExclusion, int duracionDias)` | Formulario → `#{cuestionarioController.criterio.*}` |
| 3.1 | `UIAjustes → ControlCuestionario.registrarCriterio(String criterio, String tipoExclusion, int duracionDias): boolean` | `lnCuestionario.registrarCriterio(criterio)` |
| 3.1.1 | `ControlCuestionario.validarCriterio(String tipoExclusion, int duracionDias): boolean` → `return valido` | Privado: `Temporal` ⇒ `duracionDias > 0`; `Permanente` ⇒ `duracionDias = null` |
| 3.1.2 | `alt[valido == true]` `«create» CriterioExclusion(criterio, tipoExclusion, duracionDias)` | La entidad viene del controller (`tipoRespuesta` y `activo` ya tienen default) |
| 3.1.3 | `DBMS: guardarCriterio(criterioExclusion)` → `return criterioId` | `criterioF.create(c)` |
| 4 | `EquipoDesarrollo → UIAjustes.editarCriterio(int criterioId, String criterio, String tipoExclusion, int duracionDias)` | Botón lápiz → carga el criterio en el formulario (`?id=`) |
| 4.1 | `UIAjustes → ControlCuestionario.modificarCriterio(int criterioId, …): boolean` | `lnCuestionario.modificarCriterio(criterio)` |
| 4.1.1 | `validarCriterio(...)` | mismo privado |
| 4.1.2 | `alt[valido == true]` `DBMS: actualizarCriterio(criterioId, criterio, tipoExclusion, duracionDias)` → `return true` | `criterioF.edit(c)` |
| — | `alt[valido == false]` → `return false` | Mensaje de error |

**Firmas corregidas (LnCuestionario)**

```java
public List<CriterioExclusion> obtenerCatalogo()          // NO está en el diagrama (1 línea: criterioF.findAll())
public boolean registrarCriterio(CriterioExclusion c)
public boolean modificarCriterio(CriterioExclusion c)    // también sirve para activar/desactivar (activo)
```

**Vista criteriosExclusion:**

- Las 6 filas fijas pasan a un `ui:repeat`.
- **Decisión D11:** los values deben coincidir con la BD.
  - `tipoRespuesta` ∈ {`SiNo`, `SeleccionSimple`}. **Quita "Selección múltiple"** y cambia los values
    `sino`/`simple`.
  - `tipoExclusion` ∈ {`Temporal`, `Permanente`}, con mayúscula; cambia `temporal`/`permanente`.
- El campo "referencia" no existe en la BD ([Inconsistencias](#10-inconsistencias-a-revisar) #11).
- **No se borra:** un criterio en uso se **desactiva** (comentario del script).

**Navegación**

- Menú CETS → criteriosExclusion.
- Los botones Guardar y Cancelar se quedan en la misma vista.

**Terminado cuando**

- Agregar "¿Te vacunaste en los últimos 30 días?" (Temporal, 30) → aparece en la tabla **y** en el cuestionario (HU-4.1).
- Desactivarlo → desaparece del cuestionario pero sigue en la tabla.
- Temporal con 0 días → error.
- SQL de verificación: `SELECT id, criterio, tipoRespuesta, activo, tipoExclusion, duracionDias FROM CriterioExclusion ORDER BY id DESC;`

**Checklist**

- [x] `obtenerCatalogo`, `registrarCriterio`, `modificarCriterio` y `validarCriterio` privado
- [x] Vista dinámica con values iguales a los de la BD
- [x] Pruebas + SQL

---

### 5.4 [Diego Roque] — Mi historial

Rama: `feature/historial`

**Controller:**

- Crear `-war/src/java/controller/HistorialController.java` (`@Named @RequestScoped`) → `LnDonacion`.
- Alternativa: agregar los métodos a `DonacionController` si lo acuerdas con [Galilea].

**Ln:** solo **métodos de consulta** en `-ejb/src/java/ln/LnDonacion.java`. Ponlos juntos en un bloque comentado
`// ---- Consultas (Diego) ----` para no chocar con [Galilea].

Tus métodos también los usan [Galilea] (HU-5.4) y [Alex] (HU-3.3). **Súbelos pronto.**

#### HU-5.2 Consultar mi historial de donación

**Objetivo:** el donador ve la lista de sus donaciones.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → Dashboard.seleccionarMiHistorial()` | Menú "Mi historial" |
| 1.1 | `Dashboard → UIMiHistorial.mostrar()` | `historial/miHistorial.xhtml` |
| 2 | `UIMiHistorial → ControlDonacion.obtenerHistorial(int donadorId): List<RegistroDonacion>` → `return donaciones` | `lnDonacion.obtenerHistorial(sesion.getUsuario())` |
| 2.1 | `ControlDonacion → DBMS: consultarDonaciones(donadorId)` → `return donaciones` | `registroF.buscarPorDonador(u)` |

**Firma corregida (LnDonacion)**

```java
public List<RegistroDonacion> obtenerHistorial(Usuario donador)
```

**Facade nueva (`RegistroDonacionFacade`)**

```java
public List<RegistroDonacion> buscarPorDonador(Usuario u)
// JPQL: SELECT r FROM RegistroDonacion r WHERE r.usuarioid = :u ORDER BY r.fechaDonacion DESC
```

**Vista miHistorial:**

- Las 3 filas fijas pasan a un `ui:repeat`: fecha, folio de la solicitud (`r.solicitudReposicionid.folioCETS`; puede ser
  `null` = "Donación voluntaria") y hospital.
- Los botones "Descargar" no tienen HU; déjalos sin acción ([Inconsistencias](#10-inconsistencias-a-revisar) #19).
- Si no hay donaciones, muestra un mensaje.

**Navegación**

- Entrada: menú de usuario → "Mi historial"; desde confirmarInteres cuando `elegible == false` (HU-3.3).
- *AD - Mi historial*: `HistorialDonacion (HU-5.2)` / `ProximaFecha (HU-5.3)`.

**Terminado cuando**

- `donador1@prueba.mx` ve 1 donación (hace 200 días, sin solicitud).
- `donador2@prueba.mx` ve 1 donación ligada a `CETS-OAX-2026-0903`.
- `familiar@prueba.mx` ve el mensaje de vacío.
- SQL de verificación: `SELECT fechaDonacion, SolicitudReposicionid FROM RegistroDonacion WHERE Usuarioid = 903 ORDER BY fechaDonacion DESC;`

**Checklist**

- [x] `buscarPorDonador` en `RegistroDonacionFacade`
- [x] `obtenerHistorial(Usuario)` en `LnDonacion`
- [x] Controller + vista con `ui:repeat` y estado vacío
- [x] Pruebas + SQL

---

#### HU-5.3 Ver mi próxima fecha de elegibilidad

**Objetivo:** el donador ve cuándo puede volver a donar. Si tiene una exclusión permanente o aún no llega la fecha, ve el
estado restrictivo.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `UIMiHistorial → ControlDonacion.obtenerElegibilidad(int donadorId): RegistroDonacion` → `return registro` | `lnDonacion.obtenerElegibilidad(usuario)` |
| 1.1 | `ControlDonacion → DBMS: consultarUltimaDonacion(donadorId)` → `return registro` | `registroF.buscarUltimaPorDonador(u)` |
| 1.2 | `ControlDonacion → RegistroDonacion.calcularProximaFecha(): Date` → `return proximaFechaElegible` | **En el Ln:** si `proximaFechaElegible` es null, `fechaDonacion + 56 días` (no lo guardes; solo lo calculas) |
| 1.3 | `UIMiHistorial.mostrarElegibilidad(Date proximaFechaElegible)` | EL con `f:convertDateTime pattern="dd/MM/yyyy"` |
| 2 | `UIMiHistorial → ControlDonacion.verificarExclusion(int donadorId): boolean` → `return exclusionPermanente` | `lnDonacion.verificarExclusion(usuario)` |
| 2.1 | `ControlDonacion → DBMS: consultarPadecimientos(donadorId)` → `return padecimientos` | **D10:** no se consultan padecimientos. Se lee `usuarioF.find(u.getId()).getExclusionPermanente()`, porque el objeto de sesión puede estar desactualizado |
| 2.2 | `ControlDonacion → Usuario.verificarExclusionPermanente(List<HistorialClinico> padecimientos): boolean` | **D10:** se reemplaza por el campo `Usuario.exclusionPermanente` (fuente única) |
| 2.2.1 / 2.2.1.1 | `loop[for (HistorialClinico h : padecimientos)]` `HistorialClinico.determinarExclusion(): String` → `CriterioExclusion.obtenerTipoExclusion(): String` | **D10:** ya no se recorre. El campo se marca `true` **al registrar** un `HistorialClinico` con criterio `Permanente` (ver [Inconsistencias](#10-inconsistencias-a-revisar) #13); el diagrama lo actualiza [DAVID] en la T-final |
| 3 | `alt[exclusionPermanente == true]` `UIMiHistorial.mostrarEstadoRestrictivo()` | Bloque `rendered` |
| 3 | `alt[registro.proximaFechaElegible > fechaActual]` `UIMiHistorial.mostrarEstadoRestrictivo()` | Bloque `rendered` |

**Firmas corregidas (LnDonacion)**

```java
public RegistroDonacion obtenerElegibilidad(Usuario donador)   // última donación con proximaFechaElegible lista; null si nunca donó
public boolean verificarExclusion(Usuario donador)             // D10: lee Usuario.exclusionPermanente (fuente única)
public boolean esElegible(Usuario donador)                     // para HU-3.3 ([Alex]): !exclusionPermanente && (sin donación || proximaFecha <= hoy)
```

**Facades nuevas**

```java
// RegistroDonacionFacade
public RegistroDonacion buscarUltimaPorDonador(Usuario u)
// JPQL: SELECT r FROM RegistroDonacion r WHERE r.usuarioid = :u ORDER BY r.fechaDonacion DESC   + setMaxResults(1); null si vacío
```

La exclusión no necesita facade nueva (D10): `usuarioF.find(id)` ya existe.

**Terminado cuando**

- `donador1@prueba.mx` (903) → "Ya puedes donar" (la próxima fecha fue hace 144 días).
- `donador2@prueba.mx` (904) → estado restrictivo **por exclusión permanente**: los datos de prueba 2.4 lo marcan con
  `exclusionPermanente = TRUE`.
- Con `UPDATE Usuario SET exclusionPermanente = FALSE WHERE id = 904;`, el estado restrictivo pasa a ser **por
  fecha** (dentro de 36 días). Esto demuestra que se lee el campo y no los padecimientos (D10).
- Usuario sin donaciones → "Aún no tienes donaciones registradas".
- `esElegible`: 903 → true; 904 → false.
- SQL de verificación:
  ```sql
  SELECT fechaDonacion, proximaFechaElegible FROM RegistroDonacion WHERE Usuarioid = 904 ORDER BY fechaDonacion DESC LIMIT 1;
  SELECT id, exclusionPermanente FROM Usuario WHERE id IN (903, 904);
  ```

**Checklist**

- [x] Facade: `buscarUltimaPorDonador`
- [x] `verificarExclusion` y `esElegible` leen `Usuario.exclusionPermanente` (D10)
- [x] `obtenerElegibilidad`, `verificarExclusion` y `esElegible` (+ `calcularProximaFecha` privado)
- [x] Vista con los estados elegible, restrictivo por fecha y restrictivo permanente
- [x] Avisar a [Alex] y [Galilea] cuando esté en `main`
- [x] Pruebas + SQL

---

## 6. Preparar el entorno

Para quien clona el repo. Cada paso trae la variante **Linux** (rutas `~/Payara_Server`, `~/netbeans`) y la **Windows**
(`C:\<ruta-payara>`, `C:\<ruta-jdk-21>`); ajusta las rutas a tu máquina.

### 6.1 Software

| Pieza | Versión | Notas |
|---|---|---|
| JDK | **21** (OpenJDK) | Linux: `sudo apt install openjdk-21-jdk` → `/usr/lib/jvm/java-21-openjdk-amd64`. Windows: instalador de JDK 21 |
| NetBeans | **25** | Tools → Java Platforms → agrega JDK 21 |
| Payara Server | **7** (Jakarta EE 11) | Linux: `~/Payara_Server`. Windows: `C:\<ruta-payara>` |
| MySQL | **8** | Linux: `sudo apt install mysql-server`. Windows: MySQL Installer |
| Conector JDBC | **`mysql-connector-j-9.7.0.jar`** | dev.mysql.com/downloads/connector/j → "Platform Independent" |

Valores que usa todo el equipo (los mismos en todas las variantes):

| Dato | Valor |
|---|---|
| Base de datos | `sivido` |
| Usuario MySQL | `sivido` |
| Contraseña MySQL | `Sivido2026!` |
| Pool | `sividoPool` |
| Recurso JDBC | `jdbc/sivido` |

### 6.2 Base de datos y usuario de MySQL (igual en Linux y Windows)

> **¿Ya tienes la base `sivido` con las 17 tablas?** Entonces **solo** corre `sql/datos_prueba.sql` (ver "Datos de
> prueba", más abajo) y salta al paso 6.3. No vuelvas a crear la base ni las tablas.
>
> ```bash
> mysql -u sivido -p sivido < sql/datos_prueba.sql        # Windows (cmd): sql\datos_prueba.sql
> ```
>
> También puedes abrir el archivo en el editor SQL de NetBeans, conectado a `sivido`, y ejecutarlo todo (*Run SQL*).

**Si todavía no tienes la base**, entra a MySQL como root:

- Linux: `sudo mysql`
- Windows: `mysql -u root -p` (desde *MySQL Command Line Client* o cmd)
- También sirve el editor SQL de NetBeans (Services → Databases)

Luego ejecuta:

```sql
CREATE DATABASE sivido CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sivido'@'localhost' IDENTIFIED BY 'Sivido2026!';
GRANT ALL ON sivido.* TO 'sivido'@'localhost';
FLUSH PRIVILEGES;
```

**Script de tablas** (`sql/SIVIDO_CETS_mysql.sql`): córrelo **sin** las líneas `DROP DATABASE IF EXISTS sivido;` y
`CREATE DATABASE …`. La base ya la creaste arriba, y el `DROP` borraría los datos de quien ya tiene la BD.

- **Linux:**
  ```bash
  sed '/^DROP DATABASE/d; /^CREATE DATABASE/d' sql/SIVIDO_CETS_mysql.sql | mysql -u sivido -p sivido
  ```
- **Windows (cmd):**
  1. Abre el script y borra o comenta (`-- `) esas dos líneas.
  2. Ejecuta `mysql -u sivido -p sivido < sql\SIVIDO_CETS_mysql.sql`.

  En PowerShell `<` no funciona; usa `cmd /c "mysql -u sivido -p sivido < sql\SIVIDO_CETS_mysql.sql"`.
- **Editor SQL de NetBeans:**
  1. Conéctate a `sivido` y pega el script sin esas dos líneas.
  2. Ejecuta todo con *Run SQL*.
  3. **No uses `EXIT`** ni `QUIT`; el editor de NetBeans no los acepta.

**Datos de prueba** (sección 2):

```bash
mysql -u sivido -p sivido < sql/datos_prueba.sql        # Windows: sql\datos_prueba.sql
```

**Verificar:**

```sql
USE sivido;
SHOW TABLES;                          -- 17 tablas
SELECT id, nombre FROM Hospital;      -- al menos los 5 hospitales
SELECT * FROM PersonalCETS;           -- 901, admin@cets.oaxaca.gob.mx
```

### 6.3 Driver JDBC

Copia `mysql-connector-j-9.7.0.jar` **con Payara detenido**:

- **Linux:**
  ```bash
  ~/Payara_Server/bin/asadmin stop-domain domain1
  cp mysql-connector-j-9.7.0.jar ~/Payara_Server/glassfish/domains/domain1/lib/
  ~/Payara_Server/bin/asadmin start-domain domain1
  ```
- **Windows (cmd):**
  ```bat
  cd C:\<ruta-payara>\bin
  asadmin stop-domain domain1
  copy mysql-connector-j-9.7.0.jar C:\<ruta-payara>\glassfish\domains\domain1\lib\
  asadmin start-domain domain1
  ```

### 6.4 Pool `sividoPool` y recurso `jdbc/sivido`

Hay tres variantes con **los mismos valores**. Elige una; Payara debe estar corriendo (`start-domain domain1`).

#### a) Linux (bash), desde `~/Payara_Server/bin`

```bash
cd ~/Payara_Server/bin
./asadmin create-jdbc-connection-pool \
  --restype javax.sql.DataSource \
  --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
  --property 'user=sivido:password=Sivido2026!:serverName=localhost:portNumber=3306:databaseName=sivido:useSSL=false:allowPublicKeyRetrieval=true:serverTimezone=America/Mexico_City' \
  sividoPool
./asadmin create-jdbc-resource --connectionpoolid sividoPool jdbc/sivido
./asadmin set resources.jdbc-connection-pool.sividoPool.driver-classname=com.mysql.cj.jdbc.Driver
./asadmin set 'resources.jdbc-connection-pool.sividoPool.property.URL=jdbc:mysql://localhost:3306/sivido?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City'
./asadmin ping-connection-pool sividoPool
```

- Las comillas **simples** evitan que bash interprete `!` y `&`.
- Driver: `mysql-connector-j-9.7.0.jar` en `~/Payara_Server/glassfish/domains/domain1/lib/` (paso 6.3).
- Si `asadmin` da **"class file version 65"**, ve al paso 6.5.

#### b) Windows (cmd), desde `C:\<ruta-payara>\bin`

Una línea por comando, con comillas dobles:

```bat
cd C:\<ruta-payara>\bin
asadmin create-jdbc-connection-pool --restype javax.sql.DataSource --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource --property "user=sivido:password=Sivido2026!:serverName=localhost:portNumber=3306:databaseName=sivido:useSSL=false:allowPublicKeyRetrieval=true:serverTimezone=America/Mexico_City" sividoPool
asadmin create-jdbc-resource --connectionpoolid sividoPool jdbc/sivido
asadmin set resources.jdbc-connection-pool.sividoPool.driver-classname=com.mysql.cj.jdbc.Driver
asadmin set "resources.jdbc-connection-pool.sividoPool.property.URL=jdbc:mysql://localhost:3306/sivido?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City"
asadmin ping-connection-pool sividoPool
```

- En **PowerShell** usa `.\asadmin.bat` en lugar de `asadmin`.
- Driver: `mysql-connector-j-9.7.0.jar` en `C:\<ruta-payara>\glassfish\domains\domain1\lib\` (paso 6.3).
- Si da **"class file version 65"**, ve al paso 6.5.

#### c) Consola web `http://localhost:4848`

1. **Resources → JDBC → JDBC Connection Pools → New**.
   - Pool Name: `sividoPool`
   - Resource Type: `javax.sql.DataSource`
   - Database Driver Vendor: `MySql`
   - Clic en **Next**.
2. En la página 2:
   - Datasource Classname: `com.mysql.cj.jdbc.MysqlDataSource`
   - Driver Classname: `com.mysql.cj.jdbc.Driver`
3. En **Additional Properties**, borra las que sobren y deja exactamente estas (*Add Property* para las que falten):

   | Name | Value |
   |---|---|
   | `user` | `sivido` |
   | `password` | `Sivido2026!` |
   | `serverName` | `localhost` |
   | `portNumber` | `3306` |
   | `databaseName` | `sivido` |
   | `useSSL` | `false` |
   | `allowPublicKeyRetrieval` | `true` |
   | `serverTimezone` | `America/Mexico_City` |
   | `URL` | `jdbc:mysql://localhost:3306/sivido?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Mexico_City` |

   Clic en **Finish**.
4. **Resources → JDBC → JDBC Resources → New**:
   - JNDI Name: `jdbc/sivido`
   - Pool Name: `sividoPool`
   - Clic en **OK**.
5. Regresa a **JDBC Connection Pools → sividoPool** y pulsa **Ping**. Debe decir *Ping Succeeded*.

#### Verificar (cualquier variante)

```bash
asadmin list-jdbc-resources        # debe listar jdbc/sivido
asadmin ping-connection-pool sividoPool
```

### 6.5 Error "class file version 65" (Payara corriendo con JDK 17)

`asadmin` y Payara 7 necesitan **JDK 21**. Si tu `java` por defecto es 17 (por ejemplo, con sdkman en Linux), fíjalo así:

- **Linux:** agrega al final de `~/Payara_Server/glassfish/config/asenv.conf`
  ```bash
  AS_JAVA="/usr/lib/jvm/java-21-openjdk-amd64"
  ```
- **Windows:** agrega al final de `C:\<ruta-payara>\glassfish\config\asenv.bat`
  ```bat
  set AS_JAVA=C:\<ruta-jdk-21>
  ```

Compruébalo con `asadmin version --local`.

### 6.6 Cuentas de prueba

Ya vienen en `sql/datos_prueba.sql` (sección 2.1). Al terminar puedes entrar con:

- **Usuario:** `familiar@prueba.mx` / `Donador2026!` (o crea tu cuenta con HU-1.1).
- **CETS:** `admin@cets.oaxaca.gob.mx` / `Cets2026!`.

### 6.7 NetBeans: abrir el proyecto y "Resolve Project Problems"

1. File → Open Project → `mx.edu.itoaxaca.sividocets` (el EAR). Marca "Open Required Projects".
2. Tools → Servers → Add Server → **Payara Server** → `~/Payara_Server`, dominio `domain1`.
3. Clic derecho en el EAR, el `-ejb` y el `-war` → *Properties → Run* → Server: Payara. *Sources/Libraries* →
   Java Platform: JDK 21.
4. Los jars están referenciados con rutas absolutas de la máquina de [DAVID] (`/home/eldeivis/...`). Clic derecho en
   `-war` (y en `-ejb`) → **Resolve Project Problems** → *Resolve* en cada referencia rota:

   | Referencia | Archivo en tu máquina | ¿Package? |
   |---|---|---|
   | `primefaces-16.0.1.jar` | `<netbeans>/enterprise/modules/ext/primefaces/primefaces-16.0.1.jar` | **Sí** (va en `WEB-INF/lib`) |
   | `jakarta.ejb-api.jar` | `<payara>/glassfish/modules/jakarta.ejb-api.jar` | **No** (desmarcado) |
   | `jakarta.enterprise.cdi-api.jar` | `<payara>/glassfish/modules/jakarta.enterprise.cdi-api.jar` | **No** |
   | `jakarta.inject-api.jar` | `<payara>/glassfish/modules/jakarta.inject-api.jar` | **No** |
   | `jakarta.faces.jar` | `<payara>/glassfish/modules/jakarta.faces.jar` | **No** |
   | `jakarta.persistence-api.jar` | `<payara>/glassfish/modules/jakarta.persistence-api.jar` | **No** (war y ejb) |

   Las APIs de Payara **no** se empaquetan, porque el servidor ya las trae. Revisa la columna *Package* en
   *Properties → Libraries* del `-war`.
5. Esto modifica `nbproject/project.properties`, que es un archivo compartido. **No subas** esas rutas a git. Ejecuta
   una vez:
   ```bash
   git update-index --skip-worktree \
     mx.edu.itoaxaca.sividocets-war/nbproject/project.properties \
     mx.edu.itoaxaca.sividocets-ejb/nbproject/project.properties
   ```
   Para deshacerlo, cambia `--skip-worktree` por `--no-skip-worktree`.
6. Clean and Build del EAR, y luego Run. Abre `http://localhost:8080/mx.edu.itoaxaca.sividocets-war/`.

### 6.8 Compilar desde terminal (opcional)

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=$JAVA_HOME/bin:$PATH
cd ~/NetBeansProjects/mx.edu.itoaxaca.sividocets
~/netbeans/extide/ant/bin/ant -Duser.properties.file=$HOME/.netbeans/25/build.properties clean dist
# Esperado: BUILD SUCCESSFUL, y dist/mx.edu.itoaxaca.sividocets.ear
```

---

## 7. Flujo de git

| Integrante | Rama |
|---|---|
| [DAVID] | `feature/panel-cets` |
| [Galilea] | `feature/solicitudes` |
| [Alex Torres] | `feature/notificaciones` |
| [Diego Roque] | `feature/historial` |

```bash
# Al empezar (una vez)
git checkout main
git pull origin main
git checkout -b feature/<modulo>

# Cada día ANTES de trabajar: traer lo nuevo de main
git checkout main && git pull origin main
git checkout feature/<modulo> && git merge main        # resuelve conflictos aquí, en TU rama

# Commits chicos: uno por paso de la checklist
git status                                              # revisa que NO aparezcan build/, dist/, nbproject/private/
git add <archivos concretos>                            # nunca "git add ." a ciegas
git commit -m "HU-2.4: validarFolio en LnSolicitud y existeFolioActivo en facade"
git push -u origin feature/<modulo>

# Merge a main: SOLO si compila con 0 errores y pasaste tus pruebas + la regresión (sección 8)
git checkout main && git pull origin main
git merge feature/<modulo>
# compilar y probar OTRA VEZ ya mezclado
git push origin main
```

Reglas:

- **Nunca** subas `build/`, `dist/`, `nbproject/private/` ni `*.class`; el `.gitignore` ya los excluye.
- **Nunca** subas tu `project.properties` con rutas locales (ver 6.7, paso 5).
- Mensajes de commit en español, empezando por la HU (`HU-x.y: …`).
- Si tocas un archivo compartido (sección 3, regla 7), avisa en el grupo **antes** de hacer push.
- `LnDonacion.java`: [Galilea] y [Diego] escriben en bloques separados. Hagan `git merge main` seguido para detectar
  conflictos pronto.

---

## 8. Pruebas de regresión antes de merge

Todos corren esto antes de mezclar su rama a `main` y otra vez **después** del merge.

- [x] **Compila:** Clean and Build del EAR con JDK 21 → `BUILD SUCCESSFUL`, **0 errores**. No debe haber warnings
      nuevos respecto a `main`.
- [x] **Despliega:** Run sin excepciones en el log de Payara (`~/Payara_Server/glassfish/domains/domain1/logs/server.log`).
- [x] **HU-1.1 Alta inicial:**
  - En `cuentas/crearCuenta`, crear una cuenta con un correo nuevo → te lleva a `cuentas/iniciarSesion`.
  - Repetir el mismo correo → "Ese correo ya está registrado".
  - SQL: `SELECT correo, LENGTH(passwordHash), fechaRegistro FROM Usuario ORDER BY id DESC LIMIT 1;` → hash de 64 caracteres.
- [x] **HU-1.2 Autenticación:**
  - Entrar con esa cuenta → `notificaciones/notificaciones`, con nombre e iniciales en el encabezado.
  - Una contraseña mala → "Correo o contraseña incorrectos".
  - "Cerrar sesión" → `publico/index`.
- [x] **HU-6.1 Login institucional:**
  - `admin@cets.oaxaca.gob.mx` / `Cets2026!` → `cets/solicitudesActivas`.
  - Una contraseña mala → "Credenciales institucionales incorrectas".
  - Sin sesión CETS, `/faces/vista/cets/donadores.xhtml` → redirige a `cets/loginInstitucional`.
  - Con sesión de usuario normal, una vista CETS también redirige al login institucional.
- [x] **Sesión expirada (#26):** abre un formulario (por ejemplo `cuentas/miPerfil` o `cets/detalleSolicitud`), en otra
      pestaña pulsa "Cerrar sesión" y luego envía el formulario de la primera: debe aparecer "Tu sesión expiró" con los dos
      botones (destacado el institucional si estabas en una vista CETS), sin pantalla de error.
- [x] La raíz `http://localhost:8080/mx.edu.itoaxaca.sividocets-war/` carga `publico/index`.
- [x] Las HU de tu módulo siguen pasando sus "Terminado cuando".
- [x] **Donación ligada → Atendida (#12):** el CETS registra una donación del donador 903 (O−) para la solicitud 901 (O−): el mensaje dice
      "La solicitud … pasó a Atendida", la solicitud sale de `solicitudesActivas` y el combo de `capturarDonacion` ya no la ofrece.
      Una donación general no cambia ningún estatus.
- [x] **Bitácora:** en `detalleSolicitud` de esa solicitud, "Historial de cambios" muestra Activa → Atendida con el nombre del
      personal y la hora de México; una solicitud sin cambios dice "Sin cambios de estatus". Tras "Guardar cambio de estatus" aparece
      el renglón nuevo sin recargar a mano.
- [x] **Solicitud no Activa (#25):** `publicacion?uuid=` de la solicitud Atendida muestra "Esta solicitud ya no está activa" sin folio ni
      hospital ni QR; la de una Activa se ve completa; un uuid inexistente conserva "Publicación no encontrada". `notificaciones` del
      donador ya no lista avisos de solicitudes no Activas; `misSolicitudes` del familiar sigue listando todas.
- [x] **Constancia:** en `miHistorial`, "Descargar" abre `historial/constancia?id=` con los datos de esa donación; con el id de una
      donación de otro usuario, o inexistente, muestra "Constancia no disponible"; sin sesión redirige al login.
- [x] **Impresión:** "Imprimir / guardar como PDF" (constancia) e "Imprimir ficha" (`publicacion`) imprimen solo el contenido, sin
      encabezado, menú, pie ni botones.
- [x] **Exclusión permanente (#13):** en `cets/donadores?id=903` aparece "Marcar exclusión permanente" (en el 904, ya excluido, no). El diálogo
      avisa que el motivo será visible y que no se puede deshacer; sin motivo, o con más de 200 caracteres, no guarda. Con motivo válido: mensaje de
      éxito, la ficha muestra motivo y fecha de hoy y el botón desaparece. SQL: una fila nueva en `HistorialClinico` (Usuarioid 903, PersonalCETS 901)
      y `exclusionPermanente = 1`; repetirlo no crea otra fila. `capturarDonacion?donador=903` muestra el motivo y la fecha en el aviso y deshabilita el
      botón; el 903 ve el aviso en `miPerfil`; sus solicitudes y avisos siguen igual.
- [x] **Lista de interesados (#17):** el 903 confirma interés en la 901: `detalleSolicitud?id=901` lista su nombre y `O−`; sin intereses solo el
      contador en 0; en el HTML (Ctrl+U) no hay latitud, longitud, teléfono ni correo; un donador solo notificado no aparece.

### Limitaciones conocidas al 2026-11-19

Pasada completa sobre `main` el 2026-11-19: Clean and Build con 0 errores, despliegue limpio, regresión de esta sección y
recorrido de punta a punta (familiar crea solicitud → donador recibe aviso, contesta el cuestionario y confirma interés →
CETS ve 1 interesado, registra la donación vinculada y pasa la solicitud a Atendida → el donador ve su historial). Todo pasó.
Lo que sigue **no** es un fallo de esa pasada: son límites aceptados.

- **Ficha del donador:** el botón "Registrar donación" se deshabilita si el donador donó hace menos de 56 días, aunque exista una fecha pasada válida para él (#42).
- **`capturarDonacion`:** la etiqueta "Fecha de la donación" muestra el asterisco de obligatorio dos veces (detalle visual).

**Resueltas el 2026-11-24 (`feature/cierre-reglas`):**

- **#12 (resuelta):** `LnDonacion.registrarDonacion` pasa la solicitud ligada a **Atendida** en la misma transacción, con renglón en
  `HistorialEstatusSolicitud` (reutiliza `LnPanelCETS.cambiarEstatus`). El mensaje de `capturarDonacion` lo dice.
- **#25 (resuelta):** `publicacion` muestra "Esta solicitud ya no está activa" (sin folio, hospital ni QR) si el estatus no es Activa, y
  `LnNotificacion.listarMisNotificaciones` ya no regresa avisos de solicitudes no Activas. `misSolicitudes` sigue mostrando todos los estatus.
- **Bitácora:** `detalleSolicitud` muestra el "Historial de cambios" de la solicitud.

**Resueltas el 2026-11-24 (`feature/exclusion-permanente`):**

- **#13 (resuelta):** la ficha del donador (`cets/donadores`) tiene el botón **"Marcar exclusión permanente"** (solo personal CETS, oculto si el
  donador ya está excluido). Un diálogo pide el motivo (obligatorio, máximo 200) y avisa que el donador lo verá y que no se puede deshacer
  desde la app. `LnPanelCETS.marcarExclusionPermanente` crea un `HistorialClinico` (motivo en `diagnostico`, `fechaDeteccion` = hoy, personal que
  lo marcó) y pone `Usuario.exclusionPermanente = true` en una sola transacción, actualizando las listas inversas. **No** hay botón para quitarla
  y **no** se tocan sus solicitudes ni avisos: solo se bloquean donaciones nuevas (D18). La ficha muestra motivo y fecha; `miPerfil` del donador
  muestra un aviso con el motivo; el mensaje D18 de `capturarDonacion` ahora incluye motivo y fecha.
- **#17 (resuelta):** `detalleSolicitud` lista a los donadores que **confirmaron interés** (`Interes`), solo con **nombre y tipo de sangre**.
  Criterio de diseño: los que solo fueron notificados no aparecen (no han aceptado compartir nada), y la lista coincide con el contador.
  Nunca se muestran ubicación, teléfono ni correo (el `neg` de HU-6.3 sigue vigente).

**Funcionalidades sin HU** (pendiente agregarlas al backlog y a los diagramas): **Constancia** de donación (`miHistorial` →
`historial/constancia`), **Descargar QR** e **Imprimir ficha** (`publicacion`), **Marcar exclusión permanente** (`cets/donadores`) y la
**lista de interesados** de HU-6.3 (`obtenerInteresados`, que el `.vpp` no tiene).
- **Diagramas (`SIVIDO_CETS_U4.vpp`), pendientes:** dudas 6a–6d de las secuencias (mensajes que no coinciden con el código, solo anotados); HU-2.1 sin `buscarDonadoresCercanos`; falta la dependencia `ControlPanelCETS` → `ControlCuentas` en el diagrama de clases; HU-6.1 sin la comprobación de `activo`; 36 métodos de entidades sin tocar; `generarQR` y `contarRegistros` (#15); `Interes` sin ligar al ORM.
- [x] **HU-1.4 / HU-3.3 Contacto del donador al familiar (D19):**
  - 903 (O−) confirma interés en la solicitud 901 con el consentimiento apagado → 902 en `misSolicitudes` → "Ver donadores interesados": ve nombre, tipo de sangre y fecha, y "Este donador no compartió su contacto".
  - 903 enciende el toggle en `miPerfil` y guarda → 902 recarga y ve teléfono y correo; al apagarlo, vuelve el aviso.
  - Otro familiar abre `interesados?id=<id>` a mano → "Solicitud no disponible". Sin sesión → login.
  - Ctrl+U: sin `passwordHash`, ubicación ni teléfono/correo con el consentimiento apagado. `publicacion` y el detalle CETS (#17) no cambian.
- [ ] **D20 Validadores JSF reutilizables (paquete `validadores`):**
  - `capturarSolicitud`: folio `CETS-OAX-2026-0582` (válido: "Formato de folio válido", "Folio en regla" y Publicar habilitado); `abc` (mensaje de formato junto al campo, **sin** verde y Publicar deshabilitado); corregirlo (vuelven el verde y el botón); folio de una Activa (mensaje de duplicado); vacío (sin mensaje, botón deshabilitado). Descripción de 250 (pasa), 300 pegados (mensaje junto al campo), vacía (pasa) y una de ~249 caracteres con varios saltos de línea (pasa; el Ln cuenta limpio).
  - `capturarDonacion`: fecha de hoy y de ayer (pasan); futura forzada (mensaje junto al campo); vacía ("Indica la fecha de la donación").
  - `criteriosExclusion`: **crear** y **editar** un Temporal con 365 (guarda); Temporal con 0, -5, `abc` y vacío, tanto al crear como al editar (mensaje junto a "Días de espera", no guarda); Permanente con días vacíos (guarda); Temporal→Permanente con días vacíos (guarda); pregunta vacía.
  - `cets/donadores` (#13): motivo válido (marca); vacío, solo espacios y 201 caracteres (mensaje dentro del diálogo, que se reabre).
  - Correo inválido (`juan@prueba`, `juan`, `juan@`) en `crearCuenta`, `iniciarSesion` y `loginInstitucional`: "El correo no tiene un formato válido"; válidos (`donador1@prueba.mx`, `admin@cets.oaxaca.gob.mx`) siguen entrando; vacío: "Escribe tu correo".
  - Ningún mensaje duplicado.
---

## 9. Decisiones tomadas

Son inconsistencias del `.vpp` contra la BD y las vistas que ya se resolvieron. La columna "Tarea" indica dónde
quedó el trabajo en este documento.

| # | Tema | Decisión | Tarea |
|---|---|---|---|
| D1 | Ubicación del donador | **Implementada** en `cuentas/miPerfil`: el combo "Región" se eliminó y se reemplazó por el botón **"Usar mi ubicación"**: Geolocation API del navegador → `h:inputHidden` de `PerfilClinico.latitud/longitud`. **Sin coordenadas no se notifica a ese donador.** | [DAVID] HU-1.3 (vista miPerfil); [Alex] HU-3.1 (`buscarDonadoresActivos` exige `latitud/longitud` no nulas) |
| D2 | Respuestas del cuestionario (HU-4.1) | El controller arma `CuestionarioRespuesta` con su `List<RespuestaCriterio>`. El Ln calcula `resultado` (y `tiempoEsperaDias`) **antes** de `create`. | [Alex] HU-4.1 / HU-4.2 |
| D3 | Valores de resultado | En código se usan exactamente `'Apto'` y `'NoApto'` (CHECK de la BD). La vista puede mostrar "No apto". | [Alex] HU-4.2 |
| D4 | Formato de folio | `^CETS-OAX-\d{4}-\d{4}$`. **Decisión del equipo (regla de negocio).** | [Galilea] HU-2.4 |
| D5 | QR | Librería JS de QR **local** en `-war/web/resources/js/` (sin CDN). El QR codifica la URL pública con `uuidPublico` (`/faces/vista/publico/publicacion.xhtml?uuid=…`); nunca con el id. | [Galilea] HU-2.2 |
| D6 | Falta la clase `Interes` en el diagrama | Se agrega al diagrama de clases. | [DAVID] T-final |
| D7 | Vistas que no existen (`UIAjustes`, `UIBuscarDonador`, `UIFichaDonador`) | `UIAjustes` → `cuentas/miPerfil` (HU-3.4) y `cets/criteriosExclusion` (HU-4.3). `UIBuscarDonador`/`UIFichaDonador` → `cets/donadores` (HU-5.4, HU-7.3). | [DAVID] HU-3.4; [Alex] HU-4.3 y vista notificaciones; [Galilea] HU-5.4/7.3; [DAVID] T-final (diagrama) |
| D8 | HU-1.4 (privacidad) | Se queda con [Galilea] (`ControlSolicitud` → `LnSolicitud`). [DAVID] solo agrega el toggle `consentimientoContacto` en miPerfil (**implementado**). | [Galilea] HU-1.4; [DAVID] HU-1.3 |
| D9 | Secuencia contra navegación | Cuando no coinciden, **se sigue el *Diagrama de navegación SIVIDO***: `capturarSolicitud → misSolicitudes`, `notificaciones → cuestionario`, `confirmarInteres → notificaciones`. | Todos |
| D10 | Exclusión permanente: dos fuentes de verdad | **Fuente única: `Usuario.exclusionPermanente`.** Se marca `true` al registrar un `HistorialClinico` con criterio `Permanente`. `verificarExclusion()` y `esElegible()` leen ese campo; no recorren padecimientos. | [Diego] HU-5.3; datos de prueba 2.4 |
| D11 | Valores de `criteriosExclusion` | Solo `SiNo` / `SeleccionSimple`; se quita "Selección múltiple". `tipoExclusion` va como `Temporal` / `Permanente` (sin minúsculas). | [Alex] HU-4.3 |
| D12 | Diagrama desactualizado | Actualizar el diagrama de clases y las secuencias: firmas con entidad completa, `Interes`, `SesionController`, `iniciarSesion` devuelve `Usuario`, métodos de entidades movidos a los Controls y mensajes del Ln delgado. | [DAVID] T-final |
| D13 | `consentimientoContacto` | **Opt-in: apagado por defecto** (manda el `DEFAULT FALSE` de la BD). Se activa solo si el donador lo elige en miPerfil. | [DAVID] HU-1.3 (hecho) |
| D14 | Toggles sin columna | "Recibir avisos de solicitudes compatibles" queda **deshabilitado** en miPerfil, con una nota, sin conectarlos a nada. | [DAVID] HU-1.3 (hecho) |
| D15 | Combo "Región" | Se **elimina**: no existe columna y la ubicación (D1) lo reemplaza. | [DAVID] HU-1.3 (hecho) |
| D16 | Controller de miPerfil | `PerfilController` (`@ViewScoped`) y no `CuentasController` (`@RequestScoped`): el perfil debe conservarse entre los clics de la vista. Los dos usan `LnCuentas`. | [DAVID] HU-1.3 / HU-3.4 (hecho) |
| D17 | Cuestionario Apto y confirmar interés | El resultado **Apto** del cuestionario es **orientativo, no obligatorio**: `registrarInteres` no lo exige. Sí exige solicitud Activa, que el donador no sea el solicitante, compatibilidad tipo/Rh y `esElegible` (56 días y exclusión permanente). | [Alex] HU-3.3 / HU-4.2 — **decidida** |
| D18 | Cuándo no se registra una donación | **No se puede registrar una donación si el donador tiene exclusión permanente, la fecha es futura, ya donó ese día, la fecha queda a menos de 56 días de cualquier donación suya (antes o después) o, cuando hay solicitud, la solicitud no está Activa o su grupo/Rh es incompatible: la compatibilidad bloquea la donación cuando hay solicitud.** Lo valida `LnDonacion.registrarDonacion` (vía `motivoRechazoDonacion`); la vista muestra el motivo y deshabilita el botón. La donación general (sin solicitud) no revisa compatibilidad. | [DAVID] HU-5.1 — **decidida** |
| D19 | Contacto del donador al familiar | El familiar **creador** de la solicitud ve en `solicitudes/interesados?id=N` a los donadores que confirmaron interés (`Interes`): **siempre** nombre, tipo de sangre y fecha; **teléfono y correo solo si** `Usuario.consentimientoContacto = true` al mostrar (si no: "Este donador no compartió su contacto"). Nunca ubicación ni `passwordHash`; no se muestra en `publicacion` ni al CETS (#17 sigue solo con nombre y tipo de sangre). Lo valida `LnSolicitud.obtenerInteresados`/`obtenerSolicitudPropia` (creador = usuario en sesión). En miPerfil queda **un solo** toggle, "Compartir mi contacto solo cuando yo confirme interés", ligado a `consentimientoContacto`. | [DAVID] HU-1.4 / HU-3.3 — **decidida** |
| D20 | Validaciones de captura con validadores JSF | Cada regla de captura vive en el **Ln** (método `public boolean`, el Ln sigue validando en backend) y la vista la consulta con una clase `@FacesValidator(value="…", managed=true)` + `@Dependent` + `@EJB` del paquete `validadores` del war, que extiende `ValidadorBase` (ignora `null`/vacío —eso lo hace `required`—, recorta espacios y lanza `ValidatorException` con resumen = detalle). Se conecta con `<f:validator validatorId="…"/>` dentro del campo; el mensaje sale junto al campo (`p:message for`). Validadores: `folioValidator` (`folioConFormatoValido`), `folioDisponibleValidator` (`folioDisponible`), `descripcionValidator` (`descripcionValida`, máx. 250), `fechaNoFuturaValidator` (`fechaDonacionValida`, por día en America/Mexico_City), `diasEsperaValidator` (`duracionValida`), `motivoExclusionValidator` (`motivoExclusionValido`, 1–200 y no solo espacios) y `correoValidator` (`LnCuentas.correoValido`, solo formato, también en `iniciarSesion` y `loginInstitucional`). **Validación entre campos:** el radio se liga con `binding="#{tipoExclusionComp}"` y se pasa con `<f:attribute name="tipo" value="#{tipoExclusionComp}"/>`; el campo vacío lo cubre un `required` condicional (`#{tipoExclusionComp.value eq 'Temporal'}`). No se usa `f:validateLength` para descripción ni motivo (el salto de línea cuenta distinto en el navegador y en el Ln). **Descripción:** el Ln la **rechaza** (no la trunca). **Botón Publicar:** `estadoFolio` lo calcula el Ln en el listener; si el folio es inválido el listener no corre, así que el botón y los bloques verdes también exigen `view.findComponent('formSolicitud:folio').valid`. Los controllers ya no tienen métodos `v*`. **Pendiente ([Galilea], Tarea 1):** `LnCuentas.contrasenaValida` y un `ContrasenaValidator` igual que `CorreoValidator`; `crearCuenta` ya usa `correoValidator` (el teléfono solo tiene `maxlength=10`: falta definir su regla). | [DAVID] — **decidida** |

---

## 10. Inconsistencias a revisar

Siguen **abiertas**: necesitan una decisión del equipo o del profe. Lo ya resuelto está en la
[sección 9](#9-decisiones-tomadas).

### Generales

1. **Tipos de atributos:**
   - En el diagrama, `latitud/longitud` son `double` y `distanciaAproximada` es `float`; en BD y entidades son
     `DECIMAL` / `BigDecimal`.
   - En el diagrama, `fechaHoraRegistro`, `fechaRespuesta`, etc. son `DateTime`; en Java son `Date`.
2. **Valores de estado no definidos en la BD:**
   - `Notificacion.estatus` no tiene CHECK. Este documento propone `"Enviada"`; definan la lista (¿`Enviada`, `Leida`?).
   - `UnidadSangre.estatus` tampoco tiene CHECK.
3. **Métodos del código que no están en el diagrama:**
   - `LnSolicitud.listarHospitales()` (combo de HU-2.1).
   - `LnNotificacion.listarMisNotificaciones(Usuario)` (vista notificaciones).
   - `LnCuestionario.obtenerCatalogo()` (HU-4.3, todos los criterios incluidos los inactivos; `obtenerCriterios()`
     queda para los activos de HU-4.1).
   - El guardado de `consentimientoContacto` dentro de `guardarPerfilClinico` (D8).

   Decidir si se agregan al diagrama en la T-final.

### Por HU

4. **HU-1.3:** `PerfilClinico.marcarElegibilidadPreliminar(): boolean` regresa `elegible`, pero **no hay columna**
   para guardarlo. **No se implementó** (tampoco hay regla definida).
5. **HU-1.3, miPerfil:** el toggle "Recibir avisos" no tiene columna en la BD, y la edición de datos personales
   (nombre/teléfono) no tiene HU. El toggle sin columna quedó deshabilitado (D14) y los datos personales son de
   solo lectura.
6. **HU-1.4 / HU-3.3 (cerrada, D19):** `PublicacionSolicitud.mostrarContactoDonador(int solicitudId)` (HU-3.3, 1.1.4) contradice la
   vista pública, que no lleva sesión. No está definido **a quién** se le muestra el contacto del donador (¿al familiar
   en misSolicitudes?) ni cómo entra `consentimientoContacto`.
7. **HU-3.1:**
   - `consultarDonadores(solicitudId)` no dice qué filtro aplica. Este documento usa "con perfil completo, con
     coordenadas y sin exclusión permanente".
   - `calcularRadioCercania` está en la secuencia de HU-3.2, no en la de 3.1, aunque la guarda `dentroRadio` sí
     aparece en 3.1.
   - `«create» Notificacion(donadorId, solicitudId)` no menciona `fechaEnvio` (NOT NULL) ni `distanciaAproximada`.
8. **HU-3.2:** `calcularRadioCercania(String ubicacionDonador, String ubicacionSolicitud)` usa `String`, pero la BD
   tiene `latitud/longitud DECIMAL(9,6)`. La "ubicación de la solicitud" es la del **hospital**, porque
   `SolicitudReposicion` no tiene coordenadas.
9. **HU-3.3:** con D9, un donador no elegible nunca llega a `confirmarInteres`, porque la navegación
   `resultado → confirmarInteres` exige `[elegible == true]`. Queda sin uso la rama
   `alt[elegible == false] → UIMiHistorial` de la secuencia; decidir si se quita del diagrama.
10. **HU-4.2:** no está definido qué tiempo de espera mostrar cuando el criterio es **Permanente** (`duracionDias`
    NULL), ni si el diagrama renombra `tiempoEspera` a `tiempoEsperaDias`.
11. **HU-4.3:** la vista tiene el campo "Referencia" sin columna en la BD. Desactivar un criterio no tiene mensaje
    propio en la secuencia (se usa `modificarCriterio` con `activo = false`).
12. **HU-5.1:**
    - `calcularProximaFecha()` no define la regla. El comentario del script dice
      `máx(última donación + 56 días, fechaRespuesta + tiempoEsperaDias)`, pero el diagrama no lo refleja.
    - La vista usa `datetime-local` y la columna es `DATE`.
    - ~~Falta definir si al registrar una donación ligada se cambia el estatus de la solicitud.~~ **Resuelto (2026-11-24):** pasa a
      Atendida con renglón de bitácora (ver "Limitaciones conocidas").
13. **HU-5.3 / D10 — RESUELTA (2026-11-24):** el CETS marca la exclusión desde la ficha del donador (ver "Limitaciones conocidas").
    Falta que el `.vpp` refleje el método nuevo (`PersonalCETS.registrarPadecimiento` sigue sin secuencia). Texto original: **ninguna HU registra `HistorialClinico`**. `PersonalCETS.registrarPadecimiento` no tiene
    secuencia, así que nadie marca `Usuario.exclusionPermanente` desde la aplicación. Por ahora solo se marca con los
    `INSERT`/`UPDATE` de prueba. Decidir qué HU e integrante lo implementa.
14. **HU-5.4:** la vista muestra un código "DON-OAX-2024-984" que no existe en la BD, y
    `buscarDonador(String criterio)` no define si busca por nombre, correo o tipo.
15. **HU-7.3:** `ControlDonacion.contarRegistros(List<HistorialExterno>)` solo hace `size()`. Se resolvió en la vista
    con `empty`; decidir si se quita del diagrama.
16. **HU-6.2:**
    - El nombre dice "solicitudes activas", pero la vista filtra por cualquier estatus, con contadores fijos.
    - La vista combina tipo y Rh en un solo combo (`O+`) y el diagrama los maneja por separado.
17. **HU-6.3 — RESUELTA (2026-11-24):** `detalleSolicitud` lista nombre y tipo de sangre de los donadores que confirmaron interés
    (ver "Limitaciones conocidas"). El `.vpp` no tiene la operación `obtenerInteresados`. Texto original: el título habla de "detalle de **donadores** vinculados", pero la secuencia solo obtiene la
    **cantidad** de interesados. Definir si el CETS debe ver la lista (nombres, sin ubicación). Mientras tanto
    `detalleSolicitud` muestra solo la cantidad; se retiró la tabla de identificadores "DON-OAX-…" de la maqueta
    porque eran datos inventados.
18. **HU-6.4:**
    - `«create» HistorialEstatusSolicitud(solicitudId, personalId, …)` no indica de dónde sale `personalId`. Se toma de
      la sesión.
    - No se menciona `SolicitudReposicion.PersonalCETSUsuarioid`.
    - **Transiciones:** los diagramas solo dicen (nota del atributo `SolicitudReposicion.estatus`) que la solicitud
      nace "Activa" y que el personal del CETS la pasa a Atendida, Cerrada o Cancelada. No dicen si desde esos estados
      se puede seguir cambiando (¿Atendida → Cerrada?, ¿reabrir una Cerrada o Cancelada?). **Implementado:** se puede
      asignar Atendida, Cerrada o Cancelada desde cualquier estado, siempre distinto al actual; "Activa" nunca se asigna
      (no se puede reabrir). Falta que el equipo confirme si hace falta restringir más o permitir reabrir.
19. **Navegación general:**
    - El *Diagrama de navegación* no tiene transiciones para `donadores → capturarDonacion`, ni para
      `misSolicitudes → capturarSolicitud`, aunque las vistas tienen esos botones.
    - El botón "Descargar" de miHistorial y "Descargar imagen"/"Imprimir ficha" de publicación no tienen HU.
20. **HU-6.2 (vista `solicitudesActivas`) tiene elementos que el diagrama no define:**
    - El buscador "Buscar por folio u hospital": el diagrama solo filtra por estatus, tipo y Rh. Quedó deshabilitado.
    - Las tarjetas de resumen: "Solicitudes activas", "Donadores interesados" y "Sin donador interesado" se calculan
      sobre las solicitudes mostradas. La maqueta decía "interesados hoy" y "14% vs ayer"; se cambió el texto porque no
      hay dato que lo respalde.
    - La columna "Donadores interesados" de la tabla reutiliza `obtenerCantidadInteresados` de HU-6.3 (una consulta por
      fila). Decidir si el diagrama de HU-6.2 debe incluirla o si conviene una consulta agrupada.
    - El diagrama menciona `listarSolicitudesActivas()` y `filtrarSolicitudes(...)` como dos mensajes; en código son un
      solo camino (el primero llama al segundo con el filtro por defecto).
21. **HU-6.3 y HU-6.4 comparten la vista `detalleSolicitud`:** la HU-6.4 guarda la bitácora
    (`HistorialEstatusSolicitud`), pero ninguna HU define mostrarla. El bloque "Historial de cambios" de la maqueta se
    quitó porque eran datos de ejemplo; decidir si se agrega a HU-6.3 (con quién y cuándo cambió el estatus).
22. **Sesión CETS en dos lugares:** `plantillaInstitucional` verifica el rol en `preRenderView`, pero los `f:viewAction`
    corren antes. Por eso `PanelCetsController` vuelve a llamar `sesion.verificarCETS()` antes de consultar la BD. No se
    agregó un segundo `f:event` en cada vista para no intentar redirigir dos veces en la misma respuesta (la plantilla
    ya lo hace). **Mejora opcional** (un filtro en `web.xml` para todo `/vista/cets/*`); **se deja como está**.
23. **HU-6.4, firmas del diagrama:**
    - `actualizarEstatus(int solicitudId, String nuevoEstatus)` pasa a `actualizarEstatus(SolicitudReposicion s)` (regla de
      la entidad completa). El `personalId` de `«create» HistorialEstatusSolicitud(...)` no dice de dónde sale; se toma
      de la sesión y viaja en `s.personalCETSUsuarioid`.
    - `validarEstatus(String nuevoEstatus)` solo recibe el estatus nuevo, pero para rechazar "el mismo estatus" también
      necesita el actual; en código es `validarEstatus(actual, nuevo)`.
24. **HU-6.4, mensaje de error genérico:** `actualizarEstatus` regresa `boolean`, así que el Ln no puede decir por qué
    falló (estatus no asignable, igual al actual, solicitud inexistente o personal inactivo). La vista muestra un solo
    mensaje con la regla. Si se quieren mensajes distintos, habría que cambiar el tipo de retorno en el diagrama.
25. **HU-6.4, efecto sobre los donadores — RESUELTA (2026-11-24).** Una solicitud no Activa ya se oculta en la publicación pública y
    en los avisos del donador. Texto original: la ayuda de la maqueta dice que el estatus "define si la solicitud sigue
    visible para los donadores", pero ningún diagrama lo define. Hoy cambiar el estatus solo afecta el listado del
    panel CETS; falta decidir si la publicación pública y las notificaciones deben ocultar las solicitudes no activas.
26. **Vista expirada o sin sesión en un POST — RESUELTA.** Enviar un formulario con un `ViewState` inválido (por ejemplo,
    con la sesión vencida) devolvía la página de error 500 del servidor, aunque no cambiaba ningún dato. Pasaba en
    cualquier formulario JSF del proyecto.
    - **Solución:** un `<error-page>` para `jakarta.faces.application.ViewExpiredException` en `web.xml`, que lleva a
      `web/vista/publico/sesionExpirada.xhtml` ("Tu sesión expiró"). Es una página pública (no verifica sesión) con dos
      botones: "Iniciar sesión" (`cuentas/iniciarSesion`) y "Acceso personal CETS" (`cets/loginInstitucional`).
    - **Cómo distingue el origen:** `web.xml` no puede elegir la página según la URL, así que es la propia página la que
      lee `jakarta.servlet.error.request_uri`: si venía de `/vista/cets/` destaca el acceso institucional; si no, el de
      usuario. Abierta directamente, destaca el de usuario.
    - **Verificado por HTTP:** un POST con `ViewState` inválido sin sesión a `miPerfil`, `detalleSolicitud` y
      `solicitudesActivas` muestra la página, destaca el acceso correcto y **no cambia ningún dato**. Con una sesión CETS
      válida muestra la página pero **no la cierra**. La protección de sesión (`verificar` / `verificarCETS`) sigue
      igual: sin sesión, un GET a una vista de usuario o CETS redirige a su login, y un usuario con el rol equivocado
      también.
    - **Limitaciones (no bloqueantes):**
      - El **código HTTP sigue siendo 500**: el contenedor conserva el estatus de la excepción. El usuario sí ve la
        página amigable, pero el servidor sigue registrando la excepción en el log. Quitar ambas cosas requeriría un
        `ExceptionHandler` de JSF que redirija (mejora opcional, no se hizo).
      - No hizo falta cambiar `STATE_SAVING_METHOD` ni ningún parámetro de Faces: la página carga aunque el POST traiga
        un `ViewState` vencido.
      - PrimeFaces lee este mapeo y lo escribe como `errorPages` en su configuración del cliente, así que también
        debería cubrir las peticiones AJAX vencidas (por ejemplo, el paginador de `solicitudesActivas`). Por HTTP solo se
        vio la respuesta parcial de error del servidor; **falta verificar en el navegador** que PrimeFaces redirija.
27. **HU-1.3 / HU-3.4, controller:** el diagrama pone todo en `ControlCuentas`, pero `CuentasController` es
    `@RequestScoped` (registro e inicio de sesión) y miPerfil necesita conservar el perfil entre clics. Se creó
    `PerfilController` (`@ViewScoped`, D16); ambos controllers llaman a `LnCuentas`. Decidir si el diagrama de clases
    debe mostrar los dos controllers.
28. **Métodos de `LnCuentas` que no están en el diagrama:**
    - `obtenerPerfilClinico(Usuario)`: lee el perfil y el usuario actualizados (la sesión puede estar desactualizada).
    - `receptoresCompatibles(PerfilClinico)`: tabla donador → receptores de glóbulos rojos para el texto "puedes
      donar a...". **[Alex]** puede reutilizarla en `validarCompatibilidad` (HU-3.1) en lugar de repetir la tabla.
29. **HU-1.3, reglas que el diagrama no dice:**
    - El diagrama solo contempla el alta (`opt[perfilClinico == null]`). Se implementó crear **o actualizar**.
    - Validaciones de ubicación: latitud y longitud juntas, dentro de rango y redondeadas a 6 decimales.
    - Enviar coordenadas vacías **no borra** la ubicación guardada, así que hoy el donador no puede quitarla (ninguna HU
      lo pide). Decidir si hace falta un "Quitar mi ubicación".
    - `guardarPerfilClinico` también guarda `Usuario.consentimientoContacto` (no está en la secuencia, ver #6 y D8).
30. **Dos toggles de contacto en miPerfil (cerrada, D19):** quedó un solo toggle, "Compartir mi contacto solo cuando yo
    confirme interés", ligado a `consentimientoContacto`; se eliminó el deshabilitado.
31. **Precisión de la ubicación:** se guardan las coordenadas exactas del dispositivo (6 decimales, ~0.1 m). La maqueta
    decía "tu dirección exacta nunca se guarda ni se muestra"; se cambió la nota porque ya no es cierto del todo (nunca se
    muestra ni se comparte; solo se informa una distancia aproximada, HU-3.2). Decidir si conviene guardarlas con menos
    precisión (por ejemplo, 3 decimales ≈ 110 m) para proteger al donador.
32. **Geolocalización y HTTPS:** el navegador solo da la ubicación en `localhost` o con HTTPS. Un despliegue real del CETS
    sin HTTPS no podrá capturar ubicaciones; hay que contemplarlo en el despliegue.
33. **UX de errores en miPerfil:** tras un rechazo, la vista vuelve a leer la BD y descarta lo que el usuario había elegido
    (los combos regresan a lo guardado). Es consistente con HU-6.4, pero se puede mejorar conservando la captura.
34. **Caché compartido de EclipseLink y SQL directo en MySQL:** `datos_prueba.sql`, `limpiar_datos_prueba.sql` y cualquier
    `UPDATE`/`DELETE` hecho a mano modifican la BD por fuera de la aplicación y **no invalidan el caché compartido**
    del servidor. Con la app desplegada puede mostrar datos viejos: `find()` y las consultas JPQL devuelven la entidad
    que ya tienen en memoria (por ejemplo, una solicitud con el estatus anterior), y un id borrado y reutilizado
    puede traer los datos "fantasma" del anterior (por ejemplo, el perfil clínico de un usuario ya borrado). Mitigación
    actual: **redesplegar la app (Run en NetBeans) después de correr esos scripts**. Opción de fondo, que toca un archivo
    compartido y debe decidir el equipo: `<shared-cache-mode>NONE</shared-cache-mode>` en `persistence.xml`. Tampoco
    conviene reiniciar el contador de ids (`ALTER TABLE ... AUTO_INCREMENT = 1`) después de borrar usuarios de prueba.
35. **Caché compartido y referencias inversas (`Usuario.perfilClinico`):** `Usuario.perfilClinico` es el lado inverso
    (`mappedBy = "usuario"`) de la relación con `PerfilClinico`. Al guardar el perfil, `guardarPerfilClinico` solo creaba el
    `PerfilClinico`: el `Usuario` que EclipseLink tenía en su caché compartido seguía con `perfilClinico = null` hasta
    redesplegar. Efecto visible: un donador que acababa de guardar su perfil aparecía sin grupo sanguíneo ("—") en la
    búsqueda de donadores del CETS (`donadores.xhtml`), aunque la BD sí tenía el perfil (la consulta filtra bien en SQL,
    pero la entidad que devuelve viene del caché).
    - **Arreglo:** `LnCuentas.guardarPerfilClinico` asigna `usuario.setPerfilClinico(perfil)` después de crear o actualizar
      el perfil. El consentimiento no necesita lo mismo: es un campo del propio `Usuario` administrado, que ya se actualiza
      en el caché al terminar la transacción.
    - **Estado actual:** las referencias inversas de `Interes` (`usuario.interesList`, `solicitud.interesList`), `Notificacion` y
      `CuestionarioRespuesta` ya se actualizan en el Ln (`LnNotificacion`, `LnCuestionario`), igual que `RegistroDonacion`
      (`usuario`, `solicitud` y personal) en `LnDonacion`. Ninguna vista lee todavía esas listas: hoy no hay efecto visible.
    - **Regla para el equipo:** en relaciones bidireccionales, al crear o cambiar el lado dueño hay que actualizar también
      el lado inverso en el mismo método del Ln (como ya hace `LnDonacion.registrarDonacion` con `registroDonacionList`).
      Revisar las demás: `Usuario.notificacionList` / `interesList`, `SolicitudReposicion.interesList` e
      `historialEstatusSolicitudList`, etc.
    - Relacionada con #34 (cambios por SQL directo): ambas son formas de que el caché quede desactualizado.
36. **HU-2.x, 5.x, 7.3 y 1.4: métodos y firmas que difieren de los diagramas (todos aceptados):**
    - `LnSolicitud.listarHospitales()` y `LnDonacion.obtenerDonador(Usuario)` no están en el diagrama (la ficha del donador
      es `UIFichaDonador.mostrar`).
    - **HU-5.2 / 5.3 ([Diego]):** métodos extra que no están en los diagramas: `LnDonacion.obtenerProximaFecha`,
      `diasDesdeUltimaDonacion` y `porcentajeRecuperacion` (datos de la tarjeta de recuperación). `HistorialController` es
      `@RequestScoped` **a propósito**: solo consulta en el `f:viewAction`. Si se agrega AJAX (botones o paginador), cambiarlo a
      `@ViewScoped` con `Serializable`.
    - **D18 ([David]):** `LnDonacion.motivoRechazoDonacion(Usuario, SolicitudReposicion)` y su sobrecarga
      `motivoRechazoDonacion(RegistroDonacion)` (esta última también revisa "ya donó ese día"). `registrarDonacion` conserva su
      firma y regresa `false` al rechazar, igual que antes; el controller usa el motivo para mostrarlo y deshabilitar el botón.
      `LnDonacion` inyecta ahora `PerfilClinicoFacade` y `LnCuentas` (sin dependencia circular: `LnCuentas` solo usa facades).
    - `LnSolicitud.validarFormatoFolio` es público (en el diagrama es de la entidad) y `verificarFolioUnico` quedó dentro de
      `validarFolio`.
    - **HU-3.1 a 4.3 ([Alex]):** métodos extra que no están en los diagramas: `LnNotificacion.listarMisNotificaciones`,
      `LnNotificacion.obtenerSolicitud` (la vista de confirmar interés y la del cuestionario necesitan los datos de la
      solicitud) y `LnCuestionario.obtenerCatalogo` (el catálogo completo, con inactivos). `CuestionarioController` inyecta
      `LnNotificacion` solo para usar `obtenerSolicitud` (aceptado; si se prefiere, moverlo a `LnSolicitud`). También se
      agregaron `LnNotificacion.motivoRechazoInteres` (por qué no se puede confirmar interés; la usan `registrarInteres` y
      el controller para avisar y deshabilitar el botón) y `SolicitudReposicionFacade.crearYObtenerId`.
    - `generarPublicacion` regresa la **ruta** pública y no el `imagenQR` (D5); `generarQR`, `obtenerUuidPublico` y
      `ocultarDatosContacto` no existen como métodos. `contarRegistros` (HU-7.3) se resolvió en la vista (#15).
37. **Enmascarado de contacto en el panel CETS (no está en el diagrama):** `DonacionController.enmascararCorreo` y
    `enmascararTelefono` ocultan el correo y el teléfono del **donador** en `donadores` y `capturarDonacion`. La HU-1.4 trata el
    contacto del **familiar** en la publicación (eso sí se cumple: la vista no lo pinta). Decidir si el CETS no debe ver el
    contacto de los donadores (¿cómo los contactará?) y, de mantenerlo, mover el enmascarado al Ln o a un convertidor.
38. **TODOs pendientes de otros módulos (cerrada):** HU-3.1 quedó conectada en `LnSolicitud.registrarSolicitud` ([David]; el
    aviso va después de guardar y un fallo al avisar no tumba la solicitud, aunque un error de BD al confirmar la
    transacción sí la revertiría). La ficha del donador (HU-5.4 pasos 3 y 4) ya muestra historial y elegibilidad con
    `LnDonacion.obtenerHistorial`, `obtenerElegibilidad`, `obtenerProximaFecha`, `verificarExclusion` y `esElegible`
    ([David], rama `feature/ficha-donador`); la ficha ya no lee `Usuario.registroDonacionList` (caché, #35).
39. **URL del QR:** el controller la arma con el esquema, el servidor y el puerto de la petición (`ec.getRequestServerName()`,
    cabecera Host) y la vista la escribe dentro de un `<script>` sin escapar para JavaScript. Riesgo bajo (solo se refleja en
    la petición de quien la manipula), pero detrás de un proxy o HTTPS el esquema puede salir mal. Pendiente: configurar la URL
    base o leerla de un atributo HTML escapado.
40. **`obtenerPublicacion` regresa la entidad completa** (con `usuarioid`: nombre, teléfono, correo y `passwordHash` del
    familiar). Hoy es seguro solo porque `publicacion.xhtml` no lo pinta; cualquier EL nuevo que lo toque lo expondría
    (aceptado en HU-1.4 por la regla de no modificar la entidad). `solicitudes/interesados` (D19) también depende de pintar solo
    campos sueltos de `Interes.usuario` (nombre, tipo de sangre y, con consentimiento, teléfono y correo).
41. **`buscarDonadores` no escapa `%` ni `_`:** la consulta está parametrizada (sin inyección SQL), pero buscar `%` lista a
    todos los donadores con perfil. Solo lo usa el CETS. Pendiente.
42. **Donaciones duplicadas y regla de 56 días (cerrada):** `LnDonacion.registrarDonacion` valida contra la **fecha de la donación** del
    registro y no contra hoy (`motivoRechazoDonacion(RegistroDonacion)`): la fecha no puede ser futura; no puede repetir un día en
    que el donador ya donó; y debe quedar a **56 días o más de cualquier donación existente, en ambos sentidos** (una donación
    con fecha pasada tampoco puede caer a menos de 56 días de una posterior). El mensaje indica desde qué fecha podría donar.
    `esElegible(Usuario)` no cambia: sigue comparando contra **hoy** (lo usan `registrarInteres` y la ficha). La sobrecarga
    `motivoRechazoDonacion(Usuario, SolicitudReposicion)`, que usan las vistas antes de tener la fecha, también compara contra
    hoy: un donador dentro de los 56 días de hoy ve el botón "Registrar donación" deshabilitado aunque exista una fecha pasada
    válida para él. Aceptado; si hace falta registrar donaciones antiguas, habilitar el botón y dejar que decida el Ln.
43. **Validación en el Ln de `registrarSolicitud` y `registrarDonacion`:** se agregaron (grupo ∈ {A,B,AB,O}, Rh ∈ {+,−},
    hospital y usuario existentes; donación con donador y personal CETS **activo** existentes, y solicitud asociada Activa).
    No están en los diagramas; los diagramas tampoco dicen qué mensaje mostrar cuando el Ln regresa `false`, así que los
    controllers muestran un mensaje general.
44. **Badge "3" del menú lateral del donador (cerrada):** se quitó el `<span class="menu-contador">3</span>` fijo de
    `plantillaUsuario.xhtml`; quedan el ícono y el enlace a Notificaciones. Si más adelante se quiere un contador real, debe
    salir de `LnNotificacion.listarMisNotificaciones` (avisos de solicitudes Activas).
45. **Fechas con 6 horas de diferencia (cerrada):** causa: `f:convertDateTime` sin `timeZone` convierte en **UTC** y Java guarda
    la hora en la zona del sistema (`America/Mexico_City`); solo `notificaciones.xhtml` (`Notificacion.fechaEnvio`) no tenía
    `timeZone` y mostraba, por ejemplo, 07:25 para un aviso de las 01:25. Todas las entidades usan `java.util.Date`; no hay
    `LocalDate` ni `LocalDateTime`. Arreglo:
    - **Con hora** (`@Temporal(TIMESTAMP)`: `fechaEnvio`, `fechaHoraRegistro`): `timeZone="America/Mexico_City"` explícito
      (es un instante, así que es correcto sin importar la zona de la JVM). Se agregó a `notificaciones.xhtml`.
    - **Solo día** (`@Temporal(DATE)`: `fechaDonacion`, `proximaFechaElegible`, `fechaRegistro`): **sin** `timeZone`, para que se
      lean con la misma zona con la que JDBC las construyó y nunca cambien de día. Se quitó `timeZone="UTC"` de `donadores.xhtml`
      y `timeZone="America/Mexico_City"` de `miHistorial.xhtml`; ahora la misma fecha se ve igual en ambas.
    - `web.xml`: `jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE=true`, para que un convertidor sin
      `timeZone` use la zona del sistema y no UTC. Se eligió esta combinación (y no solo el parámetro) porque un `DATE` leído por
      JDBC es medianoche en la zona de la JVM: con `America/Mexico_City` explícito se movería un día si el servidor corriera en
      UTC, y con el parámetro las fechas de solo día siguen a la JVM.
    - **Seed:** `sql/datos_prueba.sql` ahora empieza con `SET time_zone = '-06:00';` (México, sin horario de verano desde 2022), así
      que los `NOW()` del seed coinciden con la hora que muestran las vistas. `limpiar_datos_prueba.sql` no necesita el cambio
      (no usa `NOW()` ni `CURDATE()`). Las fechas creadas desde la aplicación no dependen de la zona de MySQL.
    - Regla para vistas nuevas: fecha con hora → `timeZone="America/Mexico_City"`; fecha de solo día → sin `timeZone`.
