package org.apache.zookeeper.server.persistence;
import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FileTxnLogCommitTest {

    private final long TXN_LOG_SIZE_LIMIT = 1024L; // 1 kb for test
    private FileTxnLog fileTxnLog;
    private File logDir;

    @Before
    public void setUp() {
        logDir = new File("logDir");
        logDir.mkdir();
        fileTxnLog = spy(new FileTxnLog(logDir));
        FileTxnLog.setTxnLogSizeLimit(TXN_LOG_SIZE_LIMIT);
    }

    @After
    public void tearDown() throws IOException {
        if (logDir != null) {
            FileUtils.deleteDirectory(logDir);
        }
    }

    private void appendDummyRecord(long zxid) throws IOException {
        Request request = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, zxid, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test" + zxid, "AAAA".getBytes(), null, false, 0),
                zxid);
        fileTxnLog.append(request);
        fileTxnLog.commit();
    }

    @Test
    public void testLogRollWhenSizeLimitExceeded() throws IOException {
        // Append records to exceed the log size limit
        for (long zxid = 1; zxid <= 100; zxid++) {
            appendDummyRecord(zxid);

            if (fileTxnLog.getTotalLogSize() > TXN_LOG_SIZE_LIMIT) {
                break;
            }
        }

        // Verify that the log roll has occurred
        long logSize = fileTxnLog.getTotalLogSize();
        System.out.println(logSize);
        assertTrue("Log size should exceed the limit", logSize > TXN_LOG_SIZE_LIMIT);

        FileTxnLog.setTxnLogSizeLimit(0L);
        //fileTxnLog.setTotalLogSize(0L);
        appendDummyRecord(1001);
        assertEquals(67108880, fileTxnLog.getCurrentLogSize());
        System.out.println(fileTxnLog.getTotalLogSize());
        verify(fileTxnLog, times(1)).rollLog();
    }


}
