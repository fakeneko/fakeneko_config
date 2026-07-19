package cn.com.fakeneko.config.mixins;

import cn.com.fakeneko.config.impl.keybind.KeybindManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
	@Inject(method = "onPress", at = @At("HEAD"))
	private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
		if (net.minecraft.client.Minecraft.getInstance().screen != null) {
			return;
		}
		InputConstants.Key inputKey = InputConstants.Type.MOUSE.getOrCreate(button);
		if (action == GLFW.GLFW_PRESS) {
			KeybindManager.press(inputKey);
		} else if (action == GLFW.GLFW_RELEASE) {
			KeybindManager.release(inputKey);
		}
	}
}
