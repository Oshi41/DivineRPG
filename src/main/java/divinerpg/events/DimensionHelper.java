package divinerpg.events;

import divinerpg.DivineRPG;
import divinerpg.registry.ModBlocks;
import divinerpg.registry.ModDimensions;
import divinerpg.utils.DivineTeleporter;
import divinerpg.utils.portals.ServerPortal;
import divinerpg.utils.portals.VetheaPortal;
import divinerpg.utils.portals.description.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.util.ITeleporter;

import java.util.HashMap;
import java.util.Map;

public class DimensionHelper {
    public final static Map<Block, IPortalDescription> descriptionsByBlock = new HashMap<>();
    public final static Map<DimensionType, IPortalDescription> descriptionsByDimension = new HashMap<>();
    private final static Map<DimensionType, ITeleporter> teleporterMapByModdedDimension = new HashMap<>();

    /**
     * Should call after ModDimensions.registerDimensions()
     */
    public static void initPortalDescriptions() {
        ServerPortal portal = new ServerPortal(128, 20 * 10);

        add(ModDimensions.edenDimension, new NetherLikePortalDescription(ModBlocks.divineRock, ModBlocks.edenPortal), portal);
        add(ModDimensions.wildwoodDimension, new NetherLikePortalDescription(ModBlocks.edenBlock, ModBlocks.wildwoodPortal), portal);
        add(ModDimensions.apalachiaDimension, new NetherLikePortalDescription(ModBlocks.wildwoodBlock, ModBlocks.apalachiaPortal), portal);
        add(ModDimensions.skythernDimension, new NetherLikePortalDescription(ModBlocks.apalachiaBlock, ModBlocks.skythernPortal), portal);
        add(ModDimensions.mortumDimension, new NetherLikePortalDescription(ModBlocks.skythernBlock, ModBlocks.mortumPortal), portal);
        add(ModDimensions.iceikaDimension, new NetherLikePortalDescription(Blocks.SNOW, ModBlocks.iceikaPortal), portal);
        add(ModDimensions.galaxyDimension, new NetherLikePortalDescription(ModBlocks.galaxy_block, ModBlocks.galaxy_portal), portal);

        VetheaPortal vetheaPortal = new VetheaPortal(128, 20 * 60);
        add(ModDimensions.vetheaDimension, new VetheaPortalDescription(), vetheaPortal);

        ArcanaTeleporter teleporter = new ArcanaTeleporter(20 * 60);
        add(ModDimensions.arcanaDimension,
                new ArcanaPortalRoomDescription(ModBlocks.arcanaPortalFrame, ModBlocks.arcanaPortal),
                teleporter);
    }

    private static void add(DimensionType type, IPortalDescription description, ITeleporter teleporter) {
        descriptionsByBlock.put(description.getFrame(), description);
        descriptionsByDimension.put(type, description);
        teleporterMapByModdedDimension.put(type, teleporter);
    }

    /**
     * Transfer entity to another dimension
     *
     * @param e            - any entity
     * @param modDimension - non vanilla dimension!
     */
    public static void transferEntity(Entity e, DimensionType modDimension) {
        if (e == null || modDimension == null)
            return;

        DimensionType current = e.getEntityWorld().provider.getDimensionType();

        ITeleporter teleporter = teleporterMapByModdedDimension.get(current);
        if (teleporter == null) {
            teleporter = teleporterMapByModdedDimension.get(modDimension);
        }

        if (teleporter == null) {
            DivineRPG.logger.warn(String.format("Unknown dimensions: 1) %s/n2)%s", current.getName(), modDimension.getName()));
            return;
        }

        e.changeDimension(modDimension.getId(), teleporter);
    }

    /**
     * Uses old Divine teleporter
     *
     * @param e           - entity
     * @param destination - destination
     */
    public static void transferEntityToDivineDims(Entity e, DimensionType destination) {
        if (e == null || destination == null || e.getServer() == null)
            return;

        DimensionType current = e.getEntityWorld().provider.getDimensionType();

        IPortalDescription description = descriptionsByDimension.get(destination);
        if (description == null) {
            description = descriptionsByDimension.get(current);
        }

        if (description == null) {
            DivineRPG.logger.warn(String.format("Unknown dimensions: 1) %s/n2)%s", current.getName(), destination.getName()));
            return;
        }

        ITeleporter teleporter = new DivineTeleporter(e.getServer().getWorld(destination.getId()), description.getPortal(), description.getFrame().getDefaultState());
        e.changeDimension(destination.getId(), teleporter);
    }
}
