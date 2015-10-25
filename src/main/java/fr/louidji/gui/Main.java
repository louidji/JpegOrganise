package fr.louidji.gui;

import fr.louidji.tools.FileHelper;
import fr.louidji.tools.Organize;
import fr.louidji.tools.Result;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class Main extends JDialog {
    private static final String CONF_DIR = System.getProperty("user.home").concat(File.separator).concat(".organizephoto").concat(File.separator);
    private static final String CONF_FILE = CONF_DIR.concat("lastvalues.properties");
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private final MyHandler logHandler;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldSrc;
    private JButton sourceButton;
    private JTextField textFieldDst;
    private JButton destinationButton;
    private JTextArea textArea;
    private JCheckBox renamePhoto;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    private Main() {
        setTitle("Application d'organisation des médias");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        sourceButton.addActionListener(actionEvent -> handleSrc());
        destinationButton.addActionListener(actionEvent -> handleDest());


        logHandler = new MyHandler(textArea);
        Organize.addHandler(logHandler);
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

    public static void main(String[] args) {
        Main dialog = new Main();

        dialog.setMinimumSize(new Dimension(400, 300));
        dialog.setResizable(true);

        dialog.pack();

        dialog.setVisible(true);

        System.exit(0);
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
            if (!confDir.exists()) {
                if (!confDir.mkdir())
                    textArea.append("Impossible de créer le répertoire de configuration ".concat(CONF_DIR).concat("\n"));
            }
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
            Thread worker = new Thread(() -> {
                final File sourceDir = new File(textFieldSrc.getText());
                final File destDir = new File(textFieldDst.getText());

                final long start = System.currentTimeMillis();
                final Result result = Organize.organizeAll(sourceDir, destDir, Organize.BASE_DIR_PATTERN_FORMAT, renamePhoto.isSelected() ? Organize.PHOTO_NAME_LONG_FORMAT : null);
                final long end = System.currentTimeMillis();

                SwingUtilities.invokeLater(() -> {
                    textArea.append("=============================\n");
                    textArea.append("    Resultat du traitement : " + result.getNbImagesProcessed() + "∕" + result.getNbImagesToProcess() + " (" + (end - start) + " ms)");
                    textArea.append("\n=============================\n");
                    buttonOK.setEnabled(true);
                });


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

        Organize.removeHandler(logHandler);
        FileHelper.removeHandler(logHandler);
        dispose();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel1.add(buttonOK);
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel1.add(buttonCancel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel2, gbc);
        textFieldSrc = new JTextField();
        textFieldSrc.setMargin(new Insets(0, 0, 0, 0));
        textFieldSrc.setMinimumSize(new Dimension(360, 20));
        textFieldSrc.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(textFieldSrc, gbc);
        sourceButton = new JButton();
        sourceButton.setMargin(new Insets(2, 14, 2, 14));
        sourceButton.setText("Source");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(sourceButton, gbc);
        textFieldDst = new JTextField();
        textFieldDst.setMinimumSize(new Dimension(360, 20));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(textFieldDst, gbc);
        destinationButton = new JButton();
        destinationButton.setText("Destination");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel2.add(destinationButton, gbc);
        renamePhoto = new JCheckBox();
        renamePhoto.setSelected(true);
        renamePhoto.setText("Renomer les médias avec la date");
        renamePhoto.setToolTipText("Renome les photos en fonction de la prise de vue");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(renamePhoto, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(scrollPane1, gbc);
        textArea = new JTextArea();
        textArea.setBackground(new Color(-1250059));
        textArea.setEditable(false);
        textArea.setEnabled(true);
        textArea.setLineWrap(true);
        textArea.setMinimumSize(new Dimension(525, 300));
        textArea.setRows(3);
        textArea.setText("");
        scrollPane1.setViewportView(textArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    static class MyHandler extends Handler {

        private final JTextArea textArea;

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
