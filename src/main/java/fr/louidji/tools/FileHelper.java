package fr.louidji.tools;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: louis
 * Date: 01/09/12
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */
public class FileHelper {

    public static void main(String[] args) {
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

    public static void printMetadata(File file) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        if (null != metadata) {
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    System.out.println(tag);
                    System.out.println(tag.getTagName() + " > " + tag.getDescription() + ", type : " + tag.getTagTypeHex());
                }


            }

/*            // obtain the Exif directory
            ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);

// query the tag's value
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);*/
        }
    }
}
