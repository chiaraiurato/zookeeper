package org.apache.zookeeper.server.persistence;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
@RunWith(Parameterized.class)
public class FileTxnLogReadTest {

    private static FileTxnLog fileTxnLog;
    private static File logDir;
    private final long zxid;
    private final boolean fastForward;

    public FileTxnLogReadTest(long zxid, boolean fastForward) throws IOException {
        this.zxid = zxid;
        this.fastForward = fastForward;
        //set up environment
        logDir = new File("logDir");
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        fileTxnLog = new FileTxnLog(logDir);
        populateLog();
    }

    private void populateLog() throws IOException {
        Request record1 = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, 1L, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test1", "AAAA".getBytes(), null, false, 0),
                1L);
        Request record2 = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, 2L, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test1", "BBBB".getBytes(), null, false, 0),
                2L);
        Request record3 = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, 3L, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test1", "CCCC".getBytes(), null, false, 0),
                3L);

        fileTxnLog.append(record1);
        fileTxnLog.commit();
        fileTxnLog.append(record2);
        fileTxnLog.commit();
        fileTxnLog.append(record3);
        fileTxnLog.commit();

    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        return Arrays.asList(new Object[][]{
                {-1L, true},
                {-1L, false},
                {0L, true},
                {0L, false},
                {1L, true},
                {1L, false},
        });
    }
    @Test
    public void testRead(){
        System.out.println("Starting test with zxid: " + zxid + " and fastForward: " + fastForward);
        try {
            TxnLog.TxnIterator iterator = fileTxnLog.read(zxid);
            TxnLog.TxnIterator iterator2 = fileTxnLog.read(zxid, fastForward);
            for(long i = 0; i < 2; i++) {
                assertNotNull("Header should not be null", iterator.getHeader());
                System.out.println("Written header: "+iterator.getHeader());
                assertNotNull("Txn should not be null", iterator.getTxn());
                System.out.println("Written to log record: " + iterator.getTxn());
                assertTrue("Storage size should be non-negative", iterator.getStorageSize() >= 0);
                assertTrue("Iterator should have next", iterator.next());
                assertTrue("Iterator2 should have next", iterator2.next());
            }
            assertNotNull("Header should not be null", iterator.getHeader());
            assertNotNull("Txn should not be null", iterator.getTxn());
            assertTrue("Storage size should be non-negative", iterator.getStorageSize() >= 0);

            // close resource
            iterator.close();
            iterator2.close();
        }catch (IOException e){
            e.printStackTrace();
            fail("IOException thrown: " + e.getMessage());
        }


    }

    @After
    public void clean() {

        try {
            if(logDir != null)
                FileUtils.deleteDirectory(logDir);
        } catch (IOException e) {
            System.out.println("Failed to delete directory\n");
        }
    }
}
