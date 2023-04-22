package com.ziloka.NetherBedrockCracker.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class FindCommand {

    public static final int radius = 16 * 8;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("nethercracker").then(literal("find").executes(ctx -> run(ctx.getSource()))));
    }

    private static int run(FabricClientCommandSource source) throws CommandSyntaxException {
        World world = source.getWorld();
        BlockPos senderPos = new BlockPos(source.getPosition());
        ChunkPos chunkPos = new ChunkPos(senderPos);

        List<BlockPos> blockCandidates = new ArrayList<BlockPos>();

        int chunkRadius = (radius >> 4) + 1;
        for (int r = 0; r < chunkRadius; r++) {
            for (int chunkX = chunkPos.x - r; chunkX <= chunkPos.x + r; chunkX++) {
                for (int chunkZ = chunkPos.z - r; chunkZ <= chunkPos.z
                        + r; chunkZ += chunkX == chunkPos.x - r || chunkX == chunkPos.x + r ? 1 : r + r) {
                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    addBedrockBlocks(chunk, blockCandidates);
                }
            }
        }

        source.sendFeedback(Text.literal(String.format("Found %d bedrocks at y = 4", blockCandidates.size())));

        String str = new String();
        for (BlockPos block : blockCandidates) {
            str += String.format("%d %d %d\n", block.getX(), block.getY(), block.getZ());
        }

        String finalStr = str;
        Text text = Texts.bracketed(
                (Text.literal("Click here to copy block info")).styled(style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, finalStr))
                        .withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.copy.click")))
                        .withInsertion(finalStr)));

        source.sendFeedback(text);

        return 1;
    }

    private static void addBedrockBlocks(Chunk chunk, List<BlockPos> blockCandidates) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        // search every column for the block
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = (chunk.getPos().x << 4) + x;
                int worldZ = (chunk.getPos().z << 4) + z;
                if (chunk.getBlockState(mutablePos.set(worldX, 4, worldZ)).isOf(Blocks.BEDROCK)) {
                    blockCandidates.add(mutablePos.toImmutable());
                }
            }
        }
    }

}
