package com.huawei.codecraft;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    public static void main(String[] args) {
        schedule();
    }

    private static void schedule() {
        readUtilOK();
        outStream.println("OK");
        outStream.flush();

        int frameID;
        while (inStream.hasNextLine()) {
            String line = inStream.nextLine();
            String[] parts = line.split(" ");
            frameID = Integer.parseInt(parts[0]);
            readUtilOK();

            outStream.printf("%d\n", frameID);
            int lineSpeed = 3;
            double angleSpeed = 1.5;
            for (int robotId = 0; robotId < 4; robotId++) {
                outStream.printf("forward %d %d\n", robotId, lineSpeed);
                outStream.printf("rotate %d %f\n", robotId, angleSpeed);
            }
            outStream.print("OK\n");
            outStream.flush();
        }
    }

    private static boolean readUtilOK() {
        String line;
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                return true;
            }
            // do something;
        }
        return false;
    }
}
