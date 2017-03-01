package files;

import org.omg.CORBA.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Client {

  enum Commands {help, ls, cd, touch, mkdir, rm};
  enum Param {-rw,-wa,-wt};

  public static void main(String[] arguments) throws IOException {

    ORB orb = ORB.init(arguments, null);

    if(arguments.length!=0) {
      System.err.println("utilisation : pas de parametre ");
      System.exit(1);
    }

    String ior = null;

    try {
      String ref = "fs.ref";
      FileInputStream file = new FileInputStream(ref);
      BufferedReader in = new BufferedReader(new InputStreamReader(file));
      ior = in.readLine();
      file.close();
    } catch (IOException ex) {
      System.err.println("Impossible de lire fichier : `" + ex.getMessage() + "'");
      System.exit(1);
    }

    org.omg.CORBA.Object obj = orb.string_to_object(ior);

    if (obj == null) {
      System.err.println("Erreur sur string_to_object() ");
      throw new RuntimeException();
    }

    directory root = directoryHelper.narrow(obj);

    if (root == null) {
      System.err.println("Erreur sur narrow() ");
      throw new RuntimeException();
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    try {
      regular_fileHolder curFile = new regular_fileHolder();
      directoryHolder curDir = new directoryHolder(root);
      file_listHolder flH = new file_listHolder();
      directory_entryHolder deH = new directory_entryHolder();
      deH.value = new directory_entry();
      deH.value.type = file_type.regular_file_type;
      Scanner scn = new Scanner(System.in);
      String cmd;
      String[] args;
      String path = "";

      // root.create_regular_file(refFile, "file");
      // root.open_regular_file(refFile, "file", mode.read_only);
      // root.create_directory(refDir, "dir");
      // refDir.value.create_regular_file(refFile, "lolilol");
      // String data = "Coucou";
      // refFile.value.write(data.length(), data);
      // int size = refDir.value.list_files(refFL);
      // System.out.println(size);
      // System.out.println(refFL.value.next_one(deH));              // CA MARCHE PO
      // System.out.println(deH.value.name);

      System.out.println("Welcome. Type \"help\" to display all of the available commands.");
      while(true) {
        System.out.print("\n" + (path.length() > 0 ? path + " " : "") + "> ");
        cmd = scn.nextLine();
        args = cmd.split("\\s+");
        if(args.length == 0) continue;
        switch(Commands.valueOf(args[0])) {
          case help:
          System.out.println("\tls : Display all the files and directories in the current directory.");
          System.out.println("\tcd <path> : Navigate to the given directory.");
          System.out.println("\ttouch <name> : Create a new file.");
          System.out.println("\tmkdir <name> : Create a new directory.");
          System.out.println("\tvi <option> <name> : launch file editor");
          break;
          case ls:
          int size = curDir.value.list_files(flH);
          System.out.println(size + " fichier" + (size > 1 ? "s" : ""));
          if(size == 0) break;
          while(flH.value.next_one(deH)) {
            System.out.print("\t" + deH.value.name);
            System.out.println(deH.value.type == file_type.directory_type ? "/" : "");
          }
          System.out.print("\t" + deH.value.name);
          System.out.println(deH.value.type == file_type.directory_type ? "/" : "");
          break;
          case cd:
          if(args.length > 1) {
            String[] fullPath = args[1].split("/");
            for(int i=0; i<fullPath.length; i++) {
              try {
                curDir.value.open_directory(curDir, fullPath[i]);
                path += (path.length() > 0 ? "/" : "") + fullPath[i];
              } catch(no_such_file e) {
                System.out.println(fullPath[i] + " not found");
                break;
              } catch(invalid_type_file e) {
                System.out.println(fullPath[i] + " is not a directory");
                break;
              }
            }
          } else {
            curDir.value = root;
            path = "";
          }
          break;
          case touch:
          if(args.length > 1) {
            try {
              curDir.value.create_regular_file(curFile, args[1]);
            } catch(already_exist e) {
              System.out.println(args[1] + " already exist");
              break;
            }
          } else {
            System.out.println("Missing name parameter");
          }
          break;
          case mkdir:
          if(args.length > 1) {
            try {
              curDir.value.create_directory(curDir, args[1]);
              path += (path.length() > 0 ? "/" : "") + args[1];
            } catch(already_exist e) {
              System.out.println(args[1] + " already exist");
              break;
            }
          } else {
            System.out.println("Missing name parameter");
          }
          break;
          case rm:
          if(args.length > 1) {
            try {
              curDir.value.delete_file(args[1]);
            } catch(no_such_file e) {
              System.out.println(args[1] + " no such file or directory");
              break;
            }
          }
          break;
          case vi:
          if(args.length > 1){
            try{
              switch(Param.valueOf(args[1])){
                case -wa :
                if(args.length < 2) System.out.println("Missing name parameter");
                curDir.value.open_regular_file(curFile,args[2],mode._write_append;
                break;

                case -wh :
                if(args.length < 2) System.out.println("Missing name parameter");
                curDir.value.open_regular_file(curFile,args[2],mode._write_trunc);
                break;

                default :
                curDir.value.open_regular_file(curFile,args[1]),mode.read_write);
                break;
              }
              System.out.println()
            }catch(Execption e){

            }


          }
        }
      }
    }
  } catch(Exception e) {
    System.out.println("lol:");
    e.printStackTrace(System.out);
  }

}
}
