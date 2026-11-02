package dev.jj.projects.sync;

/** Legge i dati di un repository da un provider (GITHUB, GITLAB). */
public interface RepoClient {

    String provider();

    /** @param path nel formato owner/nome. Lancia RuntimeException se il repository non è raggiungibile. */
    RepoInfo fetch(String path);
}
