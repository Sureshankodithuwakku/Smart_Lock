const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Cloud Function to remove expired keys
exports.removeExpiredKeys = functions.database.ref('/Users/{userId}/keys/{keyId}')
    .onUpdate((change, context) => {
        const newValue = change.after.val(); // New value of the key
        const expirationTime = newValue.expiration_time;

        // Check if the key has expired (by comparing expiration time with the current time)
        if (expirationTime && expirationTime <= Date.now()) {
            // Key has expired, remove it
            console.log(`Removing expired key with ID: ${context.params.keyId}`);
            return admin.database().ref(`/Users/${context.params.userId}/keys/${context.params.keyId}`).remove();
        }

        // If the key has not expired, do nothing
        return null;
    });
