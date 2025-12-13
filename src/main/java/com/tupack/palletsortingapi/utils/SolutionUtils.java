package com.tupack.palletsortingapi.utils;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.packing.core.Bin;
import org.packing.primitives.MArea;
import org.packing.utils.Utils;

public class SolutionUtils {

  public static List<String> drawbinToFile(Bin[] bins, Dimension viewPortDimension)
      throws IOException {
    List<String> paths = new ArrayList<>();
    for (int i = 0; i < bins.length; i++) {
      UUID uuid = UUID.randomUUID();
      MArea[] areasInThisbin = bins[i].getPlacedPieces();
      ArrayList<MArea> areas = new ArrayList<>();
      areas.add(new MArea());
      Collections.addAll(areas, areasInThisbin);
      String binName = "Bin-" + uuid;
      Utils.drawMAreasToFile(areas, viewPortDimension, bins[i].getDimension(), (binName));
      paths.add(binName);
      System.out.println("Generated image for bin " + (i + 1));
    }
    return paths;
  }

  public static List<String> createOutputFiles(Bin[] bins) throws IOException {
    List<String> paths = new ArrayList<>();
    for (int i = 0; i < bins.length; i++) {
      UUID uuid = UUID.randomUUID();
      String binName = "Bin-" + uuid + ".txt";
      PrintWriter writer = new PrintWriter(binName, StandardCharsets.UTF_8);
      writer.println(bins[i].getPlacedPieces().length);
      MArea[] areasInThisbin = bins[i].getPlacedPieces();
      for (MArea area : areasInThisbin) {
        double offsetX = area.getBoundingBox2D().getX();
        double offsetY = area.getBoundingBox2D().getY();
        writer.println(
            area.getID() + " " + "type: " + area.getType() + " " + area.getRotation() + " "
                + offsetX + "," + offsetY);
      }
      writer.close();
      System.out.println("Generated points file for bin " + (i + 1));
      paths.add(binName);
    }
    return paths;
  }
}
