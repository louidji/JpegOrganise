package fr.louidji.tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.*;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
                for (Tag tag : directory.getTags()) {
                    System.out.println(tag);
                }
            }
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
    public static byte[] bytesMd5(final String filePath) throws NoSuchAlgorithmException, IOException {
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
    public static boolean compareMd5File(final String filePathOne, final String filePathTwo) throws IOException, NoSuchAlgorithmException {
        byte[] byte1 = bytesMd5(filePathOne);
        byte[] byte2 = bytesMd5(filePathTwo);
        return Arrays.equals(byte1, byte2);
    }

    /**
     * Copie (ecrase sir existan) un fichier.
     *
     * @param sourceFile fichier source.
     * @param destFile   fichier destination.
     * @return vrais si le transfert est complet.
     * @throws IOException
     */
    public static boolean copyFile(final File sourceFile, final File destFile) throws IOException {
        boolean transfer = false;
        if (!destFile.exists()) {
            if (!destFile.createNewFile()) {
                logger.warning("Probleme de création du fichier " + destFile.getAbsolutePath());
            }
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            final long size = source.size();
            transfer = size == destination.transferFrom(source, 0, size);
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
        return transfer;
    }

    /**
     * Copie (ecrase sir existan) un fichier.
     *
     * @param sourceFilePath fichier source.
     * @param destFilePath   fichier destination.
     * @return vrais si le transfert est complet.
     * @throws IOException
     */
    @SuppressWarnings("UnusedDeclaration")
    public static boolean copyFile(final String sourceFilePath, final String destFilePath) throws IOException {
        return copyFile(new File(sourceFilePath), new File(destFilePath));
    }

    /**
     * Deplace le fichier.
     *
     * @param sourceFile fichier source.
     * @param destFile   fichier destination.
     * @param override   si vrais on ecrase le fichier de dest si il existe, sinon on creer un nouveau fichier avec increment.
     * @return vrais si le deplacement a reussi.
     */
    public static boolean moveFile(final File sourceFile, final File destFile, boolean override) {
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
                    logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
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


                        move = moveFile(sourceFile, new File(destName), override);
                    }
                }


            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }


        }
        return move;
    }

}
