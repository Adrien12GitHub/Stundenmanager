# Stundenmanager 📅⏳

## About the Project
**Stundenmanager** is a work hours tracking and shift management application designed for companies with shift work or varying work schedules. The app provides an intuitive interface for employees to log their working hours, manage absences, and generate automated reports. It also features push notifications, offline functionality, and cloud synchronization.

## Features 🚀
### Administration Requirements
- ✅ **User Interface** - A well-structured and user-friendly UI
- ✅ **Automated Shift Assignment**
- ✅ **Rule-based Planning**
- ✅ **Vacation & Sick Leave Management**
- ✅ **Automated Report Generation** (Planned vs. Actual Work Hours)
- ✅ **Statistics & Analytics**

### Employee Interface
- ✅ **Track Work Start, End, and Break Times**
- ✅ **Overview of Vacation and Sick Leave**

### Notifications & Synchronization
- ✅ **Push Notifications for Shift Start & End**
- ✅ **Offline Mode with Automatic Synchronization**

## Technologies Used 🛠️
- **Frontend**: Android (Kotlin, XML, Jetpack Components)
- **Backend**: Firebase Firestore (Cloud Database)
- **Authentication**: Firebase Authentication
- **Cloud Storage**: Firebase Cloud Storage
- **CI/CD**: GitHub Actions + Firebase App Distribution
- **Version Control**: Git & GitHub
- **UI Components**: Material Design, Pie Charts, RecyclerView

## Setup & Installation 📥
### Prerequisites
1. Install **Android Studio** (latest version)
2. Clone the repository:
   ```sh
   git clone https://github.com/Adrien12GitHub/Stundenmanager.git
   ```
3. Open the project in Android Studio
4. Set up **Firebase**
   - Create a Firebase project
   - Enable **Authentication** (Email/Password Sign-In)
   - Set up **Firestore Database**
   - Configure Firebase **App Distribution**
   - Download `google-services.json` and place it inside `app/`

## Continuous Integration & Deployment (CI/CD) 🔄
We use **GitHub Actions** for:
- Automated Builds & Testing
- Versioning
- Firebase App Distribution Deployment

### CI/CD Pipeline
1. **Trigger**: Runs on every push to `main`
2. **Build & Test**: Compiles the project and runs tests
3. **Versioning**: Auto-increments versionCode
4. **Upload APK**: Stores build artifacts
5. **Firebase Deployment**: Uploads the app to Firebase App Distribution for testers

## How to Contribute 🤝
1. **Fork** the repo
2. **Create a new branch**: `git checkout -b feature/new-feature`
3. **Commit changes**: `git commit -m 'Added new feature'`
4. **Push to branch**: `git push origin feature/new-feature`
5. **Open a Pull Request**

## Roadmap 🛣️
### TODOs
- [ ] **Edit & Delete Work Hours and Absences**
- [ ] **(Push) Notifications for Shifts**
- [ ] **Prevent Work Hours Entry during Absence**
- [ ] **Skip Shift if Absence Exists**

---
📩 **Contact:** If you have any questions, feel free to reach out to the maintainers!

### Maintainers:
- [Adrien12GitHub] (https://github.com/Adrien12GitHub)
- [Miniminmaximin] (https://github.com/Miniminmaximin)

Happy coding! 🎉
