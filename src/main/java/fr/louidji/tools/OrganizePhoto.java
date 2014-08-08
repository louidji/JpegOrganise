package fr.louidji.tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * Classe utilitaire d'organization des photos.
 * User: louis
 * Date: 01/09/12
 * Time: 17:30
 */
public class OrganizePhoto {
    private static final Logger logger = Logger.getLogger(OrganizePhoto.class.getName());
    /**
     * Format standard pour les repertoires
     */
    public static final String BASE_DIR_PATTERN_FORMAT = "yyyy" + File.separator + "yyyy_MM_dd";
    /**
     * Exemple de format horodate pour le nom des images (le temps au format Exif n'a pas les millisecondes) sans l'extension
     */
    public static final String PHOTO_NAME_LONG_FORMAT = "yyyy_MM_dd-HH_mm_ss";

    public static void addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    public static void removeHandler(Handler handler) throws SecurityException {
        logger.removeHandler(handler);
    }
    // Scan
    /**
     * File Filter pour filtre => accepte JPG & Dir.
     */
    private static FileFilter imageOrDirFileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            assert null != file : "Le fichier ne peut etre null";
            final String name = file.getName().toLowerCase();
            return file.isDirectory() || (name.endsWith(".jpg") || name.endsWith(".jpeg"));  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    /**
     * Organise en masse
     *
     * @param sourceDir répertoire contenant les images a déplacer.
     * @param destDir   répertoire dans lequels les images seront déplacés.
     * @return resultat du traitement.
     */
    public static Result organizeAll(File sourceDir, File destDir) {
        return organizeAll(sourceDir, destDir, BASE_DIR_PATTERN_FORMAT, null);
    }

    /**
     * Organise en masse
     *
     * @param sourceDir            répertoire contenant les images a déplacer.
     * @param destDir              répertoire dans lequels les images seront déplacés.
     * @param destDirPatternFormat pattern du sous repertoire
     * @param photoNamePattern     pattern temporelle du nom de des images sans l'extension (si null on prend le nom par defaut)
     * @return resultat du traitement.
     */
    public static Result organizeAll(File sourceDir, File destDir, String destDirPatternFormat, String photoNamePattern) {
        final Result result = new Result(0, 0);
        File files[] = sourceDir.listFiles(imageOrDirFileFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(organizeAll(file, destDir, destDirPatternFormat, photoNamePattern));
            } else {
                boolean done = organize(file, destDir, destDirPatternFormat, photoNamePattern);
                result.add(1, done ? 1 : 0);
            }
        }

        return result;
    }

    /**
     * Deplace la photo en fonction de sa date de prise de vue.
     *
     * @param photo                Image source.
     * @param destBaseDir          Répertoire de destination.
     * @param destDirPatternFormat pattern du sous repertoire
     * @param photoNamePattern     pattern temporelle du nom de des images sans l'extension (si null on prend le nom par defaut)
     * @return vrais si la photo est déplacés
     */
    public static boolean organize(File photo, File destBaseDir, String destDirPatternFormat, String photoNamePattern) {
        boolean done = false;
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(photo);
            if (null != metadata) {
                final ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
                final Date date;
                if (null != directory && (date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) != null) {
                    final SimpleDateFormat df = new SimpleDateFormat(destDirPatternFormat);
                    final String formatted = df.format(date);

                    final String destDir = destBaseDir.getPath().concat(File.separator).concat(formatted);

                    final File dir = new File(destDir);
                    if (!dir.exists() || !dir.isDirectory()) {
                        if (!dir.mkdirs()) {
                            logger.warning("Problème lors de la création de l'arborescence " + dir.getAbsolutePath());
                        }
                    }
                    final String name;
                    final String photoName = photo.getName();
                    if (null == photoNamePattern || photoNamePattern.isEmpty())
                        name = photoName;
                    else {
                        df.applyPattern(photoNamePattern);
                        name = df.format(date) + photoName.substring(photoName.lastIndexOf('.'));
                    }
                    final String destFile = destDir.concat(File.separator).concat(name);

                    done = FileHelper.moveFile(photo, new File(destFile), false);
                    if (done) {
                        logger.fine(photo.getAbsolutePath() + " => " + destFile);
                    }
                } else {
                    logger.info(photo.getAbsolutePath() + " n'a pas de metadata, image non copiée.");
                }


            }
        } catch (ImageProcessingException | IOException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }


        return done;
    }
}
