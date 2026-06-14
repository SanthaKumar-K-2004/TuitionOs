# Alphax TuitionOS — Master Product & Technical Document

**Company:** Alphax Solution, Madurai, Tamil Nadu, India
**Version:** 1.0
**Status:** Pre-revenue, Phase 1 Build
**Date:** May 2026

---

## Table of Contents

1. [Product Vision & Purpose](#1-product-vision--purpose)
2. [MVP Features — Phase 1](#2-mvp-features--phase-1)
3. [Phase 2 Features](#3-phase-2-features)
4. [Phase 3 Features — Future / Advanced](#4-phase-3-features--future--advanced)
5. [Tech Stack — Full Detailed Recommendation](#5-tech-stack--full-detailed-recommendation)
6. [Database Schema Design](#6-database-schema-design)
7. [System Architecture Diagram](#7-system-architecture-diagram)
8. [Pricing Strategy](#8-pricing-strategy)
9. [Go-to-Market Technical Requirements](#9-go-to-market-technical-requirements)
10. [Competitive Analysis](#10-competitive-analysis)
11. [Risks and Mitigations](#11-risks-and-mitigations)
12. [90-Day Build and Launch Roadmap](#12-90-day-build-and-launch-roadmap)
13. [Success Metrics](#13-success-metrics)

---

## 1. Product Vision & Purpose

### One-Line Description

**TuitionOS** is a WhatsApp-first, mobile-native management platform that replaces paper registers and manual fee chasing for small Indian tuition centers — built for the way Tamil Nadu teachers actually run their classes.

### Product Name: TuitionOS

**Rationale:** "Tuition" is the universal Indian term for after-school coaching centers — every parent and teacher in Tamil Nadu uses it. "OS" signals that this is the operating system for your center, implying completeness and control. It is short, memorable, transliterable in Tamil (டியூஷன் OS), and distinct from competitor names like ClassPlus or Teachmint which feel institutional. Tagline candidate: *"உங்கள் centre, smart ஆ நடக்கும்"* (Your center, running smart).

---

### Problem Statement

A solo tuition center owner in Madurai or Tirunelveli running 80–120 students faces a daily operational nightmare that technology has not yet solved for them:

| Pain Point | Current Workaround | Real Cost |
|---|---|---|
| Tracking who paid fees and who hasn't | Paper register + mental memory | Loses ₹3,000–8,000/month in uncollected fees |
| Informing parents about attendance | WhatsApp group spam or phone calls | 45–60 minutes/day on phone |
| Sending fee reminders | Manually texting each parent | Awkward, inconsistent, often skipped |
| Admissions tracking | Notebook or WhatsApp chat history | Leads fall through cracks |
| Batch scheduling | Paper timetable on wall | Confusion when batches change |
| Generating receipts | Handwritten or skipped entirely | No proof for parents, no audit trail |

Most existing SaaS tools (ClassPlus, Teachmint) are built for large institutes, ed-tech companies, or English-medium urban schools. They require app installs on parent phones, have complex onboarding, and price tiers that assume VC-funded coaching institutes. A solo center owner in Karur or Sivakasi earning ₹40,000–80,000/month does not need a learning management system. They need a digital assistant for the 3 hours per day they currently spend on paperwork and WhatsApp.

---

### Who This Is Built For (ICP)

**Primary:** Solo or small-team tuition center owner
- Location: Tamil Nadu tier-2 and tier-3 cities (Madurai, Salem, Tirunelveli, Tiruchirappalli, Erode, Coimbatore outskirts, Karur, Dindigul, Nagercoil, Vellore)
- Student count: 50–200 students
- Fee model: Monthly flat fee (₹500–₹2,500/student/month depending on subject and standard)
- Subjects: Maths, Science, English, all standards (6th–12th), competitive exam coaching (NEET, JEE foundation)
- Technology comfort: Owns Android phone (₹8,000–15,000 range), uses WhatsApp daily, uses UPI/GPay, may use Google Sheets occasionally
- Pain level: High — actively frustrated, has thought about "some app" but doesn't know where to look
- Willingness to pay: ₹499–₹999/month if the tool saves them ≥3 hours/week and reduces fee collection friction

**Secondary (Phase 2+):** Centers with 2–3 staff teachers, exam coaching institutes (NEET/JEE), centers in other South Indian states (Karnataka, Andhra Pradesh, Kerala), Indian diaspora running coaching centers in UK, UAE, Canada, Malaysia.

---

### Why Now

1. **India's SMB digitization inflection:** Post-pandemic, even tier-2 business owners have normalized paying for digital tools (Zoho, Khatabook, OkCredit). The mental barrier to "paying for an app" is lower than it was in 2019.
2. **WhatsApp as infrastructure:** Every parent and teacher in Tamil Nadu uses WhatsApp as their primary communication channel. A tool that sends WhatsApp messages natively does not require behavior change — it fits into existing workflows.
3. **UPI normalization:** Fee collection via UPI QR is now standard. Embedding a UPI payment link in a WhatsApp reminder is a natural next step that centers want but don't have.
4. **Supabase + Vercel economics:** Zero-to-low infra cost at early scale means a small team can build and maintain this without a server budget.
5. **Competitor gap:** No one is building for the solo Tamil Nadu tuition center owner specifically. ClassPlus targets YouTube educators; Teachmint targets schools. The ₹500–999/month price point and Tamil-language UX is an unclaimed position.

---

## 2. MVP Features — Phase 1

*Build only these. Resist adding more until the first 10 paying customers confirm value.*

---

### Feature 1: Student Enrollment & Profile Management

**User Story:** As a center owner, I want to add a new student with their basic details and parent contact so that I have a single source of truth for every student enrolled in my center.

**Description:**
- Owner can add a student via a simple form: name, standard/class, subjects enrolled, parent name, parent WhatsApp number, monthly fee amount, enrollment date
- Each student gets a profile page showing: current batch assignments, fee payment history, attendance summary (last 30 days), admission date
- Students can be marked as Active or Inactive (for students who've left without deleting their history)
- Search students by name, standard, batch, or payment status
- No parent login required in Phase 1 — this is owner-facing only

**UI/UX Notes:**
- Mobile-first form with large tap targets (min 44px height on all inputs)
- Works entirely on Android Chrome — no app install required
- Student list is a scrollable card list, not a data table — each card shows name, standard, batch, and a colored badge (Paid / Pending / Overdue)
- Adding a student should take under 2 minutes end to end
- Form validation in Tamil: if a field is missing, the error message reads in both Tamil and English

**WhatsApp Integration:** None in enrollment itself, but parent's WhatsApp number captured here is used for all future messaging features.

---

### Feature 2: Batch / Subject / Time Slot Management

**User Story:** As a center owner, I want to create batches with specific subjects, days, and time slots and assign students to them so that I can track which students attend which sessions.

**Description:**
- Owner creates a batch: name (e.g., "10th Maths — Morning"), subject, days of week (checkboxes), start time, end time, room/location (optional), teacher name (optional, free text in Phase 1)
- Students are assigned to one or more batches
- Batch list view shows: batch name, student count, schedule
- A simple weekly timetable view shows all batches for the week
- Batch can be paused (e.g., during exams) or closed

**UI/UX Notes:**
- Day selection using horizontal pill buttons (Mon Tue Wed Thu Fri Sat Sun) — easy to tap
- Time picker using native HTML time input — mobile-compatible
- Timetable view is a simple vertical list grouped by day, not a complex calendar grid
- Owner can assign multiple students to a batch from the student list using checkboxes

---

### Feature 3: Fee Tracking

**User Story:** As a center owner, I want to see which students have paid their monthly fees, which are pending, and which are overdue so that I can follow up immediately without keeping a mental tally.

**Description:**
- Each month, the system auto-generates fee records for all active students based on their enrolled fee amount
- Owner sees three tabs: Paid / Pending / Overdue (overdue = pending from a previous month)
- Marking a fee as paid: tap the student name → enter amount received, payment mode (Cash / UPI / Bank), optional note → confirm
- Fee summary card on dashboard: total expected this month, total collected, total pending, total overdue
- Simple fee history: list of all payments for a student with date, amount, mode
- Partial payment support: owner can mark partial payment and remaining balance is carried forward

**UI/UX Notes:**
- Fee status uses color coding: Green (Paid), Amber (Pending this month), Red (Overdue from prior month)
- One-tap mark as paid from the pending list — single-action confirmation
- Month navigation: left/right arrows to navigate between months
- No complex invoicing in Phase 1 — just record keeping

---

### Feature 4: One-Tap WhatsApp Reminder to Pending Parents

**User Story:** As a center owner, I want to send a fee reminder to a pending parent's WhatsApp with one tap so that I don't have to type the same message manually every month and don't feel awkward chasing fees.

**Description:**
- On any pending or overdue fee record, a "Send Reminder" button opens WhatsApp Web / WhatsApp app (via `wa.me` deep link) with a pre-filled message
- Message is pre-populated with: student name, amount due, month, center name
- Owner sees the message before sending — they tap Send in WhatsApp (not automated, avoids WhatsApp API costs in Phase 1)
- Two language variants: Tamil and English — owner selects preferred language in settings

**Example WhatsApp Message Templates:**

*Tamil:*
```
வணக்கம் [Parent Name] அவர்களே,

[Student Name] அவர்களின் [Month] மாத டியூஷன் கட்டணம் ₹[Amount] இன்னும் நிலுவையில் உள்ளது.

தயவுசெய்து இந்த மாதம் செலுத்தவும்.

நன்றி,
[Center Name]
```

*English:*
```
Hello [Parent Name],

This is a reminder that [Student Name]'s tuition fee of ₹[Amount] for [Month] is pending.

Kindly make the payment at your earliest convenience.

Thank you,
[Center Name]
```

**UI/UX Notes:**
- The "Send Reminder" button opens WhatsApp directly — the owner taps Send in WhatsApp and returns to the app
- Deep link format: `https://wa.me/91XXXXXXXXXX?text=URL_ENCODED_MESSAGE`
- Works on both mobile and desktop (opens WhatsApp Web on desktop)
- No WhatsApp API account needed in Phase 1 — zero cost

---

### Feature 5: Attendance Marking Per Batch

**User Story:** As a center owner, I want to mark attendance for a batch in under 2 minutes so that I have a record of who attended each session without breaking the flow of class.

**Description:**
- Owner opens a batch → taps "Mark Attendance" → sees a list of all students in that batch
- Each student row has two large buttons: Present (green checkmark) / Absent (red X)
- Default state is Present (since most students attend) — owner only needs to tap the absent ones
- After marking, owner taps "Submit Attendance"
- System logs: batch, date, teacher (if assigned), list of present/absent students
- Owner can edit attendance for today's session up to midnight of the same day

**UI/UX Notes:**
- Student list sorted alphabetically — 30 students should be visible in 2 scrolls
- Large tap targets (min 52px) — designed for use while standing, phone in one hand
- Confirmation screen shows count: "28 Present, 2 Absent — Saved"
- Previous attendance visible as a calendar heatmap on the batch detail page (green = session happened, grey = no session)

---

### Feature 6: Auto WhatsApp Message When Student Is Absent

**User Story:** As a center owner, I want parents to be automatically notified when their child is marked absent so that parents feel informed and I spend zero time making phone calls about attendance.

**Description:**
- When attendance is submitted and a student is marked absent, the app generates a pre-filled WhatsApp message for each absent student's parent
- Owner sees a list: "2 students absent today — notify parents?" with one-tap confirmation
- Tapping "Notify" opens WhatsApp for each parent in sequence (one at a time) with the pre-filled message
- Message includes: student name, date, batch/subject, center name

**Example Templates:**

*Tamil:*
```
வணக்கம் [Parent Name] அவர்களே,

[Date] அன்று [Student Name] [Batch Name] வகுப்பிற்கு வரவில்லை என்பதை தெரிவிக்கிறோம்.

ஏதாவது காரணம் இருந்தால் எங்களுக்கு தெரியப்படுத்துங்கள்.

[Center Name]
```

*English:*
```
Hello [Parent Name],

We would like to inform you that [Student Name] was absent for [Batch Name] on [Date].

Please let us know if there is any reason.

[Center Name]
```

**UI/UX Notes:**
- In Phase 1, messages are sent via wa.me links — manual send by owner, not automated
- The sequence opens WhatsApp for parent 1, owner sends, returns to app, taps next parent
- Owner can skip individual parents if they've already spoken to them
- Phase 2 will automate this via WhatsApp Business API

---

### Feature 7: Owner Dashboard — Today's Summary

**User Story:** As a center owner, I want to see a quick summary of today's status when I open the app so that I know what needs my attention without digging through menus.

**Description:**
The dashboard displays:
- **Today's Batches:** list of batches scheduled today, each showing attendance status (Marked / Not Marked Yet)
- **Fee Summary (This Month):** Collected: ₹X | Pending: ₹Y | Overdue: ₹Z
- **Pending Reminders:** Count of parents who haven't received a fee reminder yet this month
- **Recent Absences:** Students absent in the last 3 days with quick-notify button
- **Quick Actions:** [+ Add Student] [Mark Attendance] [Record Payment]

**UI/UX Notes:**
- Dashboard is the home screen — no onboarding friction after first setup
- Cards use large numbers (₹ amounts prominent) and clear status labels
- No charts or graphs in Phase 1 — data density over visual complexity
- Pull-to-refresh updates all counts in real time from Supabase

---

### Feature 8: Basic Inquiry / Admission Lead Log

**User Story:** As a center owner, I want to log when someone inquires about admission so that I can follow up and don't lose potential students.

**Description:**
- Simple form: inquirer name, phone number, student name, standard/class interested in, inquiry date, source (Walk-in / Phone / WhatsApp / Referral), notes
- Lead status: New → Followed Up → Admitted / Not Admitted
- Lead list view with status badges
- One-tap WhatsApp follow-up message to inquiry contact

**Example Follow-Up Message:**
```
Hello [Name], thank you for your interest in [Center Name]. We have batches available for [Standard] starting [Date]. Please visit us at [Address] or call us at [Phone]. We would love to have [Student Name] join us!
```

**UI/UX Notes:**
- Inquiry log is a secondary screen — accessible from a bottom nav tab
- No CRM complexity — just a list with status and a call/WhatsApp button
- Admitted inquiries auto-suggest creating a student profile

---

## 3. Phase 2 Features

*Build after first 20 paying customers confirm product-market fit.*

### 3.1 Monthly Fee Receipt PDF Generation + WhatsApp Delivery
- System generates a clean PDF receipt on fee payment: center logo, student name, fee amount, month, mode of payment, receipt number
- PDF stored in Supabase Storage
- WhatsApp message with PDF link or direct file share sent to parent automatically (via WhatsApp Business API)
- Receipt template: branded with center name, GST-optional, legally usable

### 3.2 Progress Report / Exam Marks Entry and Report Card Generation
- Owner enters marks per student per exam/test per subject
- System generates a PDF report card: student name, test name, subject-wise marks, total, percentage, grade, teacher remarks
- Report card delivered to parent via WhatsApp
- Owner sees a class-wise performance summary: average marks, top performers, students below pass mark

### 3.3 Multi-Branch / Multi-Center Support
- A single owner account can manage 2–3 centers
- Data is partitioned by center (organization)
- Owner switches between centers from the top navigation
- Dashboard shows combined or per-center view toggle
- Billing is per center (separate subscription per organization)

### 3.4 Staff Management
- Owner can add staff/teachers to the organization with limited role access
- Staff role: can mark attendance and view their assigned batches only — cannot see fee data or other teachers' batches
- Owner role: full access to everything
- Simple invite by WhatsApp link (no email required)

### 3.5 Bulk WhatsApp Broadcast
- Owner selects a group (all parents / parents of specific batch / overdue parents) and sends a message to all
- Powered by WhatsApp Business API (Interakt or WATI free tier)
- Use cases: exam schedule notice, holiday notification, batch change announcement, monthly fee reminder blast
- Message templates must be pre-approved by Meta (owner sets these up once)
- Broadcast log: delivered / read counts

### 3.6 Student Performance Analytics
- Per-student attendance percentage (last 30/60/90 days)
- Fee payment consistency score (paid on time for last N months)
- Exam performance trend (improving / declining / stable)
- Center-level analytics: overall attendance rate, collection efficiency percentage

### 3.7 Online Fee Payment Link via UPI/Razorpay
- Fee reminder WhatsApp message includes a Razorpay payment link
- Parent taps link in WhatsApp → Razorpay checkout → pays via UPI/card/netbanking
- Payment confirmation auto-updates fee record in TuitionOS
- Owner gets WhatsApp notification on payment
- Razorpay standard fee: 2% + GST on transactions

### 3.8 Parent-Facing View (No App Install)
- Each student gets a unique, shareable link: `tuitionos.app/student/[token]`
- Parent opens in mobile browser: sees attendance history (last 30 days), fee receipts, exam marks
- Read-only — no edits, no login required (token-based access)
- Works on any phone — no WhatsApp Business API dependency for view itself

---

## 4. Phase 3 Features — Future / Advanced

*12–24 months post-launch. Build only after reaching ₹5–7 lakhs MRR.*

### 4.1 AI-Powered Fee Defaulter Prediction
- ML model trained on payment history, attendance patterns, and inquiry source to predict which students are at risk of fee default or dropping out
- Owner gets a weekly "At Risk" list with suggested action (call / visit / discount offer)
- Uses Supabase data — model runs as a scheduled edge function or external Python microservice

### 4.2 Automated Lead Follow-Up Sequence
- Inquiry leads who didn't join get a structured follow-up sequence over 7–14 days
- Day 1: Welcome + batch info message, Day 3: Offer free demo class, Day 7: Fee structure, Day 14: Final nudge
- Managed via WhatsApp Business API automated flows
- Owner can pause or stop sequences per lead

### 4.3 Tamil-Language Voice Bot for Parent Queries
- Parent sends a voice message to the center's WhatsApp number asking about attendance or fees
- Bot transcribes (Whisper or Google Speech-to-Text, Tamil support), retrieves data, responds via text (Phase 1 of voice) or voice message (Phase 2)
- Example queries: "என் பிள்ளை இந்த மாசம் எத்தனை நாள் வந்தான்?" (How many days did my child attend this month?)
- Requires WhatsApp Business API + webhook integration

### 4.4 Google Calendar Integration
- Batches and exam schedules sync to owner's Google Calendar
- Batch changes propagate to calendar automatically
- Owner can block off holiday dates in Calendar and it reflects in TuitionOS

### 4.5 Referral Tracking
- Each student profile can link to a "referred by" parent
- Referral report: which parents have referred students, how many, conversion rate
- Referral incentive management: track if owner gives a discount or reward for referrals

### 4.6 Annual Performance Trend Reports
- Year-end PDF report per student: attendance for the year, fee payment record, exam marks trend, teacher comments
- Center-level annual report: total students, revenue collected, retention rate
- Useful as a marketing asset for parents during re-enrollment

### 4.7 International Version
- Multi-currency support (GBP, CAD, AED, MYR)
- English-only interface (no Tamil requirement for diaspora markets)
- Timezone-aware scheduling
- Stripe as payment gateway (replacing Razorpay)
- Pricing in USD/local currency equivalent
- GDPR-compliant data handling (for UK market)
- WhatsApp Business API works internationally with the same integration

### 4.8 Platform Expansion Hooks
- Shared organization/authentication model that can connect to future Alphax products (Alphax Lab SaaS, future verticals)
- Single Alphax login for owners managing multiple product subscriptions
- API-first design allows third-party integrations in the future

---

## 5. Tech Stack — Full Detailed Recommendation

### 5.1 Frontend

**Recommendation: Next.js 14+ (App Router) + Tailwind CSS + shadcn/ui**

| Choice | Rationale |
|---|---|
| Next.js (App Router) | Server components reduce JS bundle size — critical for low-end Android devices on 4G. Already works with Vercel deployment. Strong SEO for the marketing site. |
| Tailwind CSS | Utility-first, no CSS file bloat, consistent spacing system, mobile-first responsive by default. |
| shadcn/ui | Copy-paste component library built on Radix UI primitives — accessible, unstyled by default, composable. Avoids heavy UI library lock-in. |
| React Query (TanStack Query) | Client-side data fetching, caching, and sync with Supabase. Better UX than useEffect-fetch patterns. |
| Zustand | Lightweight global state for UI state (selected center, filters). Avoid Redux for a team this size. |

**Mobile-First Constraints:**
- Target: Android Chrome on ₹8,000–15,000 phones (Chrome 100+, 3G/4G)
- Max First Contentful Paint target: < 2.5 seconds on 4G
- Use `next/image` for all images — automatic WebP conversion and lazy loading
- Avoid heavy animation libraries — use CSS transitions only
- Use bottom navigation bar (not hamburger menu) for primary nav on mobile
- Font: Inter (Latin) + Noto Sans Tamil (for Tamil text) — both available via Google Fonts
- Touch target minimum: 44px height on all interactive elements
- Avoid hover-only interactions — everything must work on touch

**Progressive Web App (PWA):**
- Implement PWA manifest so owners can "Add to Home Screen" — no app store approval needed
- Service worker for offline attendance marking (sync when online)
- Push notifications via Web Push API for later phases

---

### 5.2 Backend / API

**Recommendation: Supabase Edge Functions (Deno) + Supabase Database Functions (PostgreSQL)**

| Layer | Tool | Rationale |
|---|---|---|
| Primary API | Supabase auto-generated REST API + PostgREST | Zero boilerplate CRUD. Row-level security handles multi-tenant data isolation. |
| Business logic | Supabase Edge Functions (Deno/TypeScript) | Serverless, deploys with Supabase CLI. Free tier: 500k invocations/month. Use for: WhatsApp link generation, PDF generation triggers, fee auto-generation cron. |
| Scheduled jobs | Supabase pg_cron or Edge Function cron | Auto-generate monthly fee records, send bulk reminder queues. |
| Real-time | Supabase Realtime (WebSocket) | Dashboard live updates — fee marked as paid reflects instantly. Free tier supports this. |

**REST vs GraphQL decision:** Use REST (PostgREST via Supabase) in Phase 1. It is auto-generated from the database schema, requires no resolver code, and works with the Supabase JS client out of the box. Move to a custom GraphQL layer only if query complexity demands it (Phase 3+).

**API Design Principles:**
- All routes protected by Supabase Auth JWT
- Row-Level Security (RLS) on every table — `organization_id` scoped
- Supabase JS client (`@supabase/supabase-js`) handles auth + queries on the frontend
- No Express.js server — keep infra serverless throughout Phase 1 and 2

---

### 5.3 Database

**Supabase (PostgreSQL 15)**

See Section 6 for full schema. Key design decisions:

- **Multi-tenancy model:** `organization_id` foreign key on every table — Row Level Security enforces tenant isolation at the database layer
- **Soft deletes:** Use `deleted_at` timestamp instead of hard DELETE — preserves fee history and audit trail
- **Fee generation:** A cron job runs on the 1st of every month using `pg_cron` — it creates `fee_records` rows for all active students
- **WhatsApp message log:** Every message sent (even via wa.me) is logged with status, recipient, template used, and timestamp
- **Supabase Storage:** Used for PDFs (receipts, report cards) in Phase 2 — organized in buckets per organization

**Free Tier Limits (Supabase Free):**
- 500MB database storage
- 1GB file storage
- 50,000 monthly active users
- Sufficient for first 50–100 paying centers

Upgrade to Supabase Pro ($25/month) after first 10 paying customers — adds 8GB database, 100GB storage, daily backups, and no pausing.

---

### 5.4 Authentication

**Supabase Auth**

- **Center owners:** Email + password sign-up (Phase 1). Add phone OTP (SMS via Twilio or MSG91) in Phase 2 — more natural for Indian users who don't always use email.
- **Staff (Phase 2):** Invite-based — owner sends a magic link to the staff's WhatsApp number (wraps a Supabase invite email link in a WhatsApp message). Staff sets a password on first visit.
- **Multi-center owners:** One Supabase user account can be linked to multiple `organizations` via a `user_organizations` junction table. After login, if a user belongs to multiple orgs, they see an org-selector screen before the dashboard.
- **Session management:** Supabase handles JWT refresh automatically. Sessions persist for 7 days on mobile browsers.
- **Password reset:** Email-based reset link (Phase 1). WhatsApp OTP reset in Phase 2.

**Security:**
- Enable Supabase Row Level Security on ALL tables from day one
- Never expose service role key on the frontend — all frontend queries use the anon key + RLS
- JWT claims include `organization_id` — validated server-side via RLS policies

---

### 5.5 WhatsApp Integration — Stage-by-Stage

**Phase 1 (Zero Cost) — wa.me Deep Links**
- No WhatsApp Business API account needed
- Deep link format: `https://wa.me/91[phone]?text=[URL-encoded message]`
- Implementation: JavaScript function generates the URL from template + student data, opens in new tab
- Limitation: owner must manually tap Send in WhatsApp — not automated
- Cost: Free
- Suitable for: first 0–20 paying customers

```javascript
// Example implementation
function generateWhatsAppLink(phone: string, message: string): string {
  const cleaned = phone.replace(/\D/g, '');
  const withCountryCode = cleaned.startsWith('91') ? cleaned : `91${cleaned}`;
  const encoded = encodeURIComponent(message);
  return `https://wa.me/${withCountryCode}?text=${encoded}`;
}
```

**Phase 2 (First Revenue: ₹20,000+ MRR) — WhatsApp Business API via Interakt or AiSensy**
- **Recommended provider: Interakt** (Indian provider, INR pricing, good support, free tier: 1,000 conversations/month)
- Alternative: **AiSensy** (free plan with 1,000 free conversations/month, good templating UI)
- Enables: automated sending without owner manually tapping Send
- Setup: Register a WhatsApp Business Account (WABA) for TuitionOS, submit message templates for Meta approval (takes 24–72 hours)
- Integration: Interakt/AiSensy provide an HTTP API — call from Supabase Edge Function
- Cost at scale: ~₹0.30–₹0.75 per conversation (Meta's conversation-based pricing)
- Use cases: automated absence notification, fee reminder automation, bulk broadcasts

**Phase 3 (Scale) — Direct WABA Integration**
- Apply directly to Meta's WhatsApp Business API — removes third-party provider middleman
- Higher message throughput, lower per-message cost at scale
- Requires business verification with Meta
- Enables: interactive buttons in messages, quick reply flows, media messages (PDFs natively)

---

### 5.6 File / PDF Generation

**Phase 2: Puppeteer on Vercel Serverless Function or React-PDF**

| Option | Pros | Cons |
|---|---|---|
| `@react-pdf/renderer` | Runs in browser, no server cost, TypeScript-friendly | Limited CSS support, complex layouts tricky |
| Puppeteer on serverless | Full HTML/CSS control, pixel-perfect | Cold start latency, requires Vercel Pro or separate function host |
| Docmosis / PDFShift (SaaS) | Simple HTML-to-PDF API | External dependency, per-page cost |

**Recommendation for Phase 2:** Use `@react-pdf/renderer` for receipts (simple layout). Use a Supabase Edge Function with `jsPDF` for report cards. Store PDFs in Supabase Storage at `organizations/{org_id}/receipts/{receipt_id}.pdf`. Generate a signed URL (expires in 7 days) and embed in WhatsApp message.

---

### 5.7 Hosting

**Vercel (already connected)**

| Component | Hosting | Notes |
|---|---|---|
| Marketing site | Vercel (marketing repo) | Static Next.js pages — auto CDN, fast worldwide |
| App (TuitionOS) | Vercel (app repo) | Next.js App Router — serverless by default |
| Database | Supabase Cloud | Not on Vercel — Supabase hosts PostgreSQL |
| File storage | Supabase Storage | Backed by S3-compatible object storage |
| Edge Functions | Supabase Edge Functions | Deno runtime, not Vercel functions |

**Environment Setup:**
- `main` branch → staging deployment (staging Supabase project + staging env vars)
- `prod` branch → production deployment (production Supabase project + production env vars)
- Never share Supabase service role key between staging and production

---

### 5.8 Payments

**Phase 1 — UPI / GPay (Manual)**
- Owner collects fees in person or via UPI QR
- Records payment in TuitionOS manually: amount, mode (Cash/UPI), date
- No payment gateway integration — zero cost
- Add a display of the center's UPI QR in the app for easy sharing

**Phase 2 — Razorpay (Indian payments)**
- Razorpay Payment Links embedded in WhatsApp fee reminders
- Owner creates a Razorpay account (free registration, KYC required)
- TuitionOS generates a Razorpay Payment Link via API, embeds in WhatsApp template
- Fee record auto-updates via Razorpay webhook → Supabase Edge Function
- Transaction fee: 2% + 18% GST
- Minimum: ₹0 monthly fee on standard plan

**Phase 3 — Stripe (International)**
- For diaspora market (UK, UAE, Canada, Malaysia)
- Stripe pricing: 1.5% + ₹2 for Indian cards, 2.9% + $0.30 for international
- Requires a registered business entity (Alphax Solution with proper incorporation)
- Stripe Checkout — no PCI compliance burden

---

### 5.9 Analytics

**What to Track from Day One:**

| Event | Tool | Why |
|---|---|---|
| Page views, user journeys | PostHog (free tier: 1M events/month) | Understand where users drop off in onboarding |
| Feature usage (which features used most/least) | PostHog custom events | Prioritize Phase 2 build based on actual usage |
| Error tracking | Sentry (free tier: 5k errors/month) | Catch bugs before they cause churn |
| Business metrics (MRR, churn, LTV) | Manual spreadsheet or Metabase on Supabase | Track revenue without buying a BI tool |
| Marketing funnel (ad click → trial → paid) | Google Analytics 4 (free) | Understand acquisition efficiency |
| WhatsApp message delivery (Phase 2) | Interakt/AiSensy dashboard | Track reminder effectiveness |

**Implement from Day 1:**
```typescript
// PostHog event tracking — add to key actions
posthog.capture('fee_marked_paid', {
  organization_id: org.id,
  amount: fee.amount,
  payment_mode: 'upi',
  days_overdue: fee.daysOverdue,
});

posthog.capture('whatsapp_reminder_sent', {
  type: 'fee_reminder',
  language: 'tamil',
});
```

---

### 5.10 CI/CD

**GitHub Actions (already connected)**

```yaml
# .github/workflows/deploy.yml
name: Deploy to Vercel

on:
  push:
    branches: [main, prod]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm ci
      - run: npm run build
      - run: npm run lint
      - run: npm run type-check
      - uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: ${{ github.ref == 'refs/heads/prod' && '--prod' || '' }}
```

**Branch strategy:**
- `main` → staging (auto-deploy on push)
- `prod` → production (manual merge after QA on staging)
- Feature branches → preview deployments (Vercel preview URLs per PR)

**Supabase migrations:**
```bash
# Local development
supabase db diff --file migration_name
supabase db push  # apply to staging
# After QA: apply to production manually or via CI
```

---

## 6. Database Schema Design

All tables include `created_at`, `updated_at` (auto via triggers), and `deleted_at` (soft delete) unless noted.

### 6.1 organizations

```sql
CREATE TABLE organizations (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name            TEXT NOT NULL,                          -- "Sri Vinayaga Tuition Center"
  slug            TEXT UNIQUE NOT NULL,                   -- "sri-vinayaga-madurai" (for URLs)
  owner_user_id   UUID REFERENCES auth.users(id),
  phone           TEXT,                                   -- Center contact number
  whatsapp_number TEXT,                                   -- May differ from phone
  address         TEXT,
  city            TEXT,
  state           TEXT DEFAULT 'Tamil Nadu',
  pin_code        TEXT,
  upi_id          TEXT,                                   -- Center's UPI ID for payment QR
  logo_url        TEXT,                                   -- Supabase Storage URL
  language_pref   TEXT DEFAULT 'tamil',                   -- 'tamil' | 'english' | 'both'
  timezone        TEXT DEFAULT 'Asia/Kolkata',
  plan            TEXT DEFAULT 'trial',                   -- 'trial' | 'starter' | 'growth' | 'pro'
  plan_expires_at TIMESTAMPTZ,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ DEFAULT NOW(),
  deleted_at      TIMESTAMPTZ
);

-- RLS: owners can only see their own organization
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
CREATE POLICY "org_owner_access" ON organizations
  USING (owner_user_id = auth.uid());
```

### 6.2 users (profiles extending Supabase auth.users)

```sql
CREATE TABLE user_profiles (
  id              UUID PRIMARY KEY REFERENCES auth.users(id),
  full_name       TEXT NOT NULL,
  phone           TEXT,
  role            TEXT DEFAULT 'owner',                   -- 'owner' | 'staff'
  avatar_url      TEXT,
  onboarding_done BOOLEAN DEFAULT FALSE,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Junction table for multi-center owners
CREATE TABLE user_organizations (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
  role            TEXT DEFAULT 'owner',                   -- 'owner' | 'staff' | 'teacher'
  is_active       BOOLEAN DEFAULT TRUE,
  joined_at       TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, organization_id)
);
```

### 6.3 students

```sql
CREATE TABLE students (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id     UUID REFERENCES organizations(id) ON DELETE CASCADE,
  full_name           TEXT NOT NULL,
  standard            TEXT NOT NULL,                      -- "10th", "12th", "8th" etc.
  parent_name         TEXT NOT NULL,
  parent_phone        TEXT NOT NULL,                      -- Primary contact (WhatsApp)
  parent_phone_2      TEXT,                               -- Optional secondary contact
  date_of_birth       DATE,
  school_name         TEXT,
  address             TEXT,
  enrollment_date     DATE DEFAULT CURRENT_DATE,
  monthly_fee         NUMERIC(10, 2) NOT NULL,            -- Fee amount in INR
  fee_due_day         INTEGER DEFAULT 5,                  -- Day of month fee is due (1-28)
  status              TEXT DEFAULT 'active',              -- 'active' | 'inactive' | 'left'
  left_date           DATE,
  left_reason         TEXT,
  notes               TEXT,
  photo_url           TEXT,
  created_at          TIMESTAMPTZ DEFAULT NOW(),
  updated_at          TIMESTAMPTZ DEFAULT NOW(),
  deleted_at          TIMESTAMPTZ
);

CREATE INDEX idx_students_org ON students(organization_id);
CREATE INDEX idx_students_status ON students(organization_id, status);

ALTER TABLE students ENABLE ROW LEVEL SECURITY;
CREATE POLICY "students_org_access" ON students
  USING (organization_id IN (
    SELECT organization_id FROM user_organizations WHERE user_id = auth.uid()
  ));
```

### 6.4 batches

```sql
CREATE TABLE batches (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
  name            TEXT NOT NULL,                          -- "10th Maths Morning"
  subject         TEXT NOT NULL,
  standard        TEXT,                                   -- Optional filter by standard
  days_of_week    INTEGER[] NOT NULL,                     -- [1,3,5] = Mon,Wed,Fri (0=Sun,6=Sat)
  start_time      TIME NOT NULL,                          -- "09:00:00"
  end_time        TIME NOT NULL,                          -- "10:30:00"
  room            TEXT,
  teacher_name    TEXT,                                   -- Free text in Phase 1
  teacher_user_id UUID REFERENCES auth.users(id),        -- Phase 2: linked staff account
  max_students    INTEGER DEFAULT 40,
  status          TEXT DEFAULT 'active',                  -- 'active' | 'paused' | 'closed'
  start_date      DATE DEFAULT CURRENT_DATE,
  end_date        DATE,
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ DEFAULT NOW(),
  deleted_at      TIMESTAMPTZ
);

-- Many-to-many: students to batches
CREATE TABLE student_batches (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id      UUID REFERENCES students(id) ON DELETE CASCADE,
  batch_id        UUID REFERENCES batches(id) ON DELETE CASCADE,
  joined_date     DATE DEFAULT CURRENT_DATE,
  left_date       DATE,
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(student_id, batch_id)
);
```

### 6.5 attendance

```sql
CREATE TABLE attendance_sessions (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID REFERENCES organizations(id),
  batch_id        UUID REFERENCES batches(id) ON DELETE CASCADE,
  session_date    DATE NOT NULL DEFAULT CURRENT_DATE,
  marked_by       UUID REFERENCES auth.users(id),
  marked_at       TIMESTAMPTZ DEFAULT NOW(),
  notes           TEXT,
  UNIQUE(batch_id, session_date)
);

CREATE TABLE attendance_records (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id      UUID REFERENCES attendance_sessions(id) ON DELETE CASCADE,
  student_id      UUID REFERENCES students(id) ON DELETE CASCADE,
  status          TEXT NOT NULL DEFAULT 'present',        -- 'present' | 'absent' | 'late'
  notification_sent BOOLEAN DEFAULT FALSE,
  notification_sent_at TIMESTAMPTZ,
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(session_id, student_id)
);

CREATE INDEX idx_attendance_batch_date ON attendance_sessions(batch_id, session_date);
CREATE INDEX idx_attendance_student ON attendance_records(student_id);
```

### 6.6 fee_records

```sql
CREATE TABLE fee_records (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id     UUID REFERENCES organizations(id),
  student_id          UUID REFERENCES students(id) ON DELETE CASCADE,
  month               INTEGER NOT NULL,                   -- 1-12
  year                INTEGER NOT NULL,
  amount_due          NUMERIC(10, 2) NOT NULL,
  amount_paid         NUMERIC(10, 2) DEFAULT 0,
  balance             NUMERIC(10, 2) GENERATED ALWAYS AS (amount_due - amount_paid) STORED,
  status              TEXT DEFAULT 'pending',             -- 'pending' | 'partial' | 'paid' | 'waived'
  due_date            DATE NOT NULL,
  payment_date        DATE,
  payment_mode        TEXT,                               -- 'cash' | 'upi' | 'bank_transfer' | 'online'
  transaction_ref     TEXT,                               -- UPI transaction ID or Razorpay payment ID
  receipt_number      TEXT UNIQUE,                        -- Auto-generated: ORG-YEAR-SEQNO
  receipt_url         TEXT,                               -- Supabase Storage URL (Phase 2)
  reminder_count      INTEGER DEFAULT 0,
  last_reminder_at    TIMESTAMPTZ,
  notes               TEXT,
  recorded_by         UUID REFERENCES auth.users(id),
  created_at          TIMESTAMPTZ DEFAULT NOW(),
  updated_at          TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(student_id, month, year)
);

CREATE INDEX idx_fee_records_org_month ON fee_records(organization_id, year, month);
CREATE INDEX idx_fee_records_student ON fee_records(student_id);
CREATE INDEX idx_fee_records_status ON fee_records(organization_id, status);
```

### 6.7 inquiries / leads

```sql
CREATE TABLE inquiries (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID REFERENCES organizations(id),
  inquirer_name   TEXT NOT NULL,
  phone           TEXT NOT NULL,
  student_name    TEXT,
  standard        TEXT,
  subjects        TEXT[],
  inquiry_date    DATE DEFAULT CURRENT_DATE,
  source          TEXT,                                   -- 'walk_in' | 'phone' | 'whatsapp' | 'referral' | 'social'
  referred_by_student_id UUID REFERENCES students(id),   -- Phase 3: referral tracking
  status          TEXT DEFAULT 'new',                     -- 'new' | 'contacted' | 'demo_given' | 'admitted' | 'lost'
  follow_up_date  DATE,
  notes           TEXT,
  converted_student_id UUID REFERENCES students(id),     -- Set when lead is admitted
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  updated_at      TIMESTAMPTZ DEFAULT NOW(),
  deleted_at      TIMESTAMPTZ
);
```

### 6.8 whatsapp_messages

```sql
CREATE TABLE whatsapp_messages (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id   UUID REFERENCES organizations(id),
  recipient_phone   TEXT NOT NULL,
  recipient_name    TEXT,
  student_id        UUID REFERENCES students(id),
  message_type      TEXT NOT NULL,                        -- 'fee_reminder' | 'absence_notification' | 'bulk_broadcast' | 'follow_up' | 'custom'
  message_body      TEXT NOT NULL,
  language          TEXT DEFAULT 'tamil',                 -- 'tamil' | 'english'
  channel           TEXT DEFAULT 'manual_link',           -- 'manual_link' | 'interakt_api' | 'wati_api'
  status            TEXT DEFAULT 'pending',               -- 'pending' | 'sent' | 'delivered' | 'read' | 'failed'
  provider_message_id TEXT,                               -- External API message ID
  sent_at           TIMESTAMPTZ,
  delivered_at      TIMESTAMPTZ,
  read_at           TIMESTAMPTZ,
  sent_by           UUID REFERENCES auth.users(id),
  meta              JSONB,                                -- Extra provider-specific data
  created_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_whatsapp_org ON whatsapp_messages(organization_id);
CREATE INDEX idx_whatsapp_student ON whatsapp_messages(student_id);
```

### 6.9 notifications (in-app)

```sql
CREATE TABLE notifications (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id UUID REFERENCES organizations(id),
  user_id         UUID REFERENCES auth.users(id),
  type            TEXT NOT NULL,                          -- 'fee_payment_received' | 'new_inquiry' | 'attendance_pending' etc.
  title           TEXT NOT NULL,
  body            TEXT,
  is_read         BOOLEAN DEFAULT FALSE,
  action_url      TEXT,                                   -- Deep link within the app
  meta            JSONB,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);
```

### 6.10 Useful Database Functions

```sql
-- Auto-generate monthly fee records for all active students
-- Called by pg_cron on the 1st of every month
CREATE OR REPLACE FUNCTION generate_monthly_fees(
  p_month INTEGER,
  p_year INTEGER
) RETURNS void AS $$
BEGIN
  INSERT INTO fee_records (organization_id, student_id, month, year, amount_due, due_date, receipt_number)
  SELECT
    s.organization_id,
    s.id,
    p_month,
    p_year,
    s.monthly_fee,
    DATE(p_year || '-' || LPAD(p_month::TEXT, 2, '0') || '-' || LPAD(s.fee_due_day::TEXT, 2, '0')),
    'REC-' || p_year || '-' || LPAD(p_month::TEXT, 2, '0') || '-' || LPAD(nextval('receipt_seq')::TEXT, 5, '0')
  FROM students s
  WHERE s.status = 'active'
    AND s.deleted_at IS NULL
  ON CONFLICT (student_id, month, year) DO NOTHING;
END;
$$ LANGUAGE plpgsql;

-- Schedule: first day of every month at 6 AM IST
SELECT cron.schedule('generate-monthly-fees', '30 0 1 * *', $$
  SELECT generate_monthly_fees(EXTRACT(MONTH FROM NOW())::INTEGER, EXTRACT(YEAR FROM NOW())::INTEGER);
$$);
```

---

## 7. System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      CENTER OWNER'S PHONE                        │
│                  (Android Chrome, Mobile PWA)                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     VERCEL (CDN + Edge)                          │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │          Next.js App Router (TuitionOS Web App)         │   │
│   │   - React Server Components (fast initial load)         │   │
│   │   - Client components (dashboard, attendance, fees)     │   │
│   │   - Supabase JS Client (auth + realtime + queries)      │   │
│   └──────────────────────────┬──────────────────────────────┘   │
└─────────────────────────────┼───────────────────────────────────┘
                              │ Supabase JS SDK (HTTPS/WSS)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SUPABASE CLOUD                                 │
│                                                                  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────────┐  │
│  │  Auth       │  │  PostgREST   │  │  Realtime (WebSocket)  │  │
│  │  (JWT)      │  │  (REST API)  │  │  (dashboard live sync) │  │
│  └──────┬──────┘  └──────┬───────┘  └────────────┬───────────┘  │
│         │                │                        │              │
│  ┌──────▼────────────────▼────────────────────────▼───────────┐  │
│  │                  PostgreSQL 15 Database                      │  │
│  │  (organizations, students, batches, attendance,             │  │
│  │   fee_records, inquiries, whatsapp_messages, RLS)           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────┐    ┌────────────────────────────────┐  │
│  │  Edge Functions      │    │  Supabase Storage              │  │
│  │  (Deno TypeScript)   │    │  (PDFs, logos, images)         │  │
│  │  - WhatsApp URL gen  │    │  Phase 2+                      │  │
│  │  - Fee auto-generate │    └────────────────────────────────┘  │
│  │  - PDF trigger       │                                        │
│  │  - Webhook receiver  │                                        │
│  └──────────┬───────────┘                                        │
└─────────────┼───────────────────────────────────────────────────┘
              │
              ├──────────────────────────────────────────────────────
              │        WHATSAPP FLOW
              │
              │  Phase 1 (manual):
              │  App generates wa.me link → Owner opens WhatsApp →
              │  Owner taps Send → Message logged in whatsapp_messages
              │
              │  Phase 2 (automated):
              │  Edge Function → Interakt/AiSensy API → Meta WABA →
              │  Parent's WhatsApp → Delivery status webhook →
              │  Edge Function → Update whatsapp_messages.status
              │
              │        PDF GENERATION FLOW (Phase 2)
              │
              │  Fee marked as paid → Edge Function triggered →
              │  Generate PDF (jsPDF/react-pdf) → Upload to
              │  Supabase Storage → Update fee_records.receipt_url →
              │  Send WhatsApp message with PDF link
              │
              │        AUTH FLOW
              │
              │  Owner visits app → Supabase Auth (email+password) →
              │  JWT issued → RLS enforces org-scoped data access →
              │  Multi-org owners see org-selector → Context set
```

---

## 8. Pricing Strategy

### India Pricing (INR)

| Plan | Students | Price/Month | Features |
|---|---|---|---|
| **Free Trial** | Up to 30 | ₹0 (14 days) | All MVP features, no WhatsApp API |
| **Starter** | Up to 60 | ₹499/month | All MVP features, 1 center |
| **Growth** | Up to 150 | ₹999/month | Phase 2 features, 1 center, WhatsApp API included (1,000 msgs/month) |
| **Pro** | Unlimited | ₹1,799/month | All features, up to 3 centers, unlimited WhatsApp, priority support |

**Annual discount:** 20% off on annual prepay (2 months free). Collect annual upfront — critical for cash flow at early stage.

**Soft limits:** Students above plan limit can still be added; owner sees a yellow banner with upgrade prompt. Hard block only after 14-day grace period.

---

### International Pricing (USD)

| Plan | Students | Price/Month |
|---|---|---|
| **Starter** | Up to 60 | $9/month |
| **Growth** | Up to 150 | $19/month |
| **Pro** | Unlimited | $35/month |

Annual discount: 2 months free with annual commitment.

---

### Free Trial Structure

- 14-day free trial, no credit card required
- All MVP features unlocked during trial
- At trial end: data preserved, app enters read-only mode if not upgraded
- Reminder sequence: Day 7, Day 12, Day 13, Day 14 (WhatsApp messages from Alphax)
- Convert trial to paid via UPI payment link sent on WhatsApp (Phase 1) or Razorpay checkout (Phase 2)

---

### Payment Collection by Stage

**Phase 1 (first 10 customers):**
- Collect manually via UPI/GPay to Alphax's business UPI ID
- Send WhatsApp message confirming payment receipt
- Update plan manually in Supabase dashboard
- No payment gateway integration needed

**Phase 2 (10–100 customers):**
- Razorpay subscription billing for Indian customers
- Automated invoicing and receipt
- Auto-dunning for failed payments (Razorpay built-in)

**Phase 3 (international):**
- Stripe for international customers
- Multi-currency support
- Automated invoicing compliant with local tax requirements

---

## 9. Go-to-Market Technical Requirements

### 9.1 The 10-Minute Live Demo Flow

The demo should work entirely on the owner's phone — no projector, no laptop required.

```
Minute 0–1:   Open TuitionOS on phone. Show the dashboard — today's summary.
Minute 1–3:   Add a demo student (owner's own student or a made-up name).
              Show how fast enrollment is (under 2 minutes).
Minute 3–5:   Open fee tracking. Show pending/paid/overdue view.
              Tap "Send Reminder" — show the WhatsApp message opening pre-filled.
              "You don't have to type anything — it's already done."
Minute 5–7:   Open attendance — mark attendance for a sample batch.
              Show the absent notification WhatsApp message.
              "Parents know in seconds. No phone calls."
Minute 7–8:   Show the dashboard summary — fees collected today, attendance done.
Minute 8–10:  Pricing: "₹499 per month. Less than what you lose in one uncollected fee."
              Show UPI QR or send payment link via WhatsApp.
```

**Demo account:** Maintain a seeded demo organization (`demo.tuitionos.in`) with 25 pre-loaded students, 3 batches, and 2 months of fee history. Reset weekly via a Supabase Edge Function.

---

### 9.2 Landing Page Requirements

**Domain:** `tuitionos.in` (or `alphax.solutions/tuitionos` as fallback)

**Pages required:**
1. **Home** — Hero: "Run your tuition center from your phone." Above fold CTA: "Start Free Trial" (no credit card). Key proof: number of students managed, centers using it, fee reminders sent.
2. **Features** — Each MVP feature with a screenshot and 1-line benefit
3. **Pricing** — Three-tier table, annual toggle, FAQ
4. **Contact / Demo** — WhatsApp button (chat with Alphax team), simple contact form
5. **Privacy Policy + Terms** — Required for WhatsApp Business API approval

**Above-the-fold copy (Tamil + English):**
- Tamil: "உங்கள் டியூஷன் centre-ஐ phone-லேயே நடத்துங்கள்"
- English: "Run your tuition center from your phone. Fee tracking, attendance, WhatsApp reminders — all in one place."

**Social proof to acquire:** 3–5 testimonials from beta users. Even 10-day trial users count. Short WhatsApp voice message testimonials converted to text.

**Technical requirements for landing page:**
- Lighthouse score ≥ 90 on mobile (performance, accessibility)
- Page load < 2.5s on 4G (Chennai/Madurai)
- WhatsApp chat button (wa.me link to Alphax business number) sticky on mobile
- Google Analytics 4 + PostHog installed from day one
- Schema.org SoftwareApplication markup for SEO

---

### 9.3 WhatsApp Business Setup for Alphax

1. Create a WhatsApp Business Profile for "Alphax TuitionOS" with:
   - Profile photo: TuitionOS logo
   - Business description (Tamil + English)
   - Business hours
   - Website: tuitionos.in
   - Catalog: pricing plans (as catalog items)

2. Auto-reply for common messages:
   - "Demo" → sends demo booking link
   - "Pricing" → sends pricing summary with UPI link
   - "Help" → sends FAQ or routes to human

3. Broadcast list of trial users for nudge messages (manual WhatsApp Broadcast in Phase 1)

---

### 9.4 Onboarding a New Center in Under 5 Minutes

**Onboarding checklist (auto-shown after signup):**

```
Step 1 (60 sec):  Enter center name, city, WhatsApp number → Save
Step 2 (90 sec):  Add your first batch (name, subject, days, time)
Step 3 (90 sec):  Add 5 students (name, standard, parent phone, fee)
Step 4 (30 sec):  View dashboard — your center is ready
Step 5 (30 sec):  Optional: Mark today's attendance
```

Technical implementation:
- Onboarding wizard: 5-step progress bar, data saved at each step (no data loss if interrupted)
- Skip button available after Step 1 (center profile is minimum viable)
- First-time dashboard shows "Setup checklist" card with remaining steps
- In-app tips: small tooltip cards appear on first visit to each section
- Owner is prompted to share the app link to one colleague for referral

---

## 10. Competitive Analysis

### Competitor Overview

| Competitor | Target Market | Pricing | Key Weakness | Why Alphax Wins |
|---|---|---|---|---|
| **ClassPlus** | Large coaching institutes, YouTube educators, ed-tech | ₹3,000–15,000/month | Built for institutes with 500+ students, heavy feature bloat, English-first | Over-engineered for solo centers; ClassPlus assumes tech-savvy staff and online teaching — irrelevant for a Madurai center |
| **Teachmint** | Schools and colleges, teacher-led online classes | Free (ad-supported) + paid tiers | Pivoted to LMS/online classes; not built for offline fee collection or parent WhatsApp workflow | Wrong product category — Teachmint is about online teaching, not center management |
| **Extramarks** | School chains, CBSE/ICSE curriculum delivery | ₹5,000+/month, B2B school deals | Enterprise sales cycle, not accessible to solo owners; primarily content delivery, not management | Too expensive and too complex; targets school administrators, not tuition center owners |
| **MyClassCampus** | Colleges and institutes in Gujarat/Maharashtra | ₹2,000–8,000/month | Expensive, not localized for Tamil Nadu, complex UI, requires desktop for most features | Not Tamil-localized; tier-2 Tamil Nadu owners don't know this product exists |
| **Fedena** | Schools (global, open-source) | Free (self-hosted) / paid cloud | Requires self-hosting or technical setup; no WhatsApp integration; designed for schools | Technical barrier too high; a solo center owner cannot self-host Fedena |
| **Khatabook / OkCredit** | General SMB credit/debit tracking | Free | Generic bookkeeping tool — no student management, batches, attendance, or WhatsApp templates | Solves only 15% of the problem (fee recording) but misses attendance, batches, and communication |
| **WhatsApp Groups (manual)** | Every tuition center owner currently | Free | No record keeping, chaotic, owner is always reachable = burnout | This is the status quo Alphax is replacing — the real competition |
| **Google Sheets + WhatsApp** | Tech-comfortable center owners | Free | Requires manual maintenance, no reminders, no WhatsApp automation, breaks at scale | Works until ~30 students, then becomes a burden; no mobile-native flow |

### Alphax's Sustainable Competitive Advantage

1. **Tamil-first UX:** Every message template, error, and label available in Tamil. Competitors are English-first.
2. **WhatsApp-native workflow:** Built around how owners actually communicate — WhatsApp is the app, not email or a parent portal.
3. **Price-to-value gap:** ₹499/month vs. ₹3,000+ for ClassPlus. The decision is easier when the alternative is losing ₹2,000–5,000/month in uncollected fees.
4. **Zero onboarding friction:** No sales call, no demo booking, no KYC. Sign up on phone, start in 5 minutes.
5. **Offline-first offline center focus:** Every competitor has pivoted toward online/hybrid learning. Alphax is for centers that teach in-person and want to manage operations, not deliver content.

---

## 11. Risks and Mitigations

### Risk 1: Low Willingness to Pay in Tier-2 India

**Risk:** Center owners are used to free tools (WhatsApp, paper) and may resist paying ₹499/month.

**Mitigation:**
- Position as cost recovery, not cost addition: "You lose more than ₹499 in one uncollected fee per month. This pays for itself."
- Offer 14-day free trial with full features — no credit card required. Let the tool prove value before asking for payment.
- Use social proof from early adopters in the same city ("Another Madurai center owner saved 3 hours per week")
- Introduce annual plan at ₹4,990 (equivalent to ~₹415/month) to lower monthly commitment anxiety
- Start with 5 free referrals model: pay for 3 months, get 1 month free for each referral — aligns with WhatsApp word-of-mouth behavior

### Risk 2: WhatsApp API Costs at Scale

**Risk:** As the platform scales to 500+ centers and millions of messages, WhatsApp Business API conversation costs (₹0.30–₹0.75 per conversation) become a significant cost line.

**Mitigation:**
- Phase 1 uses zero-cost wa.me links — no API costs at all
- Phase 2 uses Interakt/AiSensy free tier (1,000 conversations/month free) — covers first 100 centers
- Build message efficiency into the product: batch daily reminders instead of sending one per event (reduces conversation count)
- At scale (Phase 3), negotiate volume pricing directly with Meta or WABA resellers
- Pricing model includes WhatsApp API costs in Growth/Pro plans — cost is covered at ₹999+ price point
- Monitor API cost per center as a unit economic metric from Phase 2 day one

### Risk 3: Competition from Free Tools

**Risk:** ClassPlus or Teachmint launch a free tier targeting small centers, or Google releases a tool that undermines the ₹499 price point.

**Mitigation:**
- Speed moat: be first and deeply embedded in Tamil Nadu centers before large players notice this segment
- Switching cost moat: after 6 months, a center has student history, fee records, and attendance data in TuitionOS — switching means losing that history
- Language moat: Tamil-native UX is hard for national players to replicate without dedicated localization effort
- Relationship moat: early customers become advocates — personal referrals in small Tamil Nadu cities are more effective than any marketing campaign

### Risk 4: Single Founder / Small Team Dependency

**Risk:** Critical knowledge or code lives in one person's head. Illness, burnout, or departure creates a crisis.

**Mitigation:**
- Document everything in Notion or GitHub Wiki as you build — not after
- Use Supabase and Vercel managed infrastructure (no server admin required)
- Write tests for all fee calculation and fee generation logic — the most critical business logic
- Use Cursor/GitHub Copilot to accelerate development while maintaining code quality
- First hire (even part-time) should be a second engineer who can read and understand the codebase

### Risk 5: Scope Creep

**Risk:** Requests from early customers ("Can you add exam scheduling?" "Can you add homework tracking?") expand the MVP beyond what the team can ship.

**Mitigation:**
- Maintain a public or customer-shared Phase 2 roadmap — tell customers "this is coming in Phase 2" with a version/date, not "we'll add it"
- Every feature request goes through a filter: "Does this help the first 10 customers pay?" If no, it is Phase 2+
- Assign a dedicated "no" owner — one person on the team whose job is to decline feature requests outside the MVP
- Launch with what is built, not what is planned. A working ₹499 tool beats a promised ₹999 tool.

---

## 12. 90-Day Build and Launch Roadmap

### Phase: Pre-Build (Before Day 1)
- Set up Supabase project (staging + production)
- Connect to existing GitHub repo and Vercel projects
- Configure GitHub Actions CI/CD pipeline
- Install PostHog and Sentry on the Next.js app
- Create Supabase migrations for the core schema (organizations, users, students, batches)
- Register TuitionOS domain and set up WhatsApp Business profile for Alphax

---

### Weeks 1–2: MVP Core Build

**Week 1 — Auth + Organization + Students**
- Supabase Auth setup (email/password)
- Organization onboarding wizard (5-step)
- Student enrollment form + student list
- Student profile page
- RLS policies on organizations, users, students

**Week 2 — Batches + Attendance + Basic Dashboard**
- Batch creation form + batch list + student-batch assignment
- Attendance marking UI (mobile-optimized, under 2 minutes for 30 students)
- Attendance session history
- Owner dashboard (today's batches, attendance status, quick actions)

---

### Weeks 3–4: Fee Tracking + WhatsApp Links + Internal Testing

**Week 3 — Fee System**
- Monthly fee record auto-generation (pg_cron function)
- Fee tracking UI (Paid / Pending / Overdue tabs)
- Mark as paid flow (amount, mode, date)
- Fee summary card on dashboard
- WhatsApp reminder link generation (wa.me) for pending fees
- Absence WhatsApp notification link generation

**Week 4 — Internal Testing + Bug Fixes**
- Full end-to-end testing by team: add center → add 20 students → 3 batches → mark attendance → record fees → send WhatsApp reminders
- Fix all critical bugs (P0/P1)
- Performance audit: test on ₹8,000 Android phone on 4G network
- Accessibility audit: tab navigation, color contrast, font size
- Security audit: verify RLS policies block cross-organization data access
- Inquiry lead log feature

---

### Week 5: First 3 Free Trial Customers

**Target:** 3 tuition center owners in Madurai (personal network of founders)

**Acquisition method:**
- WhatsApp message to founders' personal contacts who run or know tuition centers
- Offer: "Free lifetime access if you use it for 2 weeks and give us honest feedback"
- In-person onboarding: sit with the owner, help them set up their center, add their first 10 students together

**Feedback collection:**
- WhatsApp voice note from owner after Day 3 and Day 7: "Tell us one thing that frustrated you and one thing you loved"
- Track PostHog events: which features are being used, where they drop off
- Daily check-in by the team via WhatsApp

---

### Weeks 6–8: Iterate Based on Feedback

**Week 6:** Synthesize feedback from 3 trial users. List all P0 (blocking) and P1 (annoying but workable) issues. Fix P0 issues immediately. Plan P1 fixes.

**Week 7:** Fix P1 issues. Add missing UX polish (loading states, empty states, error messages). Add Tamil error messages. Improve attendance speed (count of taps to complete batch).

**Week 8:** Add 2–3 more free trial users (now slightly outside personal network — ask the 3 initial users for 1 referral each). Validate that the onboarding is self-serve (founders should not need to sit with the new users).

---

### Weeks 9–10: First Paid Conversions

**Target:** 3–5 paying customers at ₹499/month

**Conversion approach:**
- Trial users who have been active for 14+ days get a WhatsApp message from founders: "Your trial ends on [date]. Switch to paid for ₹499/month — less than 2 cups of chai per day. Pay here: [UPI link]"
- Offer 1-month free if they refer one other center owner who converts
- Manual payment collection via UPI for the first 10 customers — no payment gateway needed yet

**Goal: ₹2,000–2,500 MRR by end of Week 10**

---

### Weeks 11–12: Expand to 20 Paying Customers, Prep Phase 2

**Target:** 15–20 paying customers → ₹7,500–10,000 MRR

**Acquisition beyond personal network:**
- Post in Madurai tuition center owner Facebook groups with a demo video (30-second screen recording on phone)
- Share in local teacher WhatsApp groups
- Offer a "Bring a Friend" deal: existing paying customer + new customer both get 1 month free
- Begin planning Phase 2 features based on Week 5–8 feedback

**Phase 2 prep (Week 12):**
- Finalize Phase 2 scope (top 3 requested features from feedback)
- Apply for Interakt/AiSensy WhatsApp Business API account (takes 3–7 days for approval)
- Begin Razorpay account setup (KYC for Alphax Solution)
- Build PDF receipt generation proof-of-concept

---

## 13. Success Metrics

### What to Measure from Day One

**Every metric below should be visible in PostHog or a Supabase-connected Metabase dashboard.**

---

### Monthly Business Metrics

| Metric | Week 8 Target | Month 3 Target | Month 6 Target | Month 12 Target |
|---|---|---|---|---|
| Trial signups | 10 | 30 | 100 | 500 |
| Trial → Paid conversion rate | — | 30% | 40% | 45% |
| Paying customers | 3 | 10 | 40 | 200 |
| MRR (INR) | ₹1,500 | ₹5,000 | ₹22,000 | ₹1,20,000 |
| MRR (USD equivalent) | $18 | $60 | $265 | $1,440 |
| Monthly churn rate | — | < 10% | < 7% | < 5% |
| Net Revenue Retention | — | > 90% | > 95% | > 100% |
| Average Revenue Per Customer | ₹499 | ₹499 | ₹649 | ₹749 |

**Path to $10,000/month (₹83,000 MRR):** Requires ~165 customers at ₹499/month average, or a mix of Growth/Pro plan customers (80–100 customers at ₹800+ average). Achievable within 18 months based on Tamil Nadu market size (estimated 50,000+ tuition centers in Tamil Nadu alone).

---

### Product Health Metrics (Weekly)

| Metric | Definition | Target |
|---|---|---|
| DAU/MAU ratio | Daily active users / Monthly active users | > 60% (owners use it daily) |
| Attendance marked per week | % of active batches with attendance marked ≥ 3x/week | > 70% |
| Fee reminders sent per month | WhatsApp links generated per active customer | > 5 per customer/month |
| Onboarding completion rate | % of signups who complete all 5 onboarding steps | > 60% |
| Time to first value | Time from signup to first attendance marked | < 10 minutes |
| Support tickets per customer | Customer-initiated support contacts per month | < 0.3/month |

---

### WhatsApp Engagement Metrics (Phase 2+)

| Metric | Tool | Target |
|---|---|---|
| Fee reminder delivery rate | Interakt/WATI dashboard | > 95% delivered |
| Fee reminder response rate | Payments received within 48h of reminder | > 30% |
| Absence notification open rate | WhatsApp read receipts | > 85% read |
| Bulk broadcast opt-out rate | WhatsApp block/opt-out per broadcast | < 2% |

---

### NPS and Qualitative Tracking

- **NPS survey:** WhatsApp message to paying customers every 60 days: "On a scale of 1–10, how likely are you to recommend TuitionOS to another tuition center owner? [1–5] [6–8] [9–10]"
- **Target NPS: > 50** (indicates strong word-of-mouth potential in a tight-knit market)
- **Churn interviews:** Call every churned customer within 7 days. Record reason in a Notion database. After 10 churns, look for patterns.
- **Feature request log:** Every request logged in GitHub Issues with customer name and frequency count. Top 3 most-requested features drive Phase 2 prioritization.

---

### Financial Health Metrics (Monthly)

| Metric | Formula | Target (Month 12) |
|---|---|---|
| CAC (Customer Acquisition Cost) | Total marketing spend / new customers | < ₹500 |
| LTV (Lifetime Value) | ARPU × (1 / Monthly Churn Rate) | > ₹8,000 |
| LTV:CAC Ratio | LTV / CAC | > 10x |
| Gross Margin | (MRR - WhatsApp API costs - Supabase/Vercel costs) / MRR | > 85% |
| Payback Period | CAC / (ARPU × Gross Margin %) | < 2 months |

---

*End of Document — Version 1.0*

*Maintained by: Alphax Solution, Madurai, Tamil Nadu*
*Next review: After first 10 paying customers or 90 days from document date, whichever comes first.*
