package fr.louidji.tools;

import com.drew.imaging.ImageProcessingException;

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
public class Organize {
    /**
     * Format standard pour les repertoires
     */
    public static final String BASE_DIR_PATTERN_FORMAT = "yyyy" + File.separator + "yyyy_MM_dd";
    /**
     * Exemple de format horodate pour le nom des images (le temps au format Exif n'a pas les millisecondes) sans l'extension
     */
    public static final String PHOTO_NAME_LONG_FORMAT = "yyyy_MM_dd-HH_mm_ss";
    private static final Logger logger = Logger.getLogger(Organize.class.getName());
    /**
     * File Filter pour filtre => accepte JPG & MOV & MP4 & MP2 & Dir.
     */
    private static final FileFilter mediaOrDirFileFilter = file -> {
        assert null != file : "Le fichier ne peut etre null";
        final String name = file.getName().toLowerCase();
        return file.isDirectory() || (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".avi") || name.endsWith(".mov") || name.endsWith(".mp4") || name.endsWith(".mp2"));
    };

    public static void addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }
    // Scan

    public static void removeHandler(Handler handler) throws SecurityException {
        logger.removeHandler(handler);
    }


    /**
     * Organise en masse (sans forcage)
     *
     * @param sourceDir répertoire contenant les images a déplacer.
     * @param destDir   répertoire dans lequels les images seront déplacés.
     * @return resultat du traitement.
     */
    public static Result organizeAll(final File sourceDir, final File destDir) {
        return organizeAll(sourceDir, destDir, BASE_DIR_PATTERN_FORMAT, null, false);
    }


    /**
     * Organise en masse
     *
     * @param sourceDir            répertoire contenant les images a déplacer.
     * @param destDir              répertoire dans lequels les images seront déplacés.
     * @param destDirPatternFormat pattern du sous repertoire
     * @param photoNamePattern     pattern temporelle du nom de des images sans l'extension (si null on prend le nom par defaut)
     * @param force                force l'opération sur fichier qui n'a pas de metadonnees (se basera sur la date de modification)
     * @return resultat du traitement.
     */
    public static Result organizeAll(final File sourceDir, final File destDir, final String destDirPatternFormat, final String photoNamePattern, final boolean force) {
        final Result result = new Result(0, 0);
        return recurseOrganize(sourceDir, destDir, destDirPatternFormat, photoNamePattern, force, result);
    }

    private static Result recurseOrganize(File sourceDir, File destDir, String destDirPatternFormat, String photoNamePattern, boolean force, final Result result) {
        final File files[] = sourceDir.listFiles(mediaOrDirFileFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                result.add(recurseOrganize(file, destDir, destDirPatternFormat, photoNamePattern, force, result));
            } else {
                boolean done = organize(file, destDir, destDirPatternFormat, photoNamePattern, force);
                result.add(1, done ? 1 : 0);
            }
        }
        return result;
    }

    /**
     * Deplace la photo en fonction de sa date de prise de vue (sans forcage).
     *
     * @param file                 Image ou video source.
     * @param destBaseDir          Répertoire de destination.
     * @param destDirPatternFormat pattern du sous repertoire
     * @param fileNamePattern      pattern temporelle du nom de des images sans l'extension (si null on prend le nom par defaut)
     * @return vrais si la photo est déplacés
     */
    public static boolean organize(final File file, final File destBaseDir, @SuppressWarnings("SameParameterValue") final String destDirPatternFormat, final String fileNamePattern) {
        return organize(file, destBaseDir, destDirPatternFormat, fileNamePattern, false);
    }


    /**
     * Deplace la photo en fonction de sa date de prise de vue.
     *
     * @param file                 Image ou video source.
     * @param destBaseDir          Répertoire de destination.
     * @param destDirPatternFormat pattern du sous repertoire
     * @param fileNamePattern      pattern temporelle du nom de des images sans l'extension (si null on prend le nom par defaut)
     * @param force                force l'opération sur fichier qui n'a pas de metadonnees (se basera sur la date de modification)
     * @return vrais si la photo est déplacés
     */
    private static boolean organize(final File file, final File destBaseDir, final String destDirPatternFormat, final String fileNamePattern, final boolean force) {
        boolean done = false;
        try {


            final Date date = FileHelper.getCreationDateFromMetaData(file, force);
            if (null != date) {
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
                final String photoName = file.getName();
                if (null == fileNamePattern || fileNamePattern.isEmpty())
                    name = photoName;
                else {
                    df.applyPattern(fileNamePattern);
                    name = df.format(date) + photoName.substring(photoName.lastIndexOf('.'));
                }
                final String destFile = destDir.concat(File.separator).concat(name);

                done = FileHelper.moveFile(file, new File(destFile), false);
                if (done) {
                    logger.fine(file.getAbsolutePath() + " => " + destFile);
                }
            } else {
                logger.info(file.getAbsolutePath() + " n'a pas de metadata, image non copiée.");
            }


        } catch (ImageProcessingException | IOException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        }


        return done;
    }
}
