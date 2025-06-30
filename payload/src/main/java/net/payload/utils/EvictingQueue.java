package net.payload.utils;

import java.util.concurrent.ConcurrentLinkedDeque;
import org.jetbrains.annotations.NotNull;

/**
 * A thread-safe, bounded queue that automatically evicts elements when size limit is exceeded
 * Implements FIFO (First In, First Out) with size constraints
 *
 * @param <E> the type of elements held in this queue
 */
public class EvictingQueue<E> extends ConcurrentLinkedDeque<E> {
    private final int limit;  // Maximum number of elements allowed

    /**
     * Constructs an EvictingQueue with specified size limit
     *
     * @param limit maximum number of elements the queue can hold
     * @throws IllegalArgumentException if limit is negative
     */
    public EvictingQueue(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Queue limit cannot be negative");
        }
        this.limit = limit;
    }

    /**
     * Adds an element to the tail of the queue, evicting head elements if size exceeds limit
     *
     * @param element element to be added
     * @return true if the element was added
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean add(@NotNull E element) {
        boolean add = super.add(element);
        
        // Evict elements from head until size constraint is satisfied
        while(add && this.size() > this.limit) {
            super.remove();
        }
        
        return add;
    }

    /**
     * Adds an element to the head of the queue, evicting tail elements if size exceeds limit
     *
     * @param element element to be added
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void addFirst(@NotNull E element) {
        super.addFirst(element);
        
        // Evict elements from tail until size constraint is satisfied
        while(this.size() > this.limit) {
            super.removeLast();
        }
    }

    /**
     * Returns the maximum size limit of this queue
     *
     * @return the size limit
     */
    public int limit() {
        return this.limit;
    }
}