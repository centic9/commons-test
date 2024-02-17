package org.dstadler.commons.testing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

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
            HeapDump.dumpHeap(file.getAbsolutePath(), false);
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
            assertThrows(IOException.class,
					() -> HeapDump.dumpHeap(file.getAbsolutePath(), true),
					"Should fail to write the dump in this case, tried for " + file.getAbsolutePath());
        } finally {
            assertTrue(file.exists(), "Failed for " + file.getAbsolutePath());
            assertTrue(file.delete(), "Failed for " + file.getAbsolutePath());
        }
    }
}
