package com.smashingmods.chemlib.api.utility;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Tooltip helper for fluids — fully compatible with Minecraft&nbsp;1.21 (official mappings).
 * Shows both potion effects and their attribute modifiers, matching vanilla formatting.
 */
public final class FluidEffectsTooltipUtility {

    private FluidEffectsTooltipUtility() {}

    /**
     * Adds lines describing all effects of a fluid to the given tooltip list.
     *
     * @param tooltips       Tooltip lines list (mutable).
     * @param effects        Effects provided by the fluid.
     * @param showDuration   Whether to show duration.
     * @param showAttributes Whether to append attribute‑modifier lines.
     */
    public static void addFluidEffectTooltipLines(List<Component> tooltips,
                                                  List<MobEffectInstance> effects,
                                                  boolean showDuration,
                                                  boolean showAttributes) {
        if (effects.isEmpty()) return;

        tooltips.add(Component.literal(" "));

        for (MobEffectInstance inst : effects) {
            MobEffect effect = inst.getEffect().value(); // Holder → MobEffect

            // ── base potion line ──
            MutableComponent line = Component.translatable(effect.getDescriptionId());
            if (inst.getAmplifier() > 0)
                line.append(" ").append(Component.translatable("potion.potency." + inst.getAmplifier()));
            if (showDuration && inst.getDuration() > 20)
                line = Component.translatable("potion.withDuration", line,
                        MobEffectUtil.formatDuration(inst, 1.0F, 1.0F));

            tooltips.add(line.withStyle(effect.getCategory().getTooltipFormatting()));

            // ── attribute modifiers ──
            if (showAttributes) {
                appendAttributeModifiers(inst, effect, tooltips);
            }
        }
    }

    /**
     * Reflection‑based access to private field {@code MobEffect.attributeModifiers} and its record.
     */
    @SuppressWarnings("unchecked")
    private static void appendAttributeModifiers(MobEffectInstance inst,
                                                 MobEffect effect,
                                                 List<Component> out) {
        Map<Attribute, ?> map;
        try {
            Field f = MobEffect.class.getDeclaredField("attributeModifiers");
            f.setAccessible(true);
            map = (Map<Attribute, ?>) f.get(effect);
        } catch (ReflectiveOperationException e) {
            return;                        // field gone — skip gracefully
        }
        if (map.isEmpty()) return;

        for (Map.Entry<Attribute, ?> entry : map.entrySet()) {
            Attribute attribute = entry.getKey();
            Object template    = entry.getValue();
            AttributeModifier mod = createModifier(template, inst.getAmplifier());
            if (mod == null) continue;

            double amount  = mod.getAmount();
            double display = switch (mod.getOperation()) {
                case ADD_VALUE      -> amount;
                case MULTIPLY_BASE, MULTIPLY_TOTAL -> amount * 100.0D;
            };

            Component attrLine = Component.translatable(
                            "attribute.modifier." + (display > 0 ? "plus." : "take.") + mod.getOperation().toValue(),
                            String.format("%.2f", display),
                            Component.translatable(attribute.getDescriptionId()))
                    .withStyle(display > 0 ? ChatFormatting.BLUE : ChatFormatting.RED);

            out.add(attrLine);
        }
    }

    /** Calls package‑private {@code AttributeTemplate#create(int)} via reflection. */
    private static AttributeModifier createModifier(Object template, int amp) {
        try {
            Method m = template.getClass().getMethod("create", int.class);
            m.setAccessible(true);
            return (AttributeModifier) m.invoke(template, amp);
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return null;
        }
    }
}
