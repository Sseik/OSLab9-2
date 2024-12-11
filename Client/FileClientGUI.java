import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class FileClientGUI {
    private static final String PIPE_NAME = "\\\\.\\pipe\\mynamedpipe"; // Ім'я каналу

    private JFrame frame;
    private JTextField directoryField; // Поле для введення директорії
    private JTextField typeField; // Поле для введення типу файлів
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel; 
    private JLabel firstLineLabel; 
    private JLabel secondLineLabel; 

    public FileClientGUI() {
        frame = new JFrame("File Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Панель вводу
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 10, 10)); // Додано третій рядок для типу
        inputPanel.add(new JLabel("Директорія:"));
        directoryField = new JTextField();
        inputPanel.add(directoryField);

        inputPanel.add(new JLabel("Тип файлів:"));
        typeField = new JTextField(); // Поле для введення типу файлів
        inputPanel.add(typeField);

        JButton sendButton = new JButton("Надіслати запит");
        sendButton.setBackground(new Color(0, 102, 204)); // Синій фон кнопки
        sendButton.setForeground(Color.WHITE); // Білий текст на кнопці
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });
        inputPanel.add(sendButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        // Мітки для відображення інформації
        firstLineLabel = new JLabel("Перший рядок: ");
        secondLineLabel = new JLabel("Розмір: ");
        infoLabel = new JLabel("Кількість файлів: 0, Сумарний розмір: 0 байт");
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.add(firstLineLabel);
        infoPanel.add(secondLineLabel);
        infoPanel.add(infoLabel);
        
        frame.add(infoPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new String[]{"Ім'я файлу", "Дата створення"}, 0);
        fileTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(fileTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void sendRequest() {
        String directory = directoryField.getText().trim();
        String fileType = typeField.getText().trim(); // Отримуємо тип файлів

        if (directory.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Будь ласка, введіть директорію.", "Помилка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Формат запиту: "C:/path|*.ext"
        String request = directory + "|" + (fileType.isEmpty() ? ".*" : fileType) + "\0"; // Використовуємо вказаний тип файлів

        try (RandomAccessFile pipe = new RandomAccessFile(PIPE_NAME, "rw")) {
            // Відправка запиту
            pipe.writeBytes(request);

            // Читання відповіді
            String firstLine = pipe.readLine(); // Перший рядок
            String secondLine = pipe.readLine(); // Другий рядок

            // Записуємо перші два рядки в мітки
            firstLineLabel.setText("Перший рядок: " + firstLine);
            if (secondLine != null) {
                String[] secondLineParts = secondLine.split(" ");
                if (secondLineParts.length > 0) {
                    secondLineLabel.setText("Розмір: " + secondLineParts[secondLineParts.length - 1]); // Остання частина другого рядка
                }
            }

            String response;
            long totalSize = 0; // Сумарний розмір файлів
            int fileCount = 0; // Кількість файлів
            tableModel.setRowCount(0); // Очищення таблиці
            while ((response = pipe.readLine()) != null) {
                response = response.trim(); // Обрізаємо зайві пробіли
                if (!response.isEmpty()) { // Перевіряємо, чи рядок не порожній
                    int lastSpaceIndex = response.lastIndexOf(" ");
                    if (lastSpaceIndex != -1) {
                        String name = response.substring(0, lastSpaceIndex); // Назва файлу
                        String date = response.substring(lastSpaceIndex + 1); // Дата
                        tableModel.addRow(new Object[]{name, date}); // Додаємо рядок до таблиці

                        // Додаємо розмір файлу (припускаємо, що розмір файлу передається в рядку)
                        String[] parts = response.split(" ");
                        if (parts.length > 2) {
                            try {
                                long size = Long.parseLong(parts[parts.length - 1]); // Розмір файлу
                                totalSize += size;
                                fileCount++;
                            } catch (NumberFormatException e) {
                                System.err.println("Неправильний формат розміру файлу: " + parts[parts.length - 1]);
                            }
                        }
                    } else {
                        System.err.println("Неправильний формат рядка: " + response);
                    }
                }
            }

            infoLabel.setText("Кількість файлів: " + fileCount + ", Сумарний розмір: " + totalSize + " байт");

        } catch (IOException e) {
            // Логування помилки, але не виводимо специфічний текст
            if (!e.getMessage().contains("No process is on the other end of the pipe")) {
                System.err.println("Не вдалося підключитися до сервера: " + e.getMessage());
                JOptionPane.showMessageDialog(frame, "Не вдалося підключитися до сервера: " + e.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileClientGUI::new);
    }
}