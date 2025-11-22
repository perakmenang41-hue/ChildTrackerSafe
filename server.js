require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

// ================= FIREBASE =========================
// Import your firebase.js which already initializes Firebase Admin SDK
const { db } = require('./firebase'); // Make sure the path is correct

// ================= MIDDLEWARE =======================
const app = express();
app.use(cors());
app.use(express.json()); // parse JSON body

// ================= ROUTES ===========================
const usersRoute = require('./routes/usersRoute'); 
app.use('/api/users', usersRoute);  // All user routes start with /api/users

// ================= MONGODB CONNECTION ===============
// Use Atlas connection string from environment variable
const mongoURI = process.env.MONGO_URI;

mongoose.connect(mongoURI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
})
.then(() => console.log("✅ MongoDB connected"))
.catch(err => console.log("❌ MongoDB Error:", err));

// ================= START SERVER =====================
const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running at: http://localhost:${PORT}`);
});
