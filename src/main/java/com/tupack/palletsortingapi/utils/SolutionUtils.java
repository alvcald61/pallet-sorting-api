package com.tupack.palletsortingapi.utils;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import org.packing.core.Bin;
import org.packing.primitives.MArea;
import org.packing.utils.Utils;
public class SolutionUtils {

  public static void drawbinToFile(Bin[] bins, Dimension viewPortDimension) throws IOException {
    for (int i = 0; i < bins.length; i++) {

      MArea[] areasInThisbin = bins[i].getPlacedPieces();
      ArrayList<MArea> areas = new ArrayList<>();
      areas.add(new MArea());
      Collections.addAll(areas, areasInThisbin);
      Utils.drawMAreasToFile(areas, viewPortDimension, bins[i].getDimension(),
              ("Bin-" + (i + 1)));
      System.out.println("Generated image for bin " + (i + 1));
    }
  }

  public static void createOutputFiles(Bin[] bins) throws IOException {
    for (int i = 0; i < bins.length; i++) {
      PrintWriter writer = new PrintWriter("Bin-" + (i + 1) + ".txt",
              StandardCharsets.UTF_8);
      writer.println(bins[i].getPlacedPieces().length);
      MArea[] areasInThisbin = bins[i].getPlacedPieces();
      for (MArea area : areasInThisbin) {
        double offsetX = area.getBoundingBox2D().getX();
        double offsetY = area.getBoundingBox2D().getY();
        writer.println(area.getID() + " " + "type: " + area.getType() + " " + area.getRotation() +
                " " + offsetX + "," + offsetY);
      }
      writer.close();
      System.out.println("Generated points file for bin " + (i + 1));
    }
  }
}
