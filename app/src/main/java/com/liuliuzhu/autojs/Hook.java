package com.liuliuzhu.autojs;

import android.app.Application;
import android.util.Log;
import com.liuliuzhu.autojs.utils.HookUtils;
import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;

/**
 * @author 溜溜猪
 * @date 2020/05/23
 * @desc 微信公众号：AI小子
 */
public class Hook extends Application implements IXposedHookLoadPackage, Config {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        final Class<?> clazz = XposedHelpers.findClass(
                "com.stardust.autojs.engine.encryption.ScriptEncryption",
                loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(
                clazz,
                "initFingerprint",
                XposedHelpers.findClass("com.stardust.autojs.project.ProjectConfig", loadPackageParam.classLoader),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object projectConfig = param.args[0];
                        String packageName = (String) XposedHelpers.getObjectField(projectConfig, "packageName");
                        String versionName = (String) XposedHelpers.getObjectField(projectConfig, "versionName");
                        String mainScriptFile = (String) XposedHelpers.getObjectField(projectConfig, "mainScriptFile");
                        Object buildInfo = XposedHelpers.getObjectField(projectConfig, "buildInfo");
                        String buildId = (String) XposedHelpers.callMethod(buildInfo, "getBuildId");
                        String name = (String) XposedHelpers.getObjectField(projectConfig, "name");
                        Object thisObject = param.thisObject;
                        byte[] mKey = (byte[]) XposedHelpers.getObjectField(thisObject, "mKey");
                        String mInitVector = (String) XposedHelpers.getObjectField(thisObject, "mInitVector");
                        String log = "==[ScriptEncryption::initFingerprint]==\n" +
                                "packageName: " + packageName + "\n" +
                                "versionName: " + versionName + "\n" +
                                "mainScriptFile: " + mainScriptFile + "\n" +
                                "buildId: " + buildId + "\n" +
                                "name: " + name + "\n" +
                                "Key (mKey): " + Arrays.toString(mKey) + "\n" +
                                "IV (mInitVector): " + mInitVector + "\n" +
                                "========================\n";
                        Log.e("XPOSED_HOOK", log);
                        XposedBridge.log(log);
                        XposedHelpers.findAndHookMethod(
                                clazz,
                                "decrypt",
                                byte[].class,
                                int.class,
                                int.class,
                                new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        Log.e("XPOSED", "decrypt() called");
                                        byte[] data = (byte[]) param.args[0];
                                        int offset = (int) param.args[1];
                                        int length = (int) param.args[2];
                                        Log.e("XPOSED", "offset: " + offset + ", length: " + length + "\n" + "data length: " + data.length);
                                        String str = HookUtils.bytesToString((byte[]) param.getResult());
                                        HookUtils.strToFile(str, FILEPATH);
                                    }
                                });
                    }
                }
        );
    }
}
