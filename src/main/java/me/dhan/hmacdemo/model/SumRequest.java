package me.dhan.hmacdemo.model;

/**
 * Request model for the sum operation
 */
public class SumRequest {
    private int a;
    private int b;

    // Default constructor for JSON deserialization
    @SuppressWarnings("unused")
    public SumRequest() {
    }

    public SumRequest(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    @SuppressWarnings("unused")
    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    @SuppressWarnings("unused")
    public void setB(int b) {
        this.b = b;
    }
}