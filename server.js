require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');

const { db } = require('./firebase'); // firebase-key.json is loaded here

const app = express();
app.use(cors());
app.use(express.json());

const usersRoute = require('./routes/usersRoute'); 
app.use('/api/users', usersRoute);

const mongoURI = process.env.MONGO_URI;
if (!mongoURI) {
    console.error("❌ MongoDB connection string missing! Set MONGO_URI in your .env.");
    process.exit(1);
}

// Connect to MongoDB without deprecated options
mongoose.connect(mongoURI)
    .then(() => console.log("✅ MongoDB connected"))
    .catch(err => console.log("❌ MongoDB Error:", err));

const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Server running at: http://localhost:${PORT}`);
});
