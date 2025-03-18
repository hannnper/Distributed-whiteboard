// Whiteboard: class for the whiteboard and features
package whiteboard;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class Whiteboard extends JPanel {
    // size of the canvas
    public int WIDTH = 800;
    public int HEIGHT = 500;

    // tools
    public String FREE_DRAW = "Free Draw";
    public String LINE = "Line";
    public String RECTANGLE = "Rectangle";
    public String OVAL = "Oval";
    public String CIRCLE = "Circle";
    public String TEXT = "Text";
    public String ERASE = "Erase";

    // shared remote whiteboard
    public WBInterface remoteWhiteboard;
    public String username;
    public Color userColor = Color.LIGHT_GRAY;

    // canvas and graphics objects and settings
    public BufferedImage canvas;
    public BufferedImage tempCanvas;
    private Graphics2D graphics;
    private Graphics2D tempGraphics;
    private Color backgroundColor = Color.WHITE;
    private Color drawColor = Color.BLACK;
    private Color fillColor = Color.GRAY;
    public Boolean isManager = false;
    public Boolean hasUnsavedChanges = false;
    public String currentFile = null;
    private int startX, startY, endX, endY;
    private Boolean drawing = false;
    private Boolean typing = false;
    private String currentTool = FREE_DRAW;
    private int strokeWidth = 1;
    private String inputText = "";
    private int fontSize = 20;


    public Whiteboard(Boolean isManager) {

        canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        tempCanvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        graphics = canvas.createGraphics();
        tempGraphics = tempCanvas.createGraphics();
        this.isManager = isManager;

        // set the background color for the visible canvas
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        // make the temporary layer transparent
        clearTempCanvas();

        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // a click and drag listener for drawing
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (typing) {
                    // ending text input
                    typing = false;
                    wDrawText(inputText, startX, startY, drawColor, fontSize, false);
                    handleRemoteDraw(TEXT, startX, startY, endX, endY, drawColor, null, fontSize);
                    inputText = "";
                }
                startX = e.getX();
                startY = e.getY();
                drawing = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;

                // get the new end point of the line
                endX = e.getX();
                endY = e.getY();

                // clear the temp canvas back to transparent
                clearTempCanvas();

                if (currentTool.equals(FREE_DRAW)) {
                    wDrawLine(startX, startY, endX, endY, drawColor, strokeWidth, false);
                    startX = endX;
                    startY = endY;
                }
                else if (currentTool.equals(ERASE)) {
                    wDrawLine(startX, startY, endX, endY, backgroundColor, strokeWidth, false);
                    startX = endX;
                    startY = endY;
                }
                else if (currentTool.equals(LINE)) {
                    wDrawLine(startX, startY, endX, endY, drawColor, strokeWidth, false);
                }
                else if (currentTool.equals(RECTANGLE)) {
                    wDrawRect(startX, startY, endX - startX, endY - startY, drawColor, fillColor, strokeWidth, false);
                }
                else if (currentTool.equals(OVAL)) {
                    wDrawOval(startX, startY, endX - startX, endY - startY, drawColor, fillColor, strokeWidth, false);
                }
                else if (currentTool.equals(CIRCLE)) {
                    int diameter = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
                    wDrawCircle(startX, startY, diameter, drawColor, fillColor, strokeWidth, false);
                }
                else if (currentTool.equals(TEXT)) {
                    // starting text input
                    requestFocusInWindow();
                    typing = true;
                }
                repaint();

                // sending the draw event to the remote whiteboard
                if (currentTool.equals(TEXT)) ;
                else if (currentTool.equals(ERASE)) {
                    handleRemoteDraw(currentTool, startX, startY, endX, endY, backgroundColor, null, strokeWidth);
                }
                else {
                    handleRemoteDraw(currentTool, startX, startY, endX, endY, drawColor, fillColor, strokeWidth);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing) {

                    // get the new end point of the line
                    endX = e.getX();
                    endY = e.getY();

                    // clear the temp canvas back to transparent
                    clearTempCanvas();

                    // draw the shape on the temp canvas (or the real canvas if it's free draw or erase)
                    if (currentTool.equals(FREE_DRAW)) {
                        wDrawLine(startX, startY, endX, endY, drawColor, strokeWidth, false);
                        handleRemoteDraw(currentTool, startX, startY, endX, endY, drawColor, null, strokeWidth);
                        startX = endX;
                        startY = endY;
                    }
                    else if (currentTool.equals(ERASE)) {
                        wDrawLine(startX, startY, endX, endY, backgroundColor, strokeWidth, false);
                        handleRemoteDraw(currentTool, startX, startY, endX, endY, backgroundColor, null, strokeWidth);
                        startX = endX;
                        startY = endY;
                    }
                    else if (currentTool.equals(LINE)) {
                        wDrawLine(startX, startY, endX, endY, drawColor, strokeWidth, true);
                    }
                    else if (currentTool.equals(RECTANGLE)) {
                        wDrawRect(startX, startY, endX - startX, endY - startY, drawColor, fillColor, strokeWidth, true);
                    }
                    else if (currentTool.equals(OVAL)) {
                        wDrawOval(startX, startY, endX - startX, endY - startY, drawColor, fillColor, strokeWidth, true);
                    }
                    else if (currentTool.equals(CIRCLE)) {
                        int diameter = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
                        wDrawCircle(startX, startY, diameter, drawColor, fillColor, strokeWidth, true);
                    }
                }
            }
        });

        // key listener for text input
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (typing) {
                    // handle backspace/delete key
                    if (e.getKeyChar() == '\b') {
                        if (inputText.length() > 0) {
                            inputText = inputText.substring(0, inputText.length() - 1);
                        }
                    } 
                    else {
                        inputText += e.getKeyChar();
                    }
                    clearTempCanvas();
                    wDrawText(inputText, startX, startY, drawColor, fontSize, true);
                }
            }
            
        });

    }

    public void wDrawLine(int x1, int y1, int x2, int y2, Color color, int strokeSize, Boolean temp) {
        Graphics2D g = temp ? tempGraphics : graphics;
        g.setColor(color);
        g.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(x1, y1, x2, y2);
        repaint();
        if (!temp) {
            hasUnsavedChanges = true;
        }
    }

    public void wDrawRect(int x, int y, int width, int height, Color outline, Color fill, int strokeSize, Boolean temp) {
        Graphics2D g = temp ? tempGraphics : graphics;
        g.setColor(fill);
        g.fillRect(x, y, width, height);
        g.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(outline);
        g.drawRect(x, y, width, height);
        repaint();
        if (!temp) {
            hasUnsavedChanges = true;
        }
    }

    public void wDrawOval(int x, int y, int width, int height, Color outline, Color fill, int strokeSize, Boolean temp) {
        Graphics2D g = temp ? tempGraphics : graphics;
        g.setColor(fill);
        g.fillOval(x, y, width, height);
        g.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(outline);
        g.drawOval(x, y, width, height);
        repaint();
        if (!temp) {
            hasUnsavedChanges = true;
        }
    }

    public void wDrawCircle(int x, int y, int diameter, Color outline, Color fill, int strokeSize, Boolean temp) {
        Graphics2D g = temp ? tempGraphics : graphics;
        g.setColor(fill);
        g.fillOval(x, y, diameter, diameter);
        g.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(outline);
        g.drawOval(x, y, diameter, diameter);
        repaint();
        if (!temp) {
            hasUnsavedChanges = true;
        }
    }

    public void wDrawText(String text, int x, int y, Color color, int fontSize, Boolean temp) {
        Graphics2D g = temp ? tempGraphics : graphics;
        g.setColor(color);
        g.setFont(new Font("Calibri", Font.PLAIN, fontSize));
        g.drawString(text, x, y);
        if (temp) {
            // draw a dotted box around the text
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0));
            g.drawRect(x, y - fontSize, g.getFontMetrics().stringWidth(text), fontSize);
        }
        repaint();
        if (!temp) {
            hasUnsavedChanges = true;
        }
    }

    public void clearTempCanvas() {
        tempGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        tempGraphics.fillRect(0, 0, WIDTH, HEIGHT);
        tempGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        repaint();
    }

    public void fillBackground(Color color) {
        // Note: this goes over the entire canvas, so it will erase everything
        // in case the new color is not opaque
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        setBackgroundColor(color);
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        repaint();
        handleRemoteReplace();
        handleRemoteColorChange();
    }

    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setDrawColor(Color color) {
        drawColor = color;
    }

    public Color getDrawColor() {
        return drawColor;
    }

    public void setFillColor(Color color) {
        fillColor = color;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
        graphics.setStroke(new BasicStroke(strokeWidth));
        tempGraphics.setStroke(new BasicStroke(strokeWidth));
    }

    public void setFontSize(int size) {
        fontSize = size;
    }

    public void setTool(String tool) {
        currentTool = tool;
    }

    public void clear() {
        setBackgroundColor(Color.WHITE);
        setBackground(backgroundColor);
        handleRemoteReplace();
    }

    public void setImage(BufferedImage image) {
        copyOntoCanvas(image);
        handleRemoteReplace();
    }

    public void setRemoteWhiteboard(WBInterface whiteboard) {
        remoteWhiteboard = whiteboard;
    }

    public void handleRemoteReplace() {
        try {
            if (isManager) {
            remoteWhiteboard.replaceCanvas(getCanvasBytes());
            }
        }
        catch (Exception e) {
            // show error popup
            if (!isManager) {
                JOptionPane.showMessageDialog(this, "Lost connection to whiteboard", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
    }

    public void handleRemoteDraw(String tool, int x1, int y1, int x2, int y2, Color color, Color fill, int size) {
        Element element = new Element(tool, x1, y1, x2, y2, size, color, fill, inputText);
        try {
            remoteWhiteboard.drawElement(element);
        }
        catch (Exception e) {
            if (!isManager) {
                // show error popup
                JOptionPane.showMessageDialog(this, "Lost connection to whiteboard", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
        
    }

    public void handleRemoteColorChange() {
        try {
            remoteWhiteboard.setBackgroundColor(backgroundColor);
        }
        catch (Exception e) {
            // this only gets called by manager
            e.printStackTrace();
        }
    }

    public byte[] getCanvasBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(canvas, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public BufferedImage bytesToBI(byte[] bytes) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public void copyOntoCanvas(byte[] bytes) {
        // copy the image from the bytes onto the canvas
        BufferedImage img = bytesToBI(bytes);
        graphics.drawImage(img, 0, 0, null);
        repaint();
    }

    public void copyOntoCanvas(BufferedImage img) {
        // copy the images onto the canvas (takes a BufferedImage object as input)
        graphics.drawImage(img, 0, 0, null);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
        g.drawImage(tempCanvas, 0, 0, null);
    }
}
