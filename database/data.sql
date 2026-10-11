USE chilla;

-- Données de test
INSERT INTO Membre (nommem, prenommem, emailmem, motdepassemem) VALUES
('Trabelsi', 'Sara',    'sara@test.tn',    'hash_test_1'),
('Gharbi',   'Youssef', 'youssef@test.tn', 'hash_test_2'),
('Jlassi',   'Mariem',  'mariem@test.tn',  'hash_test_3'),
('Haddad',   'Karim',   'karim@test.tn',   'hash_test_4');

INSERT INTO Projet (labpro, descrpro, datedpro, datefpro, estarchive) VALUES
('Chilla',  'Système de gestion de projets', '2026-10-01', '2027-01-31', FALSE),
('Bankify', 'Plateforme e-banking',          '2026-09-15', '2026-12-20', TRUE);

INSERT INTO Sprint (nomsp, objectifsp, datedsp, datefsp, etat, IDpro) VALUES
('Sprint 1', 'Conception et maquettes',  '2026-10-01', '2026-10-14', 'terminé',      1),
('Sprint 2', 'Authentification et CRUD', '2026-10-15', '2026-10-28', 'en cours',     1),
('Sprint 3', 'Notifications',            '2026-10-29', '2026-11-11', 'non commencé', 1),
('Sprint 0', 'Mise en place',            '2026-09-15', '2026-09-28', 'terminé',      2);

INSERT INTO Membre_Projet (IDmem, IDpro, rolemem) VALUES
(1, 1, 'chef de projet'),
(2, 1, 'membre'),
(3, 1, 'membre'),
(2, 2, 'chef de projet'),
(1, 2, 'membre'),
(4, 2, 'membre');

INSERT INTO Tache (labtach, descrtach, dateeche, estim, pos, IDpro, IDmem, IDsp) VALUES
('Conception base de données', 'MCD et MLD',             '2026-10-08', 5, 1, 1, 1, 1),
('Maquettes UI',               'Figma',                  '2026-10-10', 3, 2, 1, 3, 1),
('Authentification',           'Inscription et login',   '2026-10-22', 8, 1, 1, 2, 2),
('CRUD projets',               'Création, modification', '2026-10-09', 5, 2, 1, 1, 2),
('Tableau des tâches',         'Vue Kanban',             '2026-10-27', 8, 3, 1, 3, 2),
('Notifications',              'Envoi et lecture',       '2026-11-05', 5, 1, 1, 2, 3),
('Virement interne',           'Module virement',        '2026-09-25', 5, 1, 2, 2, 4),
('Historique transactions',    NULL,                     '2026-09-27', 3, 2, 2, 1, 4);

INSERT INTO Invitation (statut, IDpro, IDemetteur, IDrecepteur) VALUES
('acceptée',   1, 1, 2),
('acceptée',   1, 1, 3),
('en attente', 1, 1, 4),
('refusée',    2, 2, 3);

INSERT INTO Notification (message, estlue, IDmem) VALUES
('Vous avez été invité au projet Chilla',       FALSE, 4),
('Tâche attribuée : Authentification',          FALSE, 2),
('Tâche attribuée : Tableau des tâches',        FALSE, 3),
('Le Sprint 2 a démarré',                       TRUE,  1),
('Votre invitation a été acceptée par Youssef', TRUE,  1);

--Requêtes de vérification

-- SELECT + WHERE : projets non archivés
SELECT IDpro, labpro, datedpro, datefpro
FROM Projet
WHERE estarchive = FALSE;

-- JOIN : membres du projet 1 avec leur rôle
SELECT m.prenommem, m.nommem, mp.rolemem
FROM Membre_Projet mp
JOIN Membre m ON mp.IDmem = m.IDmem
WHERE mp.IDpro = 1;

-- JOIN : tâches de Sara, avec projet et sprint
SELECT t.labtach, p.labpro, s.nomsp, t.dateeche
FROM Tache t
JOIN Membre m ON t.IDmem = m.IDmem
JOIN Projet p ON t.IDpro = p.IDpro
JOIN Sprint s ON t.IDsp = s.IDsp
WHERE m.emailmem = 'sara@test.tn';

-- WHERE : tâches du sprint en cours
SELECT t.labtach, m.prenommem AS responsable, t.estim
FROM Tache t
JOIN Sprint s ON t.IDsp = s.IDsp
JOIN Membre m ON t.IDmem = m.IDmem
WHERE s.etat = 'en cours'
ORDER BY t.pos;

-- WHERE : tâches en retard (échéance passée, sprint non terminé)
-- Le résultat dépend de la date du jour (CURDATE)
SELECT t.labtach, t.dateeche, s.nomsp
FROM Tache t
JOIN Sprint s ON t.IDsp = s.IDsp
WHERE t.dateeche < CURDATE() AND s.etat <> 'terminé';

-- LEFT JOIN + GROUP BY : nombre de tâches par membre (Karim doit apparaître avec 0)
SELECT m.prenommem, COUNT(t.IDtach) AS nb_taches
FROM Membre m
LEFT JOIN Tache t ON t.IDmem = m.IDmem
GROUP BY m.IDmem, m.prenommem;

-- GROUP BY : charge estimée par sprint du projet 1
SELECT s.nomsp, s.etat, COUNT(t.IDtach) AS nb_taches, SUM(t.estim) AS total_estim
FROM Sprint s
LEFT JOIN Tache t ON t.IDsp = s.IDsp
WHERE s.IDpro = 1
GROUP BY s.IDsp, s.nomsp, s.etat;

-- GROUP BY : notifications non lues par membre
SELECT m.prenommem, COUNT(*) AS non_lues
FROM Notification n
JOIN Membre m ON n.IDmem = m.IDmem
WHERE n.estlue = FALSE
GROUP BY m.IDmem, m.prenommem;

-- Double JOIN sur Membre (alias) : invitations en attente
SELECT p.labpro, em.prenommem AS emetteur, re.prenommem AS recepteur
FROM Invitation i
JOIN Projet p  ON i.IDpro = p.IDpro
JOIN Membre em ON i.IDemetteur = em.IDmem
JOIN Membre re ON i.IDrecepteur = re.IDmem
WHERE i.statut = 'en attente';

-- INSERT : nouvelle tâche pour Youssef dans le sprint 3
INSERT INTO Tache (labtach, dateeche, estim, pos, IDpro, IDmem, IDsp)
VALUES ('Déploiement', '2026-11-10', 3, 2, 1, 2, 3);

-- UPDATE : démarrer le sprint 3, accepter l'invitation de Karim, lire une notification
UPDATE Sprint SET etat = 'en cours' WHERE IDsp = 3;
UPDATE Invitation SET statut = 'acceptée' WHERE IDinvit = 3;
INSERT INTO Membre_Projet (IDmem, IDpro, rolemem) VALUES (4, 1, 'membre');
UPDATE Notification SET estlue = TRUE WHERE IDnotif = 1;

-- UPDATE : réattribuer une tâche
UPDATE Tache SET IDmem = 3 WHERE IDtach = 3;

-- DELETE : supprimer le sprint 3 (ses tâches sont supprimées en cascade)
DELETE FROM Sprint WHERE IDsp = 3;
SELECT labtach, IDsp FROM Tache WHERE IDpro = 1;