package com.example.data.model

object BackendArchitectureSpecs {

    val FIRESTORE_SCHEMA_JSON = """
{
  "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "GEMI B2B Lead Generator - Cloud Firestore Schema",
  "version": "1.0.0",
  "collections": {
    "users": {
      "description": "Authenticated user account records (1 document per user)",
      "document_id": "{userId} (Firebase Auth UID)",
      "fields": {
        "userId": { "type": "string", "description": "Unique Firebase Authentication UID", "example": "usr_948a72be10" },
        "email": { "type": "string", "description": "User email address", "example": "antoniouioannis10@gmail.com" },
        "displayName": { "type": "string", "description": "Full name or business contact", "example": "Ioannis Antoniou" },
        "fcmToken": { "type": "string | null", "description": "Firebase Cloud Messaging device token for push alerts", "example": "eKxP89...z91" },
        "photoUrl": { "type": "string | null", "description": "Profile avatar URL", "example": "https://lh3.googleusercontent.com/..." },
        "status": { "type": "string", "enum": ["ACTIVE", "SUSPENDED", "PENDING"], "example": "ACTIVE" },
        "createdAt": { "type": "string (ISO-8601 UTC)", "description": "Account registration timestamp", "example": "2026-08-15T08:00:00Z" },
        "lastLoginAt": { "type": "string (ISO-8601 UTC)", "description": "Most recent session activity timestamp", "example": "2026-08-15T16:50:00Z" }
      }
    },
    "business_profiles": {
      "description": "B2B targeting preferences, KAD rules, and notification criteria (1 document per user)",
      "document_id": "{userId} (Matches the Auth UID)",
      "fields": {
        "userId": { "type": "string", "description": "Firebase Auth UID", "example": "usr_948a72be10" },
        "companyName": { "type": "string", "description": "Subscriber business name", "example": "Hellas Catering Solutions Ltd" },
        "ownGemiNumber": { "type": "string", "description": "Subscriber 12-digit GEMI registration number", "example": "123456789000" },
        "businessType": { "type": "string", "description": "Description of subscriber products/services", "example": "Catering Equipment & POS Systems" },
        "targetSectors": { "type": "array<string>", "description": "Industry sectors targeted", "example": ["HORECA", "TOURISM", "RETAIL"] },
        "targetKads": { "type": "array<string>", "description": "Specific 4-digit Greek NACE / KAD codes targeted", "example": ["56.10", "56.30", "56.21", "55.10"] },
        "isPanHellenic": { "type": "boolean", "description": "True if leads across all Greek regions are accepted", "example": false },
        "targetRegions": { "type": "array<string>", "description": "Targeted Greek administrative regions", "example": ["Αττική", "Κρήτη", "Κεντρική Μακεδονία"] },
        "targetLegalForms": { "type": "array<string>", "description": "Targeted Greek legal entities", "example": ["ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική"] },
        "notificationTime": { "type": "string (HH:mm)", "description": "Daily scheduled digest alert time", "example": "08:30" },
        "dailySummaryEnabled": { "type": "boolean", "description": "Toggle for daily automated lead push digest", "example": true },
        "emailAlerts": { "type": "boolean", "description": "Send daily high-match leads to email", "example": true },
        "pushNotifications": { "type": "boolean", "description": "Receive immediate FCM notifications on new matches", "example": true },
        "clientTargetGoal": { "type": "string", "description": "Ideal client profile notes", "example": "Beach bars, luxury villas & upscale restaurants in coastal areas" },
        "updatedAt": { "type": "string (ISO-8601 UTC)", "description": "Profile last modification timestamp", "example": "2026-08-15T12:00:00Z" },
        "createdAt": { "type": "string (ISO-8601 UTC)", "description": "Profile creation timestamp", "example": "2026-08-15T08:00:00Z" }
      }
    },
    "gemi_daily_leads": {
      "description": "Daily incorporated companies ingested from the Greek Commercial Registry (Γ.Ε.ΜΗ.) Open Data API",
      "document_id": "{gemiNumber} (12-digit official GEMI registry number)",
      "fields": {
        "gemiNumber": { "type": "string", "description": "Unique 12-digit GEMI registration number", "example": "179428301000" },
        "afm": { "type": "string", "description": "9-digit Greek Tax Identification Number (ΑΦΜ)", "example": "802495123" },
        "companyName": { "type": "string", "description": "Official legal corporate name", "example": "AEGEAN BLUE GASTRONOMY I.K.E." },
        "tradeName": { "type": "string", "description": "Commercial trading name (Διακριτικός Τίτλος)", "example": "Thalassa Seafood & Wine Bar" },
        "legalForm": { "type": "string", "enum": ["ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική"], "example": "ΙΚΕ" },
        "registrationDate": { "type": "string (YYYY-MM-DD)", "description": "Incorporation date in registry", "example": "2026-08-15" },
        "primaryKad": { "type": "string", "description": "Primary economic activity code (ΚΑΔ)", "example": "56.10" },
        "kadDescription": { "type": "string", "description": "Greek description of primary activity", "example": "Υπηρεσίες εστιατορίων & ταβερνών" },
        "secondaryKads": { "type": "array<string>", "description": "Secondary registered activity codes", "example": ["56.30", "56.21"] },
        "sector": { "type": "string", "description": "Standardized category identifier", "example": "HORECA" },
        "region": { "type": "string", "description": "Greek administrative region (Περιφέρεια)", "example": "Αττική" },
        "prefecture": { "type": "string", "description": "Regional unit / prefecture (Περιφερειακή Ενότητα)", "example": "Νότιος Τομέας Αθηνών" },
        "municipality": { "type": "string", "description": "Municipality (Δήμος)", "example": "Δήμος Γλυφάδας" },
        "address": { "type": "string", "description": "Registered business street address & number", "example": "Λεωφόρος Ποσειδώνος 48" },
        "postalCode": { "type": "string", "description": "5-digit postal code (Τ.Κ.)", "example": "16675" },
        "chamberName": { "type": "string", "description": "Competent Chamber of Commerce (Επιμελητήριο)", "example": "Επαγγελματικό Επιμελητήριο Αθηνών" },
        "phone": { "type": "string", "description": "Official company contact telephone", "example": "210 8945120" },
        "email": { "type": "string", "description": "Official company contact email", "example": "info@thalassagastronomy.gr" },
        "website": { "type": "string", "description": "Official company website domain", "example": "https://thalassagastronomy.gr" },
        "initialCapital": { "type": "number (double)", "description": "Initial share/equity capital in EUR (€)", "example": 20000.0 },
        "administrators": { "type": "array<string>", "description": "Legal representatives / Managers (Διαχειριστές/Εταίροι)", "example": ["Ιωάννης Παπαδόπουλος", "Μαρία Γεωργίου"] },
        "ingestedAt": { "type": "string (ISO-8601 UTC) | timestamp", "description": "Timestamp when ingestion worker synced record", "example": "2026-08-15T06:05:12Z" },
        "source": { "type": "string", "description": "Origin of record", "example": "GEMI_OPEN_DATA_API" }
      }
    }
  },
  "security_rules": {
    "users": "allow read, write: if request.auth != null && request.auth.uid == userId;",
    "business_profiles": "allow read, write: if request.auth != null && request.auth.uid == userId;",
    "gemi_daily_leads": "allow read: if request.auth != null; allow write: if false; // only Cloud Functions admin SDK"
  },
  "composite_indexes": [
    {
      "collection": "gemi_daily_leads",
      "fields": [
        { "field": "registrationDate", "order": "DESCENDING" },
        { "field": "primaryKad", "order": "ASCENDING" }
      ]
    },
    {
      "collection": "gemi_daily_leads",
      "fields": [
        { "field": "region", "order": "ASCENDING" },
        { "field": "registrationDate", "order": "DESCENDING" }
      ]
    }
  ]
}
""".trimIndent()

    val NODEJS_CLOUD_FUNCTION = """
/**
 * GEMI Open Data Daily Ingestion Worker
 * Scheduled: Every day at 06:00 AM UTC+2 (Europe/Athens) via Google Cloud Scheduler
 * Rate Limit Compliance: 8 requests / minute max (sleep 7500ms between calls)
 */
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');

admin.initializeApp();
const db = admin.firestore();

const GEMI_API_KEY = process.env.GEMI_API_KEY || "VeWz15eTqbFrqaazLUYUfyUVMr1w6Zfa";
const GEMI_BASE_URL = "https://opendata.uhc.gr/api/v1/companies/new";
const RATE_LIMIT_DELAY_MS = 7500; // 8 req/min safety barrier

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

exports.dailyGemiIngestion = functions
  .region('europe-west3')
  .pubsub.schedule('0 6 * * *')
  .timeZone('Europe/Athens')
  .onRun(async (context) => {
    console.log('Starting daily GEMI ingestion pipeline...');
    const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0];
    let page = 1;
    let hasMore = true;
    let totalIngested = 0;

    const axiosClient = axios.create({
      headers: {
        'X-API-KEY': GEMI_API_KEY,
        'Authorization': `Bearer ${'$'}{GEMI_API_KEY}`,
        'Accept': 'application/json',
        'User-Agent': 'GEMI-B2B-LeadGenerator-Backend/1.0'
      },
      timeout: 15000
    });

    while (hasMore) {
      try {
        console.log(`Fetching GEMI page ${'$'}{page} for date ${'$'}{yesterday}...`);
        const response = await axiosClient.get(GEMI_BASE_URL, {
          params: { date_from: yesterday, date_to: yesterday, page: page, limit: 50 }
        });

        const items = response.data?.data || response.data?.items || [];
        if (items.length === 0) {
          hasMore = false;
          break;
        }

        const batch = db.batch();
        for (const comp of items) {
          if (!comp.gemi_number) continue;
          const leadRef = db.collection('gemi_daily_leads').doc(comp.gemi_number);
          batch.set(leadRef, {
            gemiNumber: comp.gemi_number,
            afm: comp.afm || '',
            companyName: comp.name || '',
            tradeName: comp.trade_name || '',
            legalForm: comp.legal_form || 'ΙΚΕ',
            registrationDate: comp.registration_date || yesterday,
            primaryKad: comp.kad || '',
            kadDescription: comp.kad_description || '',
            region: comp.region || '',
            prefecture: comp.prefecture || '',
            municipality: comp.municipality || '',
            phone: comp.phone || '',
            email: comp.email || '',
            ingestedAt: admin.firestore.FieldValue.serverTimestamp()
          }, { merge: true });
          totalIngested++;
        }
        await batch.commit();

        page++;
        // Enforce 8 req/min GEMI API rate limit
        await sleep(RATE_LIMIT_DELAY_MS);
      } catch (err) {
        console.error('Error during GEMI ingestion page ' + page, err.message);
        if (err.response?.status === 429) {
          console.warn('Hit 429 Too Many Requests. Cooling down for 60s...');
          await sleep(60000);
        } else {
          hasMore = false;
        }
      }
    }

    console.log(`Ingestion completed successfully. Total ingested: ${'$'}{totalIngested}`);
    return null;
  });
""".trimIndent()
}

