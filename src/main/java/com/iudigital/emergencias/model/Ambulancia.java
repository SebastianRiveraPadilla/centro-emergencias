package com.iudigital.emergencias.model;

import java.util.Random;

public class Ambulancia extends Recurso {

    private final Hospital hospital;
    private final Random rnd = new Random();

    public Ambulancia(String id, Hospital hospital) {
        super(id);
        this.hospital = hospital;
    }

    @Override
    protected void ejecutar(OrdenTrabajo orden) throws InterruptedException {
        Emergencia e = orden.emergencia();
        estado = EstadoRecurso.EN_CAMINO;
        Thread.sleep(600 + (long) (e.getDistanciaKm() * 150));

        if (rnd.nextInt(20) == 0) {
            estado = EstadoRecurso.INACCESIBLE;
            Thread.sleep(1500);
            estado = EstadoRecurso.EN_CAMINO;
        }

        estado = EstadoRecurso.OCUPADO;
        Thread.sleep(1400);

        hospital.recibirPaciente(e);
    }
}