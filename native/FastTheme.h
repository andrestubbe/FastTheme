#ifndef FASTTHEME_H
#define FASTTHEME_H

#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>
#include <windows.h>

#ifdef __cplusplus
extern "C" {
#endif

// Methods
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_addTitleBarControlRect(JNIEnv* env, jclass clazz, jlong hwndLong, jint x, jint y, jint w, jint h);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_clearTitleBarControlRects(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_closeWindow(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_enableMica(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_forceFrameUpdate(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_maximizeWindow(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_minimizeWindow(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_restoreWindow(JNIEnv* env, jclass clazz, jlong hwndLong);
JNIEXPORT void JNICALL Java_fasttheme_FastTheme_sendSysCommand(JNIEnv* env, jclass clazz, jlong hwndLong, jint cmd);

// Getters
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_isSystemDarkMode(JNIEnv* env, jclass clazz);
JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getConsoleWindowHandle(JNIEnv* env, jclass clazz);
JNIEXPORT jlong JNICALL Java_fasttheme_FastTheme_getWindowHandle(JNIEnv* env, jclass clazz, jobject component);

// Setters
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setAlwaysOnTop(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean alwaysOnTop);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setBorderlessShadow(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setCornerStyle(JNIEnv* env, jclass clazz, jlong hwndLong, jint style);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setNativeTitleBarButtonsEnabled(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled, jint buttonWidth);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setOverlayDragHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setSystemBackdropType(JNIEnv* env, jclass clazz, jlong hwndLong, jint type);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarDarkMode(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean enabled);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarHeight(JNIEnv* env, jclass clazz, jlong hwndLong, jint height);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setTitleBarTextColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowBackgroundColor(JNIEnv* env, jclass clazz, jlong hwndLong, jint r, jint g, jint b);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowButtonsVisible(JNIEnv* env, jclass clazz, jlong hwndLong, jboolean showMinimize, jboolean showMaximize);
JNIEXPORT jboolean JNICALL Java_fasttheme_FastTheme_setWindowTransparency(JNIEnv* env, jclass clazz, jlong hwndLong, jint alpha);

#ifdef __cplusplus
}
#endif

#endif // FASTTHEME_H
