package files;

import org.omg.CORBA.*;
import java.io.*;
import java.lang.*;
import java.util.*;

public class Client {

	enum Command {
		help, ls, cd, touch, mkdir, rm, read, quit, vi
	};

	enum Param {
		wa, wh
	};

	public static void main(String[] arguments) throws IOException {

		ORB orb = ORB.init(arguments, null);

		if (arguments.length != 0) {
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

			System.out.println("Welcome. Type \"help\" to display all of the available commands.");
			while (true) {
				System.out.print("\n" + (path.length() > 0 ? path + " " : "") + "> ");
				args = scn.nextLine().split("\\s+");
				if (args.length == 0) continue;
				try {
					cmd = Command.valueOf(args[0]);
				} catch (IllegalArgumentException e) {
					System.out.println("Invalid command");
					continue;
				}
				switch (cmd) {
					case help:
						System.out.println("\tls : Display all the files and directories in the current directory.");
						System.out.println("\tcd <path> : Navigate to the given directory.");
						System.out.println("\ttouch <name> : Create a new file.");
						System.out.println("\tmkdir <name> : Create a new directory.");
						System.out.println("\trm <name> : Delete the given file or directory.");
						System.out.println("\tread <name> [size] [offset] : Display the content of the file.");
						System.out.println("\tvi [option] <name> : Open file editor");
						System.out.println("\t\tOptions");
						System.out.println("\t\t\t-wa\tThe text will be add at the end of the file");
						System.out.println("\t\t\t-wh\tThe file will be clear before adding text");
						System.out.println("\tquit : Close the client.");
						break;
					case ls:
						int size = curDir.value.list_files(flH);
						System.out.println(size + " fichier" + (size > 1 ? "s" : ""));
						if (size == 0) break;
						while (flH.value.next_one(deH)) {
							System.out.print("\t" + deH.value.name);
							System.out.println(deH.value.type == file_type.directory_type ? "/" : "");
						}
						System.out.print("\t" + deH.value.name);
						System.out.println(deH.value.type == file_type.directory_type ? "/" : "");
						break;
					case cd:
						if (args.length > 1) {
							String[] fullPath = args[1].split("/");
							for (int i = 0; i < fullPath.length; i++) {
								try {
									curDir.value.open_directory(curDir, fullPath[i]);
									path += (path.length() > 0 ? "/" : "") + fullPath[i];
								} catch (no_such_file e) {
									System.out.println(fullPath[i] + " not found");
								} catch (invalid_type_file e) {
									System.out.println(fullPath[i] + " is not a directory");
								}
							}
						} else {
							curDir.value = root;
							path = "";
						}
						break;
					case touch:
						if (args.length > 1) {
							try {
								curDir.value.create_regular_file(curFile, args[1]);
								curFile.value.close();
							} catch (already_exist e) {
								System.out.println(args[1] + " already exist");
								break;
							}
						} else {
							System.out.println("Missing name parameter");
						}
						break;
					case mkdir:
						if (args.length > 1) {
							try {
								curDir.value.create_directory(curDir, args[1]);
								path += (path.length() > 0 ? "/" : "") + args[1];
							} catch (already_exist e) {
								System.out.println(args[1] + " already exist");
								break;
							}
						} else {
							System.out.println("Missing name parameter");
						}
						break;
					case rm:
						if (args.length > 1) {
							try {
								curDir.value.delete_file(args[1]);
							} catch (no_such_file e) {
								System.out.println(args[1] + " no such file or directory");
							}
						} else {
							System.out.println("Missing name parameter");
						}
						break;
					case read:
						StringHolder sH = new StringHolder();
						sH.value = "";
						if (args.length == 2) {
							try {
								curDir.value.open_regular_file(curFile, args[1], mode.read_only);
								curFile.value.read(-1, sH);
								System.out.println(sH.value);
								curFile.value.close();
							} catch (no_such_file e) {
								System.out.println(args[1] + " no such file");
								break;
							}
						} else if (args.length == 3) {
							try {
								int s = new Integer(args[2]);
								curDir.value.open_regular_file(curFile, args[1], mode.read_only);
								while (curFile.value.read(s, sH) != 1) {
									System.out.print(sH.value);
									scn.nextLine();
									System.out.print("\b");
								}
								curFile.value.close();
							} catch (no_such_file e) {
								System.out.println(args[1] + " no such file");
							} catch (NumberFormatException e) {
								System.out.println("Parameter 2 is invalid");
							} catch (end_of_file e) {
								System.out.println("End of file");
							}
						} else if (args.length > 3) {
							try {
								int s = new Integer(args[2]);
								int o = new Integer(args[3]);
								curDir.value.open_regular_file(curFile, args[1], mode.read_only);
								curFile.value.seek(o);
								curFile.value.read(s, sH);
								System.out.print(sH.value);
								curFile.value.close();
							} catch (no_such_file e) {
								System.out.println(args[1] + " no such file");
							} catch (NumberFormatException e) {
								System.out.println("Parameter 2 or 3 is invalid");
							} catch (end_of_file e) {
								System.out.println("End of file");
							} catch (invalid_offset e) {
								System.out.println("Invalid offset");
							}
						} else {
							System.out.println("Missing name parameter");
						}
						break;
					case vi:
						if (args.length > 1) {
							try {
								mode m = mode.read_write;
								String filename = "";
								boolean erreur = false;
								if (args.length >= 3) {
									if (args[1].equals("-wa")) {
										m = mode.write_append;
									} else {
										if (args[1].equals("-wh")) {
											m = mode.write_trunc;
										} else {
											erreur = true;
											System.out.println("Invalid parrameter");

										}
									}
									filename = args[2];

								} else {
									filename = args[1];
								}

								if (!erreur) {
									curDir.value.open_regular_file(curFile, filename, m);

									System.out.println("Type the text do you want and escape by taping quit");
									Scanner sc = new Scanner(System.in);
									String saisie = "";
									String buffer = "";
									while (!(buffer = sc.nextLine()).equals("quit")) {
										saisie = saisie + buffer + "\n";
									}
									if (saisie.length() > 0) saisie = saisie.substring(0, saisie.length() - 1);
									int ret = curFile.value.write(saisie.length(), saisie);
									curFile.value.close();
									if (saisie.length() != ret) {
										System.out.println("Error Writing");
									} else {
										System.out.println("Writing Sucess");
									}
								}
							} catch (no_such_file e) {
								System.out.println(args[1] + " no such file");
							} catch (NumberFormatException e) {
								System.out.println("Parameter 2 or 3 is invalid");

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
		} catch (Exception e) {
			System.out.println("Erreur :");
			e.printStackTrace(System.out);
		}
	}
}
