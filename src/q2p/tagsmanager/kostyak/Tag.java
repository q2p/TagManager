package q2p.tagsmanager.kostyak;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import q2p.tagsmanager.engine.Assist;
import q2p.tagsmanager.engine.Storage;

public final class Tag {
	public static ArrayList<Tag> tags;
	public static final int biggestLength;
	
	static {
		int max = 0;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream("tags.dat"));
			
			int i = dis.readInt();
			int j = -1; // TODO: стопкран
			tags = new ArrayList<Tag>(i);
			for(;i != 0 && j != 0; i--, j--) {
				if(tags.size() % 1000 == 0)
					System.out.println(tags.size());
				final Tag tag = new Tag(dis, tags.size());
				max = Math.max(max, tag.amount);
				tags.add(tag);
			}
			
			
			dis.close();
		} catch(final Exception e) {
			Assist.abort(e.getMessage());
		}
		biggestLength = (""+max).length();
	}
	
	public static final void initilize() {}
	
	public final byte state;
	public static final byte STATE_NOTHING = 0;
	public static final byte STATE_TAKE_A_LOOK = 1;
	public static final byte STATE_APPROVED = 2;
	public final int id;
	public final String name;
	public final LinkedList<Integer> aliases = new LinkedList<Integer>();
	public final LinkedList<String> aliasReason = new LinkedList<String>();
	public final LinkedList<Boolean> aliasApproved = new LinkedList<Boolean>();
	public final LinkedList<Integer> implies = new LinkedList<Integer>();
	public static final byte IMPLIES_STATE_PENDING = 0;
	public static final byte IMPLIES_STATE_CHECKED = 1;
	public static final byte IMPLIES_STATE_APPROVED = 2;
	public final LinkedList<Byte> impliesState = new LinkedList<Byte>();
	public final int amount;
	public final Type type;
	public final String description;
	
	public Tag(final DataInputStream dis, final int id) throws Exception {
		this.id = id;
		
		name = Storage.readString(dis);
		state = dis.readByte();
		amount = dis.readInt();
		type = Type.getById((byte) dis.readByte());
		
		description = Storage.readString(dis);
		
		for(int i = dis.readInt(); i != 0; i--) {
			aliases.addLast(dis.readInt());
			aliasApproved.addLast(dis.readBoolean());
			aliasReason.addLast(Storage.readString(dis));
		}
		
		for(int i = dis.readInt(); i != 0; i--) {
			implies.addLast(dis.readInt());
			impliesState.addLast(dis.readBoolean()?IMPLIES_STATE_CHECKED:IMPLIES_STATE_CHECKED);
		}
	}
	public final void write(final DataOutputStream dos) throws IOException {
		Storage.writeString(dos, name);
		dos.writeByte(state);
		dos.writeInt(amount);
		dos.writeByte(type.id);
		
		Storage.writeString(dos, description);
		
		dos.writeInt(aliases.size());
		while(!aliases.isEmpty()) {
			dos.writeInt(aliases.removeFirst());
			dos.writeBoolean(aliasApproved.removeFirst());
			Storage.writeString(dos, aliasReason.removeFirst());
		}

		dos.writeInt(implies.size());
		while(!implies.isEmpty()) {
			dos.writeInt(implies.removeFirst());
			dos.writeByte(impliesState.removeFirst());
		}
		
		dos.flush();
	}
	
	public enum Type {
		general  (0, 4, 7, 0, "general",   "Общий"),
		franchise(1, 0, 3, 8, "franchise", "Франшиза"),
		character(2, 3, 7, 8, "character", "Персонаж"),
		author   (3, 8, 3, 3, "author",    "Автор"),
		species  (4, 8, 7, 2, "species",   "Вид");

		public final byte id;
		public final String abbreviation;
		public final String name;
		public final short r, g, b;
		public final short dr, dg, db;
		public final String hexColor, hexDarkerColor;
		
		private Type(final int id, final int r, final int g, final int b, final String abbreviation, final String name) {
			this.id = (byte)id;
			this.r = (short)Math.max(0, 32*r-1);
			this.g = (short)Math.max(0, 32*g-1);
			this.b = (short)Math.max(0, 32*b-1);
			final float[] d = Color.RGBtoHSB(this.r, this.g, this.b, new float[3]);
			final Color c = Color.getHSBColor(d[0], d[1], d[2]*0.2f);
			this.dr = (short)c.getRed();
			this.dg = (short)c.getGreen();
			this.db = (short)c.getBlue();
			
			StringBuilder sb = new StringBuilder(6);
			sb.append(Assist.appendChars(2, Integer.toHexString(this.r), '0'));
			sb.append(Assist.appendChars(2, Integer.toHexString(this.g), '0'));
			sb.append(Assist.appendChars(2, Integer.toHexString(this.b), '0'));
			hexColor = sb.toString();
			sb = new StringBuilder(6);
			sb.append(Assist.appendChars(2, Integer.toHexString(this.dr), '0'));
			sb.append(Assist.appendChars(2, Integer.toHexString(this.dg), '0'));
			sb.append(Assist.appendChars(2, Integer.toHexString(this.db), '0'));
			hexDarkerColor = sb.toString();
			
			this.abbreviation = abbreviation;
			this.name = name;
		}
		
		public static final Type getById(final byte id) {
			for(final Type t : values())
				if(t.id == id)
					return t;
			
			return null;
		}
	}
}