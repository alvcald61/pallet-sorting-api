package com.tupack.palletsortingapi.utils;

import com.tupack.palletsortingapi.order.application.dto.AddressDto;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.application.dto.ZoneDto;

public class AppUtils {

  public static String getZoneMapKey(Zone zone) {
    return zone.getState() + "-" + zone.getCity();
  }

  public static String getZoneMapKey(AddressDto zone) {
    return zone.state() + "-" + zone.city();
  }
}
