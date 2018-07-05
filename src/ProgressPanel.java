import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Maciek on 20/11/2016.
 */
public class ProgressPanel extends JPanel {
    private JLabel label;
    private JProgressBar progressBar;
    public JButton button;

    public ProgressPanel(String text){
        label = new JLabel(text);
        button = new JButton("Ok");
        button.setVisible(false);

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setMaximum(0);
        progressBar.setStringPainted(true);

        add(label);
        add(progressBar);
        add(button);
    }

    public JProgressBar getProgressBar(){
        return progressBar;
    }
}
