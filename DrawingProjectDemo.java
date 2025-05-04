package ProjectEyad;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DrawingProjectDemo extends JFrame {

    private DrawingPanel drawingPanel;
    private JSlider thicknessSlider;
    private JLabel statusLabel;
    private JPopupMenu popupMenu;

    // Enum for drawing modes
    private class ShapeType {
        public static int LINE = 0;
        public static int OVAL = 1;
        public static int RECTANGLE = 2;
    } // Using RECTANGLE as it's easier with mouse drag than perfect square

    // Current drawing state
    private int currentShape = ShapeType.LINE;
    private Color currentColor = Color.BLACK;
    private int currentThickness = 2;

    public DrawingProjectDemo() {
        super("Simple Drawing App Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null); // Center the window

        // --- Top Panel for Controls ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Slider for line thickness
        thicknessSlider = new JSlider(JSlider.HORIZONTAL, 0, 20, currentThickness);
        thicknessSlider.setMajorTickSpacing(5);
        thicknessSlider.setMinorTickSpacing(1);
        thicknessSlider.setPaintTicks(true);
        thicknessSlider.setPaintLabels(true);
        thicknessSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                currentThickness = thicknessSlider.getValue();
                updateStatus();
                drawingPanel.setCurrentThickness(currentThickness); // Update panel's knowledge
            }

        });

        JLabel sliderLabel = new JLabel("Thickness:");
        controlPanel.add(sliderLabel);
        controlPanel.add(thicknessSlider);

        // --- Status Label ---
        statusLabel = new JLabel();
        updateStatus(); // Initial status

        // --- Drawing Panel ---
        drawingPanel = new DrawingPanel();
        drawingPanel.setBackground(Color.WHITE);

        // --- Popup Menu ---
        setupPopupMenu();
        // Add mouse listener to drawing panel to trigger popup
        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
                // Request focus when panel is clicked, needed for KeyListener
                drawingPanel.requestFocusInWindow();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) { // Check for right-click
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // --- Key Listener ---
        // Added to the drawingPanel, make sure it's focusable
        drawingPanel.setFocusable(true);
        drawingPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_L:
                        currentShape = ShapeType.LINE;
                        System.out.println("Mode: Line");
                        break;
                    case KeyEvent.VK_O:
                        currentShape = ShapeType.OVAL;
                        System.out.println("Mode: Oval");
                        break;
                    case KeyEvent.VK_S: // Use 'S' for Square/Rectangle
                    case KeyEvent.VK_R:
                        currentShape = ShapeType.RECTANGLE;
                        System.out.println("Mode: Rectangle");
                        break;
                    case KeyEvent.VK_C:
                        chooseColor();
                        System.out.println("Color Chooser Triggered");
                        break;
                    case KeyEvent.VK_X:
                        drawingPanel.clearDrawing();
                        System.out.println("Canvas Cleared");
                        break;
                }
                updateStatus();
                drawingPanel.setCurrentShape(currentShape); // Update panel's knowledge
            }
        });

        // --- Layout ---
        setLayout(new BorderLayout(5, 5));
        add(controlPanel, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Make panel focusable initially
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                drawingPanel.requestFocusInWindow();
            }
        });
    }

    private void setupPopupMenu() {
        popupMenu = new JPopupMenu();

        // Menu items for shapes
        JMenuItem lineItem = new JMenuItem("Draw Line (L)");
        lineItem.addActionListener(e -> {
            currentShape = ShapeType.LINE;
            updateStatus();
            drawingPanel.setCurrentShape(currentShape);
        });

        JMenuItem ovalItem = new JMenuItem("Draw Oval (O)");
        ovalItem.addActionListener(e -> {
            currentShape = ShapeType.OVAL;
            updateStatus();
            drawingPanel.setCurrentShape(currentShape);
        });

        JMenuItem rectItem = new JMenuItem("Draw Square/Rect (S)");
        rectItem.addActionListener(e -> {
            currentShape = ShapeType.RECTANGLE;
            updateStatus();
            drawingPanel.setCurrentShape(currentShape);
        });

        // Menu item for color
        JMenuItem colorItem = new JMenuItem("Choose Color (C)");
        colorItem.addActionListener(e -> chooseColor());

        // Menu item to clear
        JMenuItem clearItem = new JMenuItem("Clear Canvas (X)");
        clearItem.addActionListener(e -> drawingPanel.clearDrawing());

        popupMenu.add(lineItem);
        popupMenu.add(ovalItem);
        popupMenu.add(rectItem);
        popupMenu.addSeparator();
        popupMenu.add(colorItem);
        popupMenu.addSeparator();
        popupMenu.add(clearItem);
    }

    private void chooseColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Drawinfffg Color", currentColor);
        if (newColor != null) {
            currentColor = newColor;
            updateStatus();
            drawingPanel.setCurrentColor(currentColor); // Update panel's knowledge
        }
        // Return focus to panel after dialog closes
        drawingPanel.requestFocusInWindow();
    }

    private void updateStatus() {
        statusLabel.setText(" Mode: " + currentShape + " | Color: ["
                + currentColor.getRed() + "," + currentColor.getGreen() + "," + currentColor.getBlue()
                + "] | Thickness: " + currentThickness
                + " | Keys: L/O/S=Shape, C=Color, X=Clear | Right-Click for Menu");
        // Also update panel state directly if needed (already done via
        // listeners/setters)
    }

    // --- Main Entry Point ---
    public static void main(String[] args) {

        DrawingProjectDemo app = new DrawingProjectDemo();
        app.setVisible(true);

    }

    // --- Inner Class for the Drawing Panel ---
    private class DrawingPanel extends JPanel {

        private Point startPoint = null;
        private Point endPoint = null;
        private final List<ShapeInfo> shapes = new ArrayList<>();

        // Keep local copies of current settings for drawing in progress
        private int panelCurrentShape = currentShape;
        private Color panelCurrentColor = currentColor;
        private int panelCurrentThickness = currentThickness;

        public DrawingPanel() {
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Ignore right-clicks for drawing start
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        startPoint = e.getPoint();
                        endPoint = startPoint; // Initialize endPoint
                        requestFocusInWindow(); // Ensure focus for key events
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // Ignore right-click drags
                    if (SwingUtilities.isLeftMouseButton(e) && startPoint != null) {
                        endPoint = e.getPoint();
                        repaint(); // Repaint to show shape being dragged
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && startPoint != null && endPoint != null) {
                        // Create and store the final shape
                        ShapeInfo shape = new ShapeInfo(panelCurrentShape, panelCurrentColor,
                                startPoint.x, startPoint.y, endPoint.x, endPoint.y,
                                panelCurrentThickness);
                        shapes.add(shape);
                        startPoint = null; // Reset for next shape
                        endPoint = null;
                        repaint();
                    }
                    // Handle popup trigger on release as well (common on some OS)
                    showPopup(e); // Call the outer class's popup handler
                }

                // Need this method defined here or accessible for the listener
                private void showPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            };
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter); // Handles mouseDragged
        }

        // Methods to update panel's internal state from the main class
        public void setCurrentShape(int type) {
            this.panelCurrentShape = type;
        }

        public void setCurrentColor(Color color) {
            this.panelCurrentColor = color;
        }

        public void setCurrentThickness(int thickness) {
            this.panelCurrentThickness = thickness;
        }

        public void clearDrawing() {
            shapes.clear();
            startPoint = null;
            endPoint = null;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw all the completed shapes
            for (ShapeInfo shape : shapes) {
                g2d.setColor(shape.color);
                g2d.setStroke(new BasicStroke(shape.thickness));
                drawShape(g2d, shape.type, shape.x1, shape.y1, shape.x2, shape.y2);
            }

            // 2. Draw the shape currently being created (if dragging)
            if (startPoint != null && endPoint != null) {
                g2d.setColor(panelCurrentColor);
                g2d.setStroke(new BasicStroke(panelCurrentThickness));
                // Use a slightly lighter/dashed stroke for preview if desired
                // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                // float[] dash = { 5.0f };
                // g2d.setStroke(new BasicStroke(panelCurrentThickness, BasicStroke.CAP_BUTT,
                // BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));

                drawShape(g2d, panelCurrentShape, startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
        }

        // Helper method to draw shapes based on type and coordinates
        private void drawShape(Graphics2D g2d, int type, int x1, int y1, int x2, int y2) {
            switch (type) {
                case 0:
                    g2d.drawLine(x1, y1, x2, y2);
                    break;
                case 1: {
                    int x = Math.min(x1, x2);
                    int y = Math.min(y1, y2);
                    int width = Math.abs(x1 - x2);
                    int height = Math.abs(y1 - y2);
                    g2d.drawOval(x, y, width, height);
                    break;
                }
                case 2: {
                    int x = Math.min(x1, x2);
                    int y = Math.min(y1, y2);
                    int width = Math.abs(x1 - x2);
                    int height = Math.abs(y1 - y2);
                    g2d.drawRect(x, y, width, height); // Draws a rectangle
                    break;
                }
            }
        }
    }

    // Simple record to hold shape information
    private class ShapeInfo {
        public int type;
        public Color color;
        public int x1;
        public int y1;
        public int x2;
        public int y2;
        public int thickness;

        public ShapeInfo(int type, Color color, int x1, int y1, int x2, int y2, int thickness) {
            this.type = type;
            this.color = color;
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
            this.thickness = thickness;

        }
    }
}