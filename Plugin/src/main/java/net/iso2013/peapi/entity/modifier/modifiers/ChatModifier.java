package net.iso2013.peapi.entity.modifier.modifiers;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.iso2013.peapi.api.entity.modifier.ModifiableEntity;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Created by iso2013 on 4/18/2018.
 */
public class ChatModifier extends GenericModifier<BaseComponent[]> {
    private final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getChatComponentSerializer();

    public ChatModifier(int index, String label, String def) {
        super(null, index, label, ComponentSerializer.parse(def));
    }

    @Override
    public BaseComponent[] getValue(ModifiableEntity target) {
        return ComponentSerializer.parse(((WrappedChatComponent) target.read(super.index)).getJson());
    }

    @Override
    public void setValue(ModifiableEntity target, BaseComponent[] newValue) {
        if (newValue != null) {
            target.write(
                    super.index,
                    WrappedChatComponent.fromJson(ComponentSerializer.toString(newValue)).getHandle(),
                    serializer
            );
        } else super.unsetValue(target);
    }

    @Override
    public Class<?> getFieldType() {
        return BaseComponent[].class;
    }
}
