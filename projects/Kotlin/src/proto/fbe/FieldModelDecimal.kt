// Automatically generated by the Fast Binary Encoding compiler, do not modify!
// https://github.com/chronoxor/FastBinaryEncoding

package fbe;

import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.math.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import javafx.util.*;

// Fast Binary Encoding decimal field model class
class FieldModelDecimal(buffer: Buffer, offset: Long) : FieldModel(buffer, offset)
{
    // Field size
    override val FBESize: Long = 16

    fun get(defaults: BigDecimal = BigDecimal.valueOf(0L)): BigDecimal {
        if (_buffer.offset + FBEOffset + FBESize > _buffer.size)
            return defaults

        val magnitude = readBytes(FBEOffset, 12)
        val scale = readByte(FBEOffset + 14).toInt()
        val signum = if (readByte(FBEOffset + 15) < 0) -1 else 1

        // Reverse magnitude
        for (i in 0 until magnitude.size / 2) {
            val temp = magnitude[i]
            magnitude[i] = magnitude[magnitude.size - i - 1]
            magnitude[magnitude.size - i - 1] = temp
        }

        val unscaled = BigInteger(signum, magnitude)

        return BigDecimal(unscaled, scale)
    }

    // Set the value
    fun set(value: BigDecimal) {
        assert(_buffer.offset + FBEOffset + FBESize <= _buffer.size) { "Model is broken!" }
        if (_buffer.offset + FBEOffset + FBESize > _buffer.size)
            return

        // Get unscaled absolute value
        val unscaled = value.abs().unscaledValue()
        val bitLength = unscaled.bitLength()
        if (bitLength < 0 || bitLength > 96) {
            // Value too big for .NET Decimal (bit length is limited to [0, 96])
            write(FBEOffset, 0.toByte(), FBESize)
            return
        }

        // Get byte array
        val unscaledBytes = unscaled.toByteArray()

        // Get scale
        val scale = value.scale()
        if (scale < 0 || scale > 28) {
            // Value scale exceeds .NET Decimal limit of [0, 28]
            write(FBEOffset, 0.toByte(), FBESize)
            return
        }

        // Write unscaled value to bytes 0-11
        var index = 0
        var i = unscaledBytes.size - 1
        while (i >= 0 && index < 12) {
            write(FBEOffset + index, unscaledBytes[i])
            i--
            index++
        }

        // Fill remaining bytes with zeros
        while (index < 14) {
            write(FBEOffset + index, 0.toByte())
            index++
        }

        // Write scale at byte 14
        write(FBEOffset + 14, scale.toByte())

        // Write signum at byte 15
        write(FBEOffset + 15, (if (value.signum() < 0) -128 else 0).toByte())
    }
}
