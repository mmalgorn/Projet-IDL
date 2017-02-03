package files;

import org.omg.CORBA.*;
import java.lang.*;

public class directoryImpl extends directoryPOA {

  readonly attribute long number_of_file;
  File dir;

  public directoryImpl (File f) {
      dir = f;
  }


  public void open_regular_file(regular_fileHolder r, String name, mode m) {
      File f = new File(name);
      if (!f.exists()) throw new no_such_file();
      if (!f.isFile()) throw new invalid_type_file();
      r.value = new regular_fileImpl(f);
  }

  public void open_directory(directoryHolder f, String name) {
      File f = new File(name);
      if (!f.exists()) throw new no_such_file();
      if (!f.isDirectory()) throw new invalid_type_file();

      r.value = new directoryImpl(f);
  }

  public void create_regular_file(regular_fileHolder r, String name) {
      File f = new File(name);
      if (f.exists()) throw new already_exists();

      f.createNewFile();
      r.value = new regular_fileImpl(f);
  }

  public void create_directory(directoryHolder f, String name) {
      File f = new File(name);
      if (f.exists()) throw new already_exists();

      f.mkdir();
      r.value = new directoryImpl(f);
  }

  public void delete_file(String name) {
      File f = new File(name);
      if (f.exists()) throw new no_such_file();

      f.destroy();
  }

  public long list_files(file_listHolder l) {

  }
}
