package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.utils.PackingType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification for dynamic filtering of Order entities.
 * Supports filtering by search term, status, order type, and pickup date range.
 */
public class OrderSpecification {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Build a specification for filtering orders based on multiple criteria
   *
   * @param search          Search term for client name, addresses, or truck plate
   * @param statuses        List of order statuses to filter by
   * @param orderType       Packing type (BULK, TWO_DIMENSIONAL, THREE_DIMENSIONAL)
   * @param pickupDateFrom  Start date for pickup date range (format: yyyy-MM-dd)
   * @param pickupDateTo    End date for pickup date range (format: yyyy-MM-dd)
   * @param clientId        Client ID to filter by (null to skip this filter)
   * @param driverId        Driver ID to filter by (null to skip this filter)
   * @return Specification combining all active filters
   */
  public static Specification<Order> buildSpecification(
      String search,
      List<String> statuses,
      String orderType,
      String pickupDateFrom,
      String pickupDateTo,
      Long clientId,
      Long driverId) {

    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by client (for CLIENT role)
      if (clientId != null) {
        predicates.add(criteriaBuilder.equal(root.get("client").get("id"), clientId));
      }

      // Filter by driver (for DRIVER role)
      if (driverId != null) {
        predicates.add(criteriaBuilder.equal(
            root.get("truck").get("driver").get("user").get("id"), driverId));
      }

      // Search filter (client name, addresses, or truck plate)
//      if (search != null && !search.trim().isEmpty()) {
//        String searchPattern = "%" + search.toLowerCase() + "%";
//        Predicate clientFirstName = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("client").get("user").get("firstName")),
//            searchPattern);
//        Predicate clientLastName = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("client").get("user").get("lastName")),
//            searchPattern);
//        Predicate businessName = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("client").get("businessName")),
//            searchPattern);
//        Predicate fromAddress = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("fromAddress")),
//            searchPattern);
//        Predicate toAddress = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("toAddress")),
//            searchPattern);
//        Predicate truckPlate = criteriaBuilder.like(
//            criteriaBuilder.lower(root.get("truck").get("licensePlate")),
//            searchPattern);

//        predicates.add(criteriaBuilder.or(
//            clientFirstName, clientLastName, businessName,
//            fromAddress, toAddress, truckPlate));
//      }

      if(search != null && !search.trim().isEmpty() && search.matches("\\d+")) {
        Predicate id = criteriaBuilder.equal(root.get("id"), search);
        predicates.add(id);
      }

      // Status filter
      if (statuses != null && !statuses.isEmpty()) {
        List<OrderStatus> orderStatuses = statuses.stream()
            .map(OrderStatus::valueOf)
            .toList();
        predicates.add(root.get("orderStatus").in(orderStatuses));
      }

      // Order type filter
      if (orderType != null && !orderType.trim().isEmpty()) {
        predicates.add(criteriaBuilder.equal(
            root.get("orderType"),
            PackingType.valueOf(orderType)));
      }

      // Pickup date range filter
      if (pickupDateFrom != null && !pickupDateFrom.trim().isEmpty()) {
        LocalDate dateFrom = LocalDate.parse(pickupDateFrom, DATE_FORMATTER);
        LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(
            root.get("pickupDate"), dateTimeFrom));
      }

      if (pickupDateTo != null && !pickupDateTo.trim().isEmpty()) {
        LocalDate dateTo = LocalDate.parse(pickupDateTo, DATE_FORMATTER);
        LocalDateTime dateTimeTo = dateTo.atTime(23, 59, 59);
        predicates.add(criteriaBuilder.lessThanOrEqualTo(
            root.get("pickupDate"), dateTimeTo));
      }

      // Combine all predicates with AND
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
