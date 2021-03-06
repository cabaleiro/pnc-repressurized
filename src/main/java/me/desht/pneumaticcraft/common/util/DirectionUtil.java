package me.desht.pneumaticcraft.common.util;

import net.minecraft.util.Direction;

import static net.minecraft.util.Direction.*;

/**
 * rotateAround() disappeared from Direction in 1.15
 */
public class DirectionUtil {
    public static Direction rotateAround(Direction dir, Direction.Axis axis) {
        switch (axis) {
            case X:
                return dir.getAxis() == Axis.X ? dir : rotateX(dir);
            case Y:
                return dir.getAxis() == Axis.Y ? dir : dir.rotateY();
            case Z:
                return dir.getAxis() == Axis.Z ? dir : rotateZ(dir);
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    private static Direction rotateX(Direction dir) {
        switch (dir) {
            case NORTH:
                return DOWN;
            case SOUTH:
                return UP;
            case UP:
                return NORTH;
            case DOWN:
                return SOUTH;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
        }
    }

    private static Direction rotateZ(Direction dir) {
        switch (dir) {
            case EAST:
                return DOWN;
            case WEST:
                return UP;
            case UP:
                return EAST;
            case DOWN:
                return WEST;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
        }
    }
}
