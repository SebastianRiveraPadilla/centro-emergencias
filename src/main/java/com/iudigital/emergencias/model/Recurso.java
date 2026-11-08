package com.iudigital.emergencias.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class Recurso implements Runnable {

    protected final String id;
    protected volatile EstadoRecurso estado = EstadoRecurso.DISPONIBLE;
    protected final BlockingQueue<OrdenTrabajo> inbox = new LinkedBlockingQueue<>();
    protected volatile boolean activo = true;

    protected Recurso(String id) { this.id = id; }

    public String getId() { return id; }
    public EstadoRecurso getEstado() { return estado; }
    public void setEstado(EstadoRecurso e) { this.estado = e; }
    public String getTipo() { return getClass().getSimpleName().toUpperCase(); }

    public void asignarTrabajo(OrdenTrabajo orden) { inbox.offer(orden); }

    public void detener() { activo = false; }

    @Override
    public void run() {
        while (activo) {
            OrdenTrabajo orden;
            try {
                orden = inbox.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (orden == null) continue;

            try {
                ejecutar(orden);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.printf("[%s] Error en #%d: %s%n",
                        id, orden.emergencia().getId(), e.getMessage());
            } finally {
                if (!orden.completado().isDone()) {
                    orden.completado().complete(null);
                }
            }
        }
    }

    protected abstract void ejecutar(OrdenTrabajo orden) throws InterruptedException;
}