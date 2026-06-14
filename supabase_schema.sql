-- ============================================================
-- TuitionOS — Supabase PostgreSQL Schema
-- Generated for: https://mupyyohhtsmxqxrrzoea.supabase.co
-- ============================================================
-- Execute this ENTIRE script in:
--   Supabase Dashboard → SQL Editor → New Query → Paste → Run
-- ============================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. STUDENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.students (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name       TEXT            NOT NULL DEFAULT '',
    standard        TEXT            NOT NULL DEFAULT '',
    parent_name     TEXT            NOT NULL DEFAULT '',
    parent_phone    TEXT            NOT NULL DEFAULT '',
    monthly_fee     NUMERIC(10,2)   NOT NULL DEFAULT 0.0,
    batch_name      TEXT            NOT NULL DEFAULT '',
    status          TEXT            NOT NULL DEFAULT 'Pending',
    student_id      TEXT            NOT NULL DEFAULT '',
    avatar_url      TEXT            NOT NULL DEFAULT '',
    attendance_percentage INTEGER   NOT NULL DEFAULT 100,
    term_mid_test_score   INTEGER   NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT students_name_check CHECK (length(trim(full_name)) > 0),
    CONSTRAINT students_standard_check CHECK (length(trim(standard)) > 0),
    CONSTRAINT students_fee_check CHECK (monthly_fee >= 0),
    CONSTRAINT students_attendance_check CHECK (attendance_percentage >= 0 AND attendance_percentage <= 100),
    CONSTRAINT students_status_check CHECK (status IN ('Pending', 'Paid', 'Overdue', 'Active', 'Inactive'))
);

CREATE INDEX IF NOT EXISTS idx_students_user_id ON public.students(user_id);
CREATE INDEX IF NOT EXISTS idx_students_batch   ON public.students(batch_name);

ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;

-- RLS: Users can only see/manage their own students
CREATE POLICY "students_select_own" ON public.students
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "students_insert_own" ON public.students
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "students_update_own" ON public.students
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "students_delete_own" ON public.students
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 2. BATCHES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.batches (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name            TEXT            NOT NULL DEFAULT '',
    subject         TEXT            NOT NULL DEFAULT '',
    days_of_week    TEXT            NOT NULL DEFAULT '',
    start_time      TEXT            NOT NULL DEFAULT '',
    end_time        TEXT            NOT NULL DEFAULT '',
    status          TEXT            NOT NULL DEFAULT 'ACTIVE',
    student_count   INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT batches_name_check CHECK (length(trim(name)) > 0),
    CONSTRAINT batches_subject_check CHECK (length(trim(subject)) > 0),
    CONSTRAINT batches_student_count_check CHECK (student_count >= 0),
    CONSTRAINT batches_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'ONGOING', 'UPCOMING'))
);

CREATE INDEX IF NOT EXISTS idx_batches_user_id ON public.batches(user_id);

ALTER TABLE public.batches ENABLE ROW LEVEL SECURITY;

CREATE POLICY "batches_select_own" ON public.batches
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "batches_insert_own" ON public.batches
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "batches_update_own" ON public.batches
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "batches_delete_own" ON public.batches
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 3. LEADS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.leads (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    inquirer_name   TEXT            NOT NULL DEFAULT '',
    standard        TEXT            NOT NULL DEFAULT '',
    source          TEXT            NOT NULL DEFAULT '',
    status          TEXT            NOT NULL DEFAULT 'NEW',
    phone           TEXT            NOT NULL DEFAULT '',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT leads_name_check CHECK (length(trim(inquirer_name)) > 0),
    CONSTRAINT leads_status_check CHECK (status IN ('NEW', 'CONTACTED', 'FOLLOW_UP', 'ADMITTED', 'LOST'))
);

CREATE INDEX IF NOT EXISTS idx_leads_user_id ON public.leads(user_id);
CREATE INDEX IF NOT EXISTS idx_leads_status  ON public.leads(status);

ALTER TABLE public.leads ENABLE ROW LEVEL SECURITY;

CREATE POLICY "leads_select_own" ON public.leads
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "leads_insert_own" ON public.leads
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "leads_update_own" ON public.leads
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "leads_delete_own" ON public.leads
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 4. FEE_HISTORY TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.fee_history (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    student_name        TEXT            NOT NULL DEFAULT '',
    month               TEXT            NOT NULL DEFAULT '',
    installment         TEXT            NOT NULL DEFAULT '',
    amount              NUMERIC(10,2)   NOT NULL DEFAULT 0.0,
    status              TEXT            NOT NULL DEFAULT '',
    due_date            TEXT            NOT NULL DEFAULT '',
    outstanding_balance NUMERIC(10,2)   NOT NULL DEFAULT 0.0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT fee_amount_check CHECK (amount >= 0),
    CONSTRAINT fee_outstanding_check CHECK (outstanding_balance >= 0),
    CONSTRAINT fee_student_check CHECK (length(trim(student_name)) > 0),
    CONSTRAINT fee_status_check CHECK (status IN ('Pending', 'Paid', 'Overdue', 'Partial', ''))
);

CREATE INDEX IF NOT EXISTS idx_fee_history_user_id    ON public.fee_history(user_id);
CREATE INDEX IF NOT EXISTS idx_fee_history_student    ON public.fee_history(student_name);
CREATE INDEX IF NOT EXISTS idx_fee_history_status     ON public.fee_history(status);

ALTER TABLE public.fee_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "fee_history_select_own" ON public.fee_history
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "fee_history_insert_own" ON public.fee_history
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "fee_history_update_own" ON public.fee_history
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "fee_history_delete_own" ON public.fee_history
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 5. ATTENDANCE_RECORDS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.attendance_records (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    batch_id        INTEGER         NOT NULL DEFAULT 0,
    date            TEXT            NOT NULL DEFAULT '',
    student_name    TEXT            NOT NULL DEFAULT '',
    is_present      BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_attendance_user_id  ON public.attendance_records(user_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date     ON public.attendance_records(date);
CREATE INDEX IF NOT EXISTS idx_attendance_batch    ON public.attendance_records(batch_id);

ALTER TABLE public.attendance_records ENABLE ROW LEVEL SECURITY;

CREATE POLICY "attendance_select_own" ON public.attendance_records
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "attendance_insert_own" ON public.attendance_records
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "attendance_update_own" ON public.attendance_records
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "attendance_delete_own" ON public.attendance_records
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 6. STAFF TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS public.staff (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name                TEXT            NOT NULL DEFAULT '',
    role                TEXT            NOT NULL DEFAULT '',
    tamil_role          TEXT            NOT NULL DEFAULT '',
    assigned_batches    TEXT            NOT NULL DEFAULT '',
    responsibilities    TEXT            NOT NULL DEFAULT '',
    avatar_url          TEXT            NOT NULL DEFAULT '',
    phone               TEXT            NOT NULL DEFAULT '',
    whatsapp            TEXT            NOT NULL DEFAULT '',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT staff_name_check CHECK (length(trim(name)) > 0),
    CONSTRAINT staff_role_check CHECK (length(trim(role)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_staff_user_id ON public.staff(user_id);

ALTER TABLE public.staff ENABLE ROW LEVEL SECURITY;

CREATE POLICY "staff_select_own" ON public.staff
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "staff_insert_own" ON public.staff
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "staff_update_own" ON public.staff
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "staff_delete_own" ON public.staff
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 7. SETTINGS TABLE (one row per user)
-- ============================================================
CREATE TABLE IF NOT EXISTS public.settings (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             UUID            NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    org_name            TEXT            NOT NULL DEFAULT '',
    center_id           TEXT            NOT NULL DEFAULT '',
    contact_phone       TEXT            NOT NULL DEFAULT '',
    upi_id              TEXT            NOT NULL DEFAULT '',
    language            TEXT            NOT NULL DEFAULT 'English',
    plan_name           TEXT            NOT NULL DEFAULT '',
    renew_date          TEXT            NOT NULL DEFAULT '',
    max_students        INTEGER         NOT NULL DEFAULT 0,
    active_staff_count  INTEGER         NOT NULL DEFAULT 0,
    profile_photo_path  TEXT            NOT NULL DEFAULT '',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT settings_one_per_user UNIQUE (user_id),
    CONSTRAINT settings_max_students_check CHECK (max_students >= 0),
    CONSTRAINT settings_staff_count_check CHECK (active_staff_count >= 0),
    CONSTRAINT settings_language_check CHECK (language IN ('English', 'Tamil'))
);

CREATE INDEX IF NOT EXISTS idx_settings_user_id ON public.settings(user_id);

ALTER TABLE public.settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "settings_select_own" ON public.settings
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "settings_insert_own" ON public.settings
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "settings_update_own" ON public.settings
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "settings_delete_own" ON public.settings
    FOR DELETE USING (auth.uid() = user_id);


-- ============================================================
-- 8. AUTO-UPDATE updated_at TRIGGER
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_students_updated_at
    BEFORE UPDATE ON public.students
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_batches_updated_at
    BEFORE UPDATE ON public.batches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_leads_updated_at
    BEFORE UPDATE ON public.leads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_fee_history_updated_at
    BEFORE UPDATE ON public.fee_history
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_staff_updated_at
    BEFORE UPDATE ON public.staff
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_settings_updated_at
    BEFORE UPDATE ON public.settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();


-- ============================================================
-- 9. STORAGE BUCKET: profile-photos
-- ============================================================
-- Create the storage bucket for profile photos
INSERT INTO storage.buckets (id, name, public)
VALUES ('profile-photos', 'profile-photos', true)
ON CONFLICT (id) DO NOTHING;

-- NOTE: Supabase manages storage.objects ownership and RLS for buckets.
-- Do not alter storage.objects directly in SQL unless you own that system table.

-- ============================================================
-- 10. VERIFICATION QUERIES
-- ============================================================
-- Run these AFTER executing the script to confirm everything is set up:

-- Check all tables exist:
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public'
-- ORDER BY table_name;

-- Check RLS is enabled on all tables:
-- SELECT tablename, rowsecurity
-- FROM pg_tables WHERE schemaname = 'public';

-- Check all policies:
-- SELECT schemaname, tablename, policyname, permissive, roles, cmd
-- FROM pg_policies WHERE schemaname = 'public'
-- ORDER BY tablename, policyname;

-- Check storage bucket:
-- SELECT id, name, public FROM storage.buckets WHERE id = 'profile-photos';

-- Quick connection test (insert + select + cleanup):
-- INSERT INTO public.settings (user_id, org_name) VALUES (auth.uid(), 'Test');
-- SELECT * FROM public.settings WHERE user_id = auth.uid();
-- DELETE FROM public.settings WHERE user_id = auth.uid();
