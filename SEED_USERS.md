# Utenti di prova — Our Voices

## Credenziali di accesso

| Ruolo   | Username  | Password   | Email               |
|---------|-----------|------------|---------------------|
| ADMIN   | admin     | Admin123!  | admin@voices.it     |
| BLOGGER | blogger1  | Blogger1!  | giulia@voices.it    |
| BLOGGER | blogger2  | Blogger2!  | luca@voices.it      |

## Hash MD5

| Username | Hash MD5                             |
|----------|--------------------------------------|
| admin    | 9c3477ef104a836e3dc07a2c1bd80776     |
| blogger1 | 1ebd61d582361a6c68bc9bdb01d8df64     |
| blogger2 | 2b49ea15ce262f6c31e5b9f392683d16     |

---

## Contenuto inserito

### blogger1 — Giulia Bianchi
- **Blog:** Vita da Giulia (PUBLIC, template JOURNAL, palette OCEAN)
- **10 post PUBLISHED** su argomenti: cucina, viaggi, libri, lifestyle

### blogger2 — Luca Verdi
- **Blog:** Tech & Caffè (PUBLIC, template MINIMAL, palette NIGHT)
- **10 post PUBLISHED** su argomenti: Linux, Git, Spring Boot, Docker, JWT, burnout

---

## Come eseguire il seed

```bash
mysql -u root -p voices < seed.sql
```

oppure incollare direttamente in MySQL Workbench / DBeaver.
