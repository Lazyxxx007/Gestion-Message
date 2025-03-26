package Client;

import ClientUtils.ChatClient;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatClientGUI extends JFrame {
    private String username;
    private ChatClient chatClient;
    private ThemeManager themeManager;
    private UserManager userManager;
    private JTextPane chatArea;
    private JTextField messageField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton sendButton;
    private JButton profileButton;
    private JLabel connectedLabel;
    private JButton logoutButton;
    private JLabel themeIconLabel;
    private JPanel mainPanel;
    private JButton addContactButton;
    private JButton archivedChatsButton;
    private boolean showingArchivedChats = false;
    private Map<String, String> userStatuses;
    private String selectedContact;
    private JScrollPane userScrollPane;
    private ImageIcon sunIcon;
    private ImageIcon moonIcon;
    private JPanel chatHeaderPanel;
    private JLabel contactNameLabel;
    private JLabel typingLabel;
    private JButton settingsButton;
    private JLabel contactsLabel;
    private JSplitPane splitPane;
    private JPanel headerPanel;
    private boolean isTyping = false;
    private Timer typingTimer;
    private Timer statusUpdateTimer;
    private Set<String> displayedMessages = new HashSet<>();

    public ChatClientGUI(String username, ChatClient chatClient, ThemeManager themeManager) {
        this.username = username;
        this.chatClient = chatClient;
        this.themeManager = themeManager;
        this.userManager = new UserManager();
        this.userStatuses = new HashMap<>();
        this.selectedContact = null;
        setTitle("Chat - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        typingTimer = new Timer(2000, e -> stopTyping());
        typingTimer.setRepeats(false);

        statusUpdateTimer = new Timer(1000, e -> updateContactListWithoutReloadingConversation()); // Actualisation chaque seconde
        statusUpdateTimer.start();

        try {
            sunIcon = new ImageIcon(getClass().getResource("/icons/sun.png"));
            moonIcon = new ImageIcon(getClass().getResource("/icons/moon.png"));
            sunIcon = new ImageIcon(sunIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
            moonIcon = new ImageIcon(moonIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des icônes : " + e.getMessage());
            sunIcon = null;
            moonIcon = null;
        }

        initializeUI();
        startListening();
        updateContactListWithoutReloadingConversation();
        checkPendingRequests();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(themeManager.getBackgroundColor());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(51, 51, 51));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        leftPanel.setBackground(new Color(51, 51, 51));

        themeIconLabel = new JLabel();
        updateThemeIcon();
        themeIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                themeManager.toggleTheme();
                updateTheme();
            }
        });
        leftPanel.add(themeIconLabel);

        connectedLabel = new JLabel("Connecté en tant que : " + username);
        connectedLabel.setForeground(Color.WHITE);
        connectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        connectedLabel.setFont(new Font("Arial", Font.BOLD, 20));
        leftPanel.add(connectedLabel);

        topPanel.add(leftPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightPanel.setBackground(new Color(51, 51, 51));

        profileButton = new JButton("Profil 👤");
        profileButton.setBackground(new Color(51, 51, 51));
        profileButton.setForeground(Color.WHITE);
        profileButton.setFocusPainted(false);
        profileButton.setBorderPainted(false);
        profileButton.addActionListener(e -> showProfile());
        rightPanel.add(profileButton);

        logoutButton = new JButton("Déconnexion ↪");
        logoutButton.setBackground(new Color(51, 51, 51));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(e -> logout());
        rightPanel.add(logoutButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel contactsPanel = new JPanel(new BorderLayout());
        contactsPanel.setBackground(themeManager.getBackgroundColor());

        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.DARK_GRAY);
        contactsLabel = new JLabel("Contacts");
        contactsLabel.setForeground(Color.WHITE);
        contactsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        headerPanel.add(contactsLabel, BorderLayout.WEST);

        addContactButton = new JButton("+");
        addContactButton.setBackground(themeManager.getButtonBackgroundColor());
        addContactButton.setForeground(themeManager.getForegroundColor());
        addContactButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addContactButton.addActionListener(e -> showAddContactDialog());
        headerPanel.add(addContactButton, BorderLayout.EAST);
        contactsPanel.add(headerPanel, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel) {
            @Override
            public ListCellRenderer<String> getCellRenderer() {
                return new ContactCellRenderer();
            }
        };
        userList.setBackground(themeManager.getBackgroundColor());
        userList.setForeground(themeManager.getForegroundColor());
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedValue = userList.getSelectedValue();
                if (selectedValue != null) {
                    selectedContact = selectedValue.split(" ")[0];
                    loadConversation(selectedContact);
                    updateChatHeader();
                } else {
                    selectedContact = null;
                    chatArea.setText("");
                    updateChatHeader();
                }
            }
        });
        userScrollPane = new JScrollPane(userList);
        userScrollPane.setMinimumSize(new Dimension(200, 0));
        contactsPanel.add(userScrollPane, BorderLayout.CENTER);

        archivedChatsButton = new JButton("Discussions archivées");
        archivedChatsButton.setBackground(themeManager.getButtonBackgroundColor());
        archivedChatsButton.setForeground(themeManager.getForegroundColor());
        archivedChatsButton.addActionListener(e -> toggleArchivedChats());
        contactsPanel.add(archivedChatsButton, BorderLayout.SOUTH);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(themeManager.getBackgroundColor());

        chatHeaderPanel = new JPanel(new BorderLayout());
        chatHeaderPanel.setBackground(themeManager.getBackgroundColor());
        chatHeaderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setBackground(themeManager.getBackgroundColor());

        contactNameLabel = new JLabel("");
        contactNameLabel.setForeground(themeManager.getForegroundColor());
        contactNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerTextPanel.add(contactNameLabel);

        typingLabel = new JLabel("");
        typingLabel.setForeground(themeManager.getForegroundColor());
        typingLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        headerTextPanel.add(typingLabel);

        chatHeaderPanel.add(headerTextPanel, BorderLayout.WEST);

        settingsButton = new JButton("⚙️");
        settingsButton.setBackground(themeManager.getButtonBackgroundColor());
        settingsButton.setForeground(themeManager.getForegroundColor());
        settingsButton.setFocusPainted(false);
        settingsButton.setBorderPainted(false);
        settingsButton.addActionListener(e -> showContactOptions());
        chatHeaderPanel.add(settingsButton, BorderLayout.EAST);

        chatPanel.add(chatHeaderPanel, BorderLayout.NORTH);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(themeManager.isDarkMode() ? Color.BLACK : Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        StyledDocument doc = chatArea.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style userStyle = chatArea.addStyle("UserStyle", defaultStyle);
        StyleConstants.setAlignment(userStyle, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setBackground(userStyle, new Color(173, 216, 230));
        StyleConstants.setForeground(userStyle, themeManager.isDarkMode() ? Color.WHITE : Color.BLACK);
        StyleConstants.setSpaceAbove(userStyle, 5);
        StyleConstants.setSpaceBelow(userStyle, 5);

        Style contactStyle = chatArea.addStyle("ContactStyle", defaultStyle);
        StyleConstants.setAlignment(contactStyle, StyleConstants.ALIGN_LEFT);
        StyleConstants.setBackground(contactStyle, new Color(211, 211, 211));
        StyleConstants.setForeground(contactStyle, themeManager.isDarkMode() ? Color.WHITE : Color.BLACK);
        StyleConstants.setSpaceAbove(contactStyle, 5);
        StyleConstants.setSpaceBelow(contactStyle, 5);

        Style systemStyle = chatArea.addStyle("SystemStyle", defaultStyle);
        StyleConstants.setAlignment(systemStyle, StyleConstants.ALIGN_CENTER);
        StyleConstants.setItalic(systemStyle, true);
        StyleConstants.setForeground(systemStyle, Color.GRAY);
        StyleConstants.setSpaceAbove(systemStyle, 5);
        StyleConstants.setSpaceBelow(systemStyle, 5);

        chatArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int position = chatArea.viewToModel2D(e.getPoint());
                    try {
                        int lineStart = doc.getParagraphElement(position).getStartOffset();
                        int lineEnd = doc.getParagraphElement(position).getEndOffset();
                        String line = doc.getText(lineStart, lineEnd - lineStart).trim();
                        if (line.contains(": ")) {
                            String sender = line.substring(line.indexOf(" ") + 1, line.indexOf(":")).trim();
                            if (!sender.equals(username) && !sender.startsWith("SYSTEM")) {
                                showChatOptions(sender);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Erreur lors de la récupération de la ligne : " + ex.getMessage());
                    }
                }
            }
        });
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(themeManager.getBackgroundColor());
        messageField = new JTextField();
        messageField.setForeground(themeManager.getForegroundColor());
        messageField.addActionListener(e -> sendMessage());
        messageField.addKeyListener(new KeyAdapter() {
            private long lastTypingSent = 0;
            private final long TYPING_INTERVAL = 2000;

            @Override
            public void keyTyped(KeyEvent e) {
                long currentTime = System.currentTimeMillis();
                if (!isTyping || (currentTime - lastTypingSent >= TYPING_INTERVAL)) {
                    isTyping = true;
                    if (selectedContact != null) {
                        try {
                            chatClient.sendSystemMessage("TYPING", username + ":" + selectedContact);
                            lastTypingSent = currentTime;
                        } catch (IOException ex) {
                            appendSystemMessage("Erreur lors de l'envoi de la notification TYPING : " + ex.getMessage());
                        }
                    }
                }
                typingTimer.restart();
            }
        });
        sendButton = new JButton("Envoyer");
        sendButton.setBackground(themeManager.getButtonBackgroundColor());
        sendButton.setForeground(themeManager.getForegroundColor());
        sendButton.addActionListener(e -> sendMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contactsPanel, chatPanel);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setBackground(Color.GRAY);
        splitPane.setForeground(Color.GRAY);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustContactListWidth();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    chatClient.sendSystemMessage("DISCONNECT", username);
                    userManager.mettreAJourStatutUtilisateur(username, "hors ligne");
                    chatClient.close();
                    statusUpdateTimer.stop();
                } catch (IOException ex) {
                    System.err.println("Erreur lors de la déconnexion : " + ex.getMessage());
                }
            }
        });

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void adjustContactListWidth() {
        int windowWidth = getWidth();
        int newWidth = Math.max(200, windowWidth / 4);
        splitPane.setDividerLocation(newWidth);
    }

    private void updateTheme() {
        mainPanel.setBackground(themeManager.getBackgroundColor());
        chatArea.setBackground(themeManager.isDarkMode() ? Color.BLACK : Color.WHITE);

        StyledDocument doc = chatArea.getStyledDocument();
        Style userStyle = chatArea.getStyle("UserStyle");
        StyleConstants.setForeground(userStyle, themeManager.isDarkMode() ? Color.WHITE : Color.BLACK);
        StyleConstants.setBackground(userStyle, new Color(173, 216, 230));

        Style contactStyle = chatArea.getStyle("ContactStyle");
        StyleConstants.setForeground(contactStyle, themeManager.isDarkMode() ? Color.WHITE : Color.BLACK);
        StyleConstants.setBackground(contactStyle, new Color(211, 211, 211));

        userList.setBackground(themeManager.getBackgroundColor());
        userList.setForeground(themeManager.getForegroundColor());
        messageField.setForeground(themeManager.getForegroundColor());
        sendButton.setBackground(themeManager.getButtonBackgroundColor());
        sendButton.setForeground(themeManager.getForegroundColor());
        addContactButton.setBackground(themeManager.getButtonBackgroundColor());
        addContactButton.setForeground(themeManager.getForegroundColor());
        archivedChatsButton.setBackground(themeManager.getButtonBackgroundColor());
        archivedChatsButton.setForeground(themeManager.getForegroundColor());
        logoutButton.setBackground(new Color(51, 51, 51));
        logoutButton.setForeground(Color.WHITE);
        profileButton.setBackground(new Color(51, 51, 51));
        profileButton.setForeground(Color.WHITE);
        connectedLabel.setForeground(Color.WHITE);
        chatHeaderPanel.setBackground(themeManager.getBackgroundColor());
        contactNameLabel.setForeground(themeManager.getForegroundColor());
        typingLabel.setForeground(themeManager.getForegroundColor());
        settingsButton.setBackground(themeManager.getButtonBackgroundColor());
        settingsButton.setForeground(themeManager.getForegroundColor());
        splitPane.setBackground(themeManager.getBackgroundColor());
        updateThemeIcon();
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(themeManager.getBackgroundColor());
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JLabel && subComp != contactsLabel) {
                        ((JLabel) subComp).setForeground(themeManager.getForegroundColor());
                    }
                }
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void updateThemeIcon() {
        if (themeManager.isDarkMode()) {
            if (moonIcon != null) {
                themeIconLabel.setIcon(moonIcon);
                themeIconLabel.setText("");
            } else {
                themeIconLabel.setText("🌙");
                themeIconLabel.setIcon(null);
            }
        } else {
            if (sunIcon != null) {
                themeIconLabel.setIcon(sunIcon);
                themeIconLabel.setText("");
            } else {
                themeIconLabel.setText("☀️");
                themeIconLabel.setIcon(null);
            }
        }
        themeIconLabel.setForeground(Color.WHITE);
    }

    private void updateChatHeader() {
        if (selectedContact == null) {
            contactNameLabel.setText("");
            typingLabel.setText("");
            settingsButton.setVisible(false);
        } else {
            String status = userManager.obtenirStatutUtilisateur(selectedContact);
            boolean isBlocked = userManager.estBloque(username, selectedContact);
            boolean isArchived = userManager.estArchive(username, selectedContact);
            String statusDisplay;
            if (isBlocked) {
                statusDisplay = "Bloqué 🔴";
            } else if (status.equals("en ligne")) {
                statusDisplay = "En ligne 🟢";
            } else {
                String lastSeen = userManager.obtenirTempsDepuisDerniereConnexion(selectedContact);
                statusDisplay = "Hors ligne (il y a " + lastSeen + ")";
            }
            if (isArchived) {
                statusDisplay += " (Archivé)";
            }
            contactNameLabel.setText(selectedContact + " - " + statusDisplay);
            settingsButton.setVisible(true);
        }
        chatHeaderPanel.revalidate();
        chatHeaderPanel.repaint();
    }

    private void showContactOptions() {
        if (selectedContact == null) return;

        JPopupMenu menu = new JPopupMenu();
        boolean isBlocked = userManager.estBloque(username, selectedContact);
        boolean isArchived = userManager.estArchive(username, selectedContact);

        if (isArchived) {
            JMenuItem unarchiveItem = new JMenuItem("Désarchiver la discussion");
            unarchiveItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous désarchiver la discussion avec " + selectedContact + " ?",
                    "Désarchiver",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userManager.desarchiverContact(username, selectedContact)) {
                        appendSystemMessage("Discussion avec " + selectedContact + " désarchivée.");
                        updateContactListWithoutReloadingConversation();
                        updateChatHeader();
                        if (showingArchivedChats) {
                            toggleArchivedChats();
                        }
                    } else {
                        appendSystemMessage("Erreur lors du désarchivage de la discussion avec " + selectedContact + ".");
                    }
                }
            });
            menu.add(unarchiveItem);
        } else {
            JMenuItem archiveItem = new JMenuItem("Archiver la discussion");
            archiveItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous archiver la discussion avec " + selectedContact + " ?",
                    "Archiver",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userManager.archiverContact(username, selectedContact)) {
                        appendSystemMessage("Discussion avec " + selectedContact + " archivée.");
                        updateContactListWithoutReloadingConversation();
                        selectedContact = null;
                        chatArea.setText("");
                        updateChatHeader();
                    } else {
                        appendSystemMessage("Erreur lors de l'archivage de la discussion avec " + selectedContact + ".");
                    }
                }
            });
            menu.add(archiveItem);
        }

        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous supprimer " + selectedContact + " de vos contacts ? Cette action est irréversible.",
                "Supprimer",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (userManager.supprimerContact(username, selectedContact)) {
                    appendSystemMessage(selectedContact + " a été supprimé de vos contacts.");
                    updateContactListWithoutReloadingConversation();
                    selectedContact = null;
                    chatArea.setText("");
                    updateChatHeader();
                } else {
                    appendSystemMessage("Erreur lors de la suppression de " + selectedContact + ".");
                }
            }
        });
        menu.add(deleteItem);

        if (isBlocked) {
            JMenuItem unblockItem = new JMenuItem("Débloquer");
            unblockItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous débloquer " + selectedContact + " ?",
                    "Débloquer",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userManager.debloquerContact(username, selectedContact)) {
                        appendSystemMessage(selectedContact + " a été débloqué.");
                        updateContactListWithoutReloadingConversation();
                        updateChatHeader();
                    } else {
                        appendSystemMessage("Erreur lors du déblocage de " + selectedContact + ".");
                    }
                }
            });
            menu.add(unblockItem);
        } else {
            JMenuItem blockItem = new JMenuItem("Bloquer");
            blockItem.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Voulez-vous bloquer " + selectedContact + " ?",
                    "Bloquer",
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (userManager.bloquerContact(username, selectedContact)) {
                        appendSystemMessage(selectedContact + " a été bloqué.");
                        updateContactListWithoutReloadingConversation();
                        updateChatHeader();
                    } else {
                        appendSystemMessage("Erreur lors du blocage de " + selectedContact + ".");
                    }
                }
            });
            menu.add(blockItem);
        }

        menu.show(settingsButton, settingsButton.getWidth() / 2, settingsButton.getHeight() / 2);
    }

    private void startListening() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = chatClient.receiveMessage();
                    if (message != null) {

                        if (message.startsWith("SYSTEM:CONTACT_REQUEST:")) {
                            // Gestion des demandes de contact
                            String[] parts = message.split(":", 4);
                            if (parts.length == 4) {
                                String requester = parts[2];
                                String target = parts[3];
                                if (target.equals(username)) {
                                    SwingUtilities.invokeLater(() -> {
                                        appendSystemMessage(requester + " veut entrer en contact avec vous.");
                                        showContactRequestDialog(requester);
                                    });
                                }
                            }
                        } 

                        else if (message.startsWith("SYSTEM:TYPING:")) {
                            // Affichage de "typing..." UNIQUEMENT chez le destinataire
                            String[] parts = message.split(":", 4);
                            if (parts.length == 4) {
                                String sender = parts[2];  // Celui qui tape
                                String target = parts[3];  // Celui qui reçoit la notif

                                if (!sender.equals(username) && target.equals(username)) { 
                                    // Le destinataire voit "typing..." uniquement si la conversation est ouverte
                                    if (selectedContact != null && selectedContact.equals(sender)) {
                                        SwingUtilities.invokeLater(() -> typingLabel.setText(sender + " est en train d'écrire..."));
                                    }
                                }
                            }
                        } 

                        else if (message.startsWith("SYSTEM:STOP_TYPING:")) {
                            // Stop typing doit être complètement ignoré
                            continue;
                        } 

                        else if (message.contains(": ")) {
                            // Gestion des messages normaux
                            String sender = message.substring(0, message.indexOf(":")).trim();
                            String content = message.substring(message.indexOf(":") + 2);
                            String messageKey = sender + ":" + content; // Clé unique pour éviter les doublons

                            if (displayedMessages.contains(messageKey)) {
                                continue; // Ignorer les messages déjà affichés
                            }
                            displayedMessages.add(messageKey);

                            if (!sender.equals(username)) {
                                if (userManager.estBloque(username, sender)) {
                                    System.out.println("Message de " + sender + " ignoré car l'utilisateur est bloqué.");
                                    continue;
                                }
                                if (userManager.utilisateurExiste(sender)) {
                                    SwingUtilities.invokeLater(() -> {
                                        appendMessage(content, false);
                                        chatArea.setCaretPosition(chatArea.getStyledDocument().getLength());
                                        typingLabel.setText(""); // Supprime immédiatement "typing..."
                                    });
                                }
                            } else {
                                // Affichage immédiat du message envoyé par l'utilisateur sans duplication
                                SwingUtilities.invokeLater(() -> {
                                    appendMessage(content, true);
                                    chatArea.setCaretPosition(chatArea.getStyledDocument().getLength());
                                });
                            }
                        } 

                        else {
                            // Gestion des messages système autres que TYPING et STOP_TYPING
                            if (!displayedMessages.contains(message)) {
                                displayedMessages.add(message);
                                SwingUtilities.invokeLater(() -> {
                                    appendSystemMessage(message);
                                    chatArea.setCaretPosition(chatArea.getStyledDocument().getLength());
                                });
                            }
                        }
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> appendSystemMessage("Erreur de réception des messages : " + e.getMessage()));
            }
        }).start();
    }




    
    private void appendMessage(String content, boolean isUser) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String message = time + " - " + content + "\n";
            Style style = isUser ? chatArea.getStyle("UserStyle") : chatArea.getStyle("ContactStyle");
            doc.insertString(doc.getLength(), message, style);
            doc.setParagraphAttributes(doc.getLength() - 1, 1, style, false);
        } catch (BadLocationException e) {
            System.err.println("Erreur lors de l'ajout du message : " + e.getMessage());
        }
    }

    private void appendSystemMessage(String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            String formattedMessage = message + "\n";
            Style style = chatArea.getStyle("SystemStyle");
            doc.insertString(doc.getLength(), formattedMessage, style);
            doc.setParagraphAttributes(doc.getLength() - 1, 1, style, false);
        } catch (BadLocationException e) {
            System.err.println("Erreur lors de l'ajout du message système : " + e.getMessage());
        }
    }

    private void loadConversation(String contact) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.remove(0, doc.getLength());
            Set<String> loadedMessages = new HashSet<>(); // Pour éviter les doublons dans la conversation
            List<String> messages = userManager.obtenirMessagesConversation(username, contact);
            for (String message : messages) {
                String[] parts = message.split(" ", 3);
                if (parts.length == 3) {
                    String content = parts[2];
                    String messageKey = message; // Utiliser le message complet comme clé
                    if (loadedMessages.contains(messageKey)) {
                        continue; // Ignorer les messages déjà chargés
                    }
                    loadedMessages.add(messageKey);
                    boolean isUser = parts[1].substring(0, parts[1].length() - 1).equals(username);
                    String displayMessage = parts[0] + " - " + content + "\n";
                    Style style = isUser ? chatArea.getStyle("UserStyle") : chatArea.getStyle("ContactStyle");
                    doc.insertString(doc.getLength(), displayMessage, style);
                    doc.setParagraphAttributes(doc.getLength() - 1, 1, style, false);
                }
            }
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            System.err.println("Erreur lors du chargement de la conversation : " + e.getMessage());
        }
    }

    private void updateContactListWithoutReloadingConversation() {
        SwingUtilities.invokeLater(() -> {
            String previousSelectedContact = selectedContact;  // Sauvegarde le contact sélectionné
            userListModel.clear();
            userStatuses.clear();
            List<String> contacts = showingArchivedChats ? userManager.obtenirContactsArchives(username) : userManager.obtenirContacts(username);
            for (String contact : contacts) {
                String status = userManager.obtenirStatutUtilisateur(contact);
                boolean isBlocked = userManager.estBloque(username, contact);
                String statusDisplay = isBlocked ? "🔴" : (status.equals("en ligne") ? "🟢" : "(hors ligne)");
                userListModel.addElement(contact + " " + statusDisplay);
                userStatuses.put(contact, statusDisplay);
            }
            if (previousSelectedContact != null) {
                selectedContact = previousSelectedContact; // Restaure le contact sélectionné
                loadConversation(selectedContact); // Recharge la conversation sans effacer l'écran
            }
            updateChatHeader();
        });
    }


    private void checkPendingRequests() {
        List<String> pendingRequests = userManager.obtenirDemandesEnAttente(username);
        for (String requester : pendingRequests) {
            showContactRequestDialog(requester);
        }
    }

    private void showContactRequestDialog(String requester) {
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"Accepter", "Refuser"};
            int response = JOptionPane.showOptionDialog(
                this,
                requester + " veut entrer en contact avec vous.",
                "Demande de contact",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            try {
                StyledDocument doc = chatArea.getStyledDocument();
                if (response == JOptionPane.YES_OPTION) {
                    if (userManager.accepterDemandeContact(requester, username)) {
                        appendSystemMessage("Vous avez accepté la demande de contact de " + requester);
                        updateContactListWithoutReloadingConversation();
                    } else {
                        appendSystemMessage("Erreur lors de l'acceptation de la demande de contact de " + requester);
                    }
                } else if (response == JOptionPane.NO_OPTION) {
                    if (userManager.refuserDemandeContact(requester, username)) {
                        appendSystemMessage("Vous avez refusé la demande de contact de " + requester);
                        updateContactListWithoutReloadingConversation();
                    } else {
                        appendSystemMessage("Erreur lors du refus de la demande de contact de " + requester);
                    }
                }
                chatArea.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage de la demande de contact : " + e.getMessage());
            }
        });
    }

    private void showAddContactDialog() {
        List<String> nonContacts = userManager.obtenirTousLesUtilisateursNonContacts(username);
        if (nonContacts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun nouvel utilisateur à ajouter.", "Ajouter un contact", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedUser = (String) JOptionPane.showInputDialog(
            this,
            "Sélectionnez un utilisateur à ajouter :",
            "Ajouter un contact",
            JOptionPane.PLAIN_MESSAGE,
            null,
            nonContacts.toArray(),
            nonContacts.get(0)
        );

        if (selectedUser != null && !selectedUser.isEmpty()) {
            if (userManager.ajouterDemandeContact(username, selectedUser)) {
                try {
                    chatClient.sendSystemMessage("CONTACT_REQUEST", username + ":" + selectedUser);
                    appendSystemMessage("Demande de contact envoyée à " + selectedUser);
                } catch (IOException e) {
                    appendSystemMessage("Erreur lors de l'envoi de la demande de contact : " + e.getMessage());
                }
            } else {
                appendSystemMessage("Erreur lors de l'envoi de la demande de contact à " + selectedUser);
            }
        }
    }

    private void showChatOptions(String user) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem blockItem = new JMenuItem("Bloquer");
        blockItem.addActionListener(e -> {
            if (userManager.estBloque(username, user)) {
                JOptionPane.showMessageDialog(this, user + " est déjà bloqué.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous bloquer " + user + " ?",
                "Bloquer",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (userManager.bloquerContact(username, user)) {
                    appendSystemMessage(user + " a été bloqué.");
                    updateContactListWithoutReloadingConversation();
                    if (selectedContact != null && selectedContact.equals(user)) {
                        updateChatHeader();
                    }
                } else {
                    appendSystemMessage("Erreur lors du blocage de " + user + ".");
                }
            }
        });
        JMenuItem deleteItem = new JMenuItem("Supprimer");
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous supprimer " + user + " de vos contacts ? Cette action est irréversible.",
                "Supprimer",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                if (userManager.supprimerContact(username, user)) {
                    appendSystemMessage(user + " a été supprimé de vos contacts.");
                    updateContactListWithoutReloadingConversation();
                    if (selectedContact != null && selectedContact.equals(user)) {
                        selectedContact = null;
                        chatArea.setText("");
                        updateChatHeader();
                    }
                } else {
                    appendSystemMessage("Erreur lors de la suppression de " + user + ".");
                }
            }
        });
        menu.add(blockItem);
        menu.add(deleteItem);
        menu.show(chatArea, chatArea.getMousePosition().x, chatArea.getMousePosition().y);
    }

    private void toggleArchivedChats() {
        showingArchivedChats = !showingArchivedChats;
        if (showingArchivedChats) {
            archivedChatsButton.setText("Retour aux contacts");
            contactsLabel.setText("Discussions archivées");
            addContactButton.setVisible(false);
        } else {
            archivedChatsButton.setText("Discussions archivées");
            contactsLabel.setText("Contacts");
            addContactButton.setVisible(true);
        }
        updateContactListWithoutReloadingConversation();
        headerPanel.revalidate();
        headerPanel.repaint();
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && selectedContact != null) {
            if (userManager.estBloque(username, selectedContact)) {
                appendSystemMessage("Vous ne pouvez pas envoyer de message à " + selectedContact + " car il est bloqué.");
                return;
            }

            if (userManager.estArchive(username, selectedContact)) {
                System.out.println("La discussion avec " + selectedContact + " est archivée, désarchivage en cours...");
                if (userManager.desarchiverContact(username, selectedContact)) {
                    appendSystemMessage("Discussion avec " + selectedContact + " désarchivée.");
                    updateContactListWithoutReloadingConversation();
                    if (showingArchivedChats) {
                        toggleArchivedChats();
                    }
                    updateChatHeader();
                } else {
                    appendSystemMessage("Erreur lors du désarchivage de la discussion avec " + selectedContact + ".");
                    return;
                }
            }

            try {
                chatClient.sendMessage(message); // Envoie le message au serveur

                // Désactive immédiatement l'état "isTyping" pour éviter d'envoyer "TYPING" après envoi
                isTyping = false;
                stopTyping(); // S'assure que le statut "typing" est supprimé

                // Vérifie si le message est déjà affiché avant de l'ajouter
                String messageKey = username + ":" + message;
                if (!displayedMessages.contains(messageKey)) {
                    displayedMessages.add(messageKey);
                    SwingUtilities.invokeLater(() -> {
                        appendMessage(message, true);
                        chatArea.setCaretPosition(chatArea.getStyledDocument().getLength());
                    });
                }

                // Sauvegarde le message en base de données
                LocalDateTime now = LocalDateTime.now();
                try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/messagerie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris",
                    "root", ""
                );
                     PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO Messages (expediteur, destinataire, contenu, horodatage) VALUES (?, ?, ?, ?)"
                     )) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, selectedContact);
                    pstmt.setString(3, message);
                    pstmt.setString(4, now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'enregistrement du message : " + e.getMessage());
                }

                messageField.setText(""); // Vide le champ de texte après envoi
            } catch (IOException e) {
                appendSystemMessage("Erreur lors de l'envoi du message : " + e.getMessage());
            }
        }
    }



    private void stopTyping() {
        if (isTyping && selectedContact != null) {
            try {
                chatClient.sendSystemMessage("STOP_TYPING", username + ":" + selectedContact);
                isTyping = false;
            } catch (IOException e) {
                appendSystemMessage("Erreur lors de l'envoi de la notification STOP_TYPING : " + e.getMessage());
            }
        }
    }

    private void showProfile() {
        ProfileGUI profileGUI = new ProfileGUI(this, username, userManager, themeManager);
        profileGUI.setVisible(true);
    }

    private void logout() {
        try {
            chatClient.sendSystemMessage("DISCONNECT", username);
            userManager.mettreAJourStatutUtilisateur(username, "hors ligne");
            chatClient.close();
            statusUpdateTimer.stop();
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
        dispose();
        new LoginGUI().setVisible(true);
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
        setTitle("Chat - " + username);
        connectedLabel.setText("Connecté en tant que : " + username);
    }

    private class ContactCellRenderer extends JPanel implements ListCellRenderer<String> {
        private JLabel label;

        public ContactCellRenderer() {
            setLayout(new BorderLayout());
            label = new JLabel();
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            label.setText(value);
            label.setForeground(themeManager.getForegroundColor());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            return this;
        }
    }
}