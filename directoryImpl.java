package tpcorba.projet;

import org.omg.CORBA.*;
import java.lang.*;

public class directoryImpl extends directoryPOA {

  readonly attribute long number_of_file;

  public void open_regular_file(regular_fileHolder r, String name, mode m) {

  }

  public void open_directory(directoryHolder f, String name) {

  }

  public void create_regular_file(regular_fileHolder r, String name) {

  }

  public void create_directory(directoryHolder f, String name) {

  }

  public void delete_file(String name) {

  }

  public long list_files(file_listHolder l) {

  }
}
