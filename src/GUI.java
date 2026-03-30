import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;

public class GUI extends JFrame {

    private Sorter mySorter = new Sorter();
    private File selectedFolder = null;
    
    private JTextField folderPathBox;
    private JTextArea outputBox;

    public GUI() {
        setTitle("File Organizer App");
        setSize(780, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        
        // standard white background to keep it looking clean and simple
        getContentPane().setBackground(Color.WHITE);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        makeTopSection();
        makeMiddleSection();
        makeBottomSection();
        
        catchPrints(); // grab System.out prints so they show up in the textarea
    }

    private void makeTopSection() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createTitledBorder("Folder to Organize"));

        folderPathBox = new JTextField("Pick a folder...");
        folderPathBox.setEditable(false);
        folderPathBox.setBackground(new Color(245, 245, 245)); // light gray

        JButton browseBtn = new BasicRoundButton("Browse", Color.BLACK, Color.WHITE);
        browseBtn.addActionListener(e -> {
            JFileChooser filePicker = new JFileChooser();
            filePicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (filePicker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFolder = filePicker.getSelectedFile();
                folderPathBox.setText(selectedFolder.getAbsolutePath());
                System.out.println("Chosen folder: " + selectedFolder.getAbsolutePath());
            }
        });

        topPanel.add(folderPathBox, BorderLayout.CENTER);
        topPanel.add(browseBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void makeMiddleSection() {
        outputBox = new JTextArea();
        outputBox.setEditable(false);
        outputBox.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputBox.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollBox = new JScrollPane(outputBox);
        scrollBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollBox, BorderLayout.CENTER);
    }

    private void makeBottomSection() {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnStart = new BasicRoundButton("Organize", Color.BLACK, Color.WHITE);
        JButton btnPrev = new BasicRoundButton("Preview", Color.WHITE, Color.BLACK);
        JButton btnUndo1 = new BasicRoundButton("Undo Last", Color.WHITE, Color.BLACK);
        JButton btnUndoAll = new BasicRoundButton("Undo All", Color.BLACK, Color.WHITE);

        bottomPanel.add(btnStart);
        bottomPanel.add(btnPrev);
        bottomPanel.add(btnUndo1);
        bottomPanel.add(btnUndoAll);

        // use thread so the UI doesn't freeze while copying files
        btnStart.addActionListener(e -> doWork(() -> mySorter.organizeFiles(selectedFolder, "hybrid")));
        btnPrev.addActionListener(e -> doWork(() -> mySorter.previewChanges(selectedFolder)));
        btnUndo1.addActionListener(e -> doWork(() -> mySorter.undoLast()));
        
        btnUndoAll.addActionListener(e -> doWork(() -> {
            int result = JOptionPane.showConfirmDialog(this, "Reset all files?", "Warning", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) mySorter.undoAll();
        }));

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void doWork(Runnable theJob) {
        if (selectedFolder == null) {
            JOptionPane.showMessageDialog(this, "Hey, pick a folder first!");
            return;
        }
        new Thread(() -> {
            theJob.run();
            System.out.println("--- Finished ---");
        }).start();
    }

    // intercepts standard console outputs
    private void catchPrints() {
        PrintStream stream = new PrintStream(new java.io.OutputStream() {
            public void write(int b) {
                SwingUtilities.invokeLater(() -> outputBox.append(String.valueOf((char) b)));
            }
        });
        System.setOut(stream);
        System.setErr(stream);
    }

    // A simple button class to give us those clean rounded edges in black and white
    class BasicRoundButton extends JButton {
        private Color buttonBg;
        public BasicRoundButton(String text, Color bg, Color fg) {
            super(text);
            this.buttonBg = bg;
            setForeground(fg);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics myGraphics) {
            Graphics2D g2d = (Graphics2D) myGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // change to gray if hovering
            g2d.setColor(getModel().isRollover() ? Color.GRAY : buttonBg);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            
            // add line if background is white 
            if (buttonBg == Color.WHITE) {
                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
            super.paintComponent(myGraphics);
        }
    }
}
