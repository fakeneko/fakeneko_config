package cn.com.fakeneko.config.mixins;

import cn.com.fakeneko.config.impl.keybind.KeybindManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
	@Inject(method = "keyPress", at = @At("HEAD"))
	private void onKeyPress(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
		if (net.minecraft.client.Minecraft.getInstance().screen != null) {
			return;
		}
		if (key == GLFW.GLFW_KEY_UNKNOWN) {
			return;
		}
		InputConstants.Key inputKey = InputConstants.Type.KEYSYM.getOrCreate(key);
		if (action == GLFW.GLFW_PRESS) {
			KeybindManager.press(inputKey);
		} else if (action == GLFW.GLFW_RELEASE) {
			KeybindManager.release(inputKey);
		}
	}
}
