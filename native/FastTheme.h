#ifndef FASTTHEME_H
#define FASTTHEME_H

#include <jni.h>
#include <windows.h>

#ifdef __cplusplus
extern "C" {
#endif

// ============================================================================
// FASTTHEME MONITORING API
// ============================================================================

JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_startMonitoring(JNIEnv* env, jobject obj);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_stopMonitoring(JNIEnv* env, jobject obj);

// ============================================================================
// FASTTHEME TERMINAL (WINDOW STYLING) API
// ============================================================================

JNIEXPORT jlong JNICALL Java_fasttheme_FastThemeTerminal_getWindowHandle(JNIEnv* env, jobject obj, jobject component);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setWindowTransparency(JNIEnv* env, jobject obj, jlong hwndLong, jint alpha);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarTextColor(JNIEnv* env, jobject obj, jlong hwndLong, jint r, jint g, jint b);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_setTitleBarDarkMode(JNIEnv* env, jobject obj, jlong hwndLong, jboolean enabled);
JNIEXPORT jstring JNICALL Java_fasttheme_FastThemeTerminal_getSystemResolution(JNIEnv* env, jobject obj);
JNIEXPORT jint JNICALL Java_fasttheme_FastThemeTerminal_getSystemDPI(JNIEnv* env, jobject obj);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastThemeTerminal_isSystemDarkMode(JNIEnv* env, jobject obj);
JNIEXPORT jint JNICALL Java_fasttheme_FastThemeTerminal_getSystemRefreshRate(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif

#endif // FASTTHEME_H
