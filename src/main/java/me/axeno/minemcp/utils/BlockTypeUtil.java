package me.axeno.minemcp.utils;

public class BlockTypeUtil
{

    private BlockTypeUtil()
    {
    }

    public static String baseBlock(String blockType)
    {
        if (blockType == null) return null;

        int colon = blockType.indexOf(':');
        String noNs = (colon >= 0) ? blockType.substring(colon + 1) : blockType;
        int bracket = noNs.indexOf('[');
        return (bracket >= 0) ? noNs.substring(0, bracket) : noNs;
    }

}
