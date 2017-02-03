package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;

public class regular_fileImpl extends regular_filePOA {

  int offset;
  File file;

  public regular_fileImpl (String pathname) {
    file = new File(pathname);
  }

  public int read(int size, StringHolder data) {
    if (new_offset > file.length()) throw new end_of_file();

    RandomAccessFile raf = new RandomAccessFile(file, "r");
    byte[] buf;
    try {
      int nbr = frd.read(buf, offset, size);
    } catch(IOException e) {
      throw new invalid_operation();
    }
    data.value = new String(buf);
    return nbr;
  }

  public int write(int size, String data) {
    if (new_offset > file.length()) throw new end_of_file();

    FileWriter frd = new FileWriter(file);
  }

  public void seek(int new_offset) {
    if (new_offset > file.length()) throw new end_of_file();
  }

  public void close() {

  }

}
