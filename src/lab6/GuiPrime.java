package lab6;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class GuiPrime extends JFrame {
    private static final long serialVersionUID = 1L;

    // Variables
    private JTextField NumberField = new JTextField();
    private JTextArea outputArea = new JTextArea();
    private JButton findPrimeButton = new JButton("Compute Prime");
    private JLabel directions = new JLabel("Find Prime Numbers Between 1 and Entered Number");
    private JLabel timer = new JLabel("Compute Time (Milliseconds): ");
    private int Primecount = 0;  
    private volatile boolean isCancelled = false;
    private JButton cancelButton = new JButton("Pause/Cancel");
    private JComboBox<Integer> workerSelector; // Worker selection box

    public GuiPrime() {
        // Set up JFrame properties
        setTitle("Prime Number Finder");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        directions.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(directions, BorderLayout.NORTH);
        topPanel.add(NumberField, BorderLayout.CENTER);
        topPanel.add(findPrimeButton, BorderLayout.EAST);

        // Worker Selector
        workerSelector = new JComboBox<>();
        for (int i = 1; i <= 10; i++) {
            workerSelector.addItem(i); // Add numbers 1-10 to the selection box
        }
        JLabel workerLabel = new JLabel("Number of Workers: ");
        JPanel workerPanel = new JPanel();
        workerPanel.add(workerLabel);
        workerPanel.add(workerSelector);

        // Add worker panel to top panel
        topPanel.add(workerPanel, BorderLayout.SOUTH);

        // Add top panel to frame
        getContentPane().add(topPanel, BorderLayout.NORTH);

        // Center Panel
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        timer.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottomPanel.add(timer, BorderLayout.WEST);
        bottomPanel.add(cancelButton, BorderLayout.EAST);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        // Button action listener
        findPrimeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCancelled = false; // Reset the cancel flag
                int numWorkers = (int) workerSelector.getSelectedItem(); // Get selected number of workers
                new Thread(() -> computePrimes(numWorkers)).start();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCancelled = true; // Set the cancel flag
        
                // Show the partial results dynamically
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText("Computation paused or canceled.\n" +
                        "Partial Results:\n" +
                        "Total Primes Found: " + Primecount);
                    timer.setText("Compute Time (Milliseconds): Paused");
                });
            }
        });
    }        
        

    private void computePrimes(int numWorkers) {
        String input = NumberField.getText();
        try {
            int number = Integer.parseInt(input);
            if (number < 2) {
                SwingUtilities.invokeLater(() -> outputArea.setText("Please enter a number greater than 1."));
                return;
            }

            long startTime = System.currentTimeMillis();

            // Compute primes using brute force
            StringBuilder primes = new StringBuilder();
            Primecount = 0;

            for (int currentNumber = 2; currentNumber <= number; currentNumber++) {

                if (isCancelled) {
                    final int lastProcessedNumber = currentNumber; // Make a copy of currentNumber
                    final String partialResults = primes.toString();
                    SwingUtilities.invokeLater(() -> {
                        outputArea.setText("Computation cancelled.\nPartial Results:\n" + partialResults +
                            "\nTotal Primes Found: " + Primecount +
                            "\nLast Processed Number: " + lastProcessedNumber);
                        NumberField.setText(String.valueOf(lastProcessedNumber)); // Update the input field
                    });
                    return;
                }

                if (isPrime(currentNumber)) {
                    Primecount++;
                    primes.append(currentNumber).append("\n");

                    // Add a small delay to simulate slower computation
                    try {
                        Thread.sleep(110 / numWorkers); // Adjust delay based on number of workers
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    // Periodically update the output area in the GUI thread
                    final String currentPrimes = primes.toString();
                    final int displayNumber = currentNumber; // Make a copy for lambda
                    if (Primecount % 10 == 0 || currentNumber == number) {
                        SwingUtilities.invokeLater(() -> outputArea.setText("Computing primes...\n" + currentPrimes +
                            "\nLast Processed Number: " + displayNumber));
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            double elapsedSeconds = (endTime - startTime) / 1000.0;

            SwingUtilities.invokeLater(() -> {
                outputArea.setText("Prime Numbers:\n" + primes.toString() +
                    "\nTotal Primes Found: " + Primecount);
                timer.setText("Compute Time (Seconds): " + elapsedSeconds);
            });

        } catch (NumberFormatException ex) {
            SwingUtilities.invokeLater(() -> outputArea.setText("Invalid input. Please enter a valid number."));
        }
    }

    private boolean isPrime(int num) {
        if (num < 2) return false;
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GuiPrime gui = new GuiPrime();
            gui.setVisible(true);
        });
    }
}
