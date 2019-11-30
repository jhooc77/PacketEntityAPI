package net.iso2013.peapi.api.packet;

/**
 * @author iso2013
 * @version 0.1
 * @since 2018-04-21
 */
public interface EntityStatusPacket extends EntityPacket {
    /**
     * Gets the status that will be sent
     *
     * @return the status
     */
    byte getStatus();

    /**
     * Set the status that will be sent
     *
     * @param value the new status
     */
    void setStatus(byte value);
}
