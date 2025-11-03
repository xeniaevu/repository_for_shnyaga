import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    public static void main(String[] args) {


        JFrame frame = new JFrame("Open File Dialog Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 150);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        JButton openButton = new JButton("Открыть файл");
        JLabel selectedLabel = new JLabel("Выбранного файла нет");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Выберите файл");
                int userSelection = fileChooser.showOpenDialog(frame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = fileChooser.getSelectedFile();
                    String path = file.getAbsolutePath();

                    System.out.println("Выбран файл: " + path);
                    selectedLabel.setText("Выбран файл: " + path);
                } else {
                    System.out.println("Операция выбора файла отменена.");
                    selectedLabel.setText("Выбранного файла нет");
                }
            }
        });


        panel.add(openButton);
        panel.add(selectedLabel);
        frame.add(panel);


        frame.setVisible(true);
    }
}
