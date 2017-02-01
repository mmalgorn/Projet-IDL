package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;

public class regular_fileImpl extends regular_filePOA {

    int offset;
    File file;
    mode m;

    public regular_fileImpl (File f, mode m) {
        file = f;
        this.m = m;
        offset = 0;
        switch(m.value()) {
            case mode._write_append:
                offset = new Long(file.length()).intValue();        // EXTREMEMENT SALE
                break;
            case mode._write_trunc:
                try {
                    new PrintWriter(file).close();
                } catch(IOException e) {
                    System.out.println(e);
                }
                break;
        }
    }

    public int read(int size, StringHolder data) throws end_of_file, invalid_operation {
        if (size > file.length()) throw new end_of_file();
        if (m == mode.write_append || m == mode.write_trunc) throw new invalid_operation();

        byte[] buf = new byte[size];
        int nbr = -1;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            nbr = raf.read(buf, offset, size);
            data.value = new String(buf);
            raf.close();
        } catch(IOException e) {
            System.out.println(e);
        }
        return nbr;
    }

    public int write(int size, String data) throws invalid_operation {
        if (m == mode.read_only) throw new invalid_operation();

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.write(data.getBytes(), offset, size);
            raf.close();
        } catch(IOException e) {
            System.out.println(e);
        }
        return size;
    }

    public void seek(int new_offset) throws invalid_offset, invalid_operation{
        if (new_offset > file.length()) throw new invalid_offset();
        if (m == mode.write_append || m == mode.write_trunc) throw new invalid_operation();
        offset = new_offset;
    }

    public void close() {
        
    }

}
