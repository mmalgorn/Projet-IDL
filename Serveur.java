package files;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Serveur {
	public static void main(String[] args) throws IOException {

		try {
			ORB orb = ORB.init(args, null);

			POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			poa.the_POAManager().activate();

			directoryImpl root = new directoryImpl(new File("root"), poa);
			org.omg.CORBA.Object alloc = poa.servant_to_reference(root);

			try {
				String calc_ref = orb.object_to_string(alloc);
				String refFile = "fs.ref";
				new File(".", "root").mkdir();
				PrintWriter out = new PrintWriter(new FileOutputStream(refFile));
				out.println(calc_ref);
				out.close();
			} catch (IOException ex) {
				System.err.println("Impossible d'ecrire la reference dans fs.ref");
				System.exit(1);
			}

			System.out.println("Le serveur est pret ");

			orb.run();

			System.exit(0);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
