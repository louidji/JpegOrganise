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

/**
 * Created with IntelliJ IDEA.
 * Classe utilitaire de manipulation des fichiers.
 * User: louis
 * Date: 01/09/12
 * Time: 15:58
 */
public class FileHelper {


    /**
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length > 0) {
            File file = new File(args[0]);
            try {
                printMetadata(file);
            } catch (ImageProcessingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }

    /**
     * Ecrit sur le stdout les metadata de l'image.
     *
     * @param filePath Fichier image.
     * @throws ImageProcessingException
     * @throws IOException
     */
    public static void printMetadata(final File filePath) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(filePath);
        if (null != metadata) {
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    System.out.println(tag);
                    System.out.println(tag.getTagName() + " > " + tag.getDescription() + ", type : " + tag.getTagTypeHex());
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
        System.out.println(md5 + " " + file);

        return md5;

    }

    /**
     * Renvoi le md5 sous la forme d'un tableau de byte (la version hexa est extraite de cette methode).
     *
     * @param filePath chemin du fichier dont on doit faire l'empreinte.
     * @return
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
            destFile.createNewFile();
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
        if (!destFile.exists() || override) {
            move = sourceFile.renameTo(destFile);
            if (!move) {
                // dans le cas de different file system sur Linux (windows?) le rename ne marche pas => copy puis suppression
                try {
                    move = copyFile(sourceFile, destFile);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } else {
            final String absolutePath = destFile.getAbsolutePath();
            final String extension = absolutePath.substring(absolutePath.lastIndexOf("."),absolutePath.length() -1);
            //final File newDestFile = absolutePath;

        }
        return move;
    }

}
