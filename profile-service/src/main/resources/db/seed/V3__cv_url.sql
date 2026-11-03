-- CV scaricabile dal sito (versione senza numero di telefono), servito da web-public in /cv/.
-- Percorso interno al sito: funziona sia sul dominio finale sia nella sottocartella di GitHub Pages.
UPDATE profile_translation SET cv_url = '/cv/Jeanfranco_Alvarado_CV.pdf' WHERE profile_id = 1;
