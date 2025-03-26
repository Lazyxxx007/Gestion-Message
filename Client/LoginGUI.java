package Client;

import ClientUtils.ChatClient;
import Modeles.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

public class LoginGUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheckBox;
    private JButton loginButton;
    private JLabel createAccountLabel;
    private ThemeManager themeManager;
    private UserManager userManager;
    private JPanel mainPanel;
    private JLayeredPane topPanel; // Utiliser JLayeredPane pour superposer l'icône
    private JPanel titlePanel; // Panneau pour centrer le titre
    private JLabel titleLabel; // JLabel pour le titre "Connexion"
    private JLabel themeIconLabel; // JLabel pour l'icône de thème
    private ImageIcon sunIcon; // Icône pour le mode sombre (soleil)
    private ImageIcon moonIcon; // Icône pour le mode clair (lune)

    public LoginGUI() {
        themeManager = new ThemeManager();
        userManager = new UserManager();
        setTitle("Connexion - Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        // Forcer l'utilisation des emojis (pas d'icônes pour éviter les problèmes de chemin)
        sunIcon = null;
        moonIcon = null;
        System.out.println("Icônes désactivées, utilisation des emojis comme fallback.");

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Panneau pour le titre et l'icône de thème (utilisation de JLayeredPane)
        topPanel = new JLayeredPane();
        topPanel.setBackground(new Color(30, 30, 30)); // Gris foncé comme dans ProfileGUI

        // Panneau pour centrer le titre (ignorant l'icône)
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(30, 30, 30));
        titlePanel.setBounds(0, 0, 400, 50); // Ajuster la taille pour correspondre à la fenêtre

        // Titre "Connexion" (centré)
        titleLabel = new JLabel("Connexion", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE); // Forcer la couleur à blanc
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Ajouter des marges pour un meilleur centrage
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        System.out.println("titleLabel ajouté avec texte : Connexion");

        // Ajouter le titlePanel à la couche de base
        topPanel.add(titlePanel, JLayeredPane.DEFAULT_LAYER);

        // Icône de thème (superposée)
        themeIconLabel = new JLabel();
        themeIconLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Ajouter de l'espace autour de l'icône
        themeIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeIconLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                changeTheme();
            }
        });
        updateThemeIcon();
        // Positionner l'icône à gauche
        themeIconLabel.setBounds(10, 5, 50, 40); // Ajuster la position et la taille
        topPanel.add(themeIconLabel, JLayeredPane.PALETTE_LAYER); // Couche supérieure
        System.out.println("themeIconLabel ajouté à topPanel avec bordure.");

        // Définir la taille préférée du topPanel
        topPanel.setPreferredSize(new Dimension(400, 50));
        add(topPanel, BorderLayout.NORTH);

        // Panneau principal avec GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(themeManager.getBackgroundColor());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Champ Pseudonyme
        JLabel usernameLabel = new JLabel("Pseudonyme :");
        usernameLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(usernameField, gbc);

        // Champ Mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(passwordField, gbc);

        // Case à cocher pour afficher/masquer le mot de passe
        showPasswordCheckBox = new JCheckBox("Afficher le mot de passe");
        showPasswordCheckBox.setForeground(themeManager.getForegroundColor());
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(showPasswordCheckBox, gbc);

        // Bouton Se connecter
        loginButton = new JButton("Se connecter");
        loginButton.setBackground(themeManager.getButtonBackgroundColor());
        loginButton.setForeground(themeManager.getForegroundColor());
        loginButton.addActionListener(e -> login());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(loginButton, gbc);

        // Lien Créer un compte
        createAccountLabel = new JLabel("<html><u>Toujours pas de compte ? Créer</u></html>", SwingConstants.CENTER);
        createAccountLabel.setForeground(themeManager.getForegroundColor());
        createAccountLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createAccountLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CreateAccountGUI createAccountGUI = new CreateAccountGUI(themeManager);
                createAccountGUI.setVisible(true);
                dispose();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(createAccountLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void changeTheme() {
        System.out.println("Changement de thème déclenché.");
        themeManager.toggleTheme();
        updateTheme();
        updateThemeIcon();
    }

    private void updateThemeIcon() {
        System.out.println("Mise à jour de l'icône de thème. Mode sombre : " + themeManager.isDarkMode());
        // Inverser la logique : soleil en mode sombre, lune en mode clair
        if (themeManager.isDarkMode()) {
            if (sunIcon != null) {
                themeIconLabel.setIcon(sunIcon);
                themeIconLabel.setText("");
                System.out.println("Icône soleil définie (mode sombre).");
            } else {
                themeIconLabel.setText("☀️");
                themeIconLabel.setIcon(null);
                System.out.println("Emoji soleil défini (mode sombre, fallback).");
            }
        } else {
            if (moonIcon != null) {
                themeIconLabel.setIcon(moonIcon);
                themeIconLabel.setText("");
                System.out.println("Icône lune définie (mode clair).");
            } else {
                themeIconLabel.setText("🌙");
                themeIconLabel.setIcon(null);
                System.out.println("Emoji lune défini (mode clair, fallback).");
            }
        }
        themeIconLabel.setForeground(Color.WHITE); // Forcer la couleur à blanc
        System.out.println("Couleur de l'icône définie : " + Color.WHITE);
    }

    private void updateTheme() {
        topPanel.setBackground(new Color(30, 30, 30)); // Gris foncé comme dans ProfileGUI
        titlePanel.setBackground(new Color(30, 30, 30));

        mainPanel.setBackground(themeManager.getBackgroundColor());
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JLabel) {
                if (comp == titleLabel || comp == themeIconLabel) {
                    ((JLabel) comp).setForeground(Color.WHITE); // Forcer la couleur à blanc
                    System.out.println("Couleur appliquée à " + ((JLabel) comp).getText() + " : " + Color.WHITE);
                } else {
                    ((JLabel) comp).setForeground(themeManager.getForegroundColor());
                }
            } else if (comp instanceof JButton) {
                ((JButton) comp).setBackground(themeManager.getButtonBackgroundColor());
                ((JButton) comp).setForeground(themeManager.getForegroundColor());
            } else if (comp instanceof JCheckBox) {
                ((JCheckBox) comp).setForeground(themeManager.getForegroundColor());
                ((JCheckBox) comp).setOpaque(false);
            } else if (comp instanceof JTextField || comp instanceof JPasswordField) {
                ((JTextField) comp).setForeground(themeManager.getForegroundColor());
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
        System.out.println("Thème appliqué.");
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            JOptionPane.showMessageDialog(this, "Erreur lors du hachage du mot de passe", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Utilisateur utilisateur = userManager.obtenirUtilisateur(username);
        if (utilisateur == null || !utilisateur.obtenirMotDePasseHache().equals(hashedPassword)) {
            JOptionPane.showMessageDialog(this, "Pseudonyme ou mot de passe incorrect", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Socket socket = new Socket("localhost", 12345);
            ChatClient chatClient = new ChatClient(socket, username);
            chatClient.sendSystemMessage("USERLIST", username);
            userManager.mettreAJourStatutUtilisateur(username, "en ligne");
            ChatClientGUI chatGUI = new ChatClientGUI(username, chatClient, themeManager);
            chatGUI.setVisible(true);
            dispose();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            System.err.println("Erreur lors du hachage du mot de passe : " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });
    }
}