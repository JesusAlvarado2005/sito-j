/* Applica il tema PRIMA che la pagina venga disegnata (niente "lampo" bianco).
   File separato, non inline: così la Content-Security-Policy può vietare tutti gli script inline. */
(function () {
    var saved = new URLSearchParams(location.search).get('theme') || localStorage.getItem('theme');
    var theme = saved || (matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');
    document.documentElement.dataset.theme = theme;
})();
