package com.iudigital.emergencias.core;

import com.iudigital.emergencias.model.Emergencia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ColaEmergencias {

    private final PriorityQueue<Emergencia> cola = new PriorityQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition noVacia = lock.newCondition();

    public void agregar(Emergencia e) {
        lock.lock();
        try {
            cola.offer(e);
            noVacia.signalAll();
        } finally { lock.unlock(); }
    }

    public Emergencia tomarConTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            while (cola.isEmpty()) {
                if (nanos <= 0) return null;
                nanos = noVacia.awaitNanos(nanos);
            }
            return cola.poll();
        } finally { lock.unlock(); }
    }

    public void repriorizar() {
        lock.lock();
        try {
            if (cola.size() <= 1) return;
            List<Emergencia> snapshot = new ArrayList<>(cola);
            cola.clear();
            cola.addAll(snapshot);
        } finally { lock.unlock(); }
    }

    public List<Emergencia> snapshot() {
        lock.lock();
        try {
            List<Emergencia> l = new ArrayList<>(cola);
            l.sort(Comparator.naturalOrder());
            return l;
        } finally { lock.unlock(); }
    }

    public int size() {
        lock.lock();
        try { return cola.size(); } finally { lock.unlock(); }
    }
}