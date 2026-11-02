package dev.jj.contact.message;

/** Avvisa il proprietario del sito di un nuovo messaggio. Lancia eccezione se l'invio fallisce. */
interface Notifier {

    void send(ContactMessage message);
}
