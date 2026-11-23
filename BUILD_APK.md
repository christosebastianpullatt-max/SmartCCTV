# Build Smart CCTV APK from GitHub

1. Upload the contents of this folder directly to the root of `christosebastianpullatt-max/SmartCCTV` (not the enclosing folder). Include the hidden `.github` folder and its workflow file.
2. Commit the upload to the `main` branch. The `Build Smart CCTV APK` workflow starts automatically on push.
3. In the repository, open **Actions** and select the latest `Build Smart CCTV APK` run.
4. When it succeeds, open the run and download the `smart-cctv-debug-apk` artifact. The ZIP contains `app-debug.apk`.

This is a debug APK. It is not a finished cloud-connected CCTV product: Tapo cloud-account login and CP PLUS EzyKam+ cloud streaming are not implemented. The app currently plays compatible RTSP feeds supplied by the user.
