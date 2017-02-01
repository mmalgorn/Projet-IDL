package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;

public class file_listImpl extends file_listPOA {

    String[] list;
    int index;
    File dir;

    public file_listImpl(File dir, String[] list) {
        this.list = list;
        this.dir = dir;
        index = 0;
    }

    public boolean next_one(directory_entryHolder e) {
        File f = new File(dir, list[index++]);
        e.value.name = f.getName();

        if(f.isFile()) e.value.type = file_type.regular_file_type;
        else e.value.type = file_type.directory_type;

        if(index == list.length-1) return false;
        else return true;
    }

}
