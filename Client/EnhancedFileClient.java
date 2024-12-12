import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class EnhancedFileClient {
    private static final String PIPE_NAME = "\\\\.\\pipe\\mynamedpipe"; // Ім'я каналу

    private JFrame frame;
    private JTextField directoryField; // Поле для введення директорії
    private JTextField typeField; // Поле для введення типу файлів
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JLabel infoLabel; // Мітка для відображення інформації

    public EnhancedFileClient() {
        frame = new JFrame("File Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Головний градієнтний фон
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(123, 104, 238), getWidth(), getHeight(), new Color(72, 61, 139));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        frame.add(mainPanel);

        // Панель вводу
        JPanel inputPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(70, 130, 180), 0, getHeight(), new Color(135, 206, 250));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        inputPanel.setLayout(new GridBagLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel dirLabel = new JLabel("Директорія:");
        dirLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(dirLabel, gbc);

        directoryField = new JTextField();
        directoryField.setBackground(new Color(200, 200, 200)); // Світло-сірий фон
        directoryField.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        inputPanel.add(directoryField, gbc);

        JLabel typeLabel = new JLabel("Тип файлів:");
        typeLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(typeLabel, gbc);

        typeField = new JTextField();
        typeField.setBackground(new Color(200, 200, 200)); // Світло-сірий фон
        typeField.setForeground(Color.BLACK);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        inputPanel.add(typeField, gbc);

        JButton sendButton = new JButton("Надіслати запит") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 102, 204), getWidth(), getHeight(), new Color(0, 204, 204));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g2d);
            }
        };
        sendButton.setOpaque(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setForeground(Color.WHITE);        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(sendButton, gbc);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest();
            }
        });
        // Панель інформації
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(70, 130, 180)); // Темно-синій фон панелі інформації
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        infoLabel = new JLabel("Кількість файлів: 0, Сумарний розмір: 0 байт");
        infoLabel.setForeground(Color.WHITE);
        infoPanel.add(infoLabel);

        // Таблиця файлів
        tableModel = new DefaultTableModel(new String[]{"Ім'я файлу", "Дата створення"}, 0);
        fileTable = new JTable(tableModel);
        fileTable.setBackground(new Color(200, 200, 200)); // Сірий фон
        fileTable.setForeground(new Color(25, 25, 112));
        fileTable.setRowHeight(25);
        fileTable.getTableHeader().setBackground(new Color(72, 61, 139));
        fileTable.getTableHeader().setForeground(Color.WHITE);
        fileTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(fileTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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
            String response;
            //long totalSize = 0; // Сумарний розмір файлів
            //int fileCount = 0; // Кількість файлів
            tableModel.setRowCount(0); // Очищення таблиці
            response = pipe.readLine();
            response = response.trim();
            String fileCount = response.substring(response.lastIndexOf(" ") + 1);
            response = pipe.readLine();
            response = response.trim();
            String totalSize = response.substring(response.lastIndexOf(" ") + 1);
            infoLabel.setText("Кількість файлів: " + fileCount + ", Сумарний розмір: " + totalSize + " байт");
            while ((response = pipe.readLine()) != null) {
                response = response.trim(); // Обрізаємо зайві пробіли
                if (!response.isEmpty()) { // Перевіряємо, чи рядок не порожній
                    int lastSpaceIndex = response.lastIndexOf(" ");
                    if (lastSpaceIndex != -1) {
                        String name = response.substring(0, lastSpaceIndex); // Назва файлу
                        String date = response.substring(lastSpaceIndex + 1); // Дата
                        tableModel.addRow(new Object[]{name, date}); // Додаємо рядок до таблиці
                    } else {
                        System.err.println("Неправильний формат рядка: " + response);
                    }
                }
            }


        } catch (IOException e) {
            // Логування помилки, але не виводимо специфічний текст
            if (!e.getMessage().contains("No process is on the other end of the pipe")) {
                System.err.println("Не вдалося підключитися до сервера: " + e.getMessage());
                JOptionPane.showMessageDialog(frame, "Не вдалося підключитися до сервера: " + e.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedFileClient::new);
    }
}