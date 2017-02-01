package files;

import org.omg.CORBA.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Client {

    public static void main(String[] args) throws IOException {

        ORB orb = ORB.init(args, null);

        if(args.length!=0) {
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
            regular_fileHolder refFile = new regular_fileHolder();
            directoryHolder refDir = new directoryHolder();
            root.create_regular_file(refFile, "file");
            root.open_regular_file(refFile, "file", mode.read_only);
            root.create_directory(refDir, "dir");
            refDir.value.create_regular_file(refFile, "lolilol");
            String data = "Coucou";
            refFile.value.write(data.length(), data);
        } catch(Exception e) {
            System.out.println("lol:"+e);
        }

    }
}
