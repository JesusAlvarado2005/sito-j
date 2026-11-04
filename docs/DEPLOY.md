# Deploy su Oracle Cloud (Always Free)

Schema: Internet → **Caddy** (80/443, HTTPS automatico) → web-public → profile / projects / contact → PostgreSQL.
Le immagini Docker le costruisce GitHub Actions a ogni push su `main` e le pubblica su GHCR: sulla VM non si compila niente.

## 1. Immagini pubbliche su GHCR (una volta sola)
Dopo il primo push su `main` con la CI verde, su GitHub: profilo → **Packages** → per ognuno dei 4 pacchetti
`sito-j-*` → *Package settings* → *Change visibility* → **Public**. Così la VM le scarica senza login.
Nella stessa pagina, in *Manage Actions access*, il repository `sito-j` deve avere il ruolo **Write**:
senza, la CI non può aggiornare le immagini (`permission_denied: write_package`).

## 2. La VM
1. Registrati su https://www.oracle.com/cloud/free/ (serve una carta solo per la verifica; resta sul piano Free).
2. *Compute → Instances → Create instance*:
   - Image: **Ubuntu 24.04** (aarch64) · Shape: **VM.Standard.A1.Flex**, **2 OCPU / 12 GB**
   - SSH: carica la tua chiave pubblica (`~/.ssh/id_ed25519.pub`)
   - Se esce "Out of capacity": riprova più tardi o cambia *Availability domain*.
3. Apri le porte web nella rete Oracle: *Networking → Virtual Cloud Networks → (la tua VCN) → Security Lists → Default*
   → *Add Ingress Rules*: sorgente `0.0.0.0/0`, TCP, porte **80** e **443** (e UDP 443 per HTTP/3).
4. Collegati: `ssh ubuntu@IP_DELLA_VM`
5. Le immagini Ubuntu di Oracle hanno anche un firewall interno che blocca tutto: apri 80 e 443.
   ```bash
   sudo iptables -I INPUT 6 -p tcp --dport 80 -j ACCEPT
   sudo iptables -I INPUT 6 -p tcp --dport 443 -j ACCEPT
   sudo iptables -I INPUT 6 -p udp --dport 443 -j ACCEPT
   sudo netfilter-persistent save
   ```
6. Docker:
   ```bash
   curl -fsSL https://get.docker.com | sudo sh
   sudo usermod -aG docker ubuntu && exit     # poi ricollegati con ssh
   ```

## 3. Il dominio
Richiedi `tuonome.is-a.dev` con una Pull Request su https://github.com/is-a-dev/register
(file `domains/tuonome.json` con un record **A** verso l'IP della VM). Aspetta l'approvazione e che il DNS risponda:
`dig +short tuonome.is-a.dev` deve restituire l'IP della VM. **Prima** di questo Caddy non può ottenere il certificato.

## 4. Turnstile vero
https://dash.cloudflare.com → *Turnstile* → *Add widget* → dominio `tuonome.is-a.dev` → ottieni **site key** e **secret key**.

## 5. Avvio
```bash
git clone https://github.com/JesusAlvarado2005/sito-j.git && cd sito-j
nano .env        # copia il tuo .env locale e cambia: password nuove, CONTACT_IP_SALT nuovo (openssl rand -hex 32),
                 # TURNSTILE_SITE_KEY/SECRET veri, SITE_ADDRESS=tuonome.is-a.dev, SITE_BASE_URL=https://tuonome.is-a.dev
chmod 600 .env
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
docker compose ps        # tutti "healthy" dopo circa un minuto
```
Apri `https://tuonome.is-a.dev`: il certificato lo crea Caddy al primo accesso.

## 6. Aggiornare il sito
Fai push su `main` → aspetta la CI verde → sulla VM:
```bash
cd sito-j && git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Regole di sicurezza da non rompere
- Solo Caddy pubblica porte. I servizi Java e PostgreSQL non devono mai essere raggiungibili da Internet
  (web-public si fida di `X-Forwarded-For` solo perché gli arriva da Caddy nella rete interna).
- `.env` resta solo sulla VM (`chmod 600`), mai su Git. In produzione password e salt **diversi** da quelli locali.
- SSH solo con chiave (è il default delle VM Oracle).
