package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;
import org.omg.PortableServer.*;

public class directoryImpl extends directoryPOA {

    protected POA poa_;
    private int number_of_file;
    File dir;

    public directoryImpl (File f, POA poa) {
        dir = f;
        poa_ = poa;
    }

    public int number_of_file() {
        return number_of_file;
    }

    public void open_regular_file(regular_fileHolder r, String name, mode m) throws no_such_file, invalid_type_file {
        File f = new File(dir, name);
        if (!f.exists()) throw new no_such_file();
        if (!f.isFile()) throw new invalid_type_file();

        try {
            org.omg.CORBA.Object alloc = poa_.servant_to_reference(new regular_fileImpl(f, m));
            r.value = regular_fileHelper.narrow(alloc);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void open_directory(directoryHolder d, String name) throws no_such_file, invalid_type_file{
        File f = new File(dir, name);
        if (!f.exists()) throw new no_such_file();
        if (!f.isDirectory()) throw new invalid_type_file();

        try {
            org.omg.CORBA.Object alloc = poa_.servant_to_reference(new directoryImpl(f, poa_));
            d.value = directoryHelper.narrow(alloc);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void create_regular_file(regular_fileHolder r, String name) throws already_exist {
        File f = new File(dir, name);
        if (f.exists()) throw new already_exist();

        try {
            f.createNewFile();
            org.omg.CORBA.Object alloc = poa_.servant_to_reference(new regular_fileImpl(f, mode.read_write));
            r.value = regular_fileHelper.narrow(alloc);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void create_directory(directoryHolder d, String name) throws already_exist{
        File f = new File(dir, name);
        if (f.exists()) throw new already_exist();

        f.mkdir();
        try {
            org.omg.CORBA.Object alloc = poa_.servant_to_reference(new directoryImpl(f, poa_));
            d.value = directoryHelper.narrow(alloc);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void delete_file(String name) throws no_such_file {
        File f = new File(dir, name);
        if (!f.exists()) throw new no_such_file();

        recursiveDelete(f);
    }

    private void recursiveDelete(File f) {
        if(f.isDirectory()) {
            for(File subFile : f.listFiles()) {
                recursiveDelete(subFile);
            }
        }
        f.delete();
    }

    public int list_files(file_listHolder l) {
        String[] list = dir.list();

        try {
            org.omg.CORBA.Object alloc = poa_.servant_to_reference(new file_listImpl(dir, list));
            l.value = file_listHelper.narrow(alloc);
        } catch(Exception e) {
            System.out.println(e);
        }

        return list.length;
    }
}
