package fr.louidji.tools;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 18:05
 */
public class OrganizePhotoTest {
    private static final String TEMP_FILE = "2012-01-08 16.34.06.jpg";
    private static final String TMP_PATH_FILE = "./tmp/" + TEMP_FILE;
    private static final String DEST = "./dest/";
    public static final FileFilter FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            final String fileName = file.getName().toLowerCase();
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
        }
    };


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        (new File("./tmp")).mkdir();

        File baseDir = new File("./target/test-classes");
        File files[] = baseDir.listFiles(FILTER);
        for (File file : files) {
            FileHelper.copyFile(file, new File("./tmp/" + file.getName()));
        }

    }

    @After
    public void tearDown() throws Exception {

        deleteImageDir(new File("./tmp/"));
        deleteImageDir(new File(DEST + "2012/2012_01_08"));
        deleteImageDir(new File(DEST + "2009/2009_01_25"));
        delete(DEST + "2012");
        delete(DEST + "2009");
        delete(DEST);
    }

    private void deleteImageDir(File baseDir) {
        File files[] = baseDir.listFiles(FILTER);
        if (null != files) {
            for (File file : files) {
                delete(file);
            }
        }
        delete(baseDir);
    }

    private void delete(String fileToDelete) {
        delete(new File(fileToDelete));
    }

    private void delete(File fileToDelete) {
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

        assertEquals(2, result.getNbImagesProcessed());
        assertEquals(4, result.getNbImagesToProcess());
    }
}
