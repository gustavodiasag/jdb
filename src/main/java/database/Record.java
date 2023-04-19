package main.java.database;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/*
 * Represents an "anime" entity that can be either
 * initially generated from the csv file or
 * retrieved from its byte sequence.
 */
public class Record {
    private boolean valid;
    private int id;
    private String name;
    private float score;
    private String[] genres;
    private int episodes;
    private String[] producers;
    private Date date;

    /*
     * A new Record must always be created with valid
     * attributes. Null ones should never be allowed.
     */
    public Record(
        boolean valid,
        int id,
        String name,
        float score,
        String[] genres,
        int episodes,
        String[] producers,
        Date date
    ) {

        setValid(valid);
        setId(id);
        setName(name);
        setScore(score);
        setGenres(genres);
        setEpisodes(episodes);
        setProducers(producers);
        setDate(date);
    }

    public boolean getValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return this.score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String[] getGenres() {
        return this.genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public int getEpisodes() {
        return this.episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public String[] getProducers() {
        return this.producers;
    }

    public void setProducers(String[] producers) {
        this.producers = producers;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /*
     * Writes the object's structured representation to the
     * "database" file.
     */
    public long serialize(RandomAccessFile raf) throws IOException {
        long pos = raf.getFilePointer();
        byte[] recordAsBytes = this.toByteArray();
        /*
         * The bytes corresponding to the record are not directly written
         * 'cause each one is preceded by some kind of header:
         * 
         * <validation-bit> <byte-repr-length>
         */

        // Provides an efficient way to logically delete a record.
        raf.writeBoolean(valid);

        /*
         * Storing this information makes it possible to skip considerable
         * amounts of bytes, optimizing searching operations.
         */
        raf.writeInt(recordAsBytes.length);
        raf.write(recordAsBytes);
        
        return pos;
    }

    // Used for updates that maintain the previous register's size.
    public void serialize(RandomAccessFile raf, int byteArrayLen) 
        throws IOException {

        byte[] recordAsBytes = toByteArray();

        raf.writeBoolean(valid);
        raf.writeInt(byteArrayLen);
        raf.write(recordAsBytes);
    }

    // Converts the attributes to their respective byte representation.
    public byte[] toByteArray() throws IOException {
        // Byte stream is closed even when an exception is thrown.
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            DataOutputStream stream = new DataOutputStream(byteStream);
    
            stream.writeInt(id);
    
            writeStr(name, stream);
    
            stream.writeFloat(score);
            
            // The byte type range is enough to store the amount of genres.
            stream.writeByte(genres.length);
            for (String genre : genres)
                writeStr(genre, stream);

            // Should never surpass the limit of a short integer.
            stream.writeShort(episodes);

            // Byte range is enough to store the amount of producers.
            stream.writeByte(producers.length);
            for (String producer : producers)
                writeStr(producer, stream);
            
            /*
             * Storing the date as a numeric value drastically reduces
             * the amount of memory used.
             */
            stream.writeLong(date.getTime());
            
            return byteStream.toByteArray();

        } catch (IOException e) {
            throw new IOException(
                "Could not transfer data to byte file", e);
        }
    }
    
    /*
     * Strings are also stored with a header. Having that all
     * the textual fields' values vary a lot in length, it is
     * useful to store their byte representation's length too.
     */
    private void writeStr(String field, DataOutputStream stream)
        throws IOException {
            
        byte[] strBytes = field.getBytes("UTF-8");

        // Length never surpasses the short type's limit.
        stream.writeInt(strBytes.length);
        stream.write(strBytes);
    }
    
    /*
     * Reads the bytes organized in the specified structure
     * converting them to the respective object.
     */
    public static Record deserialize(RandomAccessFile raf)
        throws IOException {
        
        try {
            boolean valid = raf.readBoolean();
            
            raf.readInt();
            
            int id = raf.readInt();
            String name = readStr(raf);
            float score = raf.readFloat();
            
            String[] genres = new String[raf.readByte()];
            for (byte i = 0; i < genres.length; i++)
                genres[i] = readStr(raf);
            
            short episodes = raf.readShort();
            
            String[] producers = new String[raf.readByte()];
            for (byte b = 0; b < producers.length; b++)
                producers[b] = readStr(raf);
            
            Date date = new Date(raf.readLong());
            
            return new Record(
                valid,
                id,
                name,
                score,
                genres,
                episodes,
                producers,
                date
            );
            
        } catch (IOException e) {
            throw new IOException(
                "Error while reading data from file", e);
        }
    }

    // Returns the required String built with the header's help.
    private static String readStr(RandomAccessFile raf)
        throws IOException {

        byte[] strBytes = new byte[raf.readInt()];
        raf.read(strBytes);

        return new String(strBytes);
    }

    @Override
    public String toString() {
        return "{" +
            valid + "," +
            id + "," +
            name + "," +
            score + "," +
            Arrays.asList(genres) + "," +
            episodes + "," +
            Arrays.asList(producers) + "," +
            new SimpleDateFormat("yyyy-MM-dd").format(date) +
            "}";
    }
}
