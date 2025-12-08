const admin = require('firebase-admin');

// Load Firebase service account key directly from the JSON file
const serviceAccount = require('./firebase-key.json');

// Initialize Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Get Firestore instance
const db = admin.firestore();

module.exports = { db };
