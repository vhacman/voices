-- ============================================================
-- SEED DATA - Our Voices
-- ============================================================

USE voices;

-- ------------------------------------------------------------
-- UTENTI
-- Password hash MD5:
--   Admin123!   → 9c3477ef104a836e3dc07a2c1bd80776
--   Blogger1!   → 1ebd61d582361a6c68bc9bdb01d8df64
--   Blogger2!   → 2b49ea15ce262f6c31e5b9f392683d16
-- ------------------------------------------------------------

INSERT INTO portal_user (first_name, last_name, username, dob, email, password, role, last_password_change) VALUES
('Mario',   'Rossi',   'admin',     '1985-03-15', 'admin@voices.it',    '9c3477ef104a836e3dc07a2c1bd80776', 'ADMIN',   CURDATE()),
('Giulia',  'Bianchi', 'blogger1',  '1995-07-22', 'giulia@voices.it',   '1ebd61d582361a6c68bc9bdb01d8df64', 'BLOGGER', CURDATE()),
('Luca',    'Verdi',   'blogger2',  '1990-11-08', 'luca@voices.it',     '2b49ea15ce262f6c31e5b9f392683d16', 'BLOGGER', CURDATE());

-- ------------------------------------------------------------
-- BLOG (uno per blogger)
-- ------------------------------------------------------------

INSERT INTO blog (title, image, description, author_id, template, type, palette) VALUES
(
  'Vita da Giulia',
  'https://picsum.photos/seed/giulia/800/400',
  'Un diario digitale tra ricette, viaggi e pensieri di una trentenne curiosa.',
  (SELECT id FROM portal_user WHERE username = 'blogger1'),
  'JOURNAL',
  'PUBLIC',
  'OCEAN'
),
(
  'Tech & Caffè',
  'https://picsum.photos/seed/luca/800/400',
  'Sviluppo software, open source e tutto quello che succede tra una tazza e l altra.',
  (SELECT id FROM portal_user WHERE username = 'blogger2'),
  'MINIMAL',
  'PUBLIC',
  'NIGHT'
);

-- ------------------------------------------------------------
-- BLOG POST - Blog 1 "Vita da Giulia" (10 post)
-- ------------------------------------------------------------

INSERT INTO blog_post (blog_id, title, text, image, status, tags, view, published_on) VALUES
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Benvenuti nel mio blog',
  'Ciao a tutti! Mi chiamo Giulia e questo è il mio angolo di web dove condivido tutto ciò che amo: cucina, viaggi, libri e molto altro. Spero che queste pagine diventino un posto accogliente anche per voi.',
  'https://picsum.photos/seed/g1/800/400',
  'PUBLISHED', 'intro,benvenuto', 42, '2024-01-10 10:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Risotto al limone: la mia ricetta del cuore',
  'Il risotto al limone è uno di quei piatti che sa sempre di domenica. Ingredienti: 320g di riso Carnaroli, 1 limone biologico, brodo vegetale, burro, parmigiano. La chiave è mantecare fuori dal fuoco con burro freddo: cremosità garantita.',
  'https://picsum.photos/seed/g2/800/400',
  'PUBLISHED', 'cucina,ricette,risotto', 87, '2024-01-18 12:30:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Weekend a Firenze: cosa vedere in 48 ore',
  'Firenze in due giorni è possibile, ma devi scegliere. Il mio itinerario: Uffizi la mattina presto (prenotate!), pranzo al mercato centrale, Palazzo Vecchio nel pomeriggio, aperitivo all Oltrarno. Il giorno dopo: Piazzale Michelangelo all alba e il battistero senza folla.',
  'https://picsum.photos/seed/g3/800/400',
  'PUBLISHED', 'viaggi,firenze,weekend', 134, '2024-02-05 09:15:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'I 5 libri che hanno cambiato il mio modo di pensare',
  'Non sono libri di self-help. Sono romanzi, saggi e memoir che mi hanno costretta a fermarmi e riconsiderare qualcosa. In cima alla lista: "Stoner" di John Williams, che parla di una vita ordinaria con una grazia straordinaria.',
  'https://picsum.photos/seed/g4/800/400',
  'PUBLISHED', 'libri,lettura,consigli', 61, '2024-02-20 15:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Minimalismo: cosa ho buttato e cosa ho imparato',
  'Ho passato un mese a svuotare l appartamento. Non in stile Marie Kondo estremo, ma con criterio. Risultato: tre sacchi donati, un armadio che respira, e la sensazione che avere meno roba occupi meno spazio anche nella testa.',
  'https://picsum.photos/seed/g5/800/400',
  'PUBLISHED', 'lifestyle,minimalismo', 55, '2024-03-02 11:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Colazione giapponese: la scoperta dell anno',
  'Miso soup alle 7 di mattina suona strano, ma dopo due settimane non riesco più a tornare al cornetto. La colazione giapponese è proteica, calda e ti porta avanti fino a pranzo senza crolli di zuccheri.',
  'https://picsum.photos/seed/g6/800/400',
  'PUBLISHED', 'cucina,giappone,colazione', 78, '2024-03-15 08:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Come ho smesso di controllare il telefono ogni 5 minuti',
  'Il primo passo è stato spostare tutte le app di social nella seconda pagina. Poi ho tolto le notifiche. Poi ho comprato una sveglia vera. Sei mesi dopo: meno ansia, più lettura, e la scoperta che il mondo non finisce se non rispondo subito.',
  'https://picsum.photos/seed/g7/800/400',
  'PUBLISHED', 'digitale,benessere,telefono', 112, '2024-04-01 17:30:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Torta alle mele di nonna: la ricetta originale',
  'Mia nonna usava le mele renette, mai le golden. Questo fa tutta la differenza. L impasto è senza lievito, si usa lo bicarbonato e un cucchiaio di aceto. Suona strano, funziona benissimo. Ve la do così, senza modifiche, perché non si tocca.',
  'https://picsum.photos/seed/g8/800/400',
  'PUBLISHED', 'cucina,dolci,nonna', 203, '2024-04-22 14:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Amsterdam in bicicletta: guida pratica',
  'Amsterdam si capisce solo in bici. Niente tram, niente metro. Solo pedali, canali e la regola implicita che chi non sa andare in bici non è il benvenuto nelle corsie preferenziali. Vi spiego come muovervi senza farsi odiare dagli olandesi.',
  'https://picsum.photos/seed/g9/800/400',
  'PUBLISHED', 'viaggi,amsterdam,bici', 167, '2024-05-10 10:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Vita da Giulia'),
  'Settembre: il mio mese preferito e perché',
  'Settembre ha la luce giusta. Non l abbaglio di agosto, non il grigio di ottobre. Ha ancora caldo ma senza afa, i bar si svuotano, i musei si respirano. È il mese in cui torno a leggere, a cucinare lentamente e a fare piani che forse poi non seguirò.',
  'https://picsum.photos/seed/g10/800/400',
  'PUBLISHED', 'vita,settembre,autunno', 89, '2024-09-01 09:00:00'
);

-- ------------------------------------------------------------
-- BLOG POST - Blog 2 "Tech & Caffè" (10 post)
-- ------------------------------------------------------------

INSERT INTO blog_post (blog_id, title, text, image, status, tags, view, published_on) VALUES
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Perché ho abbandonato Windows per Arch Linux',
  'Non sono uno snob. O forse sì, un po . Ma dopo tre anni su Arch posso dire che il tempo perso a configurare è stato ampiamente ripagato da un sistema che capisco davvero. Vi racconto la migrazione, gli errori e i pacman -Syu che mi hanno salvato.',
  'https://picsum.photos/seed/l1/800/400',
  'PUBLISHED', 'linux,arch,windows', 310, '2024-01-12 10:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Git: i comandi che uso ogni giorno (e quelli che dimentico sempre)',
  'git stash pop, git rebase -i, git bisect: li conosco tutti ma li cerco su Google ogni volta. Ho scritto questo post principalmente per me stesso. Se aiuta anche voi, meglio.',
  'https://picsum.photos/seed/l2/800/400',
  'PUBLISHED', 'git,dev,tools', 445, '2024-01-25 11:30:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Spring Boot in 20 minuti: da zero a REST API',
  'Spring Boot ha una curva di apprendimento ripida all inizio e poi diventa quasi troppo facile. In questo post costruiamo insieme una API CRUD completa con JPA e H2 in-memory. Nessuna configurazione manuale, tutto auto-configurato.',
  'https://picsum.photos/seed/l3/800/400',
  'PUBLISHED', 'spring,java,backend', 522, '2024-02-08 09:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Docker per sviluppatori: non serve diventare DevOps',
  'Docker fa paura finché non capisci che è fondamentalmente "un computer dentro al tuo computer con istruzioni scritte su un file". Dockerfile, docker-compose, volumi: vi spiego solo quello che vi serve per sviluppare senza installare tutto in locale.',
  'https://picsum.photos/seed/l4/800/400',
  'PUBLISHED', 'docker,devops,containers', 389, '2024-02-22 15:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'La mia scrivania da sviluppatore: setup 2024',
  'Monitor ultrawide 34", tastiera meccanica con switch brown, mouse verticale per il tunnel carpale che avevo iniziato a sentire. Il setup ideale non esiste ma questo è il mio compromesso tra ergonomia, estetica e budget.',
  'https://picsum.photos/seed/l5/800/400',
  'PUBLISHED', 'setup,scrivania,ergonomia', 677, '2024-03-05 12:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'TypeScript vs JavaScript: quando vale la pena il setup extra',
  'La risposta breve è: quasi sempre. La risposta lunga è questo post. TypeScript non è solo "JavaScript con i tipi": è un contratto tra te e il tuo io del futuro che spiega cosa si aspetta ogni funzione. Particolarmente utile quando lavori in team.',
  'https://picsum.photos/seed/l6/800/400',
  'PUBLISHED', 'typescript,javascript,frontend', 298, '2024-03-18 10:30:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Come funziona JWT: spiegato senza magia',
  'Un JSON Web Token è solo tre stringhe Base64 separate da punti. Header, payload, firma. Non è cifrato (chiunque può leggere il payload), è firmato. La differenza è cruciale e molti la ignorano. Ve lo spiego con esempi pratici.',
  'https://picsum.photos/seed/l7/800/400',
  'PUBLISHED', 'jwt,security,auth', 734, '2024-04-03 14:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'IntelliJ IDEA: 10 shortcut che cambiano la vita',
  'Shift+Shift per cercare ovunque. Ctrl+Alt+L per formattare. Alt+Enter per le quick fix. Refactor→Rename invece di Ctrl+H. Se usate ancora il mouse per navigare il codice, questo post è per voi.',
  'https://picsum.photos/seed/l8/800/400',
  'PUBLISHED', 'intellij,ide,produttività', 412, '2024-04-20 09:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'PostgreSQL vs MySQL: la mia opinione dopo anni su entrambi',
  'MySQL è più semplice da configurare, PostgreSQL è più potente e rispetta meglio lo standard SQL. Se state iniziando: MySQL va benissimo. Se avete query complesse, JSONB, o volete estensioni: PostgreSQL. Non esiste una risposta universale.',
  'https://picsum.photos/seed/l9/800/400',
  'PUBLISHED', 'database,postgresql,mysql', 356, '2024-05-07 11:00:00'
),
(
  (SELECT id FROM blog WHERE title = 'Tech & Caffè'),
  'Il burnout da sviluppatore: come l ho riconosciuto e affrontato',
  'A un certo punto aprire l IDE era diventato faticoso. Non per mancanza di competenze, ma perché avevo smesso di vedere il senso. Vi racconto come ho rallentato, cosa ho cambiato nel mio modo di lavorare, e perché adesso mi fermo alle 18.',
  'https://picsum.photos/seed/l10/800/400',
  'PUBLISHED', 'burnout,lavoro,benessere', 891, '2024-06-01 16:00:00'
);
