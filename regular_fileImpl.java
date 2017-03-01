package files;

import org.omg.CORBA.*;
import java.lang.*;
import java.io.*;
import org.omg.PortableServer.*;

public class regular_fileImpl extends regular_filePOA {

	int offset, fileSize;
	File file;
	mode m;
	POA poa_;
	RandomAccessFile raf;

	public regular_fileImpl(File f, mode m, POA poa) {
		poa_ = poa;
		file = f;
		this.m = m;
		offset = 0;
		fileSize = new Long(file.length()).intValue();
		try {
			switch (m.value()) {
			case mode._read_write:
				raf = new RandomAccessFile(f, "rw");
			case mode._read_only:
				raf = new RandomAccessFile(f, "r");
				break;
			case mode._write_append:
				raf = new RandomAccessFile(f, "rw");
				offset = fileSize;
				break;
			case mode._write_trunc:
				raf = new RandomAccessFile(f, "rw");
				try {
					new PrintWriter(file).close();
				} catch (IOException e) {
					System.out.println(e);
				}
				break;
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public int read(int size, StringHolder data) throws end_of_file, invalid_operation {
		if (size > fileSize - offset)
			throw new end_of_file();
		if (m == mode.write_append || m == mode.write_trunc)
			throw new invalid_operation();

		if (size == -1)
			size = fileSize;
		byte[] buf = new byte[fileSize];
		int nbr = -1;
		try {
			nbr = raf.read(buf, offset, size);
			data.value = new String(buf);
		} catch (IOException e) {
			System.out.println(e);
		}

		if (nbr != -1)
			offset += size;
		return nbr;
	}

	public int write(int size, String data) throws invalid_operation {
		if (m == mode.read_only)
			throw new invalid_operation();

		try {
			raf.write(data.getBytes(), offset, size);
		} catch (IOException e) {
			System.out.println(e);
		}
		return size;
	}

	public void seek(int new_offset) throws invalid_offset, invalid_operation {
		if (new_offset > file.length())
			throw new invalid_offset();
		if (m == mode.write_append || m == mode.write_trunc)
			throw new invalid_operation();

		offset = new_offset;
		try {
			raf.seek(new_offset);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void close() {
		try {
			raf.close();
			byte[] ObjID = poa_.reference_to_id(poa_.servant_to_reference(this));
			poa_.deactivate_object(ObjID);
		} catch (Exception e) {
			System.out.println("POA Exception");
			e.printStackTrace(System.out);
		}
	}

}
