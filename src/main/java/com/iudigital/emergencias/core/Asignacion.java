package com.iudigital.emergencias.core;

import com.iudigital.emergencias.model.Ambulancia;
import com.iudigital.emergencias.model.EquipoMedico;

public record Asignacion(Ambulancia ambulancia, EquipoMedico equipo) { }