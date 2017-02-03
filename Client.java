package files;

import org.omg.CORBA.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Client {

    enum Command {help, ls, cd, touch, mkdir, rm, cat, quit};

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
            Command cmd;
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
                args = scn.nextLine().split("\\s+");
                if(args.length == 0) continue;
                try {
                    cmd = Command.valueOf(args[0]);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid command");
                    continue;
                }
                switch(cmd) {
                    case help:
                        System.out.println("\tls : Display all the files and directories in the current directory.");
                        System.out.println("\tcd <path> : Navigate to the given directory.");
                        System.out.println("\ttouch <name> : Create a new file.");
                        System.out.println("\tmkdir <name> : Create a new directory.");
                        System.out.println("\trm <name> : Delete the given file or directory.");
                        System.out.println("\tcat <name> [size] : Display the content of the file.");
                        System.out.println("\tquit : Close the client.");
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
                            }
                        } else {
                            System.out.println("Missing name parameter");
                        }
                        break;
                    case cat:
                        StringHolder sH = new StringHolder();
                        sH.value = "";
                        if(args.length == 2) {
                            try {
                                curDir.value.open_regular_file(curFile, args[1], mode.read_only);
                                curFile.value.read(-1, sH);
                                System.out.println(sH.value);
                            } catch(no_such_file e) {
                                System.out.println(args[1] + " no such file");
                                break;
                            }
                        } else if(args.length > 2) {
                            try {
                                int s = new Integer(args[2]);
                                curDir.value.open_regular_file(curFile, args[1], mode.read_only);
                                while(curFile.value.read(s, sH) != 1) {
                                    System.out.print(sH.value);
                                    scn.nextLine();
                                    System.out.print("\b");
                                }
                            } catch(no_such_file e) {
                                System.out.println(args[1] + " no such file");
                            } catch(NumberFormatException e) {
                                System.out.println("Parameter 2 is invalid");
                            } catch(end_of_file e) {
                                System.out.println("End of file");
                            }
                        } else {
                            System.out.println("Missing name parameter");
                        }
                        break;
                    case quit:
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid command");
                        break;
                }
            }
        } catch(Exception e) {
            System.out.println("Erreur :");
            e.printStackTrace(System.out);
        }

    }
}
