/* sito-j — interazioni del prototipo.
   Principio: il contenuto è SEMPRE visibile senza JavaScript (SEO e accessibilità);
   le animazioni si aggiungono sopra solo se il browser e l'utente le vogliono. */

(function () {
    'use strict';

    const root = document.documentElement;
    const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
    const finePointer = matchMedia('(pointer: fine)').matches;

    /* ---------- Tema chiaro/scuro ---------- */
    document.getElementById('themeToggle').addEventListener('click', () => {
        const next = root.dataset.theme === 'dark' ? 'light' : 'dark';
        root.dataset.theme = next;
        localStorage.setItem('theme', next);
    });

    /* ---------- Barra di navigazione: vetro solo dopo lo scroll ---------- */
    const nav = document.getElementById('nav');
    const onScroll = () => nav.classList.toggle('scrolled', scrollY > 10);
    addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    /* ---------- Card: riflesso dorato + leggera inclinazione 3D che seguono il mouse ---------- */
    if (finePointer && !reduceMotion) {
        document.querySelectorAll('.card').forEach(card => {
            card.addEventListener('pointermove', e => {
                const r = card.getBoundingClientRect();
                const x = (e.clientX - r.left) / r.width;
                const y = (e.clientY - r.top) / r.height;
                card.style.setProperty('--mx', `${x * 100}%`);
                card.style.setProperty('--my', `${y * 100}%`);
                card.style.transform = `perspective(1200px) rotateX(${(0.5 - y) * 4}deg) rotateY(${(x - 0.5) * 4}deg)`;
            });
            card.addEventListener('pointerleave', () => { card.style.transform = ''; });
        });
    }

    /* ---------- Turnstile (anti-bot): lo script di Cloudflare si carica solo vicino al form ----------
       Così chi non arriva ai contatti non contatta mai Cloudflare (privacy e velocità). */
    const contactForm = document.getElementById('contactForm');
    if (contactForm) {
        const loadTurnstile = () => {
            if (document.getElementById('cf-turnstile-js')) return;
            const s = document.createElement('script');
            s.id = 'cf-turnstile-js';
            s.src = 'https://challenges.cloudflare.com/turnstile/v0/api.js';
            s.async = true;
            document.head.appendChild(s);
        };
        contactForm.addEventListener('focusin', loadTurnstile, { once: true });
        if ('IntersectionObserver' in window) {
            const io = new IntersectionObserver(entries => {
                if (entries.some(e => e.isIntersecting)) { loadTurnstile(); io.disconnect(); }
            }, { rootMargin: '600px' });
            io.observe(contactForm);
        } else {
            loadTurnstile();
        }
    }

    const staticMode = location.search.includes('static');   // ?static: niente animazioni (screenshot/debug)
    if (staticMode || reduceMotion || !window.gsap || !window.ScrollTrigger) {
        return;   // versione statica: tutto già visibile
    }
    gsap.registerPlugin(ScrollTrigger);

    /* ---------- Hero: entrata + "rimpicciolimento" quando si scorre ---------- */
    gsap.from('.hero-anim', {
        y: 40, opacity: 0, filter: 'blur(10px)',
        duration: 1.2, ease: 'power3.out', stagger: 0.12, delay: 0.1
    });
    gsap.to('.hero-inner', {
        scale: 0.86, opacity: 0, y: -80, ease: 'none',
        scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom top', scrub: true }
    });

    /* ---------- Manifesto: sezione fissata, le parole si accendono una dopo l'altra ---------- */
    const statement = document.getElementById('statement');
    statement.innerHTML = statement.textContent.trim().split(/\s+/)
        .map((w, i, all) => `<span class="w${i >= all.length - 4 ? ' gold-word' : ''}">${w} </span>`).join('');
    gsap.fromTo('#statement .w',
        { opacity: 0.12 },
        {
            opacity: 1, ease: 'none', stagger: 0.1,
            scrollTrigger: { trigger: '.statement', start: 'top top', end: '+=120%', scrub: true, pin: true }
        });

    /* ---------- Elementi che entrano dal basso ---------- */
    gsap.utils.toArray('[data-reveal]').forEach(el => {
        gsap.from(el, {
            y: 50, opacity: 0, duration: 1, ease: 'power3.out',
            scrollTrigger: { trigger: el, start: 'top 88%' }
        });
    });

    /* ---------- Contatori che salgono ---------- */
    document.querySelectorAll('[data-count]').forEach(el => {
        const target = Number(el.dataset.count);
        const counter = { v: 0 };
        el.textContent = '0';
        gsap.to(counter, {
            v: target, duration: 1.8, ease: 'power2.out',
            scrollTrigger: { trigger: el, start: 'top 90%' },
            onUpdate: () => { el.textContent = Math.round(counter.v); }
        });
    });

    /* ---------- Barre dei linguaggi che si riempiono ---------- */
    gsap.utils.toArray('.langbar span').forEach(bar => {
        gsap.from(bar, { width: 0, duration: 1.4, ease: 'power3.out', scrollTrigger: { trigger: bar, start: 'top 92%' } });
    });

    /* ---------- Timeline: la linea si "disegna" scorrendo ---------- */
    const timeline = document.getElementById('timeline');
    gsap.fromTo(timeline, { '--progress': 0 }, {
        '--progress': 1, ease: 'none',
        scrollTrigger: { trigger: timeline, start: 'top 75%', end: 'bottom 60%', scrub: true }
    });

    /* ---------- Skill: i nastri accelerano leggermente con la velocità di scroll ---------- */
    const tracks = gsap.utils.toArray('.marquee-track');
    ScrollTrigger.create({
        trigger: '.skills', start: 'top bottom', end: 'bottom top',
        onUpdate: self => {
            const boost = 1 + Math.min(Math.abs(self.getVelocity()) / 1500, 3);
            tracks.forEach(t => t.getAnimations().forEach(a => { a.playbackRate = boost; }));
        }
    });
})();
