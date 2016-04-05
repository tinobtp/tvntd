/*
 * Copyright (C) 2006-2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tvntd.lib;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;

/**
 * A (possibly mutable) SHA-1 abstraction.
 * <p/>
 * If this is an instance of {@link MutableObjectId} the concept of equality
 * with this instance can alter at any time, if this instance is modified to
 * represent a different object name.
 */
public abstract class AnyObjectId
{
    private static final byte[] hexbyte = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    private static final char[] hexchar = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    protected int m_w1;
    protected int m_w2;
    protected int m_w3;
    protected int m_w4;
    protected int m_w5;

    protected AnyObjectId() {}

    /**
     * Compare to object identifier byte sequences for equality.
     *
     * @param first  the first identifier to compare.
     * @param second the second identifier to compare.
     * @return true if the two identifiers are the same.
     */
    public static boolean equals(final AnyObjectId first, final AnyObjectId second)
    {
        assert first != null;
        assert second != null;

        if (first == second) {
            return true;
        }
        // We test word 2 first as odds are someone already used our
        // word 1 as a hash code, and applying that came up with these
        // two instances we are comparing for equality. Therefore the
        // first two words are very likely to be identical. We want to
        // break away from collisions as quickly as possible.
        //
        return (first.m_w2 == second.m_w2) &&
               (first.m_w3 == second.m_w3) &&
               (first.m_w4 == second.m_w4) &&
               (first.m_w5 == second.m_w5) &&
               (first.m_w1 == second.m_w1);
    }

    /**
     * Write integer value v to the output byte stream.
     *
     * @param w: the output byte stream.
     * @param v: the integer value to write.
     *
     * @throws IOException
     */
    private static void writeRawInt(final OutputStream w, int v) throws IOException
    {
        w.write(v >>> 24);
        w.write(v >>> 16);
        w.write(v >>> 8);
        w.write(v);
    }

    /**
     * Format the interger value w as hex to the byte array.
     *
     * @param dst: the destination byte array.
     * @param p:   the starting position at the byte array to write.
     * @param w:   the value to format.
     */
    private static void formatHexByte(final byte[] dst, final int p, int w)
    {
        int o = p + 7;

        while (o >= p && w != 0) {
            dst[o--] = hexbyte[w & 0xf];
            w >>>= 4;
        }
        while (o >= p) {
            dst[o--] = '0';
        }
    }

    /**
     * Format the integer value w as hex to the character array.
     *
     * @param dst: the destination char array.
     * @param p:   the starting position at the char array to write.
     * @param w:   the value to format.
     */
    static void formatHexChar(final char[] dst, final int p, int w)
    {
        int o = p + 7;
        while (o >= p && w != 0) {
            dst[o--] = hexchar[w & 0xf];
            w >>>= 4;
        }
        while (o >= p) {
            dst[o--] = '0';
        }
    }

    /**
     * Get the first 8 bits of the ObjectId.
     * <p/>
     * This is a faster version of {@code getByte(0)}.
     *
     * @return a discriminator usable for a fan-out style map. Returned values
     * are unsigned and thus are in the range [0,255] rather than the
     * signed byte range of [-128, 127].
     */
    public final int getFirstByte() {
        return m_w1 >>> 24;
    }

    /**
     * Get any byte from the ObjectId.
     *
     * Callers hard-coding {@code getByte(0)} should instead use the much faster
     * special case variant {@link #getFirstByte()}.
     *
     * @param index
     *     Index of the byte to obtain from the raw form of the ObjectId.
     *     Must be in range [0, {@link Constants#OBJECT_ID_LENGTH}).
     * @return the value of the requested byte at {@code index}. Returned values
     *     are unsigned and thus are in the range [0,255] rather than the
     *     signed byte range of [-128, 127].
     * @throws IllegalArgumentException
     *     {@code index} is less than 0, equal to
     *     {@link Constants#OBJECT_ID_LENGTH}, or greater than
     *     {@link Constants#OBJECT_ID_LENGTH}.
     */
    public final int getByte(int index) throws IllegalArgumentException
    {
        int w;
        switch (index >> 2) {
        case 0:
            w = m_w1;
            break;
        case 1:
            w = m_w2;
            break;
        case 2:
            w = m_w3;
            break;
        case 3:
            w = m_w4;
            break;
        case 4:
            w = m_w5;
            break;
        default:
            throw new IllegalArgumentException("Invalid index " + index);
        }
        return (w >>> (8 * (3 - (index & 3)))) & 0xff;
    }

    /**
     * Compare this ObjectId to a network-byte-order ObjectId.
     *
     * @param bs
     *     array containing the other ObjectId in network byte order.
     * @param p
     *     position within {@code bs} to start the compare at. At least
     *     20 bytes, starting at this position are required.
     * @return a negative integer, zero, or a positive integer as this object is
     *     less than, equal to, or greater than the specified object.
     */
    public final int compareTo(final byte[] bs, final int p)
    {
        int cmp = NB.compareUInt32(m_w1, NB.decodeInt32(bs, p));
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w2, NB.decodeInt32(bs, p + 4));
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w3, NB.decodeInt32(bs, p + 8));
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w4, NB.decodeInt32(bs, p + 12));
        if (cmp != 0) {
            return cmp;
        }
        return NB.compareUInt32(m_w5, NB.decodeInt32(bs, p + 16));
    }

    /**
     * Compare this ObjectId to another and obtain a sort ordering.
     *
     * @param other the other id to compare to. Must not be null.
     * @return &lt; 0 if this id comes before other; 0 if this id is equal to
     * other; &gt; 0 if this id comes after other.
     */
    public final int compareTo(final AnyObjectId rhs)
    {
        AnyObjectId other = (AnyObjectId)rhs;

        if (this == other) {
            return 0;
        }
        int cmp = NB.compareUInt32(this.m_w1, other.m_w1);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w2, other.m_w2);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w3, other.m_w3);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w4, other.m_w4);
        if (cmp != 0) {
            return cmp;
        }
        return NB.compareUInt32(m_w5, other.m_w5);
    }

    /**
     * Compare this ObjectId to a network-byte-order ObjectId.
     *
     * @param bs array containing the other ObjectId in network byte order.
     * @param p  position within {@code bs} to start the compare at. At least 5
     *           integers, starting at this position are required.
     * @return a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    public final int compareTo(final int[] bs, final int p)
    {
        int cmp = NB.compareUInt32(m_w1, bs[p]);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w2, bs[p + 1]);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w3, bs[p + 2]);
        if (cmp != 0) {
            return cmp;
        }
        cmp = NB.compareUInt32(m_w4, bs[p + 3]);
        if (cmp != 0) {
            return cmp;
        }
        return NB.compareUInt32(m_w5, bs[p + 4]);
    }

    /**
     * Tests if this ObjectId starts with the given abbreviation.
     *
     * @param abbr: the abbreviation.
     * @return true if this ObjectId begins with the abbreviation; else false.
     */
    public final boolean startsWith(final AbbreviatedObjectId abbr) {
        return abbr.prefixCompare(this) == 0;
    }

    public final int hashCode() {
        return m_w2;
    }

    /**
     * Determine if this ObjectId has exactly the same value as another.
     *
     * @param other
     * @return true only if both ObjectIds have identical bits.
     */
    public final boolean equals(final AnyObjectId other)
    {
        return other != null ? equals(this, other) : false;
    }

    /**
     * {@inheritDoc}
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(final Object o)
    {
        if (o instanceof AnyObjectId) {
            return equals((AnyObjectId) o);
        }
        return false;
    }

    /**
     * Copy this ObjectId to an output writer in raw binary.
     *
     * @param w: the buffer to copy to. Must be in big endian order.
     */
    public final void copyRawTo(final ByteBuffer w)
    {
        w.putInt(m_w1);
        w.putInt(m_w2);
        w.putInt(m_w3);
        w.putInt(m_w4);
        w.putInt(m_w5);
    }

    /**
     * Copy this ObjectId to a byte array.
     *
     * @param b the buffer to copy to.
     * @param o the offset within b to write at.
     */
    public final void copyRawTo(final byte[] b, final int o)
    {
        NB.encodeInt32(b, o +  0, m_w1);
        NB.encodeInt32(b, o +  4, m_w2);
        NB.encodeInt32(b, o +  8, m_w3);
        NB.encodeInt32(b, o + 12, m_w4);
        NB.encodeInt32(b, o + 16, m_w5);
    }

    /**
     * Copy this ObjectId to an int array.
     *
     * @param b the buffer to copy to.
     * @param o the offset within b to write at.
     */
    public final void copyRawTo(final int[] b, final int o)
    {
        b[o + 0] = m_w1;
        b[o + 1] = m_w2;
        b[o + 2] = m_w3;
        b[o + 3] = m_w4;
        b[o + 4] = m_w5;
    }

    /**
     * Copy this ObjectId to an output writer in raw binary.
     *
     * @param w the stream to write to.
     * @throws IOException the stream writing failed.
     */
    public final void copyRawTo(final OutputStream w) throws IOException
    {
        writeRawInt(w, m_w1);
        writeRawInt(w, m_w2);
        writeRawInt(w, m_w3);
        writeRawInt(w, m_w4);
        writeRawInt(w, m_w5);
    }

    /**
     * Copy this ObjectId to an output writer in hex format.
     * @param w the stream to copy to.
     * @throws IOException
     */
    public final void copyTo(final OutputStream w) throws IOException {
        w.write(toHexByteArray());
    }

    /**
     * Copy this ObjectId to a byte array in hex format.
     *
     * @param b the buffer to copy to.
     * @param o the offset within b to write at.
     */
    public final void copyTo(byte[] b, int o)
    {
        formatHexByte(b, o +  0, m_w1);
        formatHexByte(b, o +  8, m_w2);
        formatHexByte(b, o + 16, m_w3);
        formatHexByte(b, o + 24, m_w4);
        formatHexByte(b, o + 32, m_w5);
    }

    /**
     * Copy this ObjectId to a ByteBuffer in hex format.
     *
     * @param b the buffer to copy to.
     */
    public final void copyTo(ByteBuffer b) {
        b.put(toHexByteArray());
    }

    protected byte[] toHexByteArray()
    {
        final byte[] dst = new byte[Constants.OBJECT_ID_STRING_LENGTH];
        formatHexByte(dst,  0, m_w1);
        formatHexByte(dst,  8, m_w2);
        formatHexByte(dst, 16, m_w3);
        formatHexByte(dst, 24, m_w4);
        formatHexByte(dst, 32, m_w5);
        return dst;
    }

    /**
     * Copy this ObjectId to an output writer in hex format.
     *
     * @param w the stream to copy to.
     * @throws IOException the stream writing failed.
     */
    public final void copyTo(final Writer w) throws IOException {
        w.write(toHexCharArray());
    }

    /**
     * Copy this ObjectId to an output writer in hex format.
     *
     * @param tmp temporary char array to buffer construct into before writing.
     *            Must be at least large enough to hold 2 digits for each byte
     *            of object id (40 characters or larger).
     * @param w   the stream to copy to.
     * @throws IOException the stream writing failed.
     */
    public final void copyTo(final char[] tmp, final Writer w) throws IOException
    {
        toHexCharArray(tmp);
        w.write(tmp, 0, Constants.OBJECT_ID_STRING_LENGTH);
    }

    /**
     * Copy this ObjectId to a StringBuilder in hex format.
     *
     * @param tmp temporary char array to buffer construct into before writing.
     *            Must be at least large enough to hold 2 digits for each byte
     *            of object id (40 characters or larger).
     * @param w   the string to append onto.
     */
    public final void copyTo(final char[] tmp, final StringBuilder w)
    {
        toHexCharArray(tmp);
        w.append(tmp, 0, Constants.OBJECT_ID_STRING_LENGTH);
    }

    /**
     * Copy content from the other object, changing this object id.
     */
    public final void copyFrom(final AnyObjectId cpy)
    {
        this.m_w1 = cpy.m_w1;
        this.m_w2 = cpy.m_w2;
        this.m_w3 = cpy.m_w3;
        this.m_w4 = cpy.m_w4;
        this.m_w5 = cpy.m_w5;
    }

    private char[] toHexCharArray()
    {
        final char[] dst = new char[Constants.OBJECT_ID_STRING_LENGTH];
        toHexCharArray(dst);
        return dst;
    }

    private void toHexCharArray(final char[] dst)
    {
        formatHexChar(dst,  0, m_w1);
        formatHexChar(dst,  8, m_w2);
        formatHexChar(dst, 16, m_w3);
        formatHexChar(dst, 24, m_w4);
        formatHexChar(dst, 32, m_w5);
    }

    @SuppressWarnings("nls")
    @Override
    public String toString()
    {
        return "AnyObjectId[" + name() + "]";
    }

    /**
     * @return string form of this object in lower case hex.
     */
    public final String name() {
        return new String(toHexCharArray());
    }

    /**
     * Return an abbreviation (prefix) of this object SHA-1.
     * <p>
     * This implementation does not guarantee uniqueness. Callers should
     * instead use {@link ObjectReader#abbreviate(AnyObjectId, int)} to obtain a
     * unique abbreviation within the scope of a particular object database.
     *
     * @param len length of the abbreviated string.
     * @return SHA-1 abbreviation.
     */
    public final AbbreviatedObjectId abbreviate(final int len)
    {
        final int a = AbbreviatedObjectId.mask(len, 1, m_w1);
        final int b = AbbreviatedObjectId.mask(len, 2, m_w2);
        final int c = AbbreviatedObjectId.mask(len, 3, m_w3);
        final int d = AbbreviatedObjectId.mask(len, 4, m_w4);
        final int e = AbbreviatedObjectId.mask(len, 5, m_w5);
        return new AbbreviatedObjectId(len, a, b, c, d, e);
    }

    /**
     * Obtain an immutable copy of this current object name value.
     * <p/>
     * Only returns <code>this</code> if this instance is an unsubclassed
     * instance of {@link ObjectId}; otherwise a new instance is returned
     * holding the same value.
     * <p/>
     * This method is useful to shed any additional memory that may be tied to
     * the subclass, yet retain the unique identity of the object id for future
     * lookups within maps and repositories.
     *
     * @return an immutable copy, using the smallest memory footprint possible.
     */
    public final ObjectId copy()
    {
        if (getClass() == ObjectId.class) {
            return (ObjectId) this;
        }
        return new ObjectId(this);
    }

    /**
     * Obtain an immutable copy of this current object name value.
     * <p/>
     * See {@link #copy()} if <code>this</code> is a possibly subclassed (but
     * immutable) identity and the application needs a lightweight identity
     * <i>only</i> reference.
     *
     * @return an immutable copy. May be <code>this</code> if this is already
     * an immutable instance.
     */
    public abstract ObjectId toObjectId();
}
