-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : mer. 26 mars 2025 à 16:43
-- Version du serveur : 9.1.0
-- Version de PHP : 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `messagerie`
--

-- --------------------------------------------------------

--
-- Structure de la table `archived`
--

DROP TABLE IF EXISTS `archived`;
CREATE TABLE IF NOT EXISTS `archived` (
  `user` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `archived_contact` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`user`,`archived_contact`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `archived`
--

INSERT INTO `archived` (`user`, `archived_contact`) VALUES
('sid', 'Luff');

-- --------------------------------------------------------

--
-- Structure de la table `blockedusers`
--

DROP TABLE IF EXISTS `blockedusers`;
CREATE TABLE IF NOT EXISTS `blockedusers` (
  `blocker` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `blocked` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`blocker`,`blocked`),
  KEY `blocked` (`blocked`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `blockedusers`
--

INSERT INTO `blockedusers` (`blocker`, `blocked`) VALUES
('alice', 'bob (en ligne il y a 0 min)');

-- --------------------------------------------------------

--
-- Structure de la table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
CREATE TABLE IF NOT EXISTS `contacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `utilisateur1` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `utilisateur2` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `statut` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `date_demande` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `archive` tinyint(1) DEFAULT '0',
  `bloque` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `utilisateur1` (`utilisateur1`,`utilisateur2`),
  KEY `utilisateur2` (`utilisateur2`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `contacts`
--

INSERT INTO `contacts` (`id`, `utilisateur1`, `utilisateur2`, `statut`, `date_demande`, `archive`, `bloque`) VALUES
(1, 'sid', 'bob', 'en_attente', '2025-03-25 16:41:13', 0, 0),
(3, 'sid', 'lazyx', 'en_attente', '2025-03-25 17:57:38', 0, 0),
(6, 'Luffy', 'sid', 'accepte', '2025-03-26 09:47:35', 0, 0);

-- --------------------------------------------------------

--
-- Structure de la table `messages`
--

DROP TABLE IF EXISTS `messages`;
CREATE TABLE IF NOT EXISTS `messages` (
  `id` int NOT NULL AUTO_INCREMENT,
  `expediteur` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `destinataire` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `contenu` text COLLATE utf8mb4_general_ci NOT NULL,
  `horodatage` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `expediteur` (`expediteur`),
  KEY `destinataire` (`destinataire`)
) ENGINE=MyISAM AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `messages`
--

INSERT INTO `messages` (`id`, `expediteur`, `destinataire`, `contenu`, `horodatage`) VALUES
(1, 'alice', 'bob', 'Salut Bob, comment vas-tu ?', '2025-03-25 09:00:00'),
(2, 'bob', 'alice', 'Salut Alice, je vais bien, merci !', '2025-03-25 09:01:00'),
(3, 'alice', 'charlie', 'Hey Charlie, on se parle ?', '2025-03-25 09:02:00'),
(4, 'charlie', 'alice', 'Oui, bien sûr !', '2025-03-25 09:03:00'),
(5, 'alice', 'bob', 'et les études', '2025-03-25 11:36:46'),
(6, 'charlie', 'bob', 'kdbjkb f', '2025-03-25 11:39:08'),
(7, 'charlie', 'bob', 'comment vas tu??', '2025-03-25 11:39:51'),
(8, 'alice', 'bob', '[File: TD n°2 Hachage_Diallo_Sidney.docx]', '2025-03-25 11:40:56'),
(9, 'alice', 'bob (en ligne il y a 0 min)', 'bntntnt', '2025-03-25 11:52:55'),
(10, 'lazyx', 'alice', 'Bonjour', '2025-03-25 12:58:52'),
(11, 'sid', 'Luff', 'kuukyk;iuk', '2025-03-25 18:12:56'),
(12, 'sid', 'Luff', 'jtukukkgcuykygkiykiyyiisvfv', '2025-03-25 18:14:50'),
(13, 'sid', 'Luff', 'bfgbfng', '2025-03-25 18:34:09'),
(14, 'luff', 'sid', 'yo mec', '2025-03-25 18:53:15'),
(15, 'sid', 'Luff', 'vjkvfjvnf', '2025-03-26 04:29:15'),
(16, 'sid', 'Luff', 'kbcdhbc', '2025-03-26 04:31:44'),
(17, 'sid', 'Luff', 'yjtjt', '2025-03-26 04:37:07'),
(18, 'sid', 'Luff', 'vghn', '2025-03-26 04:47:02'),
(20, 'sid', 'Luffy', 'Salut mec', '2025-03-26 13:52:47'),
(21, 'sid', 'Luffy', 'Salut mec', '2025-03-26 13:52:47'),
(22, 'Luffy', 'sid', 'yo mec', '2025-03-26 14:06:02'),
(23, 'Luffy', 'sid', 'yo mec', '2025-03-26 14:06:02'),
(24, 'sid', 'Luffy', 'je suis là et toi', '2025-03-26 14:07:06'),
(25, 'sid', 'Luffy', 'je suis là et toi', '2025-03-26 14:07:06'),
(26, 'sid', 'Luffy', 'g fvfbnnggngg', '2025-03-26 14:15:42'),
(27, 'sid', 'Luffy', 'g fvfbnnggngg', '2025-03-26 14:15:42'),
(28, 'sid', 'Luffy', 'vfbbnbhn', '2025-03-26 14:26:53'),
(29, 'sid', 'Luffy', 'vfbbnbhn', '2025-03-26 14:26:53'),
(30, 'Luffy', 'sid', 'salut', '2025-03-26 14:43:35'),
(31, 'Luffy', 'sid', 'Luffy: salut', '2025-03-26 14:43:35'),
(32, 'sid', 'Luffy', 'viens à 16h', '2025-03-26 15:47:22'),
(33, 'Luffy', 'sid', 'ok j\'ai reçu ton message', '2025-03-26 15:48:33'),
(34, 'sid', 'Luffy', 'ok carré', '2025-03-26 15:57:55'),
(35, 'Luffy', 'sid', 'on verra', '2025-03-26 15:58:42'),
(36, 'sid', 'Luffy', 'sewi', '2025-03-26 16:29:41');

-- --------------------------------------------------------

--
-- Structure de la table `utilisateurs`
--

DROP TABLE IF EXISTS `utilisateurs`;
CREATE TABLE IF NOT EXISTS `utilisateurs` (
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `firstName` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `lastName` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `motDePasseHache` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `statut` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'hors ligne',
  `derniereConnexion` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `utilisateurs`
--

INSERT INTO `utilisateurs` (`username`, `firstName`, `lastName`, `motDePasseHache`, `statut`, `derniereConnexion`) VALUES
('alice', NULL, NULL, 'hashed_password1', 'en ligne', '2025-03-25 12:21:34'),
('bob', NULL, NULL, 'hashed_password2', 'hors ligne', '2025-03-25 11:50:43'),
('lazyx', 'JR', 'Sidney', 'sid', 'hors ligne', '2025-03-25 12:59:36'),
('sid', 'Zoro', 'Roronoa', '5ad4d6763b7a8911fd1ed1cac32b7d1c11fd418974ad082f75d8301040640cb2', 'hors ligne', '2025-03-26 16:29:48'),
('Luff', 'Luffy', 'Monkey', 'fef0f755914e06dd7ec89633ffbb7d201ec4d928d3e9f61b6f41ab2016331542', 'hors ligne', '2025-03-25 18:53:22'),
('Luffy', 'd', 'Monkey', '4c981db5c4ac03f8929e36e9d5179e4e434c70bb618b3f6b0e17ac224ddde139', 'hors ligne', '2025-03-26 16:06:08');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
