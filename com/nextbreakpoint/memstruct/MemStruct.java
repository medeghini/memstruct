/*
 * MemStruct.java
 *
 * Design, concept, code
 * Additional code - Struct storage access
 * Additional code - Labelled string access
 * Copyright (c) 2001-2003 by Michele Puccini
 * http://www.classx.it
 * mik@classx.it
 *
 * Additional code - Endian support
 * Additional code - Padding support
 * Additional code - Exceptions support
 * Additional code - ds.x accessors
 * Additional code - Extensive test main()
 * Copyright (c) 2001-2016 by Andrea Medeghini
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation; either version 2.1 of the License, 
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package com.nextbreakpoint.memstruct;

import java.util.*;

/**
 * This is a simple implementation of basic ASM struct.
 *
 * StructDef syntax is quite similar to what we usually see in macro assembers
 * (see source code for details).
 */
 
//      Example:
//
//	<label>		<dc|ds>.<b|w|s|i|l>	<data>
//
//	databyte	dc.b			0x17
//	mylong		dc.l			0x1234
//	short		dc.s			121
//	integer		dc.i			1000
//	word		dc.w			0
//	space		ds.w			20
//
//	where
//	dc = declare constant value
//	ds = declare (type.sizeof * data) space bytes
//
//	.b = byte		(1 byte)
//	.w = word		(2 bytes)
//	.s = short		(2 bytes)
//	.i = int		(2 or 4 bytes)
//	.l = long		(2, 4 or 8 bytes)

public final class MemStruct
{
	// long int size
	public final static int LONG_SIZE_16BIT= 2;
	public final static int LONG_SIZE_32BIT= 4;
	public final static int LONG_SIZE_64BIT= 8;

	// memory ordering
	public final static int ORDERING_LITTLE_ENDIAN= 1;
	public final static int ORDERING_BIG_ENDIAN= 0;

	// memory padding
	public final static int PADDING_NONE= 0;
	public final static int PADDING_SHORT= 2;
	public final static int PADDING_INT= 4;
	public final static int PADDING_LONG= 8;

	// declaration statements
	private final static int DC= 0;
	private final static int DS= 1;

	// an item in the struct
	private class StructItem
	{
		private String label;

		private int declaration;
		private int offset;
		private int size;

		private long value;

		public StructItem(String l, int d, int s, int o, long v)
		{
			declaration= d;
			offset= o;
			size= s;

			label= l;

			value= v;
		}

		public int getDeclaration()
		{
			return (declaration);
		}

		public int getOffset()
		{
			return (offset);
		}

		public int getSize()
		{
			if (declaration == DC)
			{
				return (size);
			}
			else
			{
				return ((int) (size * value));
			}
		}

		public int getTypeSize()
		{
			return (size);
		}

		public long getValue()
		{
			return (value);
		}

		public void setValue(long v)
		{
			if (declaration == DC)
			{
				value= v;
			}
		}

		public String getLabel()
		{
			return (label);
		}

		public String toString()
		{
			if (declaration == DC)
			{
				return ("[" + offset + "] size=" + (size * 8) + " bits, value=" + value + ", label=" + label + "\n");
			}
			else
			{
				return ("[" + offset + "] size=" + (size * value) + " bytes, label=" + label + "\n");
			}
		}
	}

	// here we keep our mstruct label table.
	// key   = the label
	// value = StructItem class
	Hashtable mstruct= new Hashtable();

	Vector mlabels= new Vector();

	// here we keep our data
	byte[] mdata;

	// the total data size
	int mdata_size= 0;

	// the long int size
	int mdata_longsize= LONG_SIZE_64BIT;

	// the memory ordering
	int mdata_ordering= ORDERING_BIG_ENDIAN;

	// the memory padding
	int mdata_padding= PADDING_NONE;

	/**
	 * Build a memstuct. Only the constructor does. Just for security reasons,
	 * we want to avoid that someone changes the memstruct on the fly.
	 */
	public MemStruct(String definition, int longsize, int ordering, int padding) throws MemStructException
	{
		if ((longsize != LONG_SIZE_16BIT) && (longsize != LONG_SIZE_32BIT) && (longsize != LONG_SIZE_64BIT))
		{
			throw new MemStructException("invalid long int size!");
		}

		if ((ordering != ORDERING_LITTLE_ENDIAN) && (ordering != ORDERING_BIG_ENDIAN))
		{
			throw new MemStructException("invalid memory ordering!");
		}

		if ((padding != PADDING_NONE) && (padding != PADDING_SHORT) && (padding != PADDING_INT) && (padding != PADDING_LONG))
		{
			throw new MemStructException("invalid memory padding!");
		}

		mdata_longsize= longsize;

		mdata_ordering= ordering;

		mdata_padding= padding;

		mdata_size= parseStruct(definition);

		if (mdata_size > 0)
		{
			// allocate data
			mdata= new byte[mdata_size];

			// create data
			Enumeration e= mstruct.elements();

			while (e.hasMoreElements())
			{
				put((StructItem) e.nextElement());
			}
		}
	}

	/**
	 * Returns the contents of the struct as an array of bytes.
	 */
	public byte[] getBytes()
	{
		return (mdata);
	}

	/**
	 * Gets the offset in bytes of a given label.
	 */
	public long getOffset(String label) throws MemStructException
	{
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		return (si.getOffset());
	}

	/**
	 * Gets the size of the given struct item, padding excluded.
	 */
	public long getSize(String label) throws MemStructException
	{
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		return (si.getSize());
	}

	/**
	 * test main.
	 */
	public static void main(String[] args)
	{
		int i;

		String m= "";
		m += "byte     dc.b   1\n";
		m += "word     dc.w   2\n";
		m += "short    dc.s   3\n";
		m += "int      dc.i   4\n";
		m += "long     dc.l   5\n";
		m += "string1  ds.b   3\n";
		m += "array1   ds.w   3\n";
		m += "array2   ds.s   3\n";
		m += "array3   ds.i   3\n";
		m += "array4   ds.l   3\n";
		m += "string2  ds.b   10\n";

		try
		{
			MemStruct ms;

			System.out.println("declaration : \n\n" + m + "\n");

			System.out.println("ordering : little endian / padding : none / long int size : 16 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_16BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_NONE));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : none / long int size : 32 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_32BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_NONE));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : none / long int size : 64 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_NONE));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : short / long int size : 64 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_SHORT));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : int / long int size : 64 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_INT));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : long / long int size : 64 bit\n");

			System.out.println(ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_LONG));

			System.out.println("size : " + ms.sizeOf() + " bytes\n\n");

			System.out.println("ordering : little endian / padding : none / long int size : 64 bit\n");

			ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_LITTLE_ENDIAN, MemStruct.PADDING_NONE);
			
			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			ms.set("array1",5,1);
			ms.set("array1",10,2);
			ms.set("array2",15,1);
			ms.set("array2",20,2);
			ms.set("array3",25,1);
			ms.set("array3",30,2);
			ms.set("array4",35,1);
			ms.set("array4",40,2);

			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			System.out.println("ordering : big endian / padding : none / long int size : 64 bit\n");

			ms= new MemStruct(m, MemStruct.LONG_SIZE_64BIT, MemStruct.ORDERING_BIG_ENDIAN, MemStruct.PADDING_NONE);

			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			ms.set("array1",5,1);
			ms.set("array1",10,2);
			ms.set("array2",15,1);
			ms.set("array2",20,2);
			ms.set("array3",25,1);
			ms.set("array3",30,2);
			ms.set("array4",35,1);
			ms.set("array4",40,2);

			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			System.out.println(ms.setString("string1","ABCDE"));
			
			System.out.println(ms.getString("string1"));
			
			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			System.out.println(ms.setString("string2","ABCDEFG"));
			
			System.out.println(ms.getString("string2"));
			
			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			System.out.println(ms.setString("string2","FGHIL"));
			
			System.out.println(ms.getString("string2"));
			
			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");

			System.out.println(ms.getCString("string2"));
			
			for (i= 0; i < ms.getBytes().length - 1; i++)
			{
				System.out.print(ms.getBytes()[i] + ",");
			}

			System.out.println(ms.getBytes()[i] + "\n\n");
		}
		catch (MemStructException e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Parse a memstuct definition and fill the hashtable.
	 */
	private int parseStruct(String definition) throws MemStructException
	{
		String statement= "";
		String label= "";
		String data= "";

		int declaration= 0;
		int offset= 0;
		int size= 0;

		long value= 0;

		StructItem si;

		StringTokenizer st= new StringTokenizer(definition);

		// parse definition
		while (st.hasMoreTokens())
		{
			label= st.nextToken();

			statement= st.nextToken();

			data= st.nextToken();

			// parse declaration
			if (statement.startsWith("dc"))
			{
				declaration= DC;
			}
			else if (statement.startsWith("ds"))
			{
				declaration= DS;
			}
			else
			{
				throw new MemStructException("invalid declaration!");
			}

			if (statement.endsWith(".b"))
			{
				size= 1;
			}
			else if (statement.endsWith(".w"))
			{
				size= 2;
			}
			else if (statement.endsWith(".s"))
			{
				size= 2;
			}
			else if (statement.endsWith(".i"))
			{
				if (mdata_longsize == LONG_SIZE_16BIT)
				{
					size= 2;
				}
				else if (mdata_longsize == LONG_SIZE_32BIT)
				{
					size= 2;
				}
				else if (mdata_longsize == LONG_SIZE_64BIT)
				{
					size= 4;
				}
			}
			else if (statement.endsWith(".l"))
			{
				if (mdata_longsize == LONG_SIZE_16BIT)
				{
					size= 2;
				}
				else if (mdata_longsize == LONG_SIZE_32BIT)
				{
					size= 4;
				}
				else if (mdata_longsize == LONG_SIZE_64BIT)
				{
					size= 8;
				}
			}
			else
			{
				throw new MemStructException("invalid declaration type!");
			}

			value= Integer.decode(data).longValue();

			if (declaration == DC)
			{
				switch (mdata_padding)
				{
					case (PADDING_NONE) :
						{
							break;
						}

					case (PADDING_SHORT) :
						{
							if (size == 2)
							{
								offset += (2 - offset % 2) % 2;
							}
							else if (size == 4)
							{
								offset += (2 - offset % 2) % 2;
							}
							else if (size == 8)
							{
								offset += (2 - offset % 2) % 2;
							}

							break;
						}

					case (PADDING_INT) :
						{
							if (size == 2)
							{
								offset += (2 - offset % 2) % 2;
							}
							else if (size == 4)
							{
								offset += (4 - offset % 4) % 4;
							}
							else if (size == 8)
							{
								offset += (4 - offset % 4) % 4;
							}

							break;
						}

					case (PADDING_LONG) :
						{
							if (size == 2)
							{
								offset += (2 - offset % 2) % 2;
							}
							else if (size == 4)
							{
								offset += (4 - offset % 4) % 4;
							}
							else if (size == 8)
							{
								offset += (8 - offset % 8) % 8;
							}

							break;
						}

					default :
						break;
				}
			}

			// prepare item
			si= new StructItem(label, declaration, size, offset, value);

			// compute offset
			switch (declaration)
			{
				case (DC) :
					{
						offset += size;

						break;
					}

				case (DS) :
					{
						offset += (int) (size * value);

						break;
					}

				default :
					break;
			}

			if (mstruct.get(label) == null)
			{
				mstruct.put(label, si);
			}
			else
			{
				throw new MemStructException("duplicated label!");
			}

			mlabels.addElement(label);
		}

		// set total data size
		mdata_size= offset;

		return (mdata_size);
	}

	private void put(StructItem si)
	{
		long value= si.getValue();

		// only put DC data
		if (si.getDeclaration() == DC)
		{
			for (int i= 0; i < si.getTypeSize(); i++)
			{
				if (mdata_ordering == ORDERING_BIG_ENDIAN)
				{
					mdata[si.getOffset() + si.getTypeSize() - i - 1]= (byte) (value & 0xFF);
				}
				else
				{
					mdata[si.getOffset() + i]= (byte) (value & 0xFF);
				}

				value = value >> 8;
			}
		}
	}

	/**
	 * Sets a value into the struct, given the label.
	 */
	public void set(String label, long value) throws MemStructException
	{
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		// only set DC data
		if (si.getDeclaration() == DC)
		{
			si.setValue(value);

			put(si);
		}
	}

	/**
	 * Sets a value into the struct, given the label and the offset relative to the label.
	 */
	public void set(String label, long value, int offset) throws MemStructException
	{
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		try
		{
			for (int i= 0; i < si.getTypeSize(); i++)
			{
				if (mdata_ordering == ORDERING_BIG_ENDIAN)
				{
					mdata[si.getOffset() + (1 + offset) * si.getTypeSize() - i - 1]= (byte) (value & 0xFF);
				}
				else
				{
					mdata[si.getOffset() + offset * si.getTypeSize() + i]= (byte) (value & 0xFF);
				}

				value= value >> 8;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new MemStructException("memory offset out of bounds !");
		}
	}

	/**
	 * Gets a value from the struct, given the label.
	 */
	public long get(String label) throws MemStructException
	{
		long value= 0;

		byte b= 0;

		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		// only get DC data
		if (si.getDeclaration() == DC)
		{
			for (int i= 0; i < si.getTypeSize(); i++)
			{
				value= value << 8;

				if (mdata_ordering == ORDERING_BIG_ENDIAN)
				{
					b= mdata[si.getOffset() + i];
				}
				else
				{
					b= mdata[si.getOffset() + si.getSize() - i - 1];
				}

				value |= (b << 56) >>> 56;
			}
		}

		return (value);
	}

	/**
	 * Gets a value from the struct, given the label and the offset relative to the label.
	 */
	public long get(String label, int offset) throws MemStructException
	{
		long value= 0;

		byte b= 0;

		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		try
		{
			for (int i= 0; i < si.getTypeSize(); i++)
			{
				value= value << 8;

				if (mdata_ordering == ORDERING_BIG_ENDIAN)
				{
					b= mdata[si.getOffset() + offset * si.getTypeSize() + i];
				}
				else
				{
					b= mdata[si.getOffset() + (1 + offset) * si.getTypeSize() - i - 1];
				}

				value |= (b << 56) >>> 56;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new MemStructException("memory offset out of bounds !");
		}

		return (value);
	}

	/**
	 * Sets the size of the whole struct (bytes) padding included.
	 */
	public int sizeOf()
	{
		return (mdata_size);
	}

	/**
	 * Dumps the struct to a human readable text string.
	 */
	public String toString()
	{
		String data= "";

		for (int i= 0; i < mlabels.size(); i++)
		{
			data += ((StructItem) mstruct.get((String) mlabels.elementAt(i))).toString();
		}

		return (data);
	}

	/**
	 * Copies an array of bytes into the struct. Returns the number of bytes copied.
	 */
	public int setBytes(byte[] data)
	{
		int size = Math.min(data.length,mdata.length);
		
		System.arraycopy(data,0,mdata,0,size);
		
		return (size);
	}

	/**
	 * Gets the string at the given label.
	 */
	public String getString(String label) throws MemStructException
	{
		String s = null;
		
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		// get data info
		int offset = si.getOffset();
		int size  = si.getSize();

		// create the string
		if (size > 0)
		{
			s = new String(mdata,offset,size);
		}
		
		return s;
	}

	/**
	 * Gets the "C" string at the given label.
	 */
	public String getCString(String label) throws MemStructException
	{
		String s = getString(label);
		
		if (s != null)
		{
			int idx = s.indexOf('\0');
			
			if (idx == -1)
			{
				return s;
			}
			else
			{
				return s.substring(0,idx);
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Sets a string at the given label. Returns the number of bytes copied.
	 */
	public int setString(String label, String data) throws MemStructException
	{
		String s = null;
		
		StructItem si= ((StructItem) mstruct.get(label));

		if (si == null)
		{
			throw new MemStructException("undefined label " + label + "!");
		}

		// get data info
		int offset = si.getOffset();
		int size  = si.getSize();

		// get string info
		int data_size = (data != null ? data.length() : 0);

		// fit to size
		data_size = Math.min(size,data_size);
		
		// copy the string
		if (data_size > 0)
		{
			// clear the whole space
			for (int i = data_size - 1; i < size; i++) mdata[i+offset] = 0;

			// copy the string
			System.arraycopy(data.getBytes(),0,mdata,offset,data_size);
		}
		
		return data_size;
	}
}