package com.iudigital.emergencias.threads;

import com.iudigital.emergencias.core.Asignacion;
import com.iudigital.emergencias.core.ColaEmergencias;
import com.iudigital.emergencias.core.GestorRecursos;
import com.iudigital.emergencias.model.Emergencia;
import com.iudigital.emergencias.model.EstadoEmergencia;
import com.iudigital.emergencias.model.OrdenTrabajo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Despachador implements Runnable {

    private final String id;
    private final ColaEmergencias cola;
    private final GestorRecursos gestor;
    private final AtomicBoolean activo;
    private final AtomicInteger atendidas;

    public Despachador(String id, ColaEmergencias cola, GestorRecursos gestor,
                       AtomicBoolean activo, AtomicInteger atendidas) {
        this.id = id;
        this.cola = cola;
        this.gestor = gestor;
        this.activo = activo;
        this.atendidas = atendidas;
    }

    @Override
    public void run() {
        while (activo.get()) {
            try {
                Emergencia e = cola.tomarConTimeout(1, TimeUnit.SECONDS);
                if (e == null) continue;

                Asignacion asig = gestor.reservarPar();
                if (asig == null) {
                    cola.agregar(e);
                    Thread.sleep(300);
                    continue;
                }

                e.setEstado(EstadoEmergencia.ASIGNADA);
                e.setRecursoAsignado(asig.ambulancia().getId() + " + " + asig.equipo().getId());
                System.out.printf("[Despachador %s] #%d -> %s%n",
                        id, e.getId(), e.getRecursoAsignado());

                CompletableFuture<Void> fAmb = new CompletableFuture<>();
                CompletableFuture<Void> fMed = new CompletableFuture<>();
                asig.ambulancia().asignarTrabajo(new OrdenTrabajo(e, fAmb));
                asig.equipo().asignarTrabajo(new OrdenTrabajo(e, fMed));

                CompletableFuture.allOf(fAmb, fMed).thenRun(() -> {
                    e.setEstado(EstadoEmergencia.RESUELTA);
                    gestor.liberar(asig);
                    atendidas.incrementAndGet();
                    System.out.printf("[Despachador %s] #%d RESUELTA%n", id, e.getId());
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                System.err.printf("[Despachador %s] Error: %s%n", id, ex.getMessage());
            }
        }
    }
}