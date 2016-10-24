package pervacio.com.testhandlerthread.utils;

/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

import pervacio.com.testhandlerthread.AppClass;

/**
 * Generate Random byte array, file for randomly generated uploaded file.
 *
 * @author Bertrand Martel
 */
public class RandomGen {

    /**
     * Chunk to write at each iteration for upload file generation.
     */
    public static final int UPLOAD_FILE_WRITE_CHUNK = 64000;

    /**
     * Temporary file extension.
     */
    public static final String UPLOAD_TEMP_FILE_EXTENSION = ".tmp";

    /**
     * Temporary file name for upload file.
     */
    public static final String UPLOAD_TEMP_FILE_NAME = "speed-test-random";

    /**
     * upload 1Mo file size.
     */
    public static final int FILE_SIZE = 1000000;

    private static File sFile;

    /**
     * Generate random byte array.
     *
     * @param length number of bytes to be generated
     * @return random byte array
     */
    public static byte[] generateRandomArray(final int length) {

        final byte[] buffer = new byte[length];

        final int iter = length / UPLOAD_FILE_WRITE_CHUNK;
        final int remain = length % UPLOAD_FILE_WRITE_CHUNK;

        Random randomObj = new Random();

        for (int i = 0; i < iter; i++) {
            final byte[] random = new byte[UPLOAD_FILE_WRITE_CHUNK];
            randomObj.nextBytes(random);
            System.arraycopy(random, 0, buffer, i * UPLOAD_FILE_WRITE_CHUNK, UPLOAD_FILE_WRITE_CHUNK);
        }
        if (remain > 0) {
            final byte[] random = new byte[remain];
            randomObj.nextBytes(random);
            System.arraycopy(random, 0, buffer, iter * UPLOAD_FILE_WRITE_CHUNK, remain);
        }
        return buffer;
    }

    /**
     * Generate random file.
     *
     * @param length number of bytes to be generated
     * @return file with random content
     */
    public static RandomAccessFile generateRandomFile(final int length) throws IOException {

        Random randomObj = new Random();

        File file = File.createTempFile(UPLOAD_TEMP_FILE_NAME, UPLOAD_TEMP_FILE_EXTENSION, AppClass.getContext().getCacheDir());

        final RandomAccessFile randomFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
        randomFile.setLength(length);

        final int iter = length / UPLOAD_FILE_WRITE_CHUNK;
        final int remain = length % UPLOAD_FILE_WRITE_CHUNK;

        for (int i = 0; i < iter; i++) {
            final byte[] random = new byte[UPLOAD_FILE_WRITE_CHUNK];
            randomObj.nextBytes(random);
            randomFile.write(random);
        }
        if (remain > 0) {
            final byte[] random = new byte[remain];
            randomObj.nextBytes(random);
            randomFile.write(random);
        }

        return randomFile;
    }

    public static synchronized File getFile() {
        if (sFile == null) {
            sFile = new File(AppClass.getContext().getCacheDir(), UPLOAD_TEMP_FILE_NAME + "." + UPLOAD_TEMP_FILE_EXTENSION);
        }
        return sFile;
    }

    /**
     * Delete random file.
     */
    public static boolean deleteFile() {
        return getFile().delete();
    }

    /**
     * Is file exist.
     */
    public static boolean isFileExist() {
        return getFile().exists();
    }

}