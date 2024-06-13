package org.apache.zookeeper.server.persistence;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.txn.CreateTxn;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class FileTxnLogAppendTest {

    private static FileTxnLog fileTxnLog;
    private static File logDir;
    private static Request request;
    private final Mode mode;
    private enum Mode{
        DEFAULT,
        FAULTY_SER,
    }

    public FileTxnLogAppendTest(Request request, Mode mode) {
       this.request = request;
       this.mode = mode;
       //set up environment
        logDir = new File("logDir");
        logDir.mkdir();
        fileTxnLog = new FileTxnLog(logDir);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        // Create each instance
        Request validRequest = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, 1L, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test1", "AAAA".getBytes(), null, false, 0),
                1L);

        Request invalidRequest = new Request(1L, 0, ZooDefs.OpCode.error,
                new TxnHeader(1L, 0, 4L, 1000L, ZooDefs.OpCode.create),
                new CreateTxn("/test3", "BBBB".getBytes(), null, false, 0),
                4L);

        Request nullHdrRequest = new Request(1L, 0, ZooDefs.OpCode.create, null,
                new CreateTxn("/test3", "CCCC".getBytes(), null, false, 0),
                5L);

        Request nullTxnRequest = new Request(1L, 0, ZooDefs.OpCode.create,
                new TxnHeader(1L, 0, 6L, 1000L, ZooDefs.OpCode.create),
                null,
                6L);

        Request mockedEmptyDataRq = mock(Request.class);
        when(mockedEmptyDataRq.getHdr()).thenReturn(new TxnHeader(1L, 0, 7L, 1000L, ZooDefs.OpCode.create));
        when(mockedEmptyDataRq.getSerializeData()).thenReturn(new byte[0]);

        Request zxidLessRequest = mock(Request.class);
        TxnHeader hdr = mock(TxnHeader.class);
        Mockito.when(hdr.getZxid()).thenReturn(0L); // Simuliamo il caso in cui hdr.getZxid() <= lastZxidSeen
        Mockito.when(zxidLessRequest.getHdr()).thenReturn(hdr);

        return Arrays.asList(new Object[][]{
                {validRequest, Mode.DEFAULT},
                {zxidLessRequest, Mode.FAULTY_SER},
                //{null, Mode.DEFAULT},
                {invalidRequest, Mode.DEFAULT},
                {nullHdrRequest, Mode.DEFAULT},
               // {nullTxnRequest, Mode.DEFAULT},
                {mockedEmptyDataRq, Mode.FAULTY_SER},
        });
    }
    @Test
    public void testAppendRequest(){
        System.out.println(mode.name());
        switch (mode){
            case DEFAULT:
                if(request==null) {
                    System.out.println("im here");
                    assertThrows(NullPointerException.class, () -> fileTxnLog.append(request));
                }
                else{
                try {
                    if(request.getHdr() == null || request.getTxn() == null)
                        assertFalse(fileTxnLog.append(request));
                    else {
                        assertTrue(fileTxnLog.append(request));
                    }
                } catch (IOException e) {
                    fail("no exception should thrown");
                }}
                break;
            case FAULTY_SER:
                if (request != null && request.getHdr() != null) {
                    boolean thrown = false;
                    try {
                        fileTxnLog.append(request);
                    } catch (IOException e) {
                        thrown = true;
                        assertEquals("Faulty serialization for header and txn", e.getMessage());
                    }
                    assertTrue(thrown);
                }
                break;
        }
        try {
            fileTxnLog.close();
        } catch (IOException e) {
            fail("no exception should thrown");
        }

    }

    @AfterClass
    public static void clean() {

        try {
            if(logDir != null)
                FileUtils.deleteDirectory(logDir);

            if(request.getHdr() != null) {
                File f = new File("log."+request.getHdr().getClientId());
                f.delete();
            }
        } catch (IOException e) {
            System.out.println("Failed to delete directory\n");
        }
    }
}
