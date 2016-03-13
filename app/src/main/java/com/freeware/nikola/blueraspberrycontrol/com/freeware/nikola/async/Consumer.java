package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.async;

import java.util.Stack;

/**
 * Simple consumer implementation.
 * The run method performs blocking wait().
 * When item is add() - ed the run method transfers the item from the pending stack to the
 * processing stack and the processing starts.
 * For each item from the processing stack processItem() is called
 * More items can be added meanwhile.
 * @param <T> type of the items to be processed
 */

public abstract class Consumer<T> extends Interruptible {

    private Stack<T> pending;
    private Stack<T> processing;

    public Consumer() {
        pending = new Stack<>();
        processing = new Stack<>();
    }

    public void add(T obj) {
        synchronized (pending) {
            pending.push(obj);
            pending.notify();
        }
    }

    @Override
    public void doInterrupt() {
        synchronized (interruptedLock) {
            interrupted = true;
        }
        synchronized (pending) {
            pending.notify();
        }
    }

    public void run() {
        while (!interrupted()) {
            synchronized (pending) {
                while (pending.empty() && !interrupted()){
                    try {
                        pending.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                while (!pending.empty()) {
                    processing.push(pending.pop());
                }
            }
            while (!processing.empty() && !interrupted()) {
                T item = processing.pop();
                processItem(item);
            }
        }
    }

    public abstract void processItem(T item);
}
