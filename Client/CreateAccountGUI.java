package Client;

import Modeles.Utilisateur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateAccountGUI extends JFrame {
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JButton createButton;
    private JLabel backToLoginLabel;
    private ThemeManager themeManager;
    private UserManager userManager;
    private JPanel mainPanel;
    private JLayeredPane topPanel; // Utiliser JLayeredPane pour superposer l'icône
    private JPanel titlePanel; // Panneau pour centrer le titre
    private JLabel titleLabel; // JLabel pour le titre "Créer un compte"
    private JLabel themeIconLabel; // JLabel pour l'icône de thème
    private ImageIcon sunIcon; // Icône pour le mode sombre (soleil)
    private ImageIcon moonIcon; // Icône pour le mode clair (lune)

    public CreateAccountGUI(ThemeManager themeManager) {
        this.themeManager = themeManager;
        this.userManager = new UserManager();
        setTitle("Créer un compte - Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
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

        // Titre "Créer un compte" (centré)
        titleLabel = new JLabel("Créer un compte", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE); // Forcer la couleur à blanc
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Ajouter des marges pour un meilleur centrage
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        System.out.println("titleLabel ajouté avec texte : Créer un compte");

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

        // Champ Prénom
        JLabel firstNameLabel = new JLabel("Prénom :");
        firstNameLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(firstNameLabel, gbc);

        firstNameField = new JTextField(15);
        firstNameField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 1;
        mainPanel.add(firstNameField, gbc);

        // Champ Nom
        JLabel lastNameLabel = new JLabel("Nom :");
        lastNameLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(lastNameLabel, gbc);

        lastNameField = new JTextField(15);
        lastNameField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 2;
        mainPanel.add(lastNameField, gbc);

        // Champ Mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        passwordLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 3;
        mainPanel.add(passwordField, gbc);

        // Champ Confirmer le mot de passe
        JLabel confirmPasswordLabel = new JLabel("Confirmer le mot de passe :");
        confirmPasswordLabel.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setForeground(themeManager.getForegroundColor());
        gbc.gridx = 1;
        gbc.gridy = 4;
        mainPanel.add(confirmPasswordField, gbc);

        // Case à cocher pour afficher/masquer les mots de passe
        showPasswordCheckBox = new JCheckBox("Afficher les mots de passe");
        showPasswordCheckBox.setForeground(themeManager.getForegroundColor());
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
                confirmPasswordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
                confirmPasswordField.setEchoChar('•');
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(showPasswordCheckBox, gbc);

        // Bouton Créer un compte
        createButton = new JButton("Créer un compte");
        createButton.setBackground(themeManager.getButtonBackgroundColor());
        createButton.setForeground(themeManager.getForegroundColor());
        createButton.addActionListener(e -> createAccount());
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(createButton, gbc);

        // Lien Retour à la connexion
        backToLoginLabel = new JLabel("<html><u>Déjà un compte ? Se connecter</u></html>", SwingConstants.CENTER);
        backToLoginLabel.setForeground(themeManager.getForegroundColor());
        backToLoginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
                dispose();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(backToLoginLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createAccount() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        // Vérifier si tous les champs sont remplis
        if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier si le pseudonyme existe déjà
        if (userManager.utilisateurExiste(username)) {
            JOptionPane.showMessageDialog(this, "Ce pseudonyme est déjà pris", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Hacher le mot de passe
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            JOptionPane.showMessageDialog(this, "Erreur lors du hachage du mot de passe", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insérer l'utilisateur dans la base de données
        String sql = "INSERT INTO Utilisateurs (username, firstName, lastName, motDePasseHache, statut, derniereConnexion) VALUES (?, ?, ?, ?, 'hors ligne', NOW())";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/messagerie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris",
                "root", "");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, hashedPassword);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Compte créé avec succès ! Veuillez vous connecter.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la création du compte", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la création du compte : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            System.err.println("Erreur lors du hachage du mot de passe : " + e.getMessage());
            return null;
        }
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
}