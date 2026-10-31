package bisu.tms;

/** Manually implemented FIFO queue: no Java Queue/LinkedList collection. */
public class CustomMatchQueue {
    private static class Node {
        Match data;
        Node next;
        Node(Match data) { this.data = data; }
    }

    private Node head;
    private Node tail;
    private int size;

    public void enqueue(Match match) {
        Node node = new Node(match);
        if (tail == null) head = node;
        else tail.next = node;
        tail = node;
        size++;
    }

    public Match dequeue() {
        if (head == null) return null;
        Match match = head.data;
        head = head.next;
        if (head == null) tail = null;
        size--;
        return match;
    }

    public Match peek() { return head == null ? null : head.data; }
    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
    public void clear() { head = tail = null; size = 0; }
}
