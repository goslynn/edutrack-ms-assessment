-- DDL del schema `assessment` (evaluaciones + notas + log de auditoria inmutable).
-- Mapeo 1:1 con las entidades JPA (Hibernate `validate` en dev debe cuadrar).

CREATE SCHEMA IF NOT EXISTS assessment;

-- ── Evaluaciones (nombre, fecha, ponderacion por asignatura y periodo) ───────
CREATE TABLE assessment.evaluations (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    evaluation_date DATE         NOT NULL,
    weight          NUMERIC(5,2) NOT NULL,
    subject_id      UUID         NOT NULL,
    period          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    creator_user    UUID         NOT NULL,
    updater_user    UUID         NOT NULL,
    CONSTRAINT chk_evaluation_weight CHECK (weight > 0 AND weight <= 100)
);

-- ── Notas (escala chilena 1.0–7.0; una nota por alumno y evaluacion) ─────────
CREATE TABLE assessment.grades (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    evaluation_id UUID         NOT NULL REFERENCES assessment.evaluations(id) ON DELETE CASCADE,
    student_id    UUID         NOT NULL,
    score         NUMERIC(2,1) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    creator_user  UUID         NOT NULL,
    updater_user  UUID         NOT NULL,
    CONSTRAINT uq_grade_eval_student UNIQUE (evaluation_id, student_id),
    CONSTRAINT chk_grade_score CHECK (score >= 1.0 AND score <= 7.0)
);

-- ── Log de auditoria de notas (inmutable: append-only, sin updated_at ni FK) ──
-- No referencia grades(id): el historial sobrevive aunque la nota se elimine.
CREATE TABLE assessment.grade_audit_log (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_id      UUID         NOT NULL,
    student_id    UUID         NOT NULL,
    evaluation_id UUID         NOT NULL,
    old_value     NUMERIC(2,1),
    new_value     NUMERIC(2,1) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    creator_user  UUID         NOT NULL
);

CREATE INDEX idx_evaluations_subject_period ON assessment.evaluations(subject_id, period);
CREATE INDEX idx_grades_student             ON assessment.grades(student_id);
CREATE INDEX idx_grades_evaluation          ON assessment.grades(evaluation_id);
CREATE INDEX idx_audit_grade               ON assessment.grade_audit_log(grade_id);
CREATE INDEX idx_audit_student             ON assessment.grade_audit_log(student_id);
