package com.example.data.model

object BackendArchitectureSpecs {

    val FIRESTORE_SCHEMA_JSON = """
{
  "collections": {
    "users": {
      "doc_id": "{userId}",
      "fields": {
        "email": "user@company.gr",
        "displayName": "Ioannis Antoniou",
        "fcmToken": "eKx...",
        "createdAt": "2026-08-15T08:00:00Z",
        "lastLoginAt": "2026-08-15T12:30:00Z"
      }
    },
    "business_profiles": {
      "doc_id": "{userId}",
      "fields": {
        "companyName": "Hellas Catering Equipment Ltd",
        "ownGemiNumber": "123456789000",
        "businessType": "Catering Equipment & POS Solutions",
        "targetSectors": ["HORECA", "TOURISM"],
        "targetKads": ["56.10", "56.30", "56.21", "55.10"],
        "isPanHellenic": false,
        "targetRegions": ["Αττική", "Κρήτη", "Κεντρική Μακεδονία"],
        "targetLegalForms": ["ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ"],
        "notificationTime": "08:30",
        "dailySummaryEnabled": true,
        "updatedAt": "2026-08-15T10:00:00Z"
      }
    },
    "gemi_daily_leads": {
      "doc_id": "{gemiNumber}",
      "fields": {
        "gemiNumber": "179428301000",
        "afm": "802495123",
        "companyName": "AEGEAN BLUE GASTRONOMY I.K.E.",
        "tradeName": "Thalassa Seafood & Wine Bar",
        "legalForm": "ΙΚΕ",
        "registrationDate": "2026-08-15",
        "primaryKad": "56.10",
        "kadDescription": "Υπηρεσίες εστιατορίου",
        "secondaryKads": ["56.30", "56.21"],
        "sector": "HORECA",
        "region": "Αττική",
        "prefecture": "Νότιος Τομέας Αθηνών",
        "municipality": "Δήμος Γλυφάδας",
        "address": "Λεωφόρος Ποσειδώνος 48",
        "postalCode": "16675",
        "chamberName": "Επαγγελματικό Επιμελητήριο Αθηνών",
        "phone": "210 8945120",
        "email": "info@thalassagastronomy.gr",
        "initialCapital": 20000,
        "administrators": ["Ιωάννης Παπαδόπουλος"],
        "ingestedAt": "2026-08-15T06:05:12Z"
      }
    }
  }
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
