package cn.com.fakeneko.config.mixins;

import cn.com.fakeneko.config.impl.keybind.KeybindManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
	@Inject(method = "onButton", at = @At("HEAD"))
	private void onMouseButton(long window, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
		if (net.minecraft.client.Minecraft.getInstance().screen != null) {
			return;
		}
		InputConstants.Key inputKey = InputConstants.Type.MOUSE.getOrCreate(rawButtonInfo.button());
		if (action == GLFW.GLFW_PRESS) {
			KeybindManager.press(inputKey);
		} else if (action == GLFW.GLFW_RELEASE) {
			KeybindManager.release(inputKey);
		}
	}
}
