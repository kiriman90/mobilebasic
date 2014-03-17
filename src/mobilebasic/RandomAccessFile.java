package mobilebasic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class RandomAccessFile implements DataInput, DataOutput {

    public RecordStore recordStore;
    public byte[] recordData;
    public boolean recordModified;
    public int currentRecord;
    public int magic;
    public int fileSize;
    public int fileOffset;
    public int recordSize = 256;
    private static final int MAGIC = 0x52410001; // File Format Magic (RA 0x0001)
    private static final int RESERVED = 8;
    private static final int RECORD_SIZE = 256;

    private void ReadRecord(int recordNumber) {
        try {
            recordData = recordStore.getRecord(recordNumber);
        } catch (RecordStoreException var2) {
            recordData = new byte[recordSize];
        }
    }

    private void WriteRecord(int recordNumber) {
        try {
            recordStore.setRecord(recordNumber, recordData, 0, recordSize);
        } catch (InvalidRecordIDException var6) {
            try {
                while (recordStore.addRecord(recordData, 0, recordSize) != recordNumber) {
                }

            } catch (RecordStoreFullException var4) {
                throw new BasicError(4101, "Disk full");
            } catch (Exception var5) {
                throw new BasicError(4101, var5.getClass().getName());
            }
        } catch (RecordStoreFullException var7) {
            throw new BasicError(4101, "Disk full");
        } catch (Exception var8) {
            throw new BasicError(4101, var8.getClass().getName());
        }
    }

    public RandomAccessFile(String filename, boolean readOnly) {
        try {
            recordStore = RecordStore.openRecordStore(filename, false);
            recordData = null;
            currentRecord = 0;
            recordModified = false;
            fileOffset = 0;
            fileSize = RESERVED;
            magic = readInt();
            if (magic == MAGIC) {
                fileSize = readInt();
                fileOffset = RESERVED;
            } else {
                fileSize = recordData.length;
                recordSize = fileSize;
                fileOffset = 0;
            }
        } catch (RecordStoreNotFoundException var5) {
            if (!readOnly) {
                try {
                    recordStore = RecordStore.openRecordStore(filename, true);
                    recordData = null;
                    currentRecord = 0;
                    recordModified = false;
                    fileSize = RESERVED;
                    fileOffset = RESERVED;
                    magic = MAGIC;
                } catch (Exception var4) {
                    throw new BasicError(4101, var5.getClass().getName());
                }
            } else {
                throw new BasicError(4100, "\"" + filename + "\" not found");
            }
        } catch (Exception var6) {
            throw new BasicError(4101, var6.getClass().getName());
        }
    }

    public final void close() throws RecordStoreException {
        if (magic == MAGIC) {
            fileOffset = 0;
            writeInt(MAGIC);
            writeInt(fileSize);
            WriteRecord(currentRecord);
        }
        recordStore.closeRecordStore();
    }

    public final byte readByte() throws EOFException {
        if (fileOffset >= 0) {
            if (fileOffset < fileSize) {
                int recordNumber = fileOffset / recordSize + 1;
                int recordOffset = fileOffset % recordSize;
                if (recordNumber != currentRecord) {
                    if (recordModified) {
                        WriteRecord(currentRecord);
                    }

                    ReadRecord(recordNumber);
                    currentRecord = recordNumber;
                    recordModified = false;
                }

                byte byteValue = recordData[recordOffset];
                ++fileOffset;
                return byteValue;
            } else {
                throw new EOFException();
            }
        } else {
            throw new BasicError(4101, "Invalid offset");
        }
    }

    public final void writeByte(int byteValue) {
        if (fileOffset >= 0) {
            int recordNumber = fileOffset / recordSize + 1;
            int recordOffset = fileOffset % recordSize;
            if (recordNumber != currentRecord) {
                if (recordModified) {
                    WriteRecord(currentRecord);
                }

                ReadRecord(recordNumber);
                currentRecord = recordNumber;
                recordModified = false;
            }

            recordData[recordOffset] = (byte) byteValue;
            recordModified = true;
            ++fileOffset;
            if (fileOffset >= fileSize) {
                fileSize = fileOffset;
            }
        } else {
            throw new BasicError(4101, "Invalid offset");
        }
    }

    public final boolean readBoolean() throws IOException {
        throw new IOException("function not implemented");
    }

    public final char readChar() throws IOException {
        throw new IOException("function not implemented");
    }

    public final void readFully(byte[] b) throws EOFException {
        for (int i = 0; i < b.length; ++i) {
            b[i] = this.readByte();
        }

    }

    public final void readFully(byte[] b, int off, int len) throws EOFException {
        for (int i = off; i < len; ++i) {
            b[i] = this.readByte();
        }

    }

    public final int readInt() throws EOFException {
        byte var1 = readByte();
        byte var2 = readByte();
        byte var3 = readByte();
        byte var4 = readByte();
        return (var1 & 255) << 24 | (var2 & 255) << 16 | (var3 & 255) << 8 | var4 & 255;
    }

    public final long readLong() throws IOException {
        throw new IOException("function not implemented");
    }

    public final short readShort() throws EOFException {
        byte var1 = this.readByte();
        byte var2 = this.readByte();
        return (short) ((var1 & 255) << 8 | var2 & 255);
    }

    public final int readUnsignedByte() throws IOException {
        throw new IOException("function not implemented");
    }

    public final int readUnsignedShort() throws EOFException {
        byte var1 = this.readByte();
        byte var2 = this.readByte();
        return (var1 & 255) << 8 | var2 & 255;
    }

    public final String readUTF() throws IOException {
        int var2 = this.fileOffset;
        int var3 = this.readUnsignedShort();
        this.fileOffset = var2;
        byte[] var4 = new byte[var3 + 2];
        this.readFully(var4);
        ByteArrayInputStream var5 = new ByteArrayInputStream(var4);
        return (new DataInputStream(var5)).readUTF();
    }

    public final int skipBytes(int var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void write(byte[] var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void write(byte[] var1, int var2, int var3) {
        for (int var4 = var2; var4 < var3; ++var4) {
            this.writeByte(var1[var4]);
        }

    }

    public final void write(int var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void writeBoolean(boolean var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void writeChar(int var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void writeChars(String var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void writeInt(int var1) {
        this.writeByte(var1 >> 24 & 255);
        this.writeByte(var1 >> 16 & 255);
        this.writeByte(var1 >> 8 & 255);
        this.writeByte(var1 & 255);
    }

    public final void writeLong(long var1) throws IOException {
        throw new IOException("function not implemented");
    }

    public final void writeShort(int var1) {
        this.writeByte(var1 >> 8 & 255);
        this.writeByte(var1 & 255);
    }

    public final void writeUTF(String var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        (new DataOutputStream(var2)).writeUTF(var1);
        byte[] var4 = var2.toByteArray();

        for (int var5 = 0; var5 < var4.length; ++var5) {
            this.writeByte(var4[var5]);
        }

    }

    public int getFilePointer() {
        return fileOffset - 8;
    }

    public void seek(int var1) {
        if (var1 >= 0) {
            fileOffset = var1 + 8;
            if (fileOffset <= fileSize) {
                return;
            }
        }

        fileOffset = fileSize;
    }

    public double readDouble() throws IOException {
        throw new IOException("function not implemented");
    }

    public float readFloat() throws IOException {
        throw new IOException("function not implemented");
    }

    public void writeDouble(double v) throws IOException {
        throw new IOException("function not implemented");
    }

    public void writeFloat(float v) throws IOException {
        throw new IOException("function not implemented");
    }
}
