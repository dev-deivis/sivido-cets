# SIVIDO — Correcciones para que las HU coincidan con el sistema

Origen: comparación del PDF `Espec_Epicas_HU_Cronograma_Eq.GatitosLinuxeros.pdf` contra el código y las vistas de `main`
(2026-11-26). Se va a verificar en la entrega que las historias de usuario coincidan con lo implementado.

Reglas de siempre: rama nueva desde `main` actualizada, `Ln` delgado (el controller solo llama al Ln), la entidad viaja
completa, Claude Code en **Plan Mode** y sin `git add`/`commit`/`push` (los comandos los corre David). Tras cualquier script
SQL: Run. Nunca `restart-domain`.

Ramas (una por tarea, para que no choquen entre sí):
- Tarea 1: `feature/hu-contrasena-correo`
- Tarea 2: `feature/hu-contacto-familiar`
- Tarea 3: `feature/hu-7-2-intercambio`
- Tarea 4: `feature/hu-enlace-notificacion`

| # | Tarea | HU | Esfuerzo | Responsable |
|---|---|---|---|---|
| 1 | Validar contraseña y correo al crear cuenta | HU-1.1 | Chico | [Galilea] |
| 2 | Revelar nombre y contacto del donador al familiar tras confirmar interés | HU-1.4, HU-3.3 | **Grande** | [DAVID] |
| 3 | Envío de historial a la institución externa con log | HU-7.2 | Mediano (o documentar) | [Alex] |
| 4 | Enlace a la publicación en la notificación | HU-3.1 | Chico | [Diego] |
| 5 | HU nuevas para el PDF (constancia, exclusión permanente) | — | Documento | |
| 6 | Ajustes de redacción del PDF (estatus, login) | HU-1.2, HU-2.3, HU-6.4 | Documento | |

---

## Tarea 1 — Validar contraseña y correo (HU-1.1) — [Galilea]

**La HU pide:** contraseña de mínimo 8 caracteres con mayúsculas, minúsculas, números y símbolos; el sistema impide la
captura si falta un dato o el correo tiene formato inválido.

**Hoy:** `crearCuenta.xhtml` muestra la lista de reglas (sin "símbolo") pero no las valida.
`CuentasController.registrarUsuario()` solo revisa que las dos contraseñas coincidan y el aviso de privacidad;
`LnCuentas.registrarUsuario(Usuario)` solo revisa que el correo no exista. Con la contraseña `a` se crea la cuenta.

**Qué hacer:**
- [ ] `LnCuentas`: agregar `contrasenaValida(String)` y `correoValido(String)` (métodos públicos y sin excepciones, regresan
      `boolean`). Contraseña: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$`. Correo: formato simple
      `^[^@\s]+@[^@\s]+\.[^@\s]+$`.
- [ ] `LnCuentas.registrarUsuario`: regresar `false` si alguna falla (validación en el backend, no solo en la vista).
- [ ] `CuentasController.registrarUsuario()`: antes de guardar, llamar a esos métodos y mostrar un mensaje específico
      ("La contraseña no cumple las reglas", "El correo no tiene un formato válido"). Mantener "Ese correo ya está registrado".
- [ ] `crearCuenta.xhtml`: agregar "Al menos un símbolo" a la lista de reglas y mantener los textos alineados con la regex.
- [ ] No cambiar el login: las cuentas de prueba (`Donador2026!`, `Cets2026!`) ya cumplen la regla y la validación solo
      aplica al registrar.

**Terminado cuando:**
- Contraseña `abc` → no crea la cuenta y dice por qué. `Abcdefg1` (sin símbolo) → no. `Abcdef1!` → sí.
- Correo `juan@` → no. Correo repetido → "Ese correo ya está registrado".
- SQL: la cuenta nueva guarda hash de 64 caracteres.

---

## Tarea 2 — Revelar contacto del donador al familiar (HU-1.4 y HU-3.3) — LA LARGA — [DAVID]

**La HU pide:**
- HU-3.3: "La identidad y datos de contacto del donador se revelan al familiar únicamente tras esta confirmación voluntaria."
- HU-1.4: el teléfono y correo se muestran al familiar únicamente si el donador se postula de manera activa, voluntaria y con
  consentimiento expreso; en listas generales siguen ocultos.

**Hoy:** `Usuario.consentimientoContacto` se guarda desde `miPerfil` (opt-in, apagado por defecto, D13) pero no lo usa ninguna
otra vista. Ninguna pantalla del familiar muestra datos del donador, así que la parte de "revelar tras confirmar" no existe.

**Diseño propuesto (decidir antes de empezar):**
- En `misSolicitudes` (o en una vista de detalle de la solicitud del familiar), cada solicitud suya muestra los donadores que
  confirmaron interés (`Interes`).
- Siempre: nombre y tipo de sangre. Teléfono y correo **solo si** el donador tiene `consentimientoContacto = true`; si no,
  el texto "Este donador no compartió su contacto".
- Solo el familiar creador de la solicitud lo ve (comparar el id del usuario en sesión con el creador, como en `obtenerConstancia`).
- Nunca ubicación, y nunca mostrar `passwordHash` ni la entidad completa en la vista: pintar solo los campos que se necesitan
  (cuidado con lo que ya anotamos en la inconsistencia #40).
- El toggle de `miPerfil` "Compartir mi contacto solo cuando yo confirme interés" (hoy deshabilitado, D14): decidir si se
  habilita usando `consentimientoContacto` o si se deja el texto actual y se aclara en el aviso.

**Qué hacer:**
- [ ] Decidir el diseño de arriba con el equipo y anotarlo en la sección 9 de `TASKS.md` (D19).
- [ ] `LnSolicitud`: método nuevo `obtenerInteresados(SolicitudReposicion s, Usuario familiar)` que valida que el familiar es
      el creador y regresa los `Interes` de la solicitud. Usar `InteresFacade.buscarPorSolicitud` directamente (no inyectar
      `LnNotificacion`/`LnPanelCETS` para no crear una dependencia circular).
- [ ] `SolicitudController`: cargar los interesados de la solicitud seleccionada y exponer solo lo necesario.
- [ ] Vista del familiar: tarjeta "Donadores que confirmaron interés" con la regla de consentimiento.
- [ ] Tests con el donador 903 (O−) en la solicitud 901, con el consentimiento apagado y encendido.
- [ ] Actualizar `TASKS.md` (HU-1.4, HU-3.3, casillas de la sección 8) y anotar el método nuevo como pendiente del `.vpp`.

**Terminado cuando:**
- Con el consentimiento apagado, el familiar ve nombre y tipo de sangre y el aviso de que no compartió su contacto.
- Con el consentimiento encendido, ve también teléfono y correo.
- Otro familiar (que no creó la solicitud) no ve nada. Sin sesión redirige al login.
- En Ctrl+U no aparece ubicación ni `passwordHash`.
- El CETS sigue viendo solo nombre y tipo de sangre (#17).

---

## Tarea 3 — Envío del historial a la institución externa (HU-7.2) — [Alex]

**La HU pide:** compartir el historial de donación de un donador con la institución externa, solo con el mecanismo de HU-7.1,
solo la información mínima y de donadores con consentimiento explícito, y dejar un log del intercambio.

**Hoy:** no hay código que envíe nada ni que escriba en `IntercambioExterno`. El historial que se ve en HU-7.3
(`historialExterno`) sale de datos de prueba. Existen las tablas `InstitucionExterna`, `IntercambioExterno` y
`HistorialExterno`, y el campo de consentimiento de interoperabilidad en `Usuario`.

**Decidir primero (con el documento del spike de HU-7.1 en la mano):**
[Alex] decide A o B con el equipo antes de empezar y lo anota en TASKS.md sección 9.
- **Opción A, implementarlo mínimo:** `LnDonacion` (o un `LnInteroperabilidad`) con `enviarHistorial(Usuario donador,
  InstitucionExterna institucion)`: exige consentimiento de interoperabilidad y que la institución esté activa, arma solo
  fecha de donación e identificador mínimo, y guarda un renglón de log en `IntercambioExterno`. El "envío" puede ser el
  registro del paquete más un botón en la ficha del donador (CETS). Revisar primero las columnas reales de la entidad.
- **Opción B, documentarlo:** dejar HU-7.2 como prototipo conceptual (sin pantalla, ya estaba sin secuencia) y escribir en el
  documento qué se haría. Solo es válida si el profe la acepta como alcance.

- [ ] Elegir A o B y anotarlo en `TASKS.md` sección 9.
- [ ] Si es A: plan primero, método en el Ln, botón en `donadores.xhtml`, log, casilla de prueba y secuencia pendiente.

**Terminado cuando (A):**
- Sin consentimiento de interoperabilidad no se envía y se explica por qué.
- Con consentimiento queda un renglón en `IntercambioExterno` con fecha, institución y donador.
- Solo se comparte la información mínima.

---

## Tarea 4 — Enlace a la publicación en la notificación (HU-3.1) — [Diego]

**La HU pide:** la notificación incluye tipo de sangre, hospital y un enlace a la publicación.

**Hoy:** `notificaciones.xhtml` muestra tipo de sangre y hospital; no vi el enlace. Revisar también `confirmarInteres.xhtml`
por si ya está ahí.

- [ ] En cada tarjeta de `notificaciones.xhtml`, un `p:button` "Ver publicación" con `outcome` a
      `/vista/publico/publicacion` y `f:param name="uuid" value="#{n.solicitudReposicionid.uuidPublico}"`.
- [ ] No usar el id de la solicitud, siempre el UUID (D5).

**Terminado cuando:** el botón abre la ficha pública de esa solicitud; las solicitudes que ya no están Activas siguen
fuera de la lista (#25).

---

## Tarea 5 — HU nuevas para el PDF (documento)

Funcionalidades sin HU hoy: constancia de donación, marcar exclusión permanente. (Imprimir ficha y la bitácora entran en
HU-2.2 y HU-6.4.) Borradores en el formato del PDF; las iteraciones y puntos son propuestas para que el equipo las ajuste:

**HU-5.5 — Descargar constancia de donación** · Usuario: Donador · Prioridad: Media · Puntos: 3
- Descripción: Quiero descargar una constancia de cada donación registrada a mi nombre, para contar con un comprobante.
- Criterios: la constancia muestra identificador, fecha, donador, solicitud vinculada (o donación voluntaria general) y la
  próxima fecha elegible, con la leyenda de que es informativa; solo el dueño de la donación puede verla; se puede imprimir o
  guardar como PDF.

**HU-6.5 — Marcar exclusión permanente de un donador** · Usuario: Personal del CETS · Prioridad: Media · Puntos: 5
- Descripción: Quiero marcar a un donador con exclusión permanente indicando el motivo, para que el sistema le impida donar.
- Criterios: solo personal CETS con sesión institucional; el motivo es obligatorio (máximo 200 caracteres) y queda
  registrado con fecha y personal que lo marcó; el donador ve el aviso con el motivo en su perfil; no se puede registrar
  donaciones nuevas a un donador excluido; la acción no se revierte desde la aplicación.

- [ ] Agregar las dos HU a la lista maestra del PDF y a las secciones de la épica 5 y 6 (y al cronograma si aplica).
- [ ] Mantener la numeración en `TASKS.md` y quitar la nota "Funcionalidades sin HU" cuando queden registradas.

---

## Tarea 6 — Ajustes de redacción del PDF (documento)

- [ ] **HU-6.4:** la descripción dice "(activa, atendida, cerrada)". El sistema permite Atendida, Cerrada y Cancelada, y
      nunca regresar a Activa. Alinear la redacción con la regla actual.
- [ ] **HU-2.3:** su criterio lista "Activa, Atendida, Cancelada". Incluir también Cerrada para que coincida con HU-6.4 y la BD.
- [ ] **HU-1.2:** el criterio dice que el sistema redirige a personal del CETS a su panel desde el mismo login. Hoy el CETS usa
      un login institucional aparte (coherente con HU-6.1). Redactar el criterio como "el personal del CETS accede por el
      login institucional y es redirigido a su panel administrativo".
- [ ] **HU-3.3:** aclarar que el estatus de la solicitud no cambia al confirmar interés; el CETS lo ve reflejado en el contador
      de interesados.
- [ ] **HU-6.3:** el criterio habla de la cantidad; la lista con nombre y tipo de sangre (#17) coincide con la descripción
      ("ver qué donadores han confirmado interés"). Agregarlo al criterio.

---

## Orden sugerido

1 → 4 → 5/6 (rápidos) → 2 (la más larga) → 3 (según A o B). Después de cada tarea: Clean and Build, Run, casillas de la
sección 8 de `TASKS.md`, y los comandos git uno por línea. Al terminar, volver a comparar contra el PDF.
