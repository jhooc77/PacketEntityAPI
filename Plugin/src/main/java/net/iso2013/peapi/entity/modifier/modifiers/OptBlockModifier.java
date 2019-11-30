package net.iso2013.peapi.entity.modifier.modifiers;

import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.iso2013.peapi.api.entity.modifier.ModifiableEntity;
import org.bukkit.material.MaterialData;

import java.util.Optional;

/**
 * Created by iso2013 on 4/20/2018.
 */
@SuppressWarnings("deprecation")
public class OptBlockModifier extends OptModifier<MaterialData> {
    private final WrappedDataWatcher.Serializer serializer =
            WrappedDataWatcher.Registry.getBlockDataSerializer(true);

    public OptBlockModifier(int index, String label, Optional<MaterialData> def) {
        super(MaterialData.class, index, label, def);
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public Optional<MaterialData> getValue(ModifiableEntity target) {
        Object val = target.read(super.index);
        if (val == null) return null;
        if (!(val instanceof Optional))
            throw new IllegalStateException("Read inappropriate type from modifiable entity!");
        Optional<WrappedBlockData> bp = (Optional<WrappedBlockData>) val;
        return bp.map(wrappedBlockData -> new MaterialData(wrappedBlockData.getType(),
                (byte) wrappedBlockData.getData()));
    }

    @Override
    public void setValue(ModifiableEntity target, Optional<MaterialData> newValue) {
        if (newValue != null) {
            if (newValue.isPresent()) {
                MaterialData v = newValue.get();
                WrappedBlockData wbd = WrappedBlockData.createData(v.getItemType(), 0);

                target.write(
                        index,
                        Optional.of(wbd.getHandle()),
                        serializer
                );
            } else {
                target.write(super.index, Optional.empty(), serializer);
            }
        } else super.unsetValue(target);
    }
}
