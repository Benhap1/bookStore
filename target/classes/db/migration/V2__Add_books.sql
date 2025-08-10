INSERT INTO books (name, genre, target_age_group, price, publication_date, author, pages, characteristics, description, language) VALUES
-- Fantasy
('The Hobbit', 'Fantasy', 'TEEN', 14.99, '1937-09-21', 'J.R.R. Tolkien', 310, 'Paperback, 5x8 inches, Classic cover', 'A classic fantasy adventure about Bilbo Baggins, a hobbit who joins a group of dwarves on a quest to reclaim their homeland from the dragon Smaug.', 'ENGLISH'),
('A Game of Thrones', 'Fantasy', 'ADULT', 19.99, '1996-08-01', 'George R.R. Martin', 694, 'Hardcover, 6x9 inches, Matte finish', 'The first book in the A Song of Ice and Fire series, detailing the struggle for the Iron Throne of Westeros.', 'ENGLISH'),
('The Name of the Wind', 'Fantasy', 'ADULT', 18.50, '2007-03-27', 'Patrick Rothfuss', 662, 'Paperback, 6x9 inches, Stylized art', 'The story of Kvothe, a magically gifted young man who grows to be the most notorious wizard his world has ever seen.', 'ENGLISH'),
('Mistborn: The Final Empire', 'Fantasy', 'TEEN', 17.95, '2006-07-17', 'Brandon Sanderson', 541, 'Paperback, 5.5x8.5 inches, High-fantasy cover', 'In a world where ash falls from the sky and mists rule the night, a young street urchin discovers she has magical powers and joins a rebellion.', 'ENGLISH'),
('Elantris', 'Fantasy', 'ADULT', 22.00, '2005-04-21', 'Brandon Sanderson', 622, 'Hardcover, 6x9 inches, Commemorative Edition', 'The city of Elantris, a place of magic, has fallen. Now, those afflicted with the Shaod are cast inside its walls to rot. A prince is the latest victim.', 'ENGLISH'),

-- Sci-Fi
('Dune', 'Sci-Fi', 'ADULT', 21.50, '1965-08-01', 'Frank Herbert', 412, 'Paperback, 6x9 inches, Iconic cover', 'The story of Paul Atreides, who must travel to the most dangerous planet in the universe to ensure the future of his family and his people.', 'ENGLISH'),
('Neuromancer', 'Sci-Fi', 'ADULT', 16.75, '1984-07-01', 'William Gibson', 271, 'Paperback, 5x8 inches, Cyberpunk art', 'A washed-up computer hacker is hired for one last job, which will send him to the heart of a vast corporate-controlled digital world.', 'ENGLISH'),
('Foundation', 'Sci-Fi', 'ADULT', 15.99, '1951-06-01', 'Isaac Asimov', 255, 'Paperback, 5x8 inches, Retro sci-fi cover', 'A psycho-historian creates a foundation to preserve knowledge and guide humanity through a dark age after the fall of a galactic empire.', 'ENGLISH'),
('Hyperion', 'Sci-Fi', 'ADULT', 24.99, '1989-05-26', 'Dan Simmons', 482, 'Hardcover, 6x9 inches, Modern design', 'On the world of Hyperion, seven pilgrims set out on a journey to the Time Tombs, each with a desperate hope and a terrible secret.', 'ENGLISH'),
('The Three-Body Problem', 'Sci-Fi', 'ADULT', 19.80, '2008-01-01', 'Cixin Liu', 400, 'Paperback, 5.5x8.5 inches, International bestseller', 'A secret military project sends signals into space to establish contact with aliens. An alien civilization on the brink of destruction captures the signal and plans to invade Earth.', 'ENGLISH'),

-- Classics
('1984', 'Dystopian', 'ADULT', 12.99, '1949-06-08', 'George Orwell', 328, 'Paperback, 5x8 inches, Minimalist cover', 'A haunting vision of a totalitarian future where everything and everyone is slave to a tyrannical regime.', 'ENGLISH'),
('To Kill a Mockingbird', 'Classic', 'TEEN', 11.50, '1960-07-11', 'Harper Lee', 281, 'Paperback, 5x8 inches, Timeless design', 'A novel about the seriousness of the issues of race and justice in the American South, told through the eyes of a young girl.', 'ENGLISH'),
('The Great Gatsby', 'Classic', 'ADULT', 10.95, '1925-04-10', 'F. Scott Fitzgerald', 180, 'Paperback, 5x8 inches, Art deco cover', 'A story of the fabulously wealthy Jay Gatsby and his new love for the beautiful Daisy Buchanan.', 'ENGLISH'),
('Moby Dick', 'Classic', 'ADULT', 13.25, '1851-10-18', 'Herman Melville', 635, 'Hardcover, 6x9 inches, Vintage illustration', 'The saga of Captain Ahab and his relentless pursuit of Moby Dick, the great white whale.', 'ENGLISH'),
('Fahrenheit 451', 'Dystopian', 'TEEN', 14.00, '1953-10-19', 'Ray Bradbury', 256, 'Paperback, 5x8 inches, Modern graphic design', 'In a future society where books are outlawed, a firefighter''s job is to burn them. He begins to question his role.', 'ENGLISH'),

-- Mystery & Thriller
('The Girl with the Dragon Tattoo', 'Thriller', 'ADULT', 16.99, '2005-08-01', 'Stieg Larsson', 590, 'Paperback, 6x9 inches, Swedish noir style', 'A journalist and a young computer hacker team up to investigate a 40-year-old mystery of a woman who vanished from a wealthy family.', 'ENGLISH'),
('Gone Girl', 'Thriller', 'ADULT', 15.50, '2012-06-05', 'Gillian Flynn', 415, 'Paperback, 5.5x8.5 inches, Bestseller design', 'On the day of their fifth wedding anniversary, a woman disappears, and her husband becomes the prime suspect.', 'ENGLISH'),
('The Da Vinci Code', 'Mystery', 'ADULT', 18.00, '2003-03-18', 'Dan Brown', 454, 'Hardcover, 6x9 inches, Iconic design', 'A symbologist is drawn into a tangled web of secret societies, ancient conspiracies, and a quest for the Holy Grail.', 'ENGLISH'),

-- Non-Fiction
('Sapiens: A Brief History of Humankind', 'Non-Fiction', 'ADULT', 25.00, '2011-01-01', 'Yuval Noah Harari', 443, 'Paperback, 6x9 inches, Scholarly design', 'A comprehensive account of the history of Homo sapiens, from the Stone Age to the present day.', 'ENGLISH'),
('Educated: A Memoir', 'Non-Fiction', 'ADULT', 17.99, '2018-02-20', 'Tara Westover', 352, 'Hardcover, 6x9 inches, Photographic cover', 'A memoir about a young girl who, kept out of school, leaves her survivalist family and goes on to earn a PhD from Cambridge University.', 'ENGLISH'),

-- Children's Books
('Where the Wild Things Are', 'Childrens', 'CHILD', 9.99, '1963-01-01', 'Maurice Sendak', 48, 'Hardcover, 10x9 inches, Illustrated', 'A young boy named Max, after dressing in his wolf costume, wreaks such havoc through his household that he is sent to bed without his supper.', 'ENGLISH'),
('Harry Potter and the Sorcerer''s Stone', 'Fantasy', 'CHILD', 22.99, '1997-06-26', 'J.K. Rowling', 309, 'Hardcover, 6x9 inches, Illustrated Edition', 'Harry Potter''s life is miserable. His parents are dead and he''s stuck with his heartless relatives, who force him to live in a tiny closet under the stairs. But his fortune changes when he receives a letter that tells him the truth about himself: he''s a wizard.', 'ENGLISH'),

-- International Books
('Cien años de soledad', 'Magical Realism', 'ADULT', 20.00, '1967-05-30', 'Gabriel García Márquez', 417, 'Paperback, 5.5x8.5 inches, Latin American art', 'The story of seven generations of the Buendía family in the fictional town of Macondo.', 'SPANISH'),
('L''Étranger', 'Existentialism', 'ADULT', 14.50, '1942-01-01', 'Albert Camus', 123, 'Paperback, 5x8 inches, French edition cover', 'The story of an ordinary man who is drawn into a senseless murder on an Algerian beach.', 'FRENCH'),
('Die Verwandlung', 'Classic', 'ADULT', 13.00, '1915-10-01', 'Franz Kafka', 201, 'Paperback, 5x8 inches, German edition cover', 'The story of a traveling salesman, Gregor Samsa, who wakes to find himself transformed into a large, monstrous insect-like creature.', 'GERMAN');