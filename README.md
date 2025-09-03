# Alquilame Android App

A modern Android application for the Alquilame rental platform, providing seamless access to rental management through a secure WebView interface with native Android integrations.

## Features

### 🔐 Authentication
- **QR Code Login**: Scan QR codes for instant authentication
- **Manual Login**: Traditional username/API key login
- **Secure Storage**: Encrypted credential storage using Android Keystore
- **Auto-login**: Automatic login for returning users

### 📱 WebView Integration
- **Responsive Design**: Optimized web interface
- **File Upload Support**: Camera and gallery access for file uploads
- **Download Manager**: Seamless PDF/document downloads
- **Dynamic URL Generation**: Secure, time-based URL generation

### 🔒 Security Features
- **Hash Validation**: QR code authenticity verification
- **Encrypted Storage**: Credentials stored using Android Security library
- **Environment Variables**: Secure configuration management
- **Dynamic Secrets**: Time-based secret generation for URLs

## Screenshots

Add screenshots here showing the main features*

## Requirements

- **Android SDK**: API 24 (Android 7.0) or higher
- **Target SDK**: API 36
- **Java**: JDK 11 or higher (for building)
- **Gradle**: 8.13

## Setup & Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd alquilameapp
```

### 2. Configure Environment Variables

Create or update `local.properties` file:
```properties
# SDK Location
sdk.dir=/path/to/your/Android/sdk

# Secret Configuration (required)
SECRET_STRING_LOGIN=your_secret_string_here
```

**For CI/CD or production builds**, set the environment variable:
```bash
export SECRET_STRING_LOGIN="your_secret_string_here"
```

### 3. Build the Project
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

### 4. Install on Device
```bash
# Install debug APK
./gradlew installDebug

# Or install via Android Studio
```

## Configuration

### Environment Variables
The app uses the following configuration:

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SECRET_STRING_LOGIN` | Secret string for URL generation and QR validation | Yes | - |

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with optimizations

## Security

### QR Code Validation
QR codes must contain data in the format: `tenant_slug|tenant_secret_slug|hash`

The hash is generated using SHA-256:
```
SHA256(tenant_slug|tenant_secret_slug|SECRET_STRING_LOGIN)
```

### URL Generation
Secure URLs are generated using:
```
https://[tenant].alquilame.io/s/tenant/[tenant]/[dynamic_secret]/[timestamp]
```

Where `dynamic_secret` is the first 16 characters of:
```
SHA256(tenant_slug|tenant_secret_slug|timestamp|SECRET_STRING_LOGIN)
```

### Permissions
The app requests the following permissions:

| Permission | Purpose | API Level |
|------------|---------|-----------|
| `INTERNET` | Web content access | All |
| `CAMERA` | QR code scanning | All |
| `READ_EXTERNAL_STORAGE` | File access (≤ API 32) | ≤ 32 |
| `READ_MEDIA_IMAGES` | Image file access (≥ API 33) | ≥ 33 |
| `WRITE_EXTERNAL_STORAGE` | Downloads (≤ API 28) | ≤ 28 |

## Testing

### Running Tests
```bash
# Unit tests
./gradlew testDebugUnitTest

# Integration tests  
./gradlew connectedAndroidTest

# All tests
./gradlew test connectedAndroidTest
```

## Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
# Set environment variable
export SECRET_STRING_LOGIN="production_secret"

# Build release
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/
```
## Troubleshooting

### Issues
Report bugs and feature requests using the GitHub issue tracker.

## Support

For technical support or questions:
- **Documentation**: Check this README and inline code comments
- **Issues**: Use GitHub Issues for bug reports
- **Contact**: oscar@alquilame.io

---

**Built by [Alquilame.io](https://alquilame.io)**
