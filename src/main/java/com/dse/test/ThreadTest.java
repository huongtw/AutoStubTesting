package com.dse.test;

public class ThreadTest {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 2; i++) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("Thread 1 calling");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("Thread 2 calling");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Difference between start() and run(): https://stackoverflow.com/questions/8579657/whats-the-difference-between-thread-start-and-runnable-run
        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("DONE");
    }
}
