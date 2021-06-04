package de.bytephil.utils;

import de.bytephil.app.App;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Converter {

    public static byte[] convert(String path, String name) throws IOException {

        if (!App.getInstance().converted.containsKey(name)) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
            } catch (Exception e1) {
                Console.printout(e1.getMessage(), MessageType.ERROR);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[16384];

            for (int readNum; (readNum = fis.read(b)) != -1;) {
                bos.write(b, 0, readNum);
            }

            byte[] bytes = bos.toByteArray();

            App.getInstance().converted.put(name, bytes);
            Console.printout("Succefully loaded File " + path + "!", MessageType.INFO);
            return bytes;
        } else {
            byte[] bytes = App.getInstance().converted.get(name);
            Console.printout("The File \""+ name + "\" was already loaded!", MessageType.WARNING);
            return bytes;
        }
    }
}
