package dev.jj.web.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Cache con TTL che, se il servizio è giù, restituisce l'ultimo dato buono ("stale") invece di fallire.
 * Se non c'è nessun dato precedente restituisce empty: la pagina mostra una sezione vuota, non un errore.
 */
public final class StaleCache<K, V> {

    private static final Logger log = LoggerFactory.getLogger(StaleCache.class);

    private record Entry<V>(V value, Instant loadedAt) {
    }

    private final ConcurrentHashMap<K, Entry<V>> entries = new ConcurrentHashMap<>();
    private final Duration ttl;

    public StaleCache(Duration ttl) {
        this.ttl = ttl;
    }

    public Optional<V> get(K key, Supplier<V> loader) {
        Entry<V> cached = entries.get(key);
        if (cached != null && Instant.now().isBefore(cached.loadedAt().plus(ttl))) {
            return Optional.of(cached.value());
        }
        try {
            V fresh = loader.get();
            if (fresh != null) {
                entries.put(key, new Entry<>(fresh, Instant.now()));
            }
            return Optional.ofNullable(fresh);
        } catch (RuntimeException e) {
            log.warn("Servizio non raggiungibile per '{}': {}", key, e.toString());
            return Optional.ofNullable(cached).map(Entry::value);
        }
    }
}
