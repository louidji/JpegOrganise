package fr.louidji.tools;

import org.junit.After;
import org.junit.Before;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 18:05
 * To change this template use File | Settings | File Templates.
 */
public class OrganisePhotoTest {
    private static final String tempFile = "temp2012-01-08 16.34.06.jpg";
    private static final String dest = "./dest/";


    @Before
    public void setUp() throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("2012-01-08 16.34.06.jpg");


            out = new FileOutputStream(tempFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (null != in)
                in.close();
            if (null != out)
                out.close();
        }

    }

    @After
    public void tearDown() throws Exception {
        delete(tempFile);
        delete(dest + "2012" + File.separator + "2012_01_08" + File.separator + tempFile);
        delete(dest + "2012" + File.separator + "2012_01_08");
        delete(dest + "2012");
        delete(dest);
    }

    private void delete(String file) {
        File fileToDelete = new File(file);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }

    @org.junit.Test
    public void testOrganize() throws Exception {
        String oriMd5 = md5(tempFile);
        long start = System.currentTimeMillis();
        File destBaseDir = new File(dest);
        File photo = new File(tempFile);
        assertTrue(OrganisePhoto.organize(photo, destBaseDir));
        long end = System.currentTimeMillis();
        System.out.println("Temps execution " + (end - start) + " ms");
        String destMd5 = md5(dest + "2012" + File.separator + "2012_01_08" + File.separator + tempFile);

        assertEquals(oriMd5, destMd5);

    }

    private String md5(String file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        InputStream is = null;
        DigestInputStream dis = null;
        try {
            is = new FileInputStream(file);
            dis = new DigestInputStream(is, md);
            // lecture
            while (dis.read() != -1) ;
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != dis) {
                dis.close();
            }
        }
        final byte[] digest = md.digest();


        final Formatter formatter = new Formatter();
        for (byte b : digest) {
            formatter.format("%02x", b);
        }
        String md5 = formatter.toString();
        System.out.println(md5 + " " + file);

        return md5;

    }
}
