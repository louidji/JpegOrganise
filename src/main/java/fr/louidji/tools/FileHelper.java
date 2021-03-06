package fr.louidji.tools;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileMetadataDirectory;
import com.googlecode.mp4parser.FileDataSourceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Classe utilitaire de manipulation des fichiers.
 * User: louis
 * Date: 01/09/12
 * Time: 15:58
 */
public final class FileHelper {
    private static final Pattern countPattern = Pattern.compile("_[0-9]{3}$");
    private static final Logger logger = Logger.getLogger(FileHelper.class.getName());

    public static void addHandler(Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    public static void removeHandler(Handler handler) throws SecurityException {
        logger.removeHandler(handler);
    }


    /**
     * Ecrit sur le stdout les metadata de l'image.
     *
     * @param filePath Fichier image.
     * @throws ImageProcessingException
     * @throws IOException
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void printMetadata(final File filePath) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(filePath);
        if (null != metadata) {
            for (Directory directory : metadata.getDirectories()) {
                System.out.println("\t" + directory.getName() + " - " + directory.getClass());
                directory.getTags().forEach(System.out::println);
            }
        }
    }

    private static Date getFileCreationTimeFromMovieMetaData(final File file, final boolean force) throws IOException, ImageProcessingException {

        final IsoFile isoFile = new IsoFile(new FileDataSourceImpl(file));
        final MovieBox movieBox = isoFile.getMovieBox();
        if (null != movieBox) {
            return movieBox.getMovieHeaderBox().getCreationTime();
        } else {
            // last chance via exif
            return getFileCreationTimeFromExifMetaData(file, force);
        }
    }

    private static Date getFileCreationTimeFromExifMetaData(final File file, final boolean force) throws ImageProcessingException, IOException {
        final Metadata metadata = ImageMetadataReader.readMetadata(file);
        Date value = null;
        if (null != metadata) {

            // ajout pour autres types d'encodage de la date selon les appareils...
            Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            value = null != directory ? directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) : null;

            if (null == value) {
                // cas spe 1
                directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                value = null != directory ? directory.getDate(ExifSubIFDDirectory.TAG_DATETIME) : null;
            }
            if (null == value && force) {
                directory = metadata.getFirstDirectoryOfType(FileMetadataDirectory.class);
                value = null != directory ? directory.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE) : null;
            }

        }
        if (null == value) {
            logger.warning(file.getAbsolutePath() + " => impossible de récuperer la date de création du fichier");
        }
        return value;
    }

    public static Date getCreationDateFromMetaData(final File file, final boolean force) throws ImageProcessingException, IOException {
        final String fileName = file.getName();
        final String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png":
            case "jpg":
            case "jpeg":
                return getFileCreationTimeFromExifMetaData(file, force);
            case "avi":
            case "mov":
            case "mp4":
            case "mp2":
                return getFileCreationTimeFromMovieMetaData(file, force);
            default:
                return null;
        }

    }

    /**
     * Renvoi le md5 du fichier au format hexa.
     *
     * @param file fichier dont on doit faire l'empreinte.
     * @return MD5
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String md5(final String file) throws IOException, NoSuchAlgorithmException {
        final byte[] digest = bytesMd5(file);


        final Formatter formatter = new Formatter();
        for (byte b : digest) {
            formatter.format("%02x", b);
        }
        String md5 = formatter.toString();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(md5 + " " + file);
        }
        return md5;

    }

    /**
     * Renvoi le md5 sous la forme d'un tableau de byte (la version hexa est extraite de cette methode).
     *
     * @param filePath chemin du fichier dont on doit faire l'empreinte.
     * @return tableaux de byte correspondant au MD5.
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static byte[] bytesMd5(final String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        InputStream is = null;
        DigestInputStream dis = null;
        try {
            is = new FileInputStream(filePath);
            dis = new DigestInputStream(is, md);
            // lecture => on parcours le digest..
            //noinspection StatementWithEmptyBody
            while (dis.read() != -1) ;
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != dis) {
                dis.close();
            }
        }
        return md.digest();
    }

    /**
     * Compare l'empreinte MD5 de 2 fichiers.
     *
     * @param filePathOne chemin du premier fichier.
     * @param filePathTwo chemin du deuxieme fichier.
     * @return vrais si les fichiers sont egaux.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private static boolean compareMd5File(final String filePathOne, final String filePathTwo) throws IOException, NoSuchAlgorithmException {
        byte[] byte1 = bytesMd5(filePathOne);
        byte[] byte2 = bytesMd5(filePathTwo);
        return Arrays.equals(byte1, byte2);
    }


    /**
     * Copie (ecrase si existan) un fichier.
     *
     * @param sourceFilePath fichier source.
     * @param destFilePath   fichier destination.
     * @return vrais si le transfert est complet.
     * @throws IOException
     */
    public static boolean copyFile(final File sourceFilePath, final File destFilePath) throws IOException {
        return null != Files.copy(Paths.get(sourceFilePath.toURI()), Paths.get(destFilePath.toURI()),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );

    }

    /**
     * Deplace le fichier.
     *
     * @param sourceFile fichier source.
     * @param destFile   fichier destination.
     * @param override   si vrais on ecrase le fichier de dest si il existe, sinon on creer un nouveau fichier avec increment.
     * @return vrais si le deplacement a reussi.
     */
    public static boolean moveFile(final File sourceFile, final File destFile, final boolean override) {
        boolean move = false;
        if (sourceFile.equals(destFile)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("On ne déplace pas le fichier " + sourceFile.getAbsolutePath() + " sur lui même");
            }
        } else if (!destFile.exists() || override) {
            move = sourceFile.renameTo(destFile);
            if (!move) {
                // dans le cas de different file system sur Linux (windows?) le rename ne marche pas => copy puis suppression
                try {
                    move = copyFile(sourceFile, destFile);
                    if (move) {
                        // suppression OK => delete.
                        if (!sourceFile.delete()) {
                            logger.info("Impossible de supprimer le fichier " + sourceFile.getAbsolutePath());
                        }
                    }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        } else {
            // identique ???
            try {
                if (compareMd5File(sourceFile.getAbsolutePath(), destFile.getAbsolutePath())) {
                    // Rien à faire
                    logger.info("Le fichier de destination (" + destFile.getAbsolutePath() + ") est identique à la source (" + sourceFile.getAbsolutePath() + "), on supprime juste la source");
                    move = sourceFile.delete();
                    if (!move) {
                        logger.info("Impossible de supprimer le fichier " + sourceFile.getAbsolutePath());
                    }
                } else {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Le fichier de destination existe (" + destFile + "), on essaye d'en trouver un autre : ");
                    }
                    final String fileName = destFile.getName();
                    final int indexExtension = fileName.lastIndexOf(".");
                    final String extension = indexExtension > 0 ? fileName.substring(indexExtension) : "";
                    final String shortFileName = indexExtension > 0 ? fileName.substring(0, indexExtension) : fileName;
                    String destName = destFile.getParentFile().getAbsolutePath().concat(File.separator);
                    // pattern _001 ... "_[0-9]{3}$"
                    Matcher matcher = countPattern.matcher(shortFileName);
                    String count = null;
                    if (matcher.find()) {
                        final int indexShort = matcher.start();
                        final int nextVal = Integer.valueOf(matcher.group().substring(1)) + 1;
                        if (nextVal < 1000) {
                            count = String.valueOf(nextVal);
                            while (count.length() < 3) {
                                count = "0".concat(count);
                            }
                            count = "_".concat(count);
                            destName = destName.concat(shortFileName.substring(0, indexShort));
                        } else {
                            logger.log(Level.WARNING, "Compteur saturé pour le fichier " + sourceFile.getAbsolutePath() + " , impossible de le déplacer");
                        }
                    } else {
                        destName = destName.concat(shortFileName);
                        count = "_001";
                    }
                    if (count != null) {
                        destName = destName.concat(count).concat(extension);
                        logger.info("Fichier source " + sourceFile.getAbsolutePath() + " vers destination déjà existant, nouveau chemin de destination potentiel : " + destName);


                        //noinspection ConstantConditions
                        move = moveFile(sourceFile, new File(destName), override);
                    }
                }


            } catch (IOException | NoSuchAlgorithmException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }


        }
        return move;
    }

}
