INSERT INTO users (name, email, role, created_at, updated_at)
VALUES ('Demo Developer', 'demo@aicodereview.local', 'DEVELOPER', now(), now())
ON CONFLICT (email) DO NOTHING;
