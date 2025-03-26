package Client;

import javax.swing.*;

import Modeles.Utilisateur;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileGUI extends JDialog {
    private String username;
    private UserManager userManager;
    private ThemeManager themeManager;
    private ChatClientGUI parent;
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JButton updateProfileButton;
    private JButton changePasswordButton;
    private JButton deleteAccountButton;
    private JButton backButton;
    private JLabel themeIconLabel; // JLabel pour l'icône de thème
    private JLabel connectedLabel; // JLabel pour "Profil"
    private ImageIcon sunIcon; // Icône pour le mode sombre (soleil)
    private ImageIcon moonIcon; // Icône pour le mode clair (lune)
    private JPanel mainPanel;
    private JLayeredPane topPanel; // Utiliser JLayeredPane pour superposer l'icône
    private JPanel titlePanel; // Panneau pour centrer le titre

    // Constantes pour les messages
    private static final String ERROR_FIELDS_EMPTY = "Veuillez remplir tous les champs.";
    private static final String ERROR_PASSWORD_MISMATCH = "Les nouveaux mots de passe ne correspondent pas.";
    private static final String ERROR_CURRENT_PASSWORD_INCORRECT = "Mot de passe actuel incorrect.";
    private static final String ERROR_PASSWORD_WEAK = "Le nouveau mot de passe doit contenir au moins 8 caractères, un chiffre, une lettre majuscule, une lettre minuscule et un caractère spécial.";
    private static final String ERROR_PASSWORD_SAME_AS_CURRENT = "Le nouveau mot de passe doit être différent de l'ancien.";
    private static final String ERROR_CHANGE_FAILED = "Erreur lors du changement de mot de passe : ";
    private static final String SUCCESS_PASSWORD_CHANGED = "Mot de passe changé avec succès.";

    public ProfileGUI(ChatClientGUI parent, String username, UserManager userManager, ThemeManager themeManager) {
        super(parent, true); // Rendre le JDialog modal (true)
        this.parent = parent;
        this.username = username;
        this.userManager = userManager;
        this.themeManager = themeManager;
        setTitle("Profil - " + username);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(510, 500); // Taille par défaut modifiée à 510x500
        setLocationRelativeTo(parent);

        // Forcer l'utilisation des emojis (pas d'icônes pour éviter les problèmes de chemin)
        sunIcon = null;
        moonIcon = null;
        System.out.println("Icônes désactivées, utilisation des emojis comme fallback.");

        initializeUI();
        resizeTopPanel(); // Appeler resizeTopPanel() pour s'assurer que le topPanel est correctement dimensionné au démarrage
        applyTheme();

        // Ajouter un listener pour redimensionner le topPanel lorsque la fenêtre est redimensionnée
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTopPanel();
            }
        });
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Panneau pour le label "Profil" et l'icône de thème (utilisation de JLayeredPane)
        topPanel = new JLayeredPane();
        topPanel.setBackground(new Color(30, 30, 30)); // Gris foncé comme dans l'image

        // Panneau pour centrer le titre (ignorant l'icône)
        titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(30, 30, 30));

        // Label "Profil" (centré)
        connectedLabel = new JLabel("Profil", SwingConstants.CENTER);
        connectedLabel.setFont(new Font("Arial", Font.BOLD, 16));
        connectedLabel.setForeground(Color.WHITE); // Forcer la couleur à blanc
        connectedLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Ajouter des marges pour un meilleur centrage
        titlePanel.add(connectedLabel, BorderLayout.CENTER);
        System.out.println("connectedLabel ajouté avec texte : Profil");

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

        // Définir la taille initiale du topPanel
        topPanel.setPreferredSize(new Dimension(510, 50)); // Ajuster à la nouvelle largeur de la fenêtre
        add(topPanel, BorderLayout.NORTH);

        // Panneau principal avec GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Utilisateur utilisateur = userManager.obtenirUtilisateur(username);
        if (utilisateur == null) {
            JOptionPane.showMessageDialog(this, "Erreur : Utilisateur non trouvé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        // Champ pour le nom d'utilisateur (non modifiable)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Nom d'utilisateur :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(username);
        usernameField.setEditable(false);
        mainPanel.add(usernameField, gbc);

        // Champ pour le prénom
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Prénom :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        firstNameField = new JTextField(utilisateur.obtenirFirstName());
        mainPanel.add(firstNameField, gbc);

        // Champ pour le nom
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Nom :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        lastNameField = new JTextField(utilisateur.obtenirLastName());
        mainPanel.add(lastNameField, gbc);

        // Bouton pour mettre à jour le profil
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        updateProfileButton = new JButton("Mettre à jour le profil");
        updateProfileButton.addActionListener(e -> updateProfile());
        mainPanel.add(updateProfileButton, gbc);

        // Champ pour le mot de passe actuel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Mot de passe actuel :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        currentPasswordField = new JPasswordField(15);
        mainPanel.add(currentPasswordField, gbc);

        // Champ pour le nouveau mot de passe
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Nouveau mot de passe :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(15);
        mainPanel.add(newPasswordField, gbc);

        // Champ pour confirmer le nouveau mot de passe
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Confirmer nouveau mot de passe :"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        confirmNewPasswordField = new JPasswordField(15);
        mainPanel.add(confirmNewPasswordField, gbc);

        // Case à cocher pour afficher/masquer les mots de passe
        showPasswordCheckBox = new JCheckBox("Afficher les mots de passe");
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                currentPasswordField.setEchoChar((char) 0);
                newPasswordField.setEchoChar((char) 0);
                confirmNewPasswordField.setEchoChar((char) 0);
            } else {
                currentPasswordField.setEchoChar('•');
                newPasswordField.setEchoChar('•');
                confirmNewPasswordField.setEchoChar('•');
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        mainPanel.add(showPasswordCheckBox, gbc);

        // Bouton pour changer le mot de passe
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        changePasswordButton = new JButton("Changer le mot de passe");
        changePasswordButton.addActionListener(e -> changePassword());
        mainPanel.add(changePasswordButton, gbc);

        // Panneau pour les boutons "Supprimer le compte" et "Retour"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(themeManager.getBackgroundColor());
        deleteAccountButton = new JButton("Supprimer le compte");
        deleteAccountButton.setPreferredSize(new Dimension(150, 30));
        deleteAccountButton.addActionListener(e -> deleteAccount());
        buttonPanel.add(deleteAccountButton);
        System.out.println("deleteAccountButton ajouté à buttonPanel.");

        backButton = new JButton("Retour");
        backButton.setPreferredSize(new Dimension(150, 30));
        backButton.addActionListener(e -> {
            System.out.println("Bouton Retour cliqué.");
            dispose();
        });
        buttonPanel.add(backButton);
        System.out.println("backButton ajouté à buttonPanel.");

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        mainPanel.add(buttonPanel, gbc);
        System.out.println("buttonPanel ajouté à mainPanel à la position y=9.");

        // Ajouter un JScrollPane autour du mainPanel pour permettre le défilement
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        System.out.println("mainPanel ajouté à un JScrollPane et ajouté à la fenêtre.");
    }

    private void resizeTopPanel() {
        int width = getWidth();
        topPanel.setPreferredSize(new Dimension(width, 50));
        titlePanel.setBounds(0, 0, width, 50);
        topPanel.revalidate();
        topPanel.repaint();
        System.out.println("topPanel redimensionné à la largeur : " + width);
    }

    private void updateProfile() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE Utilisateurs SET firstName = ?, lastName = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/messagerie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris",
                "root", "");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, username);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Profil mis à jour avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du profil.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour du profil : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmNewPassword = confirmNewPasswordField.getPassword();

        if (isEmpty(currentPassword) || isEmpty(newPassword) || isEmpty(confirmNewPassword)) {
            showErrorMessage(ERROR_FIELDS_EMPTY);
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        String newPasswordStr = new String(newPassword);
        if (!isPasswordStrong(newPasswordStr)) {
            showErrorMessage(ERROR_PASSWORD_WEAK);
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        Utilisateur utilisateur = userManager.obtenirUtilisateur(username);
        if (utilisateur == null) {
            showErrorMessage("Utilisateur non trouvé.");
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        String hashedCurrentPassword = hashPassword(new String(currentPassword));
        if (hashedCurrentPassword == null || !utilisateur.obtenirMotDePasseHache().equals(hashedCurrentPassword)) {
            showErrorMessage(ERROR_CURRENT_PASSWORD_INCORRECT);
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        if (newPasswordStr.equals(new String(currentPassword))) {
            showErrorMessage(ERROR_PASSWORD_SAME_AS_CURRENT);
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        if (!newPasswordStr.equals(new String(confirmNewPassword))) {
            showErrorMessage(ERROR_PASSWORD_MISMATCH);
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        String hashedNewPassword = hashPassword(newPasswordStr);
        if (hashedNewPassword == null) {
            showErrorMessage("Erreur lors du hachage du nouveau mot de passe.");
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            return;
        }

        String sql = "UPDATE Utilisateurs SET motDePasseHache = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/messagerie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris",
                "root", "");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedNewPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, SUCCESS_PASSWORD_CHANGED, "Succès", JOptionPane.INFORMATION_MESSAGE);
                clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
            } else {
                showErrorMessage("Erreur lors de la mise à jour du mot de passe dans la base de données.");
            }
        } catch (SQLException e) {
            showErrorMessage(ERROR_CHANGE_FAILED + e.getMessage());
        } finally {
            clearPasswordFields(currentPassword, newPassword, confirmNewPassword);
        }
    }

    private void deleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.",
            "Supprimer le compte",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            if (userManager.supprimerUtilisateur(username)) {
                JOptionPane.showMessageDialog(this, "Compte supprimé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                LoginGUI loginGUI = new LoginGUI();
                loginGUI.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression du compte.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeTheme() {
        System.out.println("Changement de thème déclenché.");
        themeManager.toggleTheme();
        applyTheme();
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

    private void applyTheme() {
        System.out.println("Application du thème. Mode sombre : " + themeManager.isDarkMode());
        System.out.println("Couleur de fond : " + themeManager.getBackgroundColor());
        System.out.println("Couleur de texte : " + themeManager.getForegroundColor());
        System.out.println("Couleur de fond des boutons : " + themeManager.getButtonBackgroundColor());

        // Appliquer une couleur de fond grisâtre au topPanel
        topPanel.setBackground(new Color(30, 30, 30));
        titlePanel.setBackground(new Color(30, 30, 30));

        mainPanel.setBackground(themeManager.getBackgroundColor());
        showPasswordCheckBox.setForeground(themeManager.getForegroundColor());
        showPasswordCheckBox.setOpaque(false);

        // Appliquer le thème au topPanel et à ses composants
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JLayeredPane) {
                if (comp == topPanel) {
                    for (Component subComp : ((JLayeredPane) comp).getComponents()) {
                        if (subComp instanceof JLabel) {
                            if (subComp == connectedLabel || subComp == themeIconLabel) {
                                ((JLabel) subComp).setForeground(Color.WHITE); // Forcer la couleur à blanc
                                System.out.println("Couleur appliquée à " + ((JLabel) subComp).getText() + " : " + Color.WHITE);
                            }
                        } else if (subComp instanceof JPanel) {
                            ((JPanel) subComp).setBackground(new Color(30, 30, 30));
                        }
                    }
                }
            } else if (comp instanceof JScrollPane) {
                ((JScrollPane) comp).setBackground(themeManager.getBackgroundColor());
                for (Component subComp : ((JScrollPane) comp).getViewport().getComponents()) {
                    if (subComp instanceof JPanel) {
                        ((JPanel) subComp).setBackground(themeManager.getBackgroundColor());
                    }
                }
            }
        }

        // Appliquer le thème au mainPanel et à ses composants
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JLabel && comp != themeIconLabel) {
                comp.setForeground(themeManager.getForegroundColor());
            } else if (comp instanceof JTextField || comp instanceof JPasswordField) {
                comp.setForeground(themeManager.getForegroundColor());
                comp.setBackground(themeManager.isDarkMode() ? Color.DARK_GRAY : Color.WHITE);
            } else if (comp instanceof JButton) {
                comp.setBackground(themeManager.getButtonBackgroundColor());
                comp.setForeground(themeManager.getForegroundColor());
            } else if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(themeManager.getBackgroundColor());
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof JButton) {
                        subComp.setBackground(themeManager.getButtonBackgroundColor());
                        subComp.setForeground(themeManager.getForegroundColor());
                        System.out.println("Couleur appliquée à bouton : " + ((JButton) subComp).getText());
                    }
                }
            }
        }
        mainPanel.revalidate();
        mainPanel.repaint();
        System.out.println("Thème appliqué.");
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

    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        return pattern.matcher(password).matches();
    }

    private boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    private void clearPasswordFields(char[]... passwordArrays) {
        for (char[] array : passwordArrays) {
            if (array != null) {
                java.util.Arrays.fill(array, '0');
            }
        }
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmNewPasswordField.setText("");
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}