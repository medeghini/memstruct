package com.nextbreakpoint.memstruct;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.nextbreakpoint.memstruct.MemStruct.BytesOrdering.ORDERING_BIG_ENDIAN;
import static com.nextbreakpoint.memstruct.MemStruct.BytesOrdering.ORDERING_LITTLE_ENDIAN;
import static com.nextbreakpoint.memstruct.MemStruct.BytesPadding.*;
import static com.nextbreakpoint.memstruct.MemStruct.LongSize.LONG_SIZE_16BIT;
import static com.nextbreakpoint.memstruct.MemStruct.LongSize.LONG_SIZE_32BIT;
import static com.nextbreakpoint.memstruct.MemStruct.LongSize.LONG_SIZE_64BIT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MemStructTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReturnCorrectSizeOfByte() throws MemStructException {
        assertThat(new MemStruct("label dc.b 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnCorrectSizeOfWord() throws MemStructException {
        assertThat(new MemStruct("label dc.w 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
    }

    @Test
    public void shouldReturnCorrectSizeOfShort() throws MemStructException {
        assertThat(new MemStruct("label dc.s 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
        assertThat(new MemStruct("label dc.s 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
        assertThat(new MemStruct("label dc.s 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
    }

    @Test
    public void shouldReturnCorrectSizeOfInt() throws MemStructException {
        assertThat(new MemStruct("label dc.i 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
        assertThat(new MemStruct("label dc.i 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
        assertThat(new MemStruct("label dc.i 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(4)));
    }

    @Test
    public void shouldReturnCorrectSizeOfLong() throws MemStructException {
        assertThat(new MemStruct("label dc.l 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(2)));
        assertThat(new MemStruct("label dc.l 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(4)));
        assertThat(new MemStruct("label dc.l 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(8)));
    }

    @Test
    public void shouldReturnCorrectSizeOfByteArray() throws MemStructException {
        assertThat(new MemStruct("label ds.b 5", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(5)));
    }

    @Test
    public void shouldReturnCorrectSizeOfWordArray() throws MemStructException {
        assertThat(new MemStruct("label ds.w 5", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
    }

    @Test
    public void shouldReturnCorrectSizeOfShortArray() throws MemStructException {
        assertThat(new MemStruct("label ds.s 5", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
        assertThat(new MemStruct("label ds.s 5", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
        assertThat(new MemStruct("label ds.s 5", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
    }

    @Test
    public void shouldReturnCorrectSizeOfIntArray() throws MemStructException {
        assertThat(new MemStruct("label ds.i 5", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
        assertThat(new MemStruct("label ds.i 5", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
        assertThat(new MemStruct("label ds.i 5", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(20)));
    }

    @Test
    public void shouldReturnCorrectSizeOfLongArray() throws MemStructException {
        assertThat(new MemStruct("label ds.l 5", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(10)));
        assertThat(new MemStruct("label ds.l 5", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(20)));
        assertThat(new MemStruct("label ds.l 5", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(40)));
    }

    @Test
    public void shouldReturnCorrectSize() throws MemStructException {
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.w 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).sizeOf(), is(equalTo(3)));
    }

    @Test
    public void shouldReturnCorrectValueOfByte() throws MemStructException {
        assertThat(new MemStruct("label dc.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label"), is(equalTo(10L)));
    }

    @Test
    public void shouldReturnCorrectValueOfWord() throws MemStructException {
        assertThat(new MemStruct("label dc.w 20", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label"), is(equalTo(20L)));
    }

    @Test
    public void shouldReturnCorrectValueOfShort() throws MemStructException {
        assertThat(new MemStruct("label dc.s 30", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label"), is(equalTo(30L)));
    }

    @Test
    public void shouldReturnCorrectValueOfInt() throws MemStructException {
        assertThat(new MemStruct("label dc.i 50000", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label"), is(equalTo(50000L)));
    }

    @Test
    public void shouldReturnCorrectValueOfLong() throws MemStructException {
        assertThat(new MemStruct("label dc.l 100000", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label"), is(equalTo(100000L)));
    }

    @Test
    public void shouldChangeValueOfByte() throws MemStructException {
        MemStruct ms = new MemStruct("label dc.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.set("label", 20);
        assertThat(ms.get("label"), is(equalTo(20L)));
    }

    @Test
    public void shouldChangeValueOfWord() throws MemStructException {
        MemStruct ms = new MemStruct("label dc.w 20", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.set("label", 30);
        assertThat(ms.get("label"), is(equalTo(30L)));
    }

    @Test
    public void shouldChangeValueOfShort() throws MemStructException {
        MemStruct ms = new MemStruct("label dc.s 30", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.set("label", 40);
        assertThat(ms.get("label"), is(equalTo(40L)));
    }

    @Test
    public void shouldChangeValueOfInt() throws MemStructException {
        MemStruct ms = new MemStruct("label dc.i 50000", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.set("label", 60000);
        assertThat(ms.get("label"), is(equalTo(60000L)));
    }

    @Test
    public void shouldChangeValueOfLong() throws MemStructException {
        MemStruct ms = new MemStruct("label dc.l 100000", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.set("label", 200000);
        assertThat(ms.get("label"), is(equalTo(200000L)));
    }

    @Test
    public void shouldChangeValueOfByteArray() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        for (int i = 0; i < 10; i++) ms.set("label", 20 + i, i);
        for (int i = 0; i < 10; i++) assertThat(ms.get("label", i), is(equalTo(20L + i)));
    }

    @Test
    public void shouldChangeValueOfWordArray() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.w 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        for (int i = 0; i < 10; i++) ms.set("label", 20 + i, i);
        for (int i = 0; i < 10; i++) assertThat(ms.get("label", i), is(equalTo(20L + i)));
    }

    @Test
    public void shouldChangeValueOfShortArray() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.s 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        for (int i = 0; i < 10; i++) ms.set("label", 20 + i, i);
        for (int i = 0; i < 10; i++) assertThat(ms.get("label", i), is(equalTo(20L + i)));
    }

    @Test
    public void shouldChangeValueOfIntArray() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.i 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        for (int i = 0; i < 10; i++) ms.set("label", 20 + i, i);
        for (int i = 0; i < 10; i++) assertThat(ms.get("label", i), is(equalTo(20L + i)));
    }

    @Test
    public void shouldChangeValueOfLongArray() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.l 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        for (int i = 0; i < 10; i++) ms.set("label", 20 + i, i);
        for (int i = 0; i < 10; i++) assertThat(ms.get("label", i), is(equalTo(20L + i)));
    }

    @Test
    public void shouldChangeValueOfString() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.b 4", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.setString("label", "ABCDEF");
        byte[] bytes = "ABCDEF".getBytes();
        assertThat(ms.get("label", 0), is(equalTo(new Long(bytes[0]))));
        assertThat(ms.get("label", 1), is(equalTo(new Long(bytes[1]))));
        assertThat(ms.get("label", 2), is(equalTo(new Long(bytes[2]))));
        assertThat(ms.get("label", 3), is(equalTo(new Long(bytes[3]))));
    }

    @Test
    public void shouldReturnString() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.setString("label", "ABCDEF");
        assertThat(ms.getString("label"), is(equalTo("ABCDEF\0\0\0\0")));
    }

    @Test
    public void shouldReturnCString() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.setString("label", "ABCD\0EF");
        assertThat(ms.getCString("label"), is(equalTo("ABCD")));
    }

    @Test
    public void shouldReturnBytes() throws MemStructException {
        MemStruct ms = new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        ms.setString("label", "ABCD\0EF");
        assertThat(ms.getBytes(), is(equalTo("ABCD\0EF\0\0\0".getBytes())));
    }

    @Test
    public void shouldReturnOffset() throws MemStructException {
        MemStruct ms = new MemStruct("label1 ds.b 10\nlabel2 dc.b 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE);
        assertThat(ms.getOffset("label2"), is(equalTo(10L)));
    }

    @Test
    public void shouldThrowExceptionWhenLabelDoesNotExist() throws MemStructException {
        exception.expect(MemStructException.class);
        new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).getOffset("label2");
    }

    @Test
    public void shouldThrowExceptionWhenOffsetIsOutOfBounds() throws MemStructException {
        exception.expect(MemStructException.class);
        new MemStruct("label ds.b 10", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).get("label", 20);
    }

    @Test
    public void shouldRespectBytesOrdering() throws MemStructException {
        assertThat(new MemStruct("label dc.w 2000", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).getBytes(), is(equalTo(new byte[] { -48, 7 })));
        assertThat(new MemStruct("label dc.w 2000", LONG_SIZE_16BIT, ORDERING_BIG_ENDIAN, PADDING_NONE).getBytes(), is(equalTo(new byte[] { 7, -48 })));
    }

    @Test
    public void shouldRespectBytesPadding() throws MemStructException {
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.w 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_NONE).getOffset("label2"), is(equalTo(1L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.w 0", LONG_SIZE_16BIT, ORDERING_LITTLE_ENDIAN, PADDING_SHORT).getOffset("label2"), is(equalTo(2L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.w 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_INT).getOffset("label2"), is(equalTo(2L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.w 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_LONG).getOffset("label2"), is(equalTo(2L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.i 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_INT).getOffset("label2"), is(equalTo(2L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.i 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_LONG).getOffset("label2"), is(equalTo(4L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.l 0", LONG_SIZE_32BIT, ORDERING_LITTLE_ENDIAN, PADDING_INT).getOffset("label2"), is(equalTo(4L)));
        assertThat(new MemStruct("label1 dc.b 0\nlabel2 dc.l 0", LONG_SIZE_64BIT, ORDERING_LITTLE_ENDIAN, PADDING_LONG).getOffset("label2"), is(equalTo(8L)));
    }
}
