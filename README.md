# FamChat - Arattai Clone

FamChat is a secure, real-time messaging application for Android, inspired by Zoho's Arattai.

## Key Features
- **One-to-One Messaging**: Real-time chat using Firebase Firestore.
- **End-to-End Encryption (E2EE)**: Hybrid encryption using Google's **Tink** library (ECIES with AES-GCM) ensures that only the sender and receiver can read messages.
- **Email Authentication**: Firebase Auth for secure sign-in.
- **Google Drive Backup**: Automatic background sync of your chat history to your Google Drive.
- **Modern UI**: Built entirely with Jetpack Compose using a Teal and Dark Blue theme.

## Technical Details

### E2EE Implementation (using Tink)
- Each user generates an **ECIES (P-256)** Keysethandle on first launch.
- The keyset is stored securely in the **Android Keystore**.
- Public keys are shared via Firestore.
- Messages are encrypted using the recipient's public keyset before being sent. Tink handles the hybrid encryption (AES-GCM for content, ECIES for key exchange), which overcomes payload size limits.
- Decryption happens locally using the recipient's private keyset.

### Cloud Backup
- Uses `WorkManager` to trigger backups when the device is charging and on Wi-Fi.
- Backs up the local Room database (`famchat_database`) to the user's Google Drive.
- **Note**: Requires the user to link their Google Account in the Settings screen to authorize Drive access.

### Firestore Indexing
To support efficient message fetching and listening, you **must** create the following composite index in the Firebase Console:

1.  **Collection**: `messages`
2.  **Fields**:
    - `receiverId` (Ascending)
    - `timestamp` (Ascending)

## Setup
1. Add your `google-services.json` to the `app/` directory.
2. Enable Email Auth and Firestore in the Firebase Console.
3. Configure Google Sign-In with the `https://www.googleapis.com/auth/drive.file` scope.
