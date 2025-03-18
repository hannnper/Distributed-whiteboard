// Gui: class for the graphical user interface (both client and manager)
package whiteboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import java.io.File;
import java.io.IOException;



public class Gui {
    private Dimension SQUARE_BUTTON_SIZE = new Dimension(45, 45);
    public JFrame frame;
    public Whiteboard whiteboard;
    public JTextPane chatDisplay;
    public JPanel userDisplay;
    public JList<ChatUser> userList;
    private Boolean isManager;

    public Gui(Boolean isManager) {
        this.isManager = isManager;
        init();
        frame.setVisible(true);
    }

    private void init(){
        frame = new JFrame();
        frame.setBounds(50, 50, 1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridBagLayout());

        // menu bar for manager
        if (isManager) {
            addMenuBar();
        }
            
        // panel for whiteboard and options
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(880, 700));
        leftPanel.setMinimumSize(new Dimension(850, 600));
        GridBagConstraints gbc_leftPanel = new GridBagConstraints();
        gbc_leftPanel.insets = new Insets(0, 0, 0, 0);
        gbc_leftPanel.anchor = GridBagConstraints.WEST;
        gbc_leftPanel.weightx = 0;
        gbc_leftPanel.gridx = 0;
        gbc_leftPanel.gridy = 0;
        frame.getContentPane().add(leftPanel, gbc_leftPanel);

        whiteboard = new Whiteboard(isManager);
        whiteboard.setPreferredSize(new Dimension(whiteboard.WIDTH, whiteboard.HEIGHT));
        whiteboard.setMinimumSize(new Dimension(whiteboard.WIDTH, whiteboard.HEIGHT));
        GridBagConstraints gbc_whiteboard = new GridBagConstraints();
        gbc_whiteboard.insets = new Insets(10, 10, 10, 10);
        gbc_whiteboard.anchor = GridBagConstraints.NORTH;
        gbc_whiteboard.gridx = 0;
        gbc_whiteboard.gridy = 0;
        leftPanel.add(whiteboard, gbc_whiteboard);

        whiteboard.isManager = isManager;  // not sure if this is necessary
        askForUsername();

        // panel for whiteboard options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());
        optionsPanel.setPreferredSize(new Dimension(800, 50));
        GridBagConstraints gbc_optionsPanel = new GridBagConstraints();
        gbc_optionsPanel.insets = new Insets(0, 0, 0, 0);
        gbc_optionsPanel.gridx = 0;
        gbc_optionsPanel.gridy = 1;
        //gbc_optionsPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_optionsPanel.anchor = GridBagConstraints.WEST;
        gbc_optionsPanel.weightx = 1;

        // add color picker
        JPanel colorPanel = makeColorPicker();
        colorPanel.setPreferredSize(new Dimension(800, 60));
        GridBagConstraints gbc_colorPanel = new GridBagConstraints();
        gbc_colorPanel.insets = new Insets(5, 5, 5, 5);
        gbc_colorPanel.gridx = 0;
        // left align the color picker
        gbc_colorPanel.anchor = GridBagConstraints.WEST;
        optionsPanel.add(colorPanel, gbc_colorPanel);

        // add shape picker
        JPanel shapePanel = makeShapePicker();
        GridBagConstraints gbc_shapePanel = new GridBagConstraints();
        gbc_shapePanel.insets = new Insets(5, 5, 5, 5);
        gbc_shapePanel.gridx = 1;
        optionsPanel.add(shapePanel, gbc_shapePanel);

        // add stroke width picker
        JPanel widthPicker = makeStrokeWidthPicker();
        GridBagConstraints gbc_widthPicker = new GridBagConstraints();
        gbc_widthPicker.insets = new Insets(5, 5, 5, 5);
        gbc_widthPicker.gridx = 2;
        gbc_widthPicker.anchor = GridBagConstraints.EAST;
        optionsPanel.add(widthPicker, gbc_widthPicker);

        // add the options panel to the left panel
        leftPanel.add(optionsPanel, gbc_optionsPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setMinimumSize(new Dimension(250, 700));
        GridBagConstraints gbc_rightPanel = new GridBagConstraints();
        gbc_rightPanel.insets = new Insets(0, 0, 0, 0);
        gbc_rightPanel.fill = GridBagConstraints.BOTH;
        gbc_rightPanel.weightx = 1;
        gbc_rightPanel.gridx = 1;
        gbc_rightPanel.gridy = 0;
        frame.getContentPane().add(rightPanel, gbc_rightPanel);

        // add chat panel
        JPanel chatPanel = makeChatPanel();
        GridBagConstraints gbc_chatPanel = new GridBagConstraints();
        gbc_chatPanel.insets = new Insets(0, 0, 0, 0);
        gbc_chatPanel.fill = GridBagConstraints.BOTH;
        gbc_chatPanel.anchor = GridBagConstraints.NORTH;
        gbc_chatPanel.gridx = 0;
        gbc_chatPanel.gridy = 0;
        rightPanel.add(chatPanel, gbc_chatPanel);

        // add user panel
        JPanel userPanel = displayUsers();
        GridBagConstraints gbc_userPanel = new GridBagConstraints();
        gbc_userPanel.insets = new Insets(0, 0, 0, 0);
        gbc_userPanel.fill = GridBagConstraints.BOTH;
        gbc_userPanel.gridx = 0;
        gbc_userPanel.gridy = 1;
        rightPanel.add(userPanel, gbc_userPanel);

    }

    public void askForUsername() {
        // ask for username
        String username = JOptionPane.showInputDialog(frame, "Enter your username", "Username", JOptionPane.PLAIN_MESSAGE);
        while (username == null || username.isEmpty()) {
            if (username == null) {
                System.exit(0);
            }
            username = JOptionPane.showInputDialog(frame, "Your username must be at least 1 character. Please enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
        }
        whiteboard.username = username;
    }

    public void askAgainForUsername() {
        // ask for username again
        String username = JOptionPane.showInputDialog(frame, "Username already taken. Please enter a different username:", "Username", JOptionPane.PLAIN_MESSAGE);
        while (username == null || username.isEmpty()) {
            if (username == null) {
                System.exit(0);
            }
            username = JOptionPane.showInputDialog(frame, "Your username must be at least 1 character. Please enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
        }
        whiteboard.username = username;
    }
    
    private void addMenuBar() {
        // creates a menu bar with File menu
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // options for File menu include new, open, save, saveAs, and close

        // 'New' option creates a new whiteboard
        JMenuItem newMenuItem = new JMenuItem("New");
        fileMenu.add(newMenuItem);
        newMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // prompt to save if there are any changes
                int result = promptToSave();
                if (result != JOptionPane.CANCEL_OPTION) {
                    // clear the whiteboard and then ask for background color
                    whiteboard.clear();
                    whiteboard.currentFile = null;
                    Color color = JColorChooser.showDialog(frame, "Choose Background Color", whiteboard.getBackgroundColor());
                    if (color != null) {
                        whiteboard.fillBackground(color);
                    }
                }
                
            }
        });

        // 'Open' option opens a previously saved whiteboard
        JMenuItem openMenuItem = new JMenuItem("Open");
        fileMenu.add(openMenuItem);
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // prompt to save if there are any changes
                int result = promptToSave();
                if (result != JOptionPane.CANCEL_OPTION) {
                    // open a whiteboard
                    openWhiteboard();
                }
            }
        });

        // 'Save' option saves the current whiteboard
        JMenuItem saveMenuItem = new JMenuItem("Save");
        fileMenu.add(saveMenuItem);
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveWhiteboard();
            }
        });

        // 'Save As' option saves the current whiteboard to a new file
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        fileMenu.add(saveAsMenuItem);
        saveAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAsWhiteboard();
            }
        });

        // 'Close' option closes the whiteboard
        JMenuItem closeMenuItem = new JMenuItem("Close");
        fileMenu.add(closeMenuItem);
        closeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // prompt to save if there are any changes
                int result = promptToSave();
                if (result != JOptionPane.CANCEL_OPTION) {
                    closeWhiteboard();
                }
            }
        });

        frame.setJMenuBar(menuBar);

    }

    public int promptToSave() {
        if (!whiteboard.hasUnsavedChanges) {
            return JOptionPane.NO_OPTION;
        }
        int result = JOptionPane.showConfirmDialog(frame, "Do you want to save your current whiteboard?", "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // save the whiteboard
            saveWhiteboard();
            whiteboard.hasUnsavedChanges = false;
        } 
        return result;
    }

    public void saveAsWhiteboard() {
        // open a filechooser to select a file
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            if (!fileName.toLowerCase().endsWith(".png")) {
                fileName += ".png";
            }
            try {
                // check if the file already exists
                if (new File(fileName).exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(frame, "File already exists. Do you want to overwrite it?", "Overwrite File?", JOptionPane.YES_NO_OPTION);
                    if (overwrite == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                ImageIO.write(whiteboard.canvas, "png", new File(fileName));
                whiteboard.currentFile = fileName;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error: couldn't save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveWhiteboard() {
        if (whiteboard.currentFile == null) {
            saveAsWhiteboard();
        } 
        else {
            try {
                ImageIO.write(whiteboard.canvas, "png", new File(whiteboard.currentFile));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error: couldn't save file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void openWhiteboard() {
        // open a filechooser to select a file
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            whiteboard.currentFile = file.getAbsolutePath();
            try {
                BufferedImage content = ImageIO.read(file);
                whiteboard.setImage(content);
            } 
            catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error: couldn't open file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void closeWhiteboard() {
        try {
            whiteboard.remoteWhiteboard.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        frame.dispose();
        System.exit(0);
    }

    private JPanel makeColorPicker() {
        // make square button for draw color
        JButton colorButton = new JButton();
        colorButton.setPreferredSize(SQUARE_BUTTON_SIZE);
        colorButton.setMinimumSize(SQUARE_BUTTON_SIZE);
        colorButton.setBackground(whiteboard.getDrawColor());
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // open a color picker dialog
                Color color = JColorChooser.showDialog(frame, "Choose Draw Color", whiteboard.getDrawColor());
                if (color != null) {
                    whiteboard.setDrawColor(color);
                    colorButton.setBackground(color);
                }
            }
        });

        // make square button for fill color
        JButton fillButton = new JButton();
        fillButton.setPreferredSize(SQUARE_BUTTON_SIZE);
        fillButton.setBackground(whiteboard.getFillColor());
        fillButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // open a color picker dialog
                Color color = JColorChooser.showDialog(frame, "Choose Fill Color", whiteboard.getFillColor());
                if (color != null) {
                    whiteboard.setFillColor(color);
                    fillButton.setBackground(color);
                }
            }
        });

        // for manager only, add a button to change background color
        JButton backgroundButton = new JButton();
        backgroundButton.setPreferredSize(SQUARE_BUTTON_SIZE);
        backgroundButton.setBackground(whiteboard.getBackgroundColor());
        backgroundButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // add a warning that this will erase everything, then prompt to save
                int result = JOptionPane.showConfirmDialog(frame, "Changing the background color will erase everything. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    // open a color picker dialog
                    Color color = JColorChooser.showDialog(frame, "Choose Background Color", whiteboard.getBackgroundColor());
                    if (color != null) {
                        whiteboard.fillBackground(color);
                        backgroundButton.setBackground(color);
                    }
                }
            }
        });

        // add the buttons to a panel
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new FlowLayout());
        colorPanel.add(colorButton);
        colorPanel.add(fillButton);
        if (isManager) {
            colorPanel.add(backgroundButton);
        }

        return colorPanel;
    }

    private JPanel makeShapePicker() {
        Color selectedColor = Color.LIGHT_GRAY;
        Color unselectedColor = Color.DARK_GRAY;
        // make square button for free draw, line, rectangle, oval, circle, erase, and text
        JButton freeDrawButton = new JButton("📝");
        JButton lineButton = new JButton("📏");
        JButton rectangleButton = new JButton("🟪");
        JButton ovalButton = new JButton("🥚");
        JButton circleButton = new JButton("🟢");
        JButton eraseButton = new JButton("🧽");
        JButton textButton = new JButton("🔤");
        ArrayList<JButton> buttons = new ArrayList<JButton>();
        buttons.add(freeDrawButton);
        buttons.add(lineButton);
        buttons.add(rectangleButton);
        buttons.add(ovalButton);
        buttons.add(circleButton);
        buttons.add(eraseButton);
        buttons.add(textButton);

        for (JButton button : buttons) {
            button.setFont(button.getFont().deriveFont(25.0f));
            button.setPreferredSize(SQUARE_BUTTON_SIZE);
        }

        // set the default tool to free draw and the rest unselected
        for (JButton button : buttons) {
            button.setBackground(unselectedColor);
        }
        freeDrawButton.setBackground(selectedColor);

        // add action listeners
        freeDrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.FREE_DRAW);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                freeDrawButton.setBackground(selectedColor);
            }
        });

        lineButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.LINE);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                lineButton.setBackground(selectedColor);
            }
        });

        rectangleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.RECTANGLE);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                rectangleButton.setBackground(selectedColor);
            }
        });

        ovalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.OVAL);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                ovalButton.setBackground(selectedColor);
            }
        });

        circleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.CIRCLE);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                circleButton.setBackground(selectedColor);
            }
        });

        eraseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.ERASE);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                eraseButton.setBackground(selectedColor);
            }
        });

        textButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                whiteboard.setTool(whiteboard.TEXT);
                // set other buttons to dark grey and current button to light grey
                for (JButton button : buttons) {
                    button.setBackground(unselectedColor);
                }
                textButton.setBackground(selectedColor);
            }
        });

        // add the buttons to a panel
        JPanel shapePanel = new JPanel();
        shapePanel.setMinimumSize(new Dimension(350, 55));
        shapePanel.setLayout(new FlowLayout());
        for (JButton button : buttons) {
            shapePanel.add(button);
        }

        return shapePanel;
    }

    private JPanel makeStrokeWidthPicker() {
        // use a JComboBox for the stroke width
        String[] widths = {"1", "2", "4", "8", "16", "32", "64", "128", "256"};
        JComboBox<String> widthPicker = new JComboBox<String>(widths);
        widthPicker.setSelectedItem("1");
        widthPicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String width = (String) widthPicker.getSelectedItem();
                whiteboard.setStrokeWidth(Integer.parseInt(width));
            }
        });

        // also make a JComboBox for font size
        String[] fontSizes = {"8", "12", "16", "20", "24", "30", "36", "42", "48", "60", "72", "96", "120"};
        JComboBox<String> fontSizePicker = new JComboBox<String>(fontSizes);
        fontSizePicker.setSelectedItem("20");
        fontSizePicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String fontSize = (String) fontSizePicker.getSelectedItem();
                whiteboard.setFontSize(Integer.parseInt(fontSize));
            }
        });

        // label
        JLabel widthLabel = new JLabel("Stroke width: ");
        JLabel fontSizeLabel = new JLabel("Font size: ");

        JPanel widthPanel = new JPanel();
        widthPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        widthPanel.add(widthLabel, gbc);
        gbc.gridx = 1;
        widthPanel.add(widthPicker, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        widthPanel.add(fontSizeLabel, gbc);
        gbc.gridx = 1;
        widthPanel.add(fontSizePicker, gbc);

        return widthPanel;
    }

    public JPanel makeChatPanel() {
        // make a chat panel
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new GridBagLayout());

        // display chat messages in a scrollable text pane
        chatDisplay = new JTextPane();
        chatDisplay.setContentType("text/html");
        chatDisplay.setEditable(false);

        if (isManager) {
            chatDisplay.setText("<html><p><span style=\"color:#ffd663\">Welcome to the whiteboard! You are the manager.</span></p></html>");
        }
        
        JScrollPane scrollPane = new JScrollPane(chatDisplay);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        scrollPane.setMinimumSize(new Dimension(300, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        chatPanel.add(scrollPane, gbc);
        gbc.gridwidth = 1;

        // text field for chat message
        JTextField chatField = new JTextField();

        // send button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = chatField.getText();
                if (!message.isEmpty()) {
                    ChatMessage chatMessage = new ChatMessage(whiteboard.username, message, whiteboard.userColor);
                    try {
                        whiteboard.remoteWhiteboard.broadcastChatMessage(chatMessage);
                    } catch (Exception e2) {
                        String errmsg = "<span style=\"color:red\">Error: couldn't send chat message\n</span>";
                        chatDisplay.setText(chatDisplay.getText() + errmsg);
                    }
                    chatField.setText("");
                }
            }
        });

        // add the chat field and send button to the chat panel
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        chatPanel.add(chatField, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        chatPanel.add(sendButton, gbc);

        return chatPanel;
    }

    public void addChatMessage(ChatMessage message) {
        // add a chat message to the chat display
        HTMLEditorKit kit = (HTMLEditorKit) chatDisplay.getEditorKit();
        HTMLDocument doc = (HTMLDocument) chatDisplay.getDocument();
        try {
            kit.insertHTML(doc, doc.getLength(), message.formatChatMessage(), 0, 0, null);
            chatDisplay.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JPanel displayUsers() {
        // make a panel to display chat users
        userDisplay = new JPanel();
        userDisplay.setLayout(new GridBagLayout());

        // display chat users in a scrollable list
        ArrayList<ChatUser> chatUsers = new ArrayList<ChatUser>();
        userList = new JList<ChatUser>();
        userList.setListData(chatUsers.toArray(new ChatUser[0]));

        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setMinimumSize(new Dimension(300, 200));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        userDisplay.add(scrollPane, gbc);

        // resetting gridbag constraints
        gbc = new GridBagConstraints();

        // panel for buttons and labels
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        // add small square button for user color
        JButton userColorButton = new JButton();
        userColorButton.setPreferredSize(new Dimension(20, 20));
        userColorButton.setBackground(whiteboard.userColor);
        userColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // open a color picker dialog
                Color color = JColorChooser.showDialog(frame, "Choose User Color", whiteboard.userColor);
                if (color != null) {
                    whiteboard.userColor = color;
                    userColorButton.setBackground(color);
                    try {
                        whiteboard.remoteWhiteboard.setUserColor(whiteboard.username, color);
                    }
                    catch (Exception ex) {
                        String errmsg = "<span style=\"color:red\">Error: couldn't set user color\n</span>";
                        chatDisplay.setText(chatDisplay.getText() + errmsg);
                    }
                }
            }
        });

        // add the user color button to the bottom left of the button panel
        gbc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(userColorButton, gbc);

        // add a label with the username next to color button
        JLabel userLabel = new JLabel(whiteboard.username);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        buttonPanel.add(userLabel);

        // make a button for the manager to kick a user
        if (isManager) {
            JButton kickButton = new JButton("Kick");
            kickButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // get the selected user
                    ChatUser selectedUser = userList.getSelectedValue();
                    if (selectedUser != null) {
                        try {
                            whiteboard.remoteWhiteboard.kickUser(selectedUser.getAuthor());
                        } 
                        catch (Exception e2) {
                            String errmsg = "<span style=\"color:red\">Error: couldn't kick user\n</span>";
                            chatDisplay.setText(chatDisplay.getText() + errmsg);
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(frame, "Please select a user in the userlist to kick", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // add the kick button to the user display button panel
            gbc.gridx = 2;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.NONE;
            buttonPanel.add(kickButton, gbc);
        }

        // add the button panel to the user display
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weightx = 1;
        userDisplay.add(buttonPanel, gbc);

        return userDisplay;
    }

    public void kickWarning() {
        JOptionPane.showMessageDialog(frame, "You have been kicked from the whiteboard 😢", "Kicked", JOptionPane.WARNING_MESSAGE);
        frame.dispose();
    }

    public void updateUserList(ArrayList<ChatUser> users) {
        userList.setListData(users.toArray(new ChatUser[0]));
    }

    public Boolean acceptJoinRequest(String username) {
        int result = JOptionPane.showConfirmDialog(frame, username + " wants to join your whiteboard. Allow them to join?", "Join Request", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public void showDeniedMessage() {
        JOptionPane.showMessageDialog(frame, "Your request to join the whiteboard has been denied 🙅", "Denied", JOptionPane.WARNING_MESSAGE);
        frame.dispose();
    }

    public void showServerErrorMessage() {
        JOptionPane.showMessageDialog(frame, "Error: couldn't connect to server 😔", "Error", JOptionPane.ERROR_MESSAGE);
        frame.dispose();
    }

    public void showLeaveMessage() {
        JOptionPane.showMessageDialog(frame, "You are leaving the whiteboard", "Leaving", JOptionPane.WARNING_MESSAGE);
        frame.dispose();
    }

    public void showRegistryErrorMessage() {
        JOptionPane.showMessageDialog(frame, "Error: couldn't create registry or bind remote object! Please check hostname and port number are allowed to be used for Java RMI registry.", "Error", JOptionPane.ERROR_MESSAGE);
        frame.dispose();
    }

    public void showCloseMessage() {
        JOptionPane.showMessageDialog(frame, "The whiteboard has been closed by the manager 👋", "Closed", JOptionPane.WARNING_MESSAGE);
        frame.dispose();
        System.exit(0);
    }
}

