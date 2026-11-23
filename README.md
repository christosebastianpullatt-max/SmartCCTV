# Smart CCTV — Android source project

Native Android starter app for a **two-camera simultaneous live-view dashboard**.

Configured camera slots:
- TP-Link **Tapo C510W**
- CP PLUS **CP-E25Q** (EzyKam+)

The dashboard can play both RTSP feeds at once using AndroidX Media3. Each camera has its own editable stream URL and camera credentials.

## Important: what this app can and cannot do

- This is an Android Studio **source project**, not a compiled or signed APK.
- The app does **not** log in to the Tapo cloud account. TP-Link's consumer Tapo app login is not a public third-party streaming API. The app uses the camera's local RTSP account instead; these are separate credentials.
- The Tapo C510W can be configured for RTSP using a separate Camera Account in the Tapo app. A common RTSP path is `/stream1` (high quality) or `/stream2` (lower quality), on port 554. Verify the exact URL for the camera firmware.
- CP PLUS CP-E25Q / EzyKam+ cloud live view is **not integrated** in this project. CP PLUS cloud/P2P access is proprietary unless the manufacturer supplies an SDK/API to the developer. This camera slot will play only if the device/NVR provides a compatible RTSP URL. No cloud protocol has been reverse-engineered or faked.
- For viewing away from home, use a VPN into the camera's home network (for example, a router-hosted WireGuard/Tailscale setup). Do not port-forward RTSP to the public internet.
- The app currently keeps camera settings in memory only. Closing the app clears stream URLs and credentials. Do not use real credentials in screenshots or share them.

## Build in Android Studio

1. Install the current stable Android Studio and Android SDK 35.
2. Open this `SmartCCTV` folder in Android Studio.
3. Allow Gradle sync (internet access is needed to download dependencies).
4. Connect an Android device with USB debugging enabled, or use an emulator.
5. Select **Run** to install the debug app.
6. To create a debug APK, select **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Android Studio will show the APK output location.

This project does not include a Gradle wrapper or a release signing key. A release APK must be signed by you in Android Studio.

## Configure Tapo C510W

1. In the Tapo app, open the C510W's Device Settings and create a separate **Camera Account** under Advanced Settings (menu wording may vary by firmware).
2. Find the camera's LAN IP address in Device Info or your router's device list.
3. While the phone is on the same Wi-Fi, test the RTSP stream in a trusted RTSP player. Typical URL: `rtsp://CAMERA_IP:554/stream1`.
4. In Smart CCTV, open the Tapo tile's settings and enter the RTSP URL, Camera Account username and password.
5. For remote viewing, connect the phone to the home VPN first. RTSP over ordinary mobile data will not work unless a secure route to the home LAN is configured.

**Do not enter your Tapo cloud email/password** in the RTSP username/password fields.

## Configure CP PLUS CP-E25Q

Check the camera manual or ask CP PLUS support whether your exact CP-E25Q firmware exposes RTSP. If it does, enter its RTSP URL and credentials in the CP PLUS tile. If it is cloud/P2P-only through EzyKam+, this app cannot display that feed until CP PLUS provides an official third-party SDK/API and developer access.

## Current MVP limitations

- RTSP playback only; no proprietary Tapo or EzyKam+ cloud integration.
- No camera discovery, PTZ, two-way audio, motion alerts, cloud relay, or recording.
- Settings are not persisted and credentials are not stored after process exit.
- Simultaneous playback depends on both streams being reachable and supported by Media3/codecs/network bandwidth.
