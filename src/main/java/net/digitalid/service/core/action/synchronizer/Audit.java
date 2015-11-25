package net.digitalid.service.core.action.synchronizer;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.castable.Castable;
import net.digitalid.service.core.castable.CastableObject;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.packet.Packet;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyList;

/**
 * This class models an audit with a time and trail.
 * 
 * @see RequestAudit
 * @see ResponseAudit
 */
@Immutable
public abstract class Audit extends CastableObject implements Castable, XDF<Audit, Object> {
    
    /**
     * Stores the semantic type {@code last.time.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType LAST_TIME = SemanticType.map("last.time.audit@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code this.time.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType THIS_TIME = SemanticType.map("this.time.audit@core.digitalid.net").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code trail.audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TRAIL = SemanticType.map("trail.audit@core.digitalid.net").load(Packet.SIGNATURES);
    
    /**
     * Stores the semantic type {@code audit@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("audit@core.digitalid.net").load(TupleWrapper.XDF_TYPE, LAST_TIME, THIS_TIME, TRAIL);
    
    
    /**
     * Stores the time of the last audit.
     */
    private final @Nonnull Time lastTime;
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    Audit(@Nonnull Time lastTime) {
        this.lastTime = lastTime;
    }
    
    /**
     * Creates a new audit from the given block.
     * 
     * @param block the block containing the audit.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public static @Nonnull Audit get(@Nonnull Block block) throws InvalidEncodingException, InternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
        final @Nonnull Time lastTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(0));
        if (tuple.isElementNull(1)) {
            return new RequestAudit(lastTime);
        } else {
            final @Nonnull Time thisTime = Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(1));
            final @Nonnull ReadOnlyList<Block> trail = ListWrapper.decodeNonNullableElements(tuple.getNonNullableElement(2));
            return new ResponseAudit(lastTime, thisTime, trail);
        }
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(3);
        elements.set(0, lastTime.toBlock().setType(LAST_TIME));
        return TupleWrapper.encode(TYPE, elements.freeze());
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @return the time of the last audit.
     */
    @Pure
    public final @Nonnull Time getLastTime() {
        return lastTime;
    }
    
}
