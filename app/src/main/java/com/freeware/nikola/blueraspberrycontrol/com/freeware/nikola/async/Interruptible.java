package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.async;
/**
 *  added few handy methods to Runnable which allow
 *  the thread to be stopped gracefully
 */
public abstract class Interruptible implements Runnable {

    protected boolean interrupted = false;
    protected Object interruptedLock = new Object();

    public void doInterrupt() {
        synchronized (interruptedLock) {
            interrupted = true;
        }
    }

    protected boolean interrupted() {
        synchronized (interruptedLock) {
            return interrupted;
        }
    }

    public abstract void run();
}
