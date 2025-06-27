package com.tupack.palletsortingapi;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import org.packing.core.Bin;
import org.packing.core.BinPacking;
import org.packing.primitives.MArea;
import org.packing.utils.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

@SpringBootApplication
public class PalletSortingApiApplication {

  public static void main(String[] args) throws IOException {
    SpringApplication.run(PalletSortingApiApplication.class, args);
  }

}
