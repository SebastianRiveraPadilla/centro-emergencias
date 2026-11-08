package com.iudigital.emergencias.model;

public class EquipoMedico extends Recurso {

    public EquipoMedico(String id) { super(id); }

    @Override
    protected void ejecutar(OrdenTrabajo orden) throws InterruptedException {
        estado = EstadoRecurso.EN_CAMINO;
        Thread.sleep(800);

        estado = EstadoRecurso.OCUPADO;
        Thread.sleep(2400);
    }
}