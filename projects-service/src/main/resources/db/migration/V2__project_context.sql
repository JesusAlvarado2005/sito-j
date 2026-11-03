-- Riga di contesto mostrata sulla card (es. "Tesina ITS · team di 2 · 2026"), traducibile.
ALTER TABLE project_translation ADD COLUMN context VARCHAR(150);
