package fr.louidji.gui;

import fr.louidji.tools.FileHelper;
import fr.louidji.tools.OrganizePhoto;
import fr.louidji.tools.Result;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main extends JDialog {
    private static final String CONF_DIR = System.getProperty("user.home").concat(File.separator).concat(".organizephoto").concat(File.separator);
    private static final String CONF_FILE = CONF_DIR.concat("lastvalues.properties");
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldSrc;
    private JButton sourceButton;
    private JTextField textFieldDst;
    private JButton destinationButton;
    private JTextArea textArea;
    private JCheckBox renamePhoto;
    private MyHandler logHandler = new MyHandler(textArea);

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public Main() {
        setTitle("Application d'organisation des images");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        sourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleSrc();
            }
        });
        destinationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleDest();
            }
        });

        OrganizePhoto.addHandler(logHandler);
        FileHelper.addHandler(logHandler);

        // chargement des dernieres valeurs
        File propertiesFile = new File(CONF_FILE);
        if (propertiesFile.exists() && propertiesFile.canRead()) {
            Properties properties = new Properties();
            try (InputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);

                final String src = properties.getProperty("src");
                final File srcDir = new File(src);
                if (srcDir.exists() && srcDir.isDirectory())
                    textFieldSrc.setText(src);

                final String dest = properties.getProperty("dest");
                final File destDir = new File(dest);
                if (destDir.exists() && destDir.isDirectory())
                    textFieldDst.setText(dest);
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getLocalizedMessage(), e);
            }

        }


    }

    private void handleDest() {
        chooseDir(textFieldDst);
    }

    private void handleSrc() {
        chooseDir(textFieldSrc);

    }

    private void chooseDir(JTextField testField) {
        final JFileChooser fc = new JFileChooser(testField.getText() != null && !testField.getText().isEmpty() ? testField.getText() : null);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = fc.getSelectedFile();
            testField.setText(file.getAbsolutePath());

        }
    }

    private void onOK() {
        boolean ok = true;
        if (null == textFieldSrc.getText() || textFieldSrc.getText().isEmpty()) {
            textArea.append("Le répertoire source n'est pas renseigné\n");
            ok = false;
        }
        if (null == textFieldDst.getText() || textFieldDst.getText().isEmpty()) {
            textArea.append("Le répertoire destination n'est pas renseigné\n");
            ok = false;
        }
        if (ok) {
            File confDir = new File(CONF_DIR);
            if (!confDir.exists())
                confDir.mkdir();
            Properties properties = new Properties();
            File propertiesFile = new File(CONF_FILE);
            try (OutputStream fos = new FileOutputStream(propertiesFile)) {
                properties.setProperty("src", textFieldSrc.getText());
                properties.setProperty("dest", textFieldDst.getText());

                properties.store(fos, DateFormat.getDateInstance().format(new Date()));
            } catch (IOException e) {
                logger.log(Level.WARNING, e.getLocalizedMessage(), e);
            }

            buttonOK.setEnabled(false);
            textArea.append("=============================\n");
            Thread worker = new Thread(new Runnable() {
                public void run() {
                    final File sourceDir = new File(textFieldSrc.getText());
                    final File destDir = new File(textFieldDst.getText());

                    final long start = System.currentTimeMillis();
                    final Result result = OrganizePhoto.organizeAll(sourceDir, destDir, OrganizePhoto.BASE_DIR_PATTERN_FORMAT, renamePhoto.isSelected() ? OrganizePhoto.PHOTO_NAME_LONG_FORMAT : null);
                    final long end = System.currentTimeMillis();

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            textArea.append("=============================\n");
                            textArea.append("    Resultat du traitement : " + result.getNbImagesProcessed() + "∕" + result.getNbImagesToProcess() + " (" + (end - start) + " ms)");
                            textArea.append("\n=============================\n");
                            buttonOK.setEnabled(true);
                        }
                    });


                }
            }
            );
            worker.start();
        }
    }

    private void onCancel() {
// add your code here if necessary
        handlerDispose();
    }

    private void handlerDispose() {

        OrganizePhoto.removeHandler(logHandler);
        FileHelper.removeHandler(logHandler);
        dispose();
    }

    public static void main(String[] args) {
        Main dialog = new Main();
        dialog.pack();
        dialog.setResizable(false);

        dialog.setVisible(true);

        System.exit(0);
    }

    static class MyHandler extends Handler {

        private JTextArea textArea;

        public MyHandler(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void publish(LogRecord logRecord) {
            if (isLoggable(logRecord)) {
                //textArea.append(getFormatter().format(logRecord));
                final String message = logRecord.getMessage();
                if (null != message && !message.isEmpty()) {
                    textArea.append(message.concat("\n"));
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

}
