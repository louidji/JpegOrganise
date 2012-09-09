package fr.louidji.gui;

import fr.louidji.tools.FileHelper;
import fr.louidji.tools.OrganizePhoto;
import fr.louidji.tools.Result;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Main extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textFieldSrc;
    private JButton sourceButton;
    private JTextField textFieldDst;
    private JButton destinationButton;
    private JTextArea textArea;
    private MyHandler logHandler = new MyHandler(textArea);

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
            final File sourceDir = new File(textFieldSrc.getText());
            final File destDir = new File(textFieldDst.getText());
            textArea.append("=============================\n");
            final long start = System.currentTimeMillis();
            Result result = OrganizePhoto.organizeAll(sourceDir, destDir);
            final long end = System.currentTimeMillis();
            textArea.append("=============================\n");
            textArea.append("    Resultat du traitement : " + result.getNbImagesProcessed() + "∕" + result.getNbImagesToProcess() + " (" + (end - start) + " ms)");
            textArea.append("\n=============================\n");

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
