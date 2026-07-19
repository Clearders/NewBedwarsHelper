package org.exmple.newbedwarshelper.client.gammaoverride;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class GammaOverrideIrisCompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean warnedAboutIrisApi;
    private static Object irisApi;
    private static Method isShaderPackInUseMethod;

    private GammaOverrideIrisCompat() {
    }

    public static boolean isIrisLoaded() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }

    public static boolean isShaderPackInUse() {
        if (!isIrisLoaded()) {
            return false;
        }

        try {
            ensureIrisApi();
            Object result = isShaderPackInUseMethod.invoke(irisApi);
            return result instanceof Boolean enabled && enabled;
        } catch (ReflectiveOperationException exception) {
            if (!warnedAboutIrisApi) {
                LOGGER.info("[NBH Gamma Override] Could not query Iris shader pack state: {}", exception.toString());
                warnedAboutIrisApi = true;
            }
            return false;
        }
    }

    public static boolean shouldApplyNightVisionUniformOverride() {
        return GammaOverrideManager.isEnabled()
                && GammaOverrideManager.isNightVisionMode()
                && isShaderPackInUse();
    }

    private static void ensureIrisApi() throws ReflectiveOperationException {
        if (irisApi != null && isShaderPackInUseMethod != null) {
            return;
        }

        Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        Method getInstanceMethod = irisApiClass.getMethod("getInstance");
        irisApi = getInstanceMethod.invoke(null);
        isShaderPackInUseMethod = irisApiClass.getMethod("isShaderPackInUse");
    }
}
