require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

// ============== FIREBASE =========================
// Import your firebase.js which already initializes Firebase Admin SDK
const { db } = require('./firebase'); // Make sure the path is correct

// ============== MIDDLEWARE =======================
const app = express();
app.use(cors());
app.use(express.json()); // parse JSON body

// ============== ROUTES ===========================
const usersRoute = require('./routes/usersRoute'); 
app.use('/api/users', usersRoute);  // All user routes start with /api/users

// ============== MONGODB CONNECTION ===============
const mongoConnection = 'mongodb://perakmenang41_db_user:tfrgTUsElrMjr8D0@cluster0-shard-00-00.mn1vjvg.mongodb.net:27017,cluster0-shard-00-01.mn1vjvg.mongodb.net:27017,cluster0-shard-00-02.mn1vjvg.mongodb.net:27017/kidtracker?ssl=true&replicaSet=atlas-REPLACE_WITH_YOUR_REPLICA_SET&authSource=admin&retryWrites=true&w=majority';

mongoose.connect(mongoConnection)
  .then(() => console.log("✅ MongoDB connected"))
  .catch(err => console.log("❌ MongoDB Error:", err));

// ============== START SERVER =====================
const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running at: http://localhost:${PORT}`);
});
