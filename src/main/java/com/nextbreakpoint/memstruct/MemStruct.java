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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import static com.nextbreakpoint.memstruct.MemStruct.BytesOrdering.*;
import static com.nextbreakpoint.memstruct.MemStruct.BytesPadding.*;
import static com.nextbreakpoint.memstruct.MemStruct.LongSize.*;

/**
 * Structure definition syntax is quite similar to what we usually see in macro assembers:
 * <p>
 * label		dc|ds.b|w|s|i|l	  data
 * <p>
 * Examples:
 * <p>
 * databyte	    dc.b			0x17
 * mylong		dc.l			0x1234
 * short		dc.s			121
 * integer		dc.i			1000
 * word		    dc.w			0
 * space		ds.w			20
 * <p>
 * where
 * dc = declare constant value
 * ds = declare (type.sizeof * data) space bytes
 * <p>
 * .b = byte		(1 byte)
 * .w = word		(2 bytes)
 * .s = short		(2 bytes)
 * .i = int		    (2 or 4 bytes)
 * .l = long		(2, 4 or 8 bytes)
 */
public final class MemStruct {
    // declaration statements
    private final static int DC = 0;
    private final static int DS = 1;

    // here we keep our mstruct label table (key = the label, value = StructItem)
    private HashMap<String, StructItem> mstruct = new HashMap<String, StructItem>();

    // here we keep our list of labels
    private List<String> mlabels = new ArrayList<String>();

    // here we keep our data
    private byte[] mdata;

    // the total data size
    private int mdataSize = 0;

    // the long int size
    private LongSize mdataLongSize = LONG_SIZE_64BIT;

    // the memory ordering
    private BytesOrdering mdataOrdering = ORDERING_BIG_ENDIAN;

    // the memory padding
    private BytesPadding mdataPadding = PADDING_NONE;

    /**
     * Build a memstuct. Only the constructor does. Just for security reasons,
     * we want to avoid that someone changes the memstruct on the fly.
     */
    public MemStruct(String definition, LongSize longsize, BytesOrdering ordering, BytesPadding padding) throws MemStructException {
        mdataLongSize = longsize;
        mdataOrdering = ordering;
        mdataPadding = padding;

        mdataSize = parseStruct(definition);

        if (mdataSize > 0) {
            // allocate data
            mdata = new byte[mdataSize];

            // create data
            for (StructItem si : mstruct.values()) put(si);
        }
    }

    /**
     * Returns the contents of the struct as an array of bytes.
     */
    public byte[] getBytes() {
        return mdata;
    }

    /**
     * Gets the offset in bytes of a given label.
     */
    public long getOffset(String label) throws MemStructException {
        StructItem si = findItem(label);

        return si.getOffset();
    }

    /**
     * Gets the size of the given struct item, padding excluded.
     */
    public long getSize(String label) throws MemStructException {
        StructItem si = findItem(label);

        return si.getTotalSize();
    }

    private StructItem findItem(String label) throws MemStructException {
        StructItem si = mstruct.get(label);

        if (si == null) {
            throw new MemStructException("undefined label " + label);
        }

        return si;
    }

    /**
     * Parse a memstuct definition and fill the hashmap.
     */
    private int parseStruct(String definition) throws MemStructException {
        StringTokenizer st = new StringTokenizer(definition);

        int offset = 0;

        // parse definition
        while (st.hasMoreTokens()) {
            String label = st.nextToken();

            String statement = st.nextToken();

            String data = st.nextToken();

            int declaration = 0;

            // parse declaration
            if (statement.startsWith("dc")) {
                declaration = DC;
            } else if (statement.startsWith("ds")) {
                declaration = DS;
            } else {
                throw new MemStructException("invalid declaration");
            }

            int size = 0;

            if (statement.endsWith(".b")) {
                size = 1;
            } else if (statement.endsWith(".w")) {
                size = 2;
            } else if (statement.endsWith(".s")) {
                size = 2;
            } else if (statement.endsWith(".i")) {
                if (mdataLongSize == LONG_SIZE_16BIT) {
                    size = 2;
                } else if (mdataLongSize == LONG_SIZE_32BIT) {
                    size = 2;
                } else if (mdataLongSize == LONG_SIZE_64BIT) {
                    size = 4;
                }
            } else if (statement.endsWith(".l")) {
                if (mdataLongSize == LONG_SIZE_16BIT) {
                    size = 2;
                } else if (mdataLongSize == LONG_SIZE_32BIT) {
                    size = 4;
                } else if (mdataLongSize == LONG_SIZE_64BIT) {
                    size = 8;
                }
            } else {
                throw new MemStructException("invalid declaration type");
            }

            long value = Integer.decode(data).longValue();

            if (declaration == DC) {
                switch (mdataPadding) {
                    case PADDING_NONE: {
                        break;
                    }

                    case PADDING_SHORT: {
                        if (size == 2) {
                            offset += (2 - offset % 2) % 2;
                        } else if (size == 4) {
                            offset += (2 - offset % 2) % 2;
                        } else if (size == 8) {
                            offset += (2 - offset % 2) % 2;
                        }

                        break;
                    }

                    case PADDING_INT: {
                        if (size == 2) {
                            offset += (2 - offset % 2) % 2;
                        } else if (size == 4) {
                            offset += (4 - offset % 4) % 4;
                        } else if (size == 8) {
                            offset += (4 - offset % 4) % 4;
                        }

                        break;
                    }

                    case PADDING_LONG: {
                        if (size == 2) {
                            offset += (2 - offset % 2) % 2;
                        } else if (size == 4) {
                            offset += (4 - offset % 4) % 4;
                        } else if (size == 8) {
                            offset += (8 - offset % 8) % 8;
                        }

                        break;
                    }

                    default:
                        break;
                }
            }

            // prepare item
            StructItem si = new StructItem(label, declaration, size, offset, value);

            // compute offset
            switch (declaration) {
                case (DC): {
                    offset += size;

                    break;
                }

                case (DS): {
                    offset += (int) (size * value);

                    break;
                }

                default:
                    break;
            }

            if (mstruct.get(label) == null) {
                mstruct.put(label, si);
            } else {
                throw new MemStructException("duplicated label");
            }

            mlabels.add(label);
        }

        // set total data size
        mdataSize = offset;

        return mdataSize;
    }

    private void put(StructItem si) {
        long value = si.getValue();

        // only put DC data
        if (si.getDeclaration() == DC) {
            for (int i = 0; i < si.getTypeSize(); i++) {
                if (mdataOrdering == ORDERING_BIG_ENDIAN) {
                    mdata[si.getOffset() + si.getTypeSize() - i - 1] = (byte) (value & 0xFF);
                } else {
                    mdata[si.getOffset() + i] = (byte) (value & 0xFF);
                }

                value = value >> 8;
            }
        }
    }

    /**
     * Sets a value into the struct, given the label.
     */
    public void set(String label, long value) throws MemStructException {
        StructItem si = findItem(label);

        // only set DC data
        if (si.getDeclaration() == DC) {
            si.setValue(value);

            put(si);
        }
    }

    /**
     * Sets a value into the struct, given the label and the offset relative to the label.
     */
    public void set(String label, long value, int offset) throws MemStructException {
        StructItem si = findItem(label);

        try {
            for (int i = 0; i < si.getTypeSize(); i++) {
                if (mdataOrdering == ORDERING_BIG_ENDIAN) {
                    mdata[si.getOffset() + (1 + offset) * si.getTypeSize() - i - 1] = (byte) (value & 0xFF);
                } else {
                    mdata[si.getOffset() + offset * si.getTypeSize() + i] = (byte) (value & 0xFF);
                }

                value = value >> 8;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MemStructException("memory offset out of bounds ");
        }
    }

    /**
     * Gets a value from the struct, given the label.
     */
    public long get(String label) throws MemStructException {
        StructItem si = findItem(label);

        long value = 0;

        // only get DC data
        if (si.getDeclaration() == DC) {
            for (int i = 0; i < si.getTypeSize(); i++) {
                value = value << 8;

                byte b = 0;

                if (mdataOrdering == ORDERING_BIG_ENDIAN) {
                    b = mdata[si.getOffset() + i];
                } else {
                    b = mdata[si.getOffset() + si.getTotalSize() - i - 1];
                }

                value |= (b << 56) >>> 56;
            }
        }

        return value;
    }

    /**
     * Gets a value from the struct, given the label and the offset relative to the label.
     */
    public long get(String label, int offset) throws MemStructException {
        StructItem si = findItem(label);

        long value = 0;

        try {
            for (int i = 0; i < si.getTypeSize(); i++) {
                value = value << 8;

                byte b = 0;

                if (mdataOrdering == ORDERING_BIG_ENDIAN) {
                    b = mdata[si.getOffset() + offset * si.getTypeSize() + i];
                } else {
                    b = mdata[si.getOffset() + (1 + offset) * si.getTypeSize() - i - 1];
                }

                value |= (b << 56) >>> 56;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MemStructException("memory offset out of bounds");
        }

        return value;
    }

    /**
     * Sets the size of the whole struct (bytes) padding included.
     */
    public int sizeOf() {
        return mdataSize;
    }

    /**
     * Dumps the struct to a human readable text string.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < mlabels.size(); i++) {
            builder.append(mstruct.get(mlabels.get(i)));
            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * Copies an array of bytes into the struct. Returns the number of bytes copied.
     */
    public int setBytes(byte[] data) {
        int size = Math.min(data.length, mdata.length);

        System.arraycopy(data, 0, mdata, 0, size);

        return size;
    }

    /**
     * Gets the string at the given label.
     */
    public String getString(String label) throws MemStructException {
        StructItem si = findItem(label);

        // get data info
        int offset = si.getOffset();
        int size = si.getTotalSize();

        // create the string
        if (size > 0) {
            return new String(mdata, offset, size);
        } else {
            return null;
        }
    }

    /**
     * Gets the "C" string at the given label.
     */
    public String getCString(String label) throws MemStructException {
        String s = getString(label);

        if (s != null) {
            int idx = s.indexOf('\0');

            if (idx == -1) {
                return s;
            } else {
                return s.substring(0, idx);
            }
        } else {
            return null;
        }
    }

    /**
     * Sets a string at the given label. Returns the number of bytes copied.
     */
    public int setString(String label, String data) throws MemStructException {
        StructItem si = findItem(label);

        // get data info
        int offset = si.getOffset();
        int size = si.getTotalSize();

        // get string info
        int dataSize = (data != null ? data.length() : 0);

        // fit to size
        dataSize = Math.min(size, dataSize);

        // copy the string
        if (dataSize > 0) {
            // clear the whole space
            for (int i = dataSize - 1; i < size; i++) mdata[i + offset] = 0;

            // copy the string
            System.arraycopy(data.getBytes(), 0, mdata, offset, dataSize);
        }

        return dataSize;
    }

    // long int size
    public enum LongSize {
        LONG_SIZE_16BIT,
        LONG_SIZE_32BIT,
        LONG_SIZE_64BIT;
    }

    // memory ordering
    public enum BytesOrdering {
        ORDERING_LITTLE_ENDIAN,
        ORDERING_BIG_ENDIAN
    }

    // memory padding
    public enum BytesPadding {
        PADDING_NONE,
        PADDING_SHORT,
        PADDING_INT,
        PADDING_LONG
    }

    // an item in the struct
    private class StructItem {
        private String label;
        private int declaration;
        private int offset;
        private int size;
        private long value;

        public StructItem(String label, int declaration, int size, int offset, long value) {
            this.declaration = declaration;
            this.offset = offset;
            this.size = size;
            this.label = label;
            this.value = value;
        }

        public int getDeclaration() {
            return declaration;
        }

        public int getOffset() {
            return offset;
        }

        public int getTotalSize() {
            if (declaration == DC) {
                return size;
            } else {
                return (int) (size * value);
            }
        }

        public int getTypeSize() {
            return size;
        }

        public String getLabel() {
            return label;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            if (declaration == DC) {
                this.value = value;
            }
        }

        public String toString() {
            if (declaration == DC) {
                return "[" + offset + "] size=" + (size * 8) + " bits, label=" + label + ", value=" + value;
            } else {
                return "[" + offset + "] size=" + (size * value) + " bytes, label=" + label;
            }
        }
    }
}