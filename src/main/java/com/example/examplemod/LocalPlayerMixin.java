package com.example.examplemod.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Убираем доп. замедление ×0.2 когда "используешь предмет" (щит).
// Остаётся только обычная скорость "крадусь".
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @ModifyConstant(
            method = "aiStep",
            constant = @Constant(floatValue = 0.2F),
            require = 0
    )
    private float examplemod$noShieldSlow(float original) {
        return 1.0F;
    }
}
