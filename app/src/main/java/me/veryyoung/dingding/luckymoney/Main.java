package me.veryyoung.dingding.luckymoney;

import android.widget.ImageButton;

import org.json.JSONObject;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findConstructorBestMatch;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static java.lang.Thread.sleep;


public class Main implements IXposedHookLoadPackage {

    private static final String DINGDING_PACKAGE_NAME = "com.alibaba.android.rimet";

    private static final String MAP_CLASS_NAME = "com.alibaba.android.rimet.biz.im.notification.MessageNotificationManager";
    private static final String MAP_FUNCTION_NAME = "a";


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(DINGDING_PACKAGE_NAME)) {

            findAndHookMethod(MAP_CLASS_NAME, lpparam.classLoader, MAP_FUNCTION_NAME, int.class, "com.alibaba.wukong.im.Message", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!PreferencesUtils.open()) {
                                return;
                            }
                            if (null != param.args[1]) {
                                Field messageContentFileld = param.args[1].getClass().getSuperclass().getSuperclass().getDeclaredField("mMessageContent");
                                String messageContent = messageContentFileld.get(param.args[1]).toString();

                                JSONObject jsonObject = new JSONObject(messageContent);
                                int tp = jsonObject.optInt("tp", 0);
                                if (901 == tp || 902 == tp) {
                                    String ext = jsonObject.getJSONArray("multi").getJSONObject(0).getString("ext");
                                    ext = ext.replace("\\", "").replace("\"{", "{").replace("}\"", "}");
                                    jsonObject = new JSONObject(ext);
                                    Long sender = jsonObject.getLong("sid");
                                    String clusterId = jsonObject.getString("clusterid");

                                    Object redPacketsRpc = callStaticMethod(findClass("zy", lpparam.classLoader), "a");
                                    Object redPacketsRpc$9 = findConstructorBestMatch(findClass("zy$9", lpparam.classLoader), redPacketsRpc.getClass(), findClass("aeb", lpparam.classLoader)).newInstance(redPacketsRpc, null);

                                    Object redEnvelopPickIService = callStaticMethod(findClass("crh", lpparam.classLoader), "a", findClass("com.alibaba.android.dingtalk.redpackets.idl.service.RedEnvelopPickIService", lpparam.classLoader));

                                    if (PreferencesUtils.delay()) {
                                        sleep(PreferencesUtils.delayTime());
                                    }
                                    callMethod(redEnvelopPickIService, "pickRedEnvelopCluster", sender, clusterId, redPacketsRpc$9);
                                }
                            }
                        }
                    }
            );


            findAndHookMethod("com.alibaba.android.dingtalk.redpackets.activities.PickRedPacketsActivity", lpparam.classLoader, MAP_FUNCTION_NAME, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (PreferencesUtils.quickOpen()) {
                        ImageButton imageButton = (ImageButton) findFirstFieldByExactType(param.thisObject.getClass(), ImageButton.class).get(param.thisObject);
                        if (imageButton.isClickable()) {
                            imageButton.performClick();
                        }
                    }
                }
            });


        }
    }


}