package com.example.tfg_1.model

import com.example.tfg_1.R

enum class PeriodoFiltro {SEMANA, MES, ANIO }

//nombres legibles
fun PeriodoFiltro.toDisplayString(context: android.content.Context): String = when (this) {
    PeriodoFiltro.SEMANA -> context.getString(R.string.semana)
    PeriodoFiltro.MES -> context.getString(R.string.mes)
    PeriodoFiltro.ANIO -> context.getString(R.string.anio)
}