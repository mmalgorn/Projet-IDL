package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;
import org.omg.PortableServer.*;

public class directoryImpl extends directoryPOA {

	protected POA poa_;
	private int number_of_file;
	File dir;

	public directoryImpl(File f, POA poa) {
		dir = f;
		poa_ = poa;
	}

	/*
	 * Renvoie le nombre de fichiers et sous-r�pertoires pr�sents dans le r�pertoire courant
	 */
	public int number_of_file() {
		return number_of_file;
	}

	/*
	 * Affecte au holder le fichier r�gulier correspondant au nom pass� en argument et ouvert dans le mode
	 * d'�criture sp�cifi�
	 * Renvoie une exception No_such_file si aucun fichier ne correspond au nom
	 * Renvoie une exception Invalid_type_file si le fichier correspondant � name est un r�pertoire
	 */
	public void open_regular_file(regular_fileHolder r, String name, mode m) throws no_such_file, invalid_type_file {
		File f = new File(dir, name);
		if (!f.exists())
			throw new no_such_file();
		if (!f.isFile())
			throw new invalid_type_file();

		try {
			org.omg.CORBA.Object alloc = poa_.servant_to_reference(new regular_fileImpl(f, m, poa_));
			r.value = regular_fileHelper.narrow(alloc);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*
	 * Affecte au holder le r�pertoire correspondant au nom pass� en argument
	 * Renvoie une exception No_such_file si aucun fichier ne correspond au nom
	 * Renvoie une exception Invalid_type_file si le fichier correspondant � name n'est pas un r�pertoire
	 */
	public void open_directory(directoryHolder d, String name) throws no_such_file, invalid_type_file {
		File f = new File(dir, name);
		if (!f.exists())
			throw new no_such_file();
		if (!f.isDirectory())
			throw new invalid_type_file();

		try {
			org.omg.CORBA.Object alloc = poa_.servant_to_reference(new directoryImpl(f, poa_));
			d.value = directoryHelper.narrow(alloc);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/*
	 * Cr�er un nouveau fichier physiquement ainsi qu'une nouvelle instance de fichier r�gulier et l'affecte au holder
	 * Renvoie une exception Already_exist si un fichier ou repertoire du m�me nom existe d�j�
	 */
	public void create_regular_file(regular_fileHolder r, String name) throws already_exist {
		File f = new File(dir, name);
		if (f.exists())
			throw new already_exist();

		try {
			f.createNewFile();
			org.omg.CORBA.Object alloc = poa_.servant_to_reference(new regular_fileImpl(f, mode.read_write, poa_));
			r.value = regular_fileHelper.narrow(alloc);
		} catch (Exception e) {
			System.out.println(e);
		}
		number_of_file = dir.listFiles().length;
	}

	/*
	 * Cr�er un nouveau r�pertoire physiquement ainsi qu'une une nouvelle instance de r�pertoire et l'affecte au holder
	 * Renvoie une exception Already_exist si un fichier ou repertoire du m�me nom existe d�j�
	 */
	public void create_directory(directoryHolder d, String name) throws already_exist {
		File f = new File(dir, name);
		if (f.exists())
			throw new already_exist();

		f.mkdir();
		try {
			org.omg.CORBA.Object alloc = poa_.servant_to_reference(new directoryImpl(f, poa_));
			d.value = directoryHelper.narrow(alloc);
		} catch (Exception e) {
			System.out.println(e);
		}
		number_of_file = dir.listFiles().length;
	}

	/*
	 * Supprime le fichier ou le r�pertoire correspondant au nom pass� en argument
	 * si le fichier est non-vide ses fichiers et sous-r�pertoires sont supprim�s
	 * Renvoie une exception No_such_file si aucun fichier ne correspond au nom donn�
	 */
	public void delete_file(String name) throws no_such_file {
		File f = new File(dir, name);
		if (!f.exists())
			throw new no_such_file();

		recursiveDelete(f);
		number_of_file = dir.listFiles().length;
	}

	/*
	 * Fonction r�cursive permettant de supprimer le contenu d'un r�pertoire
	 */
	private void recursiveDelete(File f) {
		if (f.isDirectory())
			for (File subFile : f.listFiles())
				recursiveDelete(subFile);
		f.delete();
	}

	/*
	 * Affecte au holder l'instance de liste de fichiers correspondant au r�pertoire courant
	 */
	public int list_files(file_listHolder l) {
		String[] list = dir.list();

		try {
			org.omg.CORBA.Object alloc = poa_.servant_to_reference(new file_listImpl(dir, list));
			l.value = file_listHelper.narrow(alloc);
		} catch (Exception e) {
			System.out.println(e);
		}

		return list.length;
	}
}
