package net.iso2013.peapi.entity.modifier.modifiers;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.iso2013.peapi.api.entity.modifier.ModifiableEntity;
import org.bukkit.util.Vector;

/**
 * Created by iso2013 on 4/18/2018.
 */
public class PositionModifier extends GenericModifier<Vector> {
    private final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getBlockPositionSerializer
            (false);

    public PositionModifier(int index, String label, Vector def) {
        super(null, index, label, def);
    }

    @Override
    public Vector getValue(ModifiableEntity target) {
        BlockPosition bp = (BlockPosition) target.read(super.index);
        if (bp == null) return null;
        return bp.toVector();
    }

    @Override
    public void setValue(ModifiableEntity target, Vector newValue) {
        if (newValue != null) {
            target.write(
                    super.index,
                    BlockPosition.getConverter().getGeneric(new BlockPosition(newValue.getBlockX(), newValue
                            .getBlockY(), newValue.getBlockZ())),
                    serializer
            );
        } else super.unsetValue(target);
    }

    @Override
    public Class<?> getFieldType() {
        return Vector.class;
    }
}
