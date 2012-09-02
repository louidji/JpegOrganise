package fr.louidji.tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class OrganizePhoto {
    private static final Logger logger = Logger.getLogger(OrganizePhoto.class.getName());

    /**
     * Deplace la photo en fonction de sa date de prise de vue.
     * @param photo Image source.
     * @param destBaseDir Répertoire de destination.
     * @return vrais si la photo est déplacés
     */
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

                done = FileHelper.moveFile(photo, new File(destFile), false);
                if (done) {
                    logger.info(photo.getAbsolutePath() + " => " + destFile);
                }


            }
        } catch (ImageProcessingException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }


        return done;
    }
}
