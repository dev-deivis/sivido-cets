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
| Todos | Solicitudes en la BD | [Galilea] HU-2.1 | Cargar los `INSERT` de prueba de 2.2 |
| Todos los que usan `f:viewParam` | `<ui:insert name="metadata"/>` en `plantillaBase.xhtml` | [DAVID] T0.1 | — (son 5 minutos; DAVID lo sube a `main` el día 1) |
| [Alex] HU-3.3 | `LnDonacion.esElegible(Usuario)` | [Diego] | Un *stub* local que regrese `true` (no subirlo) |
| [Galilea] HU-5.4 | `LnDonacion.obtenerHistorial(Usuario)`, `obtenerElegibilidad(Usuario)`, `verificarExclusion(Usuario)` | [Diego] | Mostrar solo la lista de donadores |
| [Galilea] HU-5.1 | `LnPanelCETS.listarSolicitudesActivas()` | [DAVID] HU-6.2 | `INSERT` de prueba + combo vacío |
| [Galilea] HU-2.1 (paso final) | `LnNotificacion.buscarDonadoresCercanos(SolicitudReposicion)` | [Alex] HU-3.1 | Dejar `// TODO HU-3.1` en `LnSolicitud.registrarSolicitud` |
| [Galilea] HU-2.2 / HU-1.4 | `LnSolicitud.obtenerPublicacion(...)` | [Galilea] (ambas HU, D8) | — |
| [Alex] HU-3.1 | Donadores con `latitud/longitud` (botón "Usar mi ubicación", D1) | [DAVID] HU-1.3 | `INSERT` de prueba de 2.1 (903 y 904 ya traen coordenadas) |

### Datos de prueba por módulo

Corre los `INSERT` en este orden, solo una vez y sobre una BD recién creada (sección 6). Usan IDs **901+**
para no chocar con lo que se registre desde la aplicación.

**Contraseñas de prueba**, en SHA-256 sin sal, igual que `LnCuentas.hash()`:

| Cuenta | Contraseña | Hash SHA-256 |
|---|---|---|
| Personal CETS | `Cets2026!` | `b4eb84f0c1d0ee7d6a1cd590e626a405a50e2a005dc68683543bc93762cd8525` |
| Familiar y donadores | `Donador2026!` | `555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4` |

#### 2.1 Comunes (los carga [DAVID] en T0; todos los necesitan)

```sql
USE sivido;

-- Catálogo de hospitales (HU-2.1). Coordenadas aproximadas de la ciudad de Oaxaca.
INSERT INTO Hospital (id, nombre, latitud, longitud) VALUES
 (1, 'Hospital General "Dr. Aurelio Valdivieso" (Hospital Civil)', 17.070200, -96.718700),
 (2, 'Hospital Regional de Alta Especialidad de Oaxaca (HRAEO)',    16.963700, -96.712000),
 (3, 'Hospital General de Zona No. 1 IMSS Oaxaca',                 17.079000, -96.709000),
 (4, 'Hospital Regional Presidente Juárez ISSSTE',                 17.080700, -96.710600),
 (5, 'Hospital de la Niñez Oaxaqueña',                             16.970000, -96.718000);

-- Personal CETS (HU-6.x, HU-5.1). Login institucional: cets@cets.oaxaca.gob.mx / Cets2026!
INSERT INTO Usuario (id, nombreCompleto, telefono, correo, passwordHash, fechaRegistro) VALUES
 (901, 'Personal CETS Prueba', '9510000001', 'personal.cets@prueba.mx',
  'b4eb84f0c1d0ee7d6a1cd590e626a405a50e2a005dc68683543bc93762cd8525', CURDATE());
INSERT INTO PersonalCETS (Usuarioid, rol, correoInstitucional, activo) VALUES
 (901, 'Personal', 'cets@cets.oaxaca.gob.mx', TRUE);

-- Familiar que crea solicitudes y dos donadores. Login de usuario: <correo> / Donador2026!
INSERT INTO Usuario (id, nombreCompleto, telefono, correo, passwordHash, fechaRegistro) VALUES
 (902, 'Familiar Prueba',     '9510000002', 'familiar@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE()),
 (903, 'Donador Cercano O-',  '9510000003', 'donador1@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE()),
 (904, 'Donador Lejano A+',   '9510000004', 'donador2@prueba.mx',
  '555d0ba5699947a8381c09a09a0c93e591e12623ccbf5ba7ff6e89f74528b5d4', CURDATE());

-- Perfiles clínicos (PK compartida con Usuario). 903 está a ~1 km del Hospital Civil; 904 está en el Istmo.
INSERT INTO PerfilClinico (Usuarioid, tipoSanguineo, factorRh, fechaRegistroPerfil, radioNotificacion, latitud, longitud) VALUES
 (903, 'O', '-', CURDATE(), 5, 17.065000, -96.720000),
 (904, 'A', '+', CURDATE(), 3, 16.200000, -95.200000);
```

#### 2.2 Solicitudes (para [DAVID], [Alex] y [Diego] mientras [Galilea] termina HU-2.1)

```sql
INSERT INTO SolicitudReposicion
 (id, folioCETS, tipoSanguineo, factorRh, descripcion, estatus, fechaHoraRegistro, uuidPublico, Usuarioid, Hospitalid) VALUES
 (901, 'CETS-OAX-2026-0901', 'O', '-', 'Cirugía programada, se requieren 3 donadores', 'Activa',   NOW(),                    UUID(), 902, 1),
 (902, 'CETS-OAX-2026-0902', 'A', '+', 'Accidente vial',                               'Activa',   NOW() - INTERVAL 1 DAY,   UUID(), 902, 2),
 (903, 'CETS-OAX-2026-0903', 'B', '+', 'Solicitud ya atendida',                        'Atendida', NOW() - INTERVAL 3 DAY,   UUID(), 902, 3);
```

#### 2.3 Notificaciones y cuestionario ([Alex])

```sql
INSERT INTO CriterioExclusion (id, criterio, tipoRespuesta, activo, tipoExclusion, duracionDias) VALUES
 (901, '¿Te hiciste un tatuaje o perforación en los últimos 12 meses?', 'SiNo', TRUE,  'Temporal',   365),
 (902, '¿Has tenido hepatitis B o C?',                                  'SiNo', TRUE,  'Permanente', NULL),
 (903, '¿Tomaste antibióticos en los últimos 7 días?',                  'SiNo', TRUE,  'Temporal',   7),
 (904, '¿Pesas menos de 50 kg?',                                        'SiNo', FALSE, 'Temporal',   0);

INSERT INTO Notificacion (distanciaAproximada, estatus, fechaEnvio, Usuarioid, SolicitudReposicionid) VALUES
 (1.00, 'Enviada', NOW(), 903, 901);
```

#### 2.4 Donaciones e historial ([Diego] y [Galilea])

```sql
-- 903: donó hace 200 días → ya es elegible.  904: donó hace 20 días → todavía no.
INSERT INTO RegistroDonacion (fechaDonacion, proximaFechaElegible, Usuarioid, PersonalCETSUsuarioid, SolicitudReposicionid) VALUES
 (CURDATE() - INTERVAL 200 DAY, CURDATE() - INTERVAL 144 DAY, 903, 901, NULL),
 (CURDATE() - INTERVAL 20 DAY,  CURDATE() + INTERVAL 36 DAY,  904, 901, 903);

-- 904 tiene un padecimiento con exclusión permanente (requiere los criterios de 2.3).
INSERT INTO HistorialClinico (diagnostico, fechaDeteccion, observaciones, Usuarioid, PersonalCETSUsuarioid, CriterioExclusionid) VALUES
 ('Hepatitis C', CURDATE() - INTERVAL 1 YEAR, 'Dato de prueba', 904, 901, 902);
-- D10: la fuente única es Usuario.exclusionPermanente; se marca al registrar un padecimiento con criterio Permanente.
UPDATE Usuario SET exclusionPermanente = TRUE WHERE id = 904;

-- Historial externo (HU-7.3)
INSERT INTO InstitucionExterna (id, nombreInstitucion, tipoConexion, contactoTecnico, activa) VALUES
 (901, 'Banco de Sangre externo (prueba)', 'API REST', 'soporte@prueba.mx', TRUE);
INSERT INTO HistorialExterno (Usuarioid, InstitucionExternaid, fechaDonacion, observaciones) VALUES
 (903, 901, '2025-03-10', 'Donación registrada en otra institución');
```

#### Borrar los datos de prueba (si los quieres recargar)

```sql
DELETE FROM HistorialExterno       WHERE Usuarioid >= 901;
DELETE FROM InstitucionExterna     WHERE id >= 901;
DELETE FROM HistorialClinico       WHERE Usuarioid >= 901;
DELETE FROM RegistroDonacion       WHERE Usuarioid >= 901;
DELETE FROM RespuestaCriterio      WHERE CriterioExclusionid >= 901;
DELETE FROM CuestionarioRespuesta  WHERE Usuarioid >= 901;
DELETE FROM Interes                WHERE Usuarioid >= 901;
DELETE FROM Notificacion           WHERE Usuarioid >= 901;
DELETE FROM CriterioExclusion      WHERE id >= 901;
DELETE FROM HistorialEstatusSolicitud WHERE SolicitudReposicionid >= 901;
DELETE FROM SolicitudReposicion    WHERE id >= 901;
DELETE FROM PerfilClinico          WHERE Usuarioid >= 901;
DELETE FROM PersonalCETS           WHERE Usuarioid >= 901;
DELETE FROM Usuario                WHERE id >= 901;
```

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

- [ ] **T0.1** Agregar `<ui:insert name="metadata"/>` en `-war/web/plantillas/plantillaBase.xhtml`, justo antes de
      `<h:head>`. Así cualquier vista puede definir `<ui:define name="metadata"><f:metadata>…`, aun con las
      plantillas anidadas (`plantillaUsuario`, `plantillaInstitucional` y `plantillaPublica` heredan de `plantillaBase`).
- [ ] **T0.2** Subir el script de la BD al repo, en `sql/SIVIDO_CETS_mysql.sql`, junto con los `INSERT` de la
      sección 2 en `sql/datos_prueba.sql`, para que todos usen el mismo.
- [ ] **T0.3** Cargar los datos comunes (2.1) y de solicitudes (2.2), y comprobar el login CETS con
      `cets@cets.oaxaca.gob.mx` / `Cets2026!`.
- [ ] **T0.4** Merge a `main` y avisar al equipo.

---

#### HU-6.2 Visualizar solicitudes activas

**Objetivo:** el personal CETS ve la lista de solicitudes activas y la puede filtrar por estatus, tipo sanguíneo y Rh.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `UIPanelCETS → ControlPanelCETS.listarSolicitudesActivas(): List<SolicitudReposicion>` | `f:viewAction="#{panelCetsController.listarSolicitudesActivas}"` → `lnPanelCETS.listarSolicitudesActivas()` |
| 1.1 | `ControlPanelCETS → DBMS: consultarSolicitudesActivas()` → `return solicitudes` | `solicitudF.buscarActivas()` |
| 2 | `PersonalInstitucional → UIPanelCETS.filtrar(estatus, tipoSanguineo, factorRh)` | Botón "Refrescar" → `#{panelCetsController.filtrarSolicitudes}` |
| 2.1 | `UIPanelCETS → ControlPanelCETS.filtrarSolicitudes(String estatus, String tipoSanguineo, String factorRh)` | `lnPanelCETS.filtrarSolicitudes(filtro)` |
| 2.1.1 | `ControlPanelCETS → DBMS: consultarSolicitudesFiltradas(estatus, tipoSanguineo, factorRh)` → `return solicitudes` | `solicitudF.buscarFiltradas(filtro)` |

**Firmas corregidas (LnPanelCETS)**

```java
public List<SolicitudReposicion> listarSolicitudesActivas()                       // 1 línea a la facade (permitido)
public List<SolicitudReposicion> filtrarSolicitudes(SolicitudReposicion filtro)   // filtro.estatus / tipoSanguineo / factorRh; null = todos
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
public List<SolicitudReposicion> buscarActivas() {
    // JPQL: SELECT s FROM SolicitudReposicion s WHERE s.estatus = 'Activa' ORDER BY s.fechaHoraRegistro DESC
}
public List<SolicitudReposicion> buscarFiltradas(SolicitudReposicion f) {
    // JPQL: SELECT s FROM SolicitudReposicion s
    //       WHERE (:estatus IS NULL OR s.estatus = :estatus)
    //         AND (:tipo IS NULL OR s.tipoSanguineo = :tipo)
    //         AND (:rh IS NULL OR s.factorRh = :rh)
    //       ORDER BY s.fechaHoraRegistro DESC
}
```

**Vista:**

- Cambia las 6 filas fijas de la tabla por un `ui:repeat value="#{panelCetsController.solicitudes}" var="s"`.
  Deja las mismas `<tr>`/`<td>` y clases.
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

- [ ] `buscarActivas()` y `buscarFiltradas(...)` en `SolicitudReposicionFacade`
- [ ] `listarSolicitudesActivas()` y `filtrarSolicitudes(SolicitudReposicion)` en `LnPanelCETS`
- [ ] Controller: lista, filtro y método para el `f:viewAction`
- [ ] Vista: `ui:repeat`, combos separados, `f:param id`, sin datos fijos
- [ ] Pruebas en navegador + SQL
- [ ] Regresión (sección 8)

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
| 2.1 | `ControlPanelCETS → DBMS: consultarSolicitudPorId(solicitudId)` → `return solicitud` | `solicitudF.find(s.getId())` |
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

**Checklist**

- [ ] `contarPorSolicitud` en `InteresFacade`
- [ ] `obtenerDetalle` y `obtenerCantidadInteresados` en `LnPanelCETS`
- [ ] `f:viewParam` y `f:viewAction` en la vista (requiere T0.1)
- [ ] Vista sin datos fijos y sin ubicación
- [ ] Pruebas + SQL

---

#### HU-6.4 Actualizar estatus de una solicitud

**Objetivo:** el CETS cambia el estatus de una solicitud (Activa / Atendida / Cerrada / Cancelada) y queda bitácora
de quién lo cambió y cuándo.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `PersonalInstitucional → UIDetalleSolicitud.cambiarEstatus(int solicitudId, String nuevoEstatus)` | Combo `nuevoEstatus` + botón "Guardar cambio de estatus" |
| 1.1 | `UIDetalleSolicitud → ControlPanelCETS.actualizarEstatus(int solicitudId, String nuevoEstatus): boolean` | `lnPanelCETS.actualizarEstatus(historial)` |
| 1.1.1 | `ControlPanelCETS → ControlPanelCETS.validarEstatus(String nuevoEstatus): boolean` → `return valido` | Validación privada en el Ln: uno de los 4 valores y distinto del actual |
| 1.1.2 | `alt[valido == true]` `DBMS: consultarSolicitudPorId(solicitudId)` → `return solicitud` | `solicitudF.find(...)` |
| 1.1.3 | `SolicitudReposicion.actualizarEstatus(String nuevoEstatus)` | **En el Ln:** `s.setEstatus(nuevo)` y `s.setPersonalCETSUsuarioid(personal)` |
| 1.1.4 | `DBMS: actualizarSolicitud(solicitud)` → `return true` | `solicitudF.edit(s)` |
| 1.1.5 | `«create» HistorialEstatusSolicitud(solicitudId, personalId, estatusAnterior, estatusNuevo)` | El controller arma la entidad `HistorialEstatusSolicitud`; el Ln pone `estatusAnterior` y `fechaCambio` |
| 1.1.6 | `DBMS: guardarHistorialEstatus(historial)` → `return true` | `historialEstatusF.create(h)` |
| — | `alt[valido == false]` `return false` | Mensaje de error en la vista |

**Firma corregida (LnPanelCETS).** La entidad completa es la bitácora:

```java
// h.solicitudReposicionid (con id), h.estatusNuevo, h.personalCETSUsuarioid (de la sesión)
public boolean actualizarEstatus(HistorialEstatusSolicitud h)
```

El personal en sesión es `sesion.getUsuario().getPersonalCETS()` (relación `@OneToOne` de `Usuario`), así que
no hay que cambiar `SesionController`.

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnPanelCETS.java` (`@EJB HistorialEstatusSolicitudFacade historialEstatusF`) |
| Modificar | `-war/src/java/controller/PanelCetsController.java` (`HistorialEstatusSolicitud cambio` y `actualizarEstatus()`) |
| Modificar | `-war/web/vista/cets/detalleSolicitud.xhtml` (`p:commandButton` en lugar de `p:button`) |

**Facade:** no necesita métodos nuevos (`find`, `edit`, `create`).

**Navegación:** se queda en `detalleSolicitud?id=…`, con mensaje de éxito o error.
En el diagrama de actividad (*AD - Panel CETS*): `DetalleSolicitud (HU-6.3) → ActualizarEstatus (HU-6.4) [cambiarEstatus == true]`.

**Terminado cuando**

- Cambiar la 901 a "Atendida" la saca de solicitudesActivas (HU-6.2) y crea un renglón de bitácora.
- Elegir el mismo estatus que ya tiene muestra un error y no inserta nada.
- SQL de verificación:
  ```sql
  SELECT estatus, PersonalCETSUsuarioid FROM SolicitudReposicion WHERE id = 901;          -- Atendida, 901
  SELECT estatusAnterior, estatusNuevo, PersonalCETSUsuarioid, fechaCambio
    FROM HistorialEstatusSolicitud WHERE SolicitudReposicionid = 901 ORDER BY id DESC;    -- Activa → Atendida
  ```

**Checklist**

- [ ] `actualizarEstatus(HistorialEstatusSolicitud)` + `validarEstatus` privado en `LnPanelCETS`
- [ ] Controller con la entidad `cambio` inicializada en el constructor (`cambio.setSolicitudReposicionid(new SolicitudReposicion())`)
- [ ] Vista con `p:commandButton` y `p:messages`
- [ ] Pruebas + SQL

---

#### HU-1.3 Perfil clínico del donador

**Objetivo:**

- El donador captura su tipo sanguíneo, su Rh y su **ubicación** (decisión D1). Sin perfil y sin coordenadas
  no le llegan notificaciones (HU-3.1).
- En la misma vista elige si se revela su contacto al confirmar interés (checkbox de HU-1.4, decisión D8).

**Secuencia (`.vpp`).** Todo va dentro de `opt[perfilClinico == null]`.

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → Dashboard.seleccionarPerfilClinico()` | Menú "Mi perfil" |
| 1.1 | `Dashboard → UIPerfilClinico.mostrar()` | `cuentas/miPerfil.xhtml` |
| 2 | `Donador → UIPerfilClinico.capturar(String tipoSanguineo, String factorRh)` | Combos `grupo` y `rh` → `#{cuentasController.perfil.tipoSanguineo}` y `.factorRh`; botón "Usar mi ubicación" → `perfil.latitud` y `perfil.longitud` (D1) |
| 2.1 | `UIPerfilClinico → ControlCuentas.guardarPerfilClinico(String tipoSanguineo, String factorRh): boolean` | `lnCuentas.guardarPerfilClinico(perfil)` |
| 2.1.1 | `ControlCuentas → Usuario.completarPerfilClinico(tipoSanguineo, factorRh)` | **En el Ln** (no en la entidad) |
| 2.1.1.1 | `Usuario → «create» PerfilClinico(tipoSanguineo, factorRh)` | El controller ya trae la entidad; el Ln pone `usuarioid` (PK compartida) y `fechaRegistroPerfil` |
| 2.1.1.2 | `PerfilClinico.marcarElegibilidadPreliminar(): boolean` → `return elegible` | Ver [Inconsistencias](#10-inconsistencias-a-revisar): no hay columna para "elegible" |
| 2.1.2 | `ControlCuentas → DBMS: insertarPerfilClinico(usuarioId, tipoSanguineo, factorRh)` → `return usuarioId` | `perfilF.create(p)`, o `perfilF.edit(p)` si ya existía; además `usuarioF.edit(p.getUsuario())` para guardar `consentimientoContacto` (D8) |
| — | `return true` → `UIPerfilClinico → Dashboard` | Mensaje "Perfil guardado" |

**Firma corregida (LnCuentas)**

```java
public boolean guardarPerfilClinico(PerfilClinico p)   // p.usuario = usuario en sesión (lo pone el controller)
```

Lógica del Ln:

1. `p.setUsuarioid(p.getUsuario().getId())`.
2. `p.setFechaRegistroPerfil(new Date())`.
3. Si `perfilF.find(id) == null` → `create`; si no → `edit`.
4. `usuarioF.edit(p.getUsuario())`, que guarda el checkbox `consentimientoContacto` (D8).

`latitud` y `longitud` pueden quedar en `null`; el perfil se guarda igual, pero ese donador **no** recibe
notificaciones hasta que comparta su ubicación (D1). El `radioNotificacion=5` ya viene del constructor de
`PerfilClinico`.

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnCuentas.java` (`@EJB PerfilClinicoFacade perfilF`) |
| Modificar | `-war/src/java/controller/CuentasController.java` (`PerfilClinico perfil`; carga el perfil de `sesion.getUsuario().getPerfilClinico()` o crea uno nuevo con `setUsuario(sesion.getUsuario())`) |
| Modificar | `-war/web/vista/cuentas/miPerfil.xhtml` |

**Facade:** no necesita métodos nuevos (`find`, `create`, `edit`).

**Vista miPerfil**

- **Datos personales:** muestra `#{sesionController.usuario.nombreCompleto}`, correo y teléfono, en solo lectura
  (ninguna HU cubre la edición).
- **Combos grupo/rh:** se enlazan a `cuentasController.perfil`.
- **Ubicación (D1):**
  - Quita el combo "Región". En su lugar va un botón **"Usar mi ubicación"** con las mismas clases CSS
    (`type="button"`, no envía el formulario).
  - El botón llama a la Geolocation API del navegador y llena dos `h:inputHidden`
    (`#{cuentasController.perfil.latitud}` y `#{cuentasController.perfil.longitud}`; JSF convierte a `BigDecimal`).
  - Muestra un texto "Ubicación registrada", **sin** mostrar las coordenadas.
  ```html
  <h:inputHidden id="lat" value="#{cuentasController.perfil.latitud}"/>
  <h:inputHidden id="lng" value="#{cuentasController.perfil.longitud}"/>
  <script>
    function usarMiUbicacion() {
      navigator.geolocation.getCurrentPosition(function (pos) {
        document.getElementById('formPerfil:lat').value = pos.coords.latitude.toFixed(6);
        document.getElementById('formPerfil:lng').value = pos.coords.longitude.toFixed(6);
        document.getElementById('estadoUbicacion').textContent = 'Ubicación registrada';
      }, function () {
        document.getElementById('estadoUbicacion').textContent = 'No se pudo obtener la ubicación';
      });
    }
  </script>
  ```
  - Ajusta `formPerfil` al id real del `h:form`.
  - El navegador solo permite geolocalización en `localhost` o con HTTPS. Desde otra máquina por `http://IP:8080`
    el botón no funciona; para probar usa `localhost` o los `INSERT` de prueba.
- **Checkbox de contacto (D8):** el toggle `soloAlConfirmar` se enlaza a
  `#{cuentasController.perfil.usuario.consentimientoContacto}`. Es lo único de HU-1.4 que hace [DAVID].
- El toggle "Recibir avisos" no tiene columna en la BD ([Inconsistencias](#10-inconsistencias-a-revisar) #5). Déjalo sin enlazar.
- **Botón "Guardar cambios":** cámbialo a `p:commandButton action="#{cuentasController.guardarPerfilClinico}"`.

**Navegación**

- Entrada: menú de usuario → "Mi perfil" (`Usuario → miPerfil`).
- Salida: se queda en miPerfil.
- Activity *AD - Acceso al sistema*: `IniciarSesion (HU-1.2) → PerfilClinico (HU-1.3)`.

**Terminado cuando**

- Entrar como un usuario nuevo (HU-1.1), elegir O / −, pulsar "Usar mi ubicación" (aceptar el permiso del
  navegador), guardar y recargar: los combos conservan O / − y la BD tiene coordenadas.
- Volver a guardar con A / + actualiza el registro, no lo duplica.
- Activar el checkbox de contacto lo guarda en `Usuario`.
- El HTML (Ctrl+U) no muestra las coordenadas como texto, solo en los campos ocultos del propio usuario.
- SQL de verificación:
  ```sql
  SELECT p.Usuarioid, p.tipoSanguineo, p.factorRh, p.radioNotificacion, p.latitud, p.longitud, p.fechaRegistroPerfil,
         u.consentimientoContacto
    FROM PerfilClinico p JOIN Usuario u ON u.id = p.Usuarioid
   WHERE u.correo = '<tu correo>';
  ```

**Checklist**

- [ ] `guardarPerfilClinico(PerfilClinico)` en `LnCuentas` (perfil + `consentimientoContacto`)
- [ ] `perfil` en `CuentasController` (inicializado en el constructor o con `f:viewAction`)
- [ ] Vista miPerfil sin datos fijos ("Rosalba Méndez…")
- [ ] Combo "Región" reemplazado por "Usar mi ubicación" + `h:inputHidden` latitud/longitud (D1)
- [ ] Checkbox `consentimientoContacto` (D8)
- [ ] Actualizar `sesion.getUsuario().setPerfilClinico(p)` después de guardar
- [ ] Pruebas + SQL

---

#### HU-3.4 Configurar radio de notificación

**Objetivo:** el donador elige el radio (3, 5 o 10 km) en que quiere recibir avisos.

**Secuencia (`.vpp`)**

| # | Mensaje en el diagrama | Equivalente en código |
|---|---|---|
| 1 | `Donador → Dashboard.seleccionarAjustes()` | Menú "Mi perfil" |
| 1.1 | `Dashboard → UIAjustes.mostrar()` | `cuentas/miPerfil.xhtml`, sección "Radio de notificación" (decisión D7: `UIAjustes` = miPerfil) |
| 2 | `UIAjustes → ControlCuentas.obtenerRadio(): int` | `lnCuentas.obtenerRadio(usuario)` |
| 2.1 | `ControlCuentas → DBMS: consultarRadio(usuarioId)` → `return radioNotificacion` | `perfilF.find(u.getId())` |
| 3 | `Donador → UIAjustes.seleccionarRadio(int radioNotificacion)` | `p:selectOneButton` 3/5/10 → `#{cuentasController.perfil.radioNotificacion}` |
| 3.1 | `UIAjustes → ControlCuentas.configurarRadio(int radioNotificacion): boolean` | `lnCuentas.configurarRadio(perfil)` |
| 3.1.1 | `ControlCuentas → ControlCuentas.validarRadio(int radioNotificacion): boolean` → `return valido` | Privado en el Ln: `radio ∈ {3, 5, 10}`, que es el CHECK de la BD |
| 3.1.2 | `alt[valido == true]` `DBMS: actualizarRadio(usuarioId, radioNotificacion)` → `return true` | `perfilF.edit(p)` |
| — | `alt[valido == false]` `return false` | Mensaje de error |

**Firmas corregidas (LnCuentas)**

```java
public int obtenerRadio(Usuario u)             // si no tiene PerfilClinico → 5 (default)
public boolean configurarRadio(PerfilClinico p)
```

**Archivos**

| Acción | Archivo |
|---|---|
| Modificar | `-ejb/src/java/ln/LnCuentas.java` |
| Modificar | `-war/src/java/controller/CuentasController.java` |
| Modificar | `-war/web/vista/cuentas/miPerfil.xhtml` (copiar el `p:selectOneButton id="radio"` que hoy está en `notificaciones.xhtml`) |
| Avisar | `notificaciones/notificaciones.xhtml` ([Alex]): por D7, su selector de radio se reemplaza por el radio actual + un enlace a miPerfil (tarea de [Alex] en HU-3.1) |

**Facade:** no necesita métodos nuevos.

**Navegación**

- Usuario → miPerfil.
- Activity *AD - Notificaciones*: `ConfigurarRadio (HU-3.4)`.

**Terminado cuando**

- Elegir 10 km y guardar, luego recargar: queda seleccionado 10.
- Un usuario **sin** perfil clínico ve el mensaje "Primero completa tu perfil clínico" (requiere HU-1.3).
- SQL de verificación: `SELECT radioNotificacion FROM PerfilClinico WHERE Usuarioid = 903;`

**Checklist**

- [ ] `obtenerRadio(Usuario)`, `configurarRadio(PerfilClinico)` y `validarRadio` privado
- [ ] Selector 3/5/10 en miPerfil
- [ ] Avisar a [Alex] cuando el selector de miPerfil esté en `main` (D7)
- [ ] Pruebas + SQL

---

#### T-final — Actualizar el diagrama `.vpp` (decisiones D6 y D12)

Se hace al final, cuando el código de todos esté en `main`, para que el diagrama refleje lo implementado.

- [ ] **Firmas con entidad completa** en todos los Controls del diagrama de clases y en los mensajes de las secuencias.
      Ejemplos: `ControlCuentas.registrarUsuario(Usuario u)`, `ControlSolicitud.registrarSolicitud(SolicitudReposicion s)`,
      `ControlPanelCETS.actualizarEstatus(HistorialEstatusSolicitud h)`. Usa las firmas de este documento.
- [ ] **`iniciarSesion` devuelve `Usuario`**, no `boolean`. `iniciarSesionInstitucional` devuelve `PersonalCETS`.
      Ajustar los `return` y las guardas `alt[autenticado …]` de HU-1.2 y HU-6.1.
- [ ] **Agregar la clase `Interes`** (con `fechaInteres` y la PK `Usuario`+`SolicitudReposicion`) al diagrama de
      clases, con sus asociaciones a `Usuario` y `SolicitudReposicion`.
- [ ] **Agregar `SesionController`** (sesión: `usuario`, `rol`, `iniciar`, `cerrarSesion`, `verificar`, `verificarCETS`).
- [ ] **Mover a los Controls** los métodos de negocio que hoy están en entidades: `Usuario.autenticarse`,
      `SolicitudReposicion.validarFormatoFolio`, `PerfilClinico.validarCompatibilidad`,
      `RegistroDonacion.calcularProximaFecha`, etc. En las secuencias, el mensaje sale del Control hacia sí mismo.
- [ ] **Quitar los mensajes que ya no son métodos** (Ln delgado): `validarDatos` y `guardarUsuario` (HU-1.1),
      `verificarFolioUnico` (HU-2.1/2.4).
- [ ] **D7:** en las secuencias, `UIAjustes` pasa a `miPerfil` (HU-3.4) y a `criteriosExclusion` (HU-4.3).
      `UIBuscarDonador`/`UIFichaDonador` pasan a `donadores` (HU-5.4, HU-7.3).
- [ ] **D10:** en HU-5.3 y HU-5.4, `verificarExclusion` lee `Usuario.exclusionPermanente`; ya no se recorren los
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

**Formato (decisión D4):** `^CETS-OAX-\d{4}-\d{4}$`. Está **pendiente de confirmar con el profe**; mientras
tanto, deja la regex en una sola constante del Ln para cambiarla fácil.

**Terminado cuando**

- `ABC-123` → error de formato.
- `CETS-OAX-2026-0901` (ya activa por los `INSERT` de prueba) → error "ya existe una solicitud activa".
- `CETS-OAX-2026-0903` (existe pero está *Atendida*) → **válido**.
- SQL de verificación: `SELECT folioCETS, estatus FROM SolicitudReposicion WHERE folioCETS = 'CETS-OAX-2026-0901';`

**Checklist**

- [ ] `existeFolioActivo` en `SolicitudReposicionFacade`
- [ ] `validarFolio(SolicitudReposicion)` con la regex en una constante del Ln
- [ ] Mensajes distintos para formato y duplicado
- [ ] Pruebas + SQL

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

- [ ] `LnSolicitud.registrarSolicitud` + `listarHospitales`
- [ ] `SolicitudController` + vista
- [ ] (Integración) llamar `lnNotificacion.buscarDonadoresCercanos(s)` cuando [Alex] lo suba
- [ ] Pruebas + SQL
- [ ] **Merge a `main` lo antes posible** (los demás dependen de solicitudes reales)

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

- [ ] `buscarPorUuid` en la facade
- [ ] `generarPublicacion` y `obtenerPublicacion` en `LnSolicitud`
- [ ] Librería QR local en `-war/web/resources/js/` + QR con la URL de `uuidPublico` (D5)
- [ ] Vista sin datos fijos ni contacto del familiar
- [ ] Pruebas + SQL

---

#### HU-1.4 Privacidad de contacto (decisión D8: se queda con [Galilea])

**Objetivo:** la publicación pública de una solicitud **nunca** muestra el contacto del familiar mientras no haya un
interés confirmado.

La parte de `Usuario.consentimientoContacto` (checkbox en miPerfil) la hace [DAVID] en HU-1.3.

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

- [ ] `obtenerPublicacion` sin modificar la entidad
- [ ] `publicacion.xhtml` sin datos de contacto del familiar (`neg`)
- [ ] Pruebas + SQL

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

- [ ] `buscarPorUsuario` + `listarMisSolicitudes`
- [ ] Vista con `ui:repeat`, estado vacío y `f:param uuid`
- [ ] Pruebas + SQL

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

- [ ] `registrarDonacion(RegistroDonacion)` + `calcularProximaFecha` privado
- [ ] `DonacionController` + vista
- [ ] Pruebas + SQL

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

- [ ] `buscarDonadores` en `UsuarioFacade`
- [ ] `buscarDonador(Usuario)` en `LnDonacion`
- [ ] Ficha usando los métodos de [Diego]
- [ ] Pruebas + SQL

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

- [ ] `buscarPorUsuario` en `HistorialExternoFacade`
- [ ] `obtenerHistorialExterno(Usuario)`
- [ ] Vista con los dos casos (`alt`)
- [ ] Pruebas + SQL

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

- [ ] Facades: `buscarDonadoresActivos` (sin coordenadas no se notifica, D1) y `buscarPorUsuario`
- [ ] Vista: selector de radio → radio actual + enlace a miPerfil (D7); aviso si no hay ubicación (D1)
- [ ] `buscarDonadoresCercanos`, `obtenerNotificacion`, `listarMisNotificaciones` + compatibilidad privada
- [ ] Avisar a [Galilea] para que lo llame desde `registrarSolicitud`
- [ ] `NotificacionController` + vista
- [ ] Pruebas + SQL

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

- [ ] `calcularRadioCercania` con haversine y redondeo
- [ ] Revisar que ninguna vista pinte `perfilClinico.latitud/longitud`
- [ ] Pruebas + SQL

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

- [ ] `registrarInteres(Interes)` con PK compartida
- [ ] Stub temporal de `esElegible` hasta que [Diego] suba el real (no subir el stub)
- [ ] Vista + los dos casos (elegible → notificaciones; no elegible → mensaje)
- [ ] Pruebas + SQL

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

- [ ] `buscarActivos` en la facade
- [ ] `obtenerCriterios` y `registrarRespuestas`
- [ ] `CuestionarioController` `@ViewScoped` + vista dinámica
- [ ] Pruebas + SQL

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

- [ ] `evaluarRespuestas`, `evaluarCriterio` y `calcularTiempoEspera` privados
- [ ] `obtenerResultado`
- [ ] Vista con los dos casos
- [ ] Pruebas + SQL

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

- [ ] `obtenerCatalogo`, `registrarCriterio`, `modificarCriterio` y `validarCriterio` privado
- [ ] Vista dinámica con values iguales a los de la BD
- [ ] Pruebas + SQL

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

- [ ] `buscarPorDonador` en `RegistroDonacionFacade`
- [ ] `obtenerHistorial(Usuario)` en `LnDonacion`
- [ ] Controller + vista con `ui:repeat` y estado vacío
- [ ] Pruebas + SQL

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

- [ ] Facade: `buscarUltimaPorDonador`
- [ ] `verificarExclusion` y `esElegible` leen `Usuario.exclusionPermanente` (D10)
- [ ] `obtenerElegibilidad`, `verificarExclusion` y `esElegible` (+ `calcularProximaFecha` privado)
- [ ] Vista con los estados elegible, restrictivo por fecha y restrictivo permanente
- [ ] Avisar a [Alex] y [Galilea] cuando esté en `main`
- [ ] Pruebas + SQL

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

Entra a MySQL como root:

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
SELECT id, nombre FROM Hospital;      -- 5 hospitales
SELECT * FROM PersonalCETS;           -- 901, cets@cets.oaxaca.gob.mx
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
- **CETS:** `cets@cets.oaxaca.gob.mx` / `Cets2026!`.

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

- [ ] **Compila:** Clean and Build del EAR con JDK 21 → `BUILD SUCCESSFUL`, **0 errores**. No debe haber warnings
      nuevos respecto a `main`.
- [ ] **Despliega:** Run sin excepciones en el log de Payara (`~/Payara_Server/glassfish/domains/domain1/logs/server.log`).
- [ ] **HU-1.1 Alta inicial:**
  - En `cuentas/crearCuenta`, crear una cuenta con un correo nuevo → te lleva a `cuentas/iniciarSesion`.
  - Repetir el mismo correo → "Ese correo ya está registrado".
  - SQL: `SELECT correo, LENGTH(passwordHash), fechaRegistro FROM Usuario ORDER BY id DESC LIMIT 1;` → hash de 64 caracteres.
- [ ] **HU-1.2 Autenticación:**
  - Entrar con esa cuenta → `notificaciones/notificaciones`, con nombre e iniciales en el encabezado.
  - Una contraseña mala → "Correo o contraseña incorrectos".
  - "Cerrar sesión" → `publico/index`.
- [ ] **HU-6.1 Login institucional:**
  - `cets@cets.oaxaca.gob.mx` / `Cets2026!` → `cets/solicitudesActivas`.
  - Una contraseña mala → "Credenciales institucionales incorrectas".
  - Sin sesión CETS, `/faces/vista/cets/donadores.xhtml` → redirige a `cets/loginInstitucional`.
  - Con sesión de usuario normal, una vista CETS también redirige al login institucional.
- [ ] La raíz `http://localhost:8080/mx.edu.itoaxaca.sividocets-war/` carga `publico/index`.
- [ ] Las HU de tu módulo siguen pasando sus "Terminado cuando".

---

## 9. Decisiones tomadas

Son inconsistencias del `.vpp` contra la BD y las vistas que ya se resolvieron. La columna "Tarea" indica dónde
quedó el trabajo en este documento.

| # | Tema | Decisión | Tarea |
|---|---|---|---|
| D1 | Ubicación del donador | En `cuentas/miPerfil` el combo "Región" se reemplaza por el botón **"Usar mi ubicación"**: Geolocation API del navegador → `h:inputHidden` de `PerfilClinico.latitud/longitud`. **Sin coordenadas no se notifica a ese donador.** | [DAVID] HU-1.3 (vista miPerfil); [Alex] HU-3.1 (`buscarDonadoresActivos` exige `latitud/longitud` no nulas) |
| D2 | Respuestas del cuestionario (HU-4.1) | El controller arma `CuestionarioRespuesta` con su `List<RespuestaCriterio>`. El Ln calcula `resultado` (y `tiempoEsperaDias`) **antes** de `create`. | [Alex] HU-4.1 / HU-4.2 |
| D3 | Valores de resultado | En código se usan exactamente `'Apto'` y `'NoApto'` (CHECK de la BD). La vista puede mostrar "No apto". | [Alex] HU-4.2 |
| D4 | Formato de folio | `^CETS-OAX-\d{4}-\d{4}$`. **Pendiente confirmar con el profe.** | [Galilea] HU-2.4 |
| D5 | QR | Librería JS de QR **local** en `-war/web/resources/js/` (sin CDN). El QR codifica la URL pública con `uuidPublico` (`/faces/vista/publico/publicacion.xhtml?uuid=…`); nunca con el id. | [Galilea] HU-2.2 |
| D6 | Falta la clase `Interes` en el diagrama | Se agrega al diagrama de clases. | [DAVID] T-final |
| D7 | Vistas que no existen (`UIAjustes`, `UIBuscarDonador`, `UIFichaDonador`) | `UIAjustes` → `cuentas/miPerfil` (HU-3.4) y `cets/criteriosExclusion` (HU-4.3). `UIBuscarDonador`/`UIFichaDonador` → `cets/donadores` (HU-5.4, HU-7.3). | [DAVID] HU-3.4; [Alex] HU-4.3 y vista notificaciones; [Galilea] HU-5.4/7.3; [DAVID] T-final (diagrama) |
| D8 | HU-1.4 (privacidad) | Se queda con [Galilea] (`ControlSolicitud` → `LnSolicitud`). [DAVID] solo agrega el checkbox `consentimientoContacto` en miPerfil. | [Galilea] HU-1.4; [DAVID] HU-1.3 |
| D9 | Secuencia contra navegación | Cuando no coinciden, **se sigue el *Diagrama de navegación SIVIDO***: `capturarSolicitud → misSolicitudes`, `notificaciones → cuestionario`, `confirmarInteres → notificaciones`. | Todos |
| D10 | Exclusión permanente: dos fuentes de verdad | **Fuente única: `Usuario.exclusionPermanente`.** Se marca `true` al registrar un `HistorialClinico` con criterio `Permanente`. `verificarExclusion()` y `esElegible()` leen ese campo; no recorren padecimientos. | [Diego] HU-5.3; datos de prueba 2.4 |
| D11 | Valores de `criteriosExclusion` | Solo `SiNo` / `SeleccionSimple`; se quita "Selección múltiple". `tipoExclusion` va como `Temporal` / `Permanente` (sin minúsculas). | [Alex] HU-4.3 |
| D12 | Diagrama desactualizado | Actualizar el diagrama de clases y las secuencias: firmas con entidad completa, `Interes`, `SesionController`, `iniciarSesion` devuelve `Usuario`, métodos de entidades movidos a los Controls y mensajes del Ln delgado. | [DAVID] T-final |

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
   para guardarlo.
5. **HU-1.3, miPerfil:** el toggle "Recibir avisos" no tiene columna en la BD, y la edición de datos personales
   (nombre/teléfono) no tiene HU.
6. **HU-1.4 / HU-3.3:** `PublicacionSolicitud.mostrarContactoDonador(int solicitudId)` (HU-3.3, 1.1.4) contradice la
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
    - Falta definir si al registrar una donación ligada se cambia el estatus de la solicitud.
13. **HU-5.3 / D10:** **ninguna HU registra `HistorialClinico`**. `PersonalCETS.registrarPadecimiento` no tiene
    secuencia, así que nadie marca `Usuario.exclusionPermanente` desde la aplicación. Por ahora solo se marca con los
    `INSERT`/`UPDATE` de prueba. Decidir qué HU e integrante lo implementa.
14. **HU-5.4:** la vista muestra un código "DON-OAX-2024-984" que no existe en la BD, y
    `buscarDonador(String criterio)` no define si busca por nombre, correo o tipo.
15. **HU-7.3:** `ControlDonacion.contarRegistros(List<HistorialExterno>)` solo hace `size()`. Se resolvió en la vista
    con `empty`; decidir si se quita del diagrama.
16. **HU-6.2:**
    - El nombre dice "solicitudes activas", pero la vista filtra por cualquier estatus, con contadores fijos.
    - La vista combina tipo y Rh en un solo combo (`O+`) y el diagrama los maneja por separado.
17. **HU-6.3:** el título habla de "detalle de **donadores** vinculados", pero la secuencia solo obtiene la
    **cantidad** de interesados. Definir si el CETS debe ver la lista (nombres, sin ubicación).
18. **HU-6.4:**
    - `«create» HistorialEstatusSolicitud(solicitudId, personalId, …)` no indica de dónde sale `personalId`. Se toma de
      la sesión.
    - No se menciona `SolicitudReposicion.PersonalCETSUsuarioid`.
    - Las transiciones de estatus permitidas (¿se puede reabrir una Cerrada?) no están definidas.
19. **Navegación general:**
    - El *Diagrama de navegación* no tiene transiciones para `donadores → capturarDonacion`, ni para
      `misSolicitudes → capturarSolicitud`, aunque las vistas tienen esos botones.
    - El botón "Descargar" de miHistorial y "Descargar imagen"/"Imprimir ficha" de publicación no tienen HU.
