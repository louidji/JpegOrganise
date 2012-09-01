package fr.louidji.tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class OrganisePhoto {

    public static boolean organize(File photo, File destBaseDir) {
        boolean done = false;
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(photo);
            if (null != metadata) {
                final ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
                final Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                final SimpleDateFormat df = new SimpleDateFormat("yyyy");
                final String year = df.format(date);
                df.applyPattern("MM");
                final String month = df.format(date);
                df.applyPattern("DD");
                final String day = df.format(date);

                final String destDir = destBaseDir.getPath().concat(File.separator).concat(year).concat(File.separator)
                        .concat(year).concat("_").concat(month).concat("_").concat(day);

                final File dir = new File(destDir);
                if (!dir.exists() || !dir.isDirectory()) {
                    dir.mkdirs();
                }

                final String destFile = destDir.concat(File.separator).concat(photo.getName());

                done = photo.renameTo(new File(destFile));
                if (done) {
                    System.out.println(photo.getAbsolutePath() + " => " + destFile);
                }


            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        return done;
    }
}
