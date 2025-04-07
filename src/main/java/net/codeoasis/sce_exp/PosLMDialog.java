package net.codeoasis.sce_exp;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PosLMDialog extends DialogWrapper {

    private JTextField extractLineField;

    public PosLMDialog() {
        super(true); // use current window as parent
        setTitle("Labeling positive long method.");
        init();  // Initializes the dialog
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        panel.add(new JLabel("Save as positive sample:."));
        panel.add(new JLabel(""));
        panel.add(new JLabel("Extract Lines:"));
        extractLineField = new JTextField(20);
        panel.add(extractLineField);

//        panel.add(new JLabel("Password:"));
//        passwordField = new JPasswordField(20);
//        panel.add(passwordField);

        return panel;
    }

    public String getExtractLine() {
        return extractLineField.getText();
    }
}
