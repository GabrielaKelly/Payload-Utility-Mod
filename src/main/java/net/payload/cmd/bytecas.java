package net.payload.cmd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class bytecas {

    public static void main(String[] args) throws IOException {
        File classFile = new File("D:\\cODE\\payloadtech\\payload\\build\\classes\\java\\main\\net\\payload\\PayloadClient.class"); // Replace with the actual path
        FileInputStream inputStream = new FileInputStream(classFile);
        byte[] bytecode = new byte[(int) classFile.length()];
        inputStream.read(bytecode);
        inputStream.close();

        // Print out the bytecode as a hex string or store it for use in the `expectedClassContent`
        for (byte b : bytecode) {
            System.out.printf("0x%02X, ", b);
        }
    }
}
