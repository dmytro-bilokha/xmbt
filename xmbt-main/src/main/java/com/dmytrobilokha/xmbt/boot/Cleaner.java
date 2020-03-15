package com.dmytrobilokha.xmbt.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cleaner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);

    @Nonnull
    private final Set<Thread> threadsToBeInterrupted = new CopyOnWriteArraySet<>();
    @Nonnull
    private final List<Runnable> hooksToExecute = new CopyOnWriteArrayList<>();

    public Cleaner() {
        this.setName("cleaner-hook");
    }

    public void registerHook(@Nonnull Runnable hook) {
        hooksToExecute.add(hook);
    }

    public void registerThread(@Nonnull Thread thread) {
        threadsToBeInterrupted.add(thread);
        LOG.debug("Registered {} to be interrupted on exit", thread);
    }

    @Override
    public void run() {
        hooksToExecute.forEach(Runnable::run);
        threadsToBeInterrupted.forEach(Thread::interrupt);
        LOG.debug("Interrupt signal has been sent to {}", threadsToBeInterrupted);
    }

}
