const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  email: { type: String, required: true, unique: true },
  childName: { type: String, required: true },
  password: { type: String, required: true },      // Password for login
  uniqueId: { type: String, required: true, unique: true }, // Keep UID for backend/email
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('User', userSchema);
