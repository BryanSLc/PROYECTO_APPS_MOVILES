package com.kilo.kilo.ui.principal

/** Lo que necesita la actividad principal: el tema elegido y si debe pedir el permiso de avisos. */
data class PrincipalState(
    val temaOscuro: Boolean = false,
    val debePedirPermisoNotificaciones: Boolean = false
)