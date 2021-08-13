package program.utils;

import java.util.LinkedList;

public class StackForDirectories {
    private LinkedList<String> stack = new LinkedList<>();
    public String peek() {
        StringBuilder fullString = new StringBuilder();
        for (int i = stack.size() - 1; i >= 0; i--) {
            fullString.append(stack.get(i));
        }
        return fullString.toString();
    }
    public String poll() {
        return stack.poll();
    }
    public void push(String s) {
        stack.addFirst(s);
    }

    public void printStack() {
        for (String s : stack) {
            System.out.println(s);
        }
    }
    public boolean contains(String s) {
        return stack.contains(s);
    }
    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
