package de.bytephil.utils;

import de.bytephil.app.App;
import de.bytephil.enums.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Converter {

    public static byte[] convert(String path, String name, boolean System) throws IOException {

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
            } catch (Exception e1) {
                Console.printout(e1.getMessage(), MessageType.ERROR);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            File file = new File(path);
            int size = (int) file.length();

            byte[] b = new byte[size];

            for (int readNum; (readNum = fis.read(b)) != -1;) {
                bos.write(b, 0, readNum);
            }

            byte[] bytes = bos.toByteArray();

            if (!App.getInstance().currentVideoBytes.containsKey(name)) {
                Console.printout("Succefully loaded File " + path + "!", MessageType.INFO);
                App.getInstance().currentVideoBytes.clear();
                App.getInstance().currentVideoBytes.put(name, bytes);
            }
            return bytes;
    }
}
