package q2p.tagsmanager.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Storage {
	public static final String readString(final DataInputStream dis) throws Exception {
		final byte[] buff = new byte[dis.readInt()];
		dis.read(buff);
		return new String(buff, Assist.UTF_8);
	}
	public static final void writeString(final DataOutputStream dos, final String string) throws IOException {
		final byte[] buff = string.getBytes(Assist.UTF_8);
		dos.writeInt(buff.length);
		dos.write(buff);
	}
	public static final String getFileContents(final String fileName) {
		try {
			final FileInputStream fis = new FileInputStream(fileName);
			Assist.storeCloseable(fis);
			byte[] buff = new byte[fis.available()];
			fis.read(buff);
			Assist.safeClose(fis);
			
			return new String(buff, StandardCharsets.UTF_8);
		} catch(final IOException e) {
			Assist.abort("Failed to read file: \""+fileName+"\"", e);
			return null;
		}
	}
	public static final String getResFileContents(final String fileName) {
		try {
			final InputStream is = Assist.class.getClassLoader().getResourceAsStream("res/"+fileName);
			if(is == null)
				throw new FileNotFoundException();
			Assist.storeCloseable(is);
			byte[] buff = new byte[is.available()];
			is.read(buff);
			Assist.safeClose(is);
			
			return new String(buff, StandardCharsets.UTF_8);
		} catch(final IOException e) {
			Assist.abort("Failed to read resource file: \""+fileName+"\"", e);
			return null;
		}
	}
}