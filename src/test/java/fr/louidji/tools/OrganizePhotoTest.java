package fr.louidji.tools;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 18:05
 */
public class OrganizePhotoTest {
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
            //noinspection ResultOfMethodCallIgnored
            fileToDelete.delete();
        }
    }

    @org.junit.Test
    public void testOrganize() throws Exception {
        String oriMd5 = FileHelper.md5(tempFile);
        long start = System.currentTimeMillis();
        File destBaseDir = new File(dest);
        File photo = new File(tempFile);
        assertTrue(OrganizePhoto.organize(photo, destBaseDir));
        long end = System.currentTimeMillis();
        System.out.println("Temps execution " + (end - start) + " ms");
        String destMd5 = FileHelper.md5(dest + "2012" + File.separator + "2012_01_08" + File.separator + tempFile);
        assertEquals(destMd5, "6deb5bec09d0a2e82a71a71f574d893a");

        System.out.println(destMd5);
        assertEquals(oriMd5, destMd5);


    }

}
