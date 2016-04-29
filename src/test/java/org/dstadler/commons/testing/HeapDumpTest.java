package org.dstadler.commons.testing;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class HeapDumpTest {
    @Test
    public void testDumpHeap() throws IOException {
        File file = File.createTempFile("HeapDumpTest", ".hprof");
        assertTrue(file.delete());
        assertFalse(file.exists());

        try {
            HeapDump.dumpHeap(file.getAbsolutePath(), true);
        } finally {
            assertTrue(file.exists());
            assertTrue(file.delete());
        }

        // a second time to cover singleton
        try {
            HeapDump.dumpHeap(file.getAbsolutePath(), true);
        } finally {
            assertTrue(file.exists());
            assertTrue(file.delete());
        }
    }

    @Test
    public void testDumpHeapFailsOnWrongFilename() throws IOException {
        File file = File.createTempFile("HeapDumpTest", ".hprof");
        assertTrue(file.delete());
        assertFalse(file.exists());
        assertTrue(file.mkdirs());

        try {
            HeapDump.dumpHeap(file.getAbsolutePath(), true);
            fail("Should fail to write the dump in this case, tried for " + file.getAbsolutePath());
        } catch (IOException e) {
            // expected in this case
        } finally {
            assertTrue(file.exists());
            assertTrue(file.delete());
        }
    }
}
