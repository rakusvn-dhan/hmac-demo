package me.dhan.hmacdemo.model;

/**
 * Request model for the sum operation
 */
public class SumRequest {
    private int a;
    private int b;

    // Default constructor for JSON deserialization
    public SumRequest() {
    }

    public SumRequest(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}