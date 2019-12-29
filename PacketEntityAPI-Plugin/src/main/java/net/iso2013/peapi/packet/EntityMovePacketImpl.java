package net.iso2013.peapi.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.iso2013.peapi.api.entity.EntityIdentifier;
import net.iso2013.peapi.api.packet.EntityMovePacket;
import net.iso2013.peapi.entity.EntityIdentifierImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Created by iso2013 on 8/1/2018.
 */
public class EntityMovePacketImpl extends EntityPacketImpl implements EntityMovePacket {
    private boolean onGround;
    private MoveType type;
    private Vector position;
    private double pitch, yaw;

    EntityMovePacketImpl(EntityIdentifier identifier, MoveType type) {
        super(identifier, new PacketContainer(type.getPacketType()), true);
        this.type = type;
    }

    private EntityMovePacketImpl(EntityIdentifier identifier, PacketContainer rawPacket, Byte newPitch, Byte newYaw,
                                 Vector
            newLocation, boolean onGround, boolean teleport) {
        super(identifier, rawPacket, true);
        if (teleport) {
            type = MoveType.TELEPORT;
        } else if (newPitch == null && newLocation != null) {
            type = MoveType.REL_MOVE;
        } else if (newPitch != null && newLocation == null) {
            type = MoveType.LOOK;
        } else if (newPitch != null) {
            type = MoveType.LOOK_AND_REL_MOVE;
        }
        this.pitch = newPitch != null ? newPitch : 0;
        this.yaw = newYaw != null ? newYaw : 0;
        this.position = newLocation != null ? newLocation : new Vector();
        this.onGround = onGround;
    }

    public static EntityPacketImpl unwrap(int entityID, PacketContainer c, Player p) {
        PacketType pt = c.getType();

        if (pt == PacketType.Play.Server.ENTITY_TELEPORT) {
            return new EntityMovePacketImpl(
                    EntityIdentifierImpl.produce(entityID, p),
                    c,
                    c.getBytes().read(1), c.getBytes().read(0),
                    new Vector(c.getDoubles().read(0), c.getDoubles().read(1), c.getDoubles().read(2)),
                    c.getBooleans().read(0),
                    true
            );
        } else if (pt == PacketType.Play.Server.REL_ENTITY_MOVE) {
            return new EntityMovePacketImpl(
                    EntityIdentifierImpl.produce(entityID, p),
                    c,
                    null, null,
                    readVector(c),
                    c.getBooleans().read(0),
                    false
            );
        } else if (pt == PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
            return new EntityMovePacketImpl(
                    EntityIdentifierImpl.produce(entityID, p),
                    c,
                    c.getBytes().read(1), c.getBytes().read(0),
                    readVector(c),
                    c.getBooleans().read(0),
                    false
            );
        } else if (pt == PacketType.Play.Server.ENTITY_LOOK) {
            return new EntityMovePacketImpl(
                    EntityIdentifierImpl.produce(entityID, p),
                    c,
                    c.getBytes().read(1), c.getBytes().read(0),
                    null,
                    c.getBooleans().read(0),
                    false
            );
        }
        return null;
    }

    private static Vector readVector(PacketContainer c) {
        if (c.getShorts().size() >= 3) {
            return new Vector(
                    ((double) c.getShorts().read(0)) / 4096.0,
                    ((double) c.getShorts().read(1)) / 4096.0,
                    ((double) c.getShorts().read(2)) / 4096.0
            );
        } else {
            return new Vector(
                    ((double) c.getIntegers().read(1)) / 4096.0,
                    ((double) c.getIntegers().read(2)) / 4096.0,
                    ((double) c.getIntegers().read(3)) / 4096.0
            );
        }
    }

    private static void writeVector(PacketContainer c, Vector vector) {
        if (c.getShorts().size() >= 3) {
            c.getShorts().write(0, (short) (vector.getX() * 4096.0));
            c.getShorts().write(1, (short) (vector.getY() * 4096.0));
            c.getShorts().write(2, (short) (vector.getZ() * 4096.0));
        } else {
            c.getIntegers().write(1, (int) (vector.getX() * 4096.0));
            c.getIntegers().write(2, (int) (vector.getY() * 4096.0));
            c.getIntegers().write(3, (int) (vector.getZ() * 4096.0));
        }
    }

    private static Vector vectorFromAngles(double pitch, double yaw) {
        Vector dir = new Vector();
        pitch = Math.toRadians(pitch);
        yaw = Math.toRadians(yaw);
        dir.setY(-Math.sin(pitch));
        double horizontalLength = Math.cos(pitch);
        dir.setX(-horizontalLength * Math.sin(yaw));
        dir.setX(horizontalLength * Math.cos(yaw));
        return dir;
    }

    private static double[] vectorToAngles(Vector v) {
        double[] angles = new double[2];
        if (v == null) return angles;
        if (v.getX() == 0 && v.getZ() == 0) {
            angles[0] = (byte) (v.getY() > 0 ? -90 : 90);
        }

        double theta = Math.atan2(-v.getX(), v.getZ());
        angles[1] = Math.toDegrees((theta + (Math.PI * 2) % (Math.PI * 2)));
        angles[0] = Math.toDegrees(Math.atan(-v.getY() / Math.sqrt((v.getX() * v.getX()) + (v.getY() * v.getY()))));
        return angles;
    }

    @Override
    public Vector getNewDirection() {
        return vectorFromAngles(pitch, yaw);
    }

    @Override
    public void setNewDirection(Vector direction) {
        double[] angles = vectorToAngles(direction);
        setPitchYaw(angles[0], angles[1]);
    }

    @Override
    public Vector getNewPosition() {
        return position;
    }

    @Override
    public void setNewPosition(Vector position, boolean teleport) {
        this.position = position;
        if (position == null) position = new Vector();
        switch (type) {
            case REL_MOVE:
            case LOOK_AND_REL_MOVE:
                writeVector(super.rawPacket, position);
                break;
            case LOOK:
                if (teleport) {
                    setType(MoveType.TELEPORT);
                } else {
                    setType(MoveType.LOOK_AND_REL_MOVE);
                    writeVector(super.rawPacket, position);
                    break;
                }
            case TELEPORT:
                super.rawPacket.getDoubles().write(0, position.getX());
                super.rawPacket.getDoubles().write(1, position.getY());
                super.rawPacket.getDoubles().write(2, position.getZ());
                break;
        }
    }

    private void setType(MoveType newType) {
        if (newType == type) return;
        type = newType;
        super.rawPacket = new PacketContainer(type.getPacketType());
        super.rawPacket.getModifier().writeDefaults();
        setNewPosition(position, newType == MoveType.TELEPORT);
        setPitchYaw(pitch, yaw);
        setOnGround(onGround);
    }

    @Override
    public MoveType getMoveType() {
        return type;
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
        super.rawPacket.getBooleans().write(0, onGround);
    }

    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public double getYaw() { return yaw; }

    @Override
    public void setPitchYaw(double pitch, double yaw) {
        switch (type) {
            case REL_MOVE:
                setType(MoveType.LOOK_AND_REL_MOVE);
            case LOOK_AND_REL_MOVE:
            case LOOK:
            case TELEPORT:
                super.rawPacket.getBytes().write(1, (byte) pitch);
                super.rawPacket.getBytes().write(0, (byte) yaw);
                break;
        }
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public Location getLocation(Location currentLocation) {
        if (type != MoveType.TELEPORT && position != null) {
            return new Location(
                    currentLocation.getWorld(),
                    currentLocation.getX() + position.getX(),
                    currentLocation.getY() + position.getY(),
                    currentLocation.getZ() + position.getZ(),
                    (float) yaw, (float) pitch
            );
        } else if (position == null) {
            return new Location(
                    currentLocation.getWorld(),
                    currentLocation.getX(),
                    currentLocation.getY(),
                    currentLocation.getZ(),
                    (float) yaw, (float) pitch
            );
        } else {
            return new Location(
                    currentLocation.getWorld(),
                    position.getX(),
                    position.getY(),
                    position.getZ(),
                    (float) yaw, (float) pitch
            );
        }
    }

    @Override
    public void setLocation(Location newLocation, Location currentLocation) {
        if (newLocation.distanceSquared(currentLocation) > 64) {
            setNewPosition(newLocation.toVector(), true);
        } else {
            setNewPosition(newLocation.toVector().subtract(currentLocation.toVector()), false);
        }
        setNewDirection(newLocation.getDirection());
    }

    @Override
    public PacketContainer getRawPacket() {
        switch (type) {
            case LOOK:
                break;
            case TELEPORT:
            case LOOK_AND_REL_MOVE:
            case REL_MOVE:
                assert position != null;
        }
        return super.getRawPacket();
    }

    @Override
    public EntityPacketImpl clone() {
        EntityMovePacketImpl p = new EntityMovePacketImpl(getIdentifier(), type);
        p.setNewPosition(position, type == MoveType.TELEPORT);
        p.setPitchYaw(pitch, yaw);
        p.setOnGround(onGround);
        return p;
    }
}
