const admin = require('./firebase'); // Your Firebase admin instance
const db = admin.firestore();

async function clearUsers() {
    const usersSnapshot = await db.collection('users').get();
    const batch = db.batch();

    usersSnapshot.forEach(doc => batch.delete(doc.ref));

    await batch.commit();
    console.log(`✅ Deleted ${usersSnapshot.size} users`);
}

clearUsers().then(() => process.exit(0)).catch(err => {
    console.error(err);
    process.exit(1);
});
