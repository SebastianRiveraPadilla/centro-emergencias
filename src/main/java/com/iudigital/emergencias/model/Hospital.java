package com.iudigital.emergencias.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Hospital implements Runnable {

    private final String id;
    private final String nombre;
    private final BlockingQueue<Emergencia> pacientes = new LinkedBlockingQueue<>();
    private final AtomicInteger totalRecibidos = new AtomicInteger(0);
    private volatile boolean activo = true;

    public Hospital(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public void recibirPaciente(Emergencia e) { pacientes.offer(e); }
    public int getTotalRecibidos() { return totalRecibidos.get(); }
    public void detener() { activo = false; }

    @Override
    public void run() {
        System.out.printf("[Hospital %s] Abierto: %s%n", id, nombre);
        while (activo) {
            try {
                Emergencia e = pacientes.poll(1, TimeUnit.SECONDS);
                if (e == null) continue;
                Thread.sleep(600);
                totalRecibidos.incrementAndGet();
                System.out.printf("[Hospital %s] Paciente #%d admitido (%s)%n",
                        id, e.getId(), e.getGravedad());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                System.err.printf("[Hospital %s] Error: %s%n", id, ex.getMessage());
            }
        }
    }
}
