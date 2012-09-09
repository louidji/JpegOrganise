package fr.louidji.tools;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 18:05
 */
public class OrganizePhotoTest {
    private static final String TEMP_FILE = "temp2012-01-08 16.34.06.jpg";
    private static final String TMP_PATH_FILE = "./tmp/" + TEMP_FILE;
    private static final String DEST = "./dest/";


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        (new File("./tmp")).mkdir();
        FileHelper.copyFile("./target/test-classes/2012-01-08 16.34.06.jpg", TMP_PATH_FILE);
        FileHelper.copyFile("./target/test-classes/20110226175822.jpg", "./tmp/20110226175822.jpg");
        FileHelper.copyFile("./target/test-classes/2009-01-25_0069.jpg", "./tmp/2009-01-25_0069.jpg");


    }

    @After
    public void tearDown() throws Exception {
        delete(TMP_PATH_FILE);
        delete("./tmp/20110226175822.jpg");
        delete("./tmp/2009-01-25_0069.jpg");
        delete("./tmp");
        delete(DEST + "2012" + File.separator + "2012_01_08" + File.separator + TEMP_FILE);
        delete(DEST + "2012" + File.separator + "2012_01_08");
        delete(DEST + "2012");
        delete(DEST);
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
        String oriMd5 = FileHelper.md5(TMP_PATH_FILE);
        long start = System.currentTimeMillis();
        File destBaseDir = new File(DEST);
        File photo = new File(TMP_PATH_FILE);
        assertTrue(OrganizePhoto.organize(photo, destBaseDir));
        long end = System.currentTimeMillis();
        System.out.println("Temps execution " + (end - start) + " ms");
        String destMd5 = FileHelper.md5(DEST + "2012" + File.separator + "2012_01_08" + File.separator + TEMP_FILE);
        assertEquals(destMd5, "6deb5bec09d0a2e82a71a71f574d893a");

        System.out.println(destMd5);
        assertEquals(oriMd5, destMd5);
    }

    @Test
    public void testOrganizeAll() throws Exception {
        Result result = OrganizePhoto.organizeAll(new File("./tmp"), new File(DEST));
        System.out.println(result.getNbImagesProcessed() + "/" + result.getNbImagesToProcess());
    }
}
